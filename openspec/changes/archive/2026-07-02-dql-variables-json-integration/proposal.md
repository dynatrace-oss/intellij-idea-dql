## Why

The plugin's support for `dql-variables.json` is one-directional: a DQL query resolves each `$variable`
to a definition in the nearest `dql-variables.json` and caches it, but the plugin never treats the JSON
file as *its own* artifact. As a result the file gets no validation (a malformed definition fails silently
at query-substitution time), stale definitions can linger in already-analysed queries when the file changes,
and there is no way to navigate from a definition to the queries that consume it. Making the relationship
two-sided turns `dql-variables.json` into a first-class, validated, reactive part of the DQL tooling.

## What Changes

- The plugin **recognises `dql-variables.json` as its own managed file** and validates its content against a
  published JSON schema: the top level must be an object, each property is a variable whose value is a string,
  number, boolean, null, array, object, or a DQL fragment object (`{"$type": "dql", "dql": "<query>"}`).
  Schema violations are reported as inspections directly in the JSON editor.
- DQL queries **reactively reload variable definitions** whenever the governing `dql-variables.json` is created,
  edited, or deleted — resolved values, data types, inlay hints, and inspections update without reopening the
  query. This fixes the current gap where a query that resolved to *no definition* never re-resolves after the
  file appears, and where cached definitions can outlive edits to the file.
- **Find Usages works from a variable definition**: invoking Find Usages on a property in `dql-variables.json`
  lists every DQL query (within the file's directory scope) that references that variable.

## Capabilities

### New Capabilities
- `dql-variables-schema`: Recognition of `dql-variables.json` as a plugin-owned file and validation of its
  contents against the DQL variables JSON schema.
- `dql-variables-live-reload`: Reactive propagation of `dql-variables.json` changes to every DQL query that
  resolves variables against it.
- `dql-variables-find-usages`: Reverse navigation from a variable definition in `dql-variables.json` to all
  DQL queries that use the variable.

### Modified Capabilities
_(none — no existing spec's requirements change)_

## Impact

- **New JSON schema resource** (e.g. `resources/schemas/dql-variables.schema.json`) plus a
  `JsonSchemaProviderFactory` registered in `plugin.xml`, scoped to files named `dql-variables.json`.
- **`DQLVariablesService` / `DQLVariablesServiceImpl`**: gains discovery of governed DQL files for a given
  `dql-variables.json` (the reverse of `findVariableDefinitionFiles`) to support Find Usages and reload scope.
- **`VariableElementImpl.getDefinition()`**: cache-dependency change so the resolved definition (including the
  null/unresolved case) invalidates when any relevant `dql-variables.json` changes.
- **Find Usages**: a `referencesSearch` (or equivalent `UsageSearcher`) extension and Find-Usages handling that
  make a `JsonProperty` in `dql-variables.json` a searchable target resolving to `DQLVariableExpression` usages;
  registered in `plugin.xml`.
- **`plugin.xml`**: new `<extensions>` registrations (JSON schema provider, references search / find-usages).
- **Tests**: extend `DQLVariableReferencesTest`; add schema-validation and reload/find-usages coverage.
- **Docs**: update `docs/wiki/DQL.md` variables section.
