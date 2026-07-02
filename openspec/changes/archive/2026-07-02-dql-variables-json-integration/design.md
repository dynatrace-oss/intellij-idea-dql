## Context

Today the `dql-variables.json` relationship is one-directional and name-based:

- `DQLVariablesServiceImpl.findVariableDefinitionFiles(name, file)` scans every JSON file named
  `dql-variables.json` in the project, keeps those whose parent directory is a prefix of the query file's path,
  and returns the matching `JsonProperty` definitions. `findClosestDefinition(...)` picks the one whose directory
  shares the most leading path segments (the "closest" file).
- `VariableElementImpl.getDefinition()` caches the resolved `JsonProperty` in a `CachedValue`. When a definition
  is found, the cache depends on `[this, resolvedJsonProperty]`; when nothing is found it depends on `[this]`
  only — so a later-created file or added property never invalidates it.
- `DQLVariableReference.multiResolve()` re-runs the scan for go-to-definition; `isReferenceTo()` compares by name.
- The JSON file itself is an ordinary JSON file: no schema, no plugin ownership, no reverse navigation.

There is no existing `JsonSchemaProviderFactory` in the codebase, and Find Usages / usage-type providers exist
for the DQL language (`DQLFindUsagesProvider`, `DQLUsageTypeProvider`) but nothing bridges from a JSON property
to DQL usages. The bundled IntelliJ `com.intellij.json` module (already a dependency) provides the JSON schema
and PSI infrastructure we need.

## Goals / Non-Goals

**Goals:**
- Treat `dql-variables.json` as a plugin-owned, schema-validated artifact (by file name).
- Make DQL query analysis react to creation/edit/deletion of the governing `dql-variables.json`.
- Support Find Usages from a variable definition to all DQL queries that consume it, respecting the existing
  "closest definition wins" directory-scoping rules.
- Reuse the existing scoping logic (`findVariableDefinitionFiles` / `findClosestDefinition`) so definition
  resolution and reverse lookup stay symmetric.

**Non-Goals:**
- Changing the `dql-variables.json` format or the value → DQL substitution semantics in `getVariableValue`.
- Introducing a stub index / global variable index (directory-prefix scan is retained; no format migration).
- Rename refactoring from the JSON side (only Find Usages navigation is in scope).
- Validating that a `$type: "dql"` fragment is itself a syntactically valid DQL query (schema checks structure
  only; DQL-fragment linting stays out of scope).

## Decisions

### Decision: Validate via a bundled JSON Schema + `JsonSchemaProviderFactory`

Register a `JsonSchemaProviderFactory` (extension point `com.intellij.json.jsonSchemaProviderFactory`) returning
a provider that matches files named `dql-variables.json` and serves a bundled schema resource
(`resources/schemas/dql-variables.schema.json`, Draft-07). The schema encodes: object at top level;
`additionalProperties` describing a variable value as `oneOf` [string, number, boolean, null, array, dql-fragment
object, record object]; the dql-fragment branch requires `$type == "dql"` and a string `dql`.

- **Why**: The JSON schema mechanism gives validation *and* completion/quick-docs for free, renders errors inline
  through the standard annotator, and keeps the rules declarative in one file.
- **Alternatives considered**: A hand-written `Annotator`/`LocalInspectionTool` over `JsonFile` PSI — more code,
  duplicates what schema validation already does, and no completion. Rejected. A JSON Schema *mapping* file the
  user maintains — pushes setup onto users; we want zero-config recognition by name.
- **Recursion note**: records and arrays reference the same value definition via a `$ref` to a shared
  `$defs/variableValue`, so nesting is validated at any depth.

### Decision: Fix reactivity through cache dependencies, not a listener

Change `VariableElementImpl.getDefinition()` so its `CachedValue` always depends on a modification tracker that
advances when any relevant `dql-variables.json` changes — including the unresolved case. The simplest correct
dependency is `PsiModificationTracker.MODIFICATION_COUNT` (project-wide PSI + VFS structure changes advance it,
covering create/edit/delete/move). Resolution then re-runs on the next access and the daemon re-highlights open
DQL files automatically.

- **Why**: The bug is purely a stale-cache problem: the unresolved branch depends only on `this`, so a
  newly-appearing definition is never observed. Depending on a tracker that also moves for the null case fixes
  create/edit/delete uniformly with minimal surface area.
- **Alternatives considered**: A VFS/PSI-tree listener that walks open DQL editors and forces re-highlight — more
  moving parts, easy to leak, and redundant with the daemon's own mod-count-driven restart. Rejected. A narrower
  tracker keyed only to `dql-variables.json` files would avoid some needless recomputation, but resolution is
  cheap and correctness/simplicity win here; a dedicated tracker can be a later optimization.
- **Consistency**: `DQLVariableReference.multiResolve()` already re-scans on each call, so go-to-definition is
  already reactive; this change brings `getDefinition()` (value/type/inlay/inspection path) in line.

### Decision: Reverse lookup via a `referencesSearch` extension + JSON-side Find Usages support

Add the inverse of `findVariableDefinitionFiles`: given a `JsonProperty` in a `dql-variables.json`, enumerate the
DQL files it governs and collect the `DQLVariableExpression`s whose name matches and for which *this* file is the
closest definition. Expose it two ways:

1. A `com.intellij.referencesSearch` `QueryExecutorBase` that, when the search target is a variable `JsonProperty`
   in a `dql-variables.json`, yields the `DQLVariableReference`s from the governed DQL files. This is what powers
   Find Usages results and keeps them consistent with existing reference semantics.
2. Find-Usages target support so the action is offered on the property. The default word-based searcher is
   unreliable here because DQL usages are written `$name` while the JSON key is `name`; the explicit executor
   removes that dependency on tokenization.

Governed-scope filtering reuses `findClosestDefinition` so a usage in a nested directory that has its own
`dql-variables.json` is attributed to the closer file only — the reverse direction is symmetric with resolution.

- **Why**: Reusing the existing directory-prefix + closest-definition logic guarantees Find Usages and
  Go-to-Definition agree on which query maps to which file.
- **Alternatives considered**: Relying solely on IntelliJ's default reference search over the property name —
  fails/incomplete because of the `$` prefix mismatch and would ignore the closest-definition scoping. Rejected.
- **Usage typing**: results reuse `DQLUsageTypeProvider` / the existing `findUsages.types.variables` category so
  they group with other DQL variable usages.

## Risks / Trade-offs

- **[MODIFICATION_COUNT is coarse — many unrelated edits invalidate the cache]** → Resolution is a bounded
  directory-prefix scan and only runs on access during analysis; acceptable. A dedicated tracker scoped to
  `dql-variables.json` files is a clean future optimization if profiling shows cost.
- **[Find Usages could be slow on large projects]** (scans DQL files in scope) → Scope is limited to the file's
  governed directory subtree, mirroring the existing definition scan; run under the standard search progress so
  it stays cancellable.
- **[Schema false positives block valid files]** → Keep the value schema permissive (open `record` objects allow
  arbitrary properties of supported value types); only the DQL-fragment shape is strict. Cover edge cases with
  tests before shipping.
- **[Name-based recognition matches an unrelated `dql-variables.json`]** → This matches today's discovery
  behavior exactly (name-based), so no regression; documented in the wiki.

## Migration Plan

Additive change — no format or API breakage for users. Existing `dql-variables.json` files gain validation and
reverse navigation automatically. Rollback is removing the new `plugin.xml` extension registrations and reverting
the `getDefinition()` cache-dependency change; the schema resource is inert if unregistered.

## Open Questions

- Should the schema also warn on duplicate variable keys (JSON allows them; IntelliJ already flags duplicate keys
  independently)? Leaning on the built-in duplicate-key inspection rather than encoding it in the schema.
- Do we want a dedicated `dql-variables.json` file-type icon/branding, or is name-based schema association
  sufficient for "recognised as our file"? Proposed: schema association only for this change.
