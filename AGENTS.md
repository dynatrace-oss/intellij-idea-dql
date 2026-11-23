# AGENTS.md

Guidance for AI coding agents working in this repository.

## What this project is

An IntelliJ Platform plugin (works in most JetBrains IDEs) that adds language support for two Dynatrace languages:

- **DQL**:
  [Dynatrace Query Language](https://docs.dynatrace.com/docs/discover-dynatrace/references/dynatrace-query-language)
- **DPL**:
  [Dynatrace Pattern Language](https://docs.dynatrace.com/docs/discover-dynatrace/platform/grail/dynatrace-pattern-language)

It is a community open-source project (`dynatrace-oss/intellij-idea-dql`), not an official Dynatrace product.

Features fall into two groups:

- **Local** (no network): syntax highlighting, formatting/code style, completion, references, hover docs,
  inspections with quick fixes, intentions, structure view, folding, nested languages (DPL and JSON injected into
  DQL strings), support for DQL that users inject into other languages via IntelliJ language injection, and
  Dynatrace Dashboard variables (`dql-variables.json`).
- **Remote** (requires a connected Dynatrace tenant via OAuth/SSO or platform token): query execution with
  table/JSON/chart results, live validation (external annotator), live autocompletion of fields and data objects,
  run configurations, a query console, an execution toolbar and gutter icons.

User-facing feature documentation lives in `README.md` and `docs/wiki/` (published to the GitHub wiki by
`.github/workflows/wiki.yml`). Read the relevant wiki page before changing a feature's behavior.

## Critical rule: no deprecated or internal IntelliJ Platform API

**Never use deprecated, internal or scheduled-for-removal IntelliJ Platform API.** This is a hard requirement,
not a style preference. Such usages break the plugin on newer IDE versions and are flagged by the JetBrains
Marketplace verification.

Treat as forbidden anything that is:

- annotated `@Deprecated`, `@ApiStatus.Internal` or `@ApiStatus.ScheduledForRemoval`, or deprecated in Kotlin
- marked `@ApiStatus.OverrideOnly` or `@ApiStatus.NonExtendable`, when called or extended against those contracts

Judge by annotations and documentation, not by package name. Some public, documented API lives in `impl`
packages, for example the extension point classes `PostFormatProcessor` and `UsageTypeProvider`, and is fine
to use. Classes in `impl` packages are internal more often than not, though, so check them before relying on them.

Avoid `@ApiStatus.Experimental` API too. If there is no alternative, ask before using it, and call it out in the
change.

How to comply:

- Before using a platform class or method, check its annotations and the
  [IntelliJ Platform SDK docs](https://plugins.jetbrains.com/docs/intellij/) for the recommended replacement.
- If the only way to do something seems to need such an API, stop and ask instead of using it.
- Don't copy a deprecated or internal usage from existing code. Report it so it can be fixed.
- `./gradlew verifyPlugin` reports these usages in `build/reports/pluginVerifier`, but it does **not** fail the
  build for them. A green build is not proof. Check the report when you touch platform API.

## Languages and file types

| Language | Extension  | Package                       | Grammar                                |
|----------|------------|-------------------------------|----------------------------------------|
| DQL      | `.dql`     | `pl.thedeem.intellij.dql`     | `src/main/grammar/dql.bnf`, `dql.flex` |
| DPL      | `.dpl`     | `pl.thedeem.intellij.dpl`     | `src/main/grammar/dpl.bnf`, `dpl.flex` |
| DQLExpr  | `.dqlexpr` | `pl.thedeem.intellij.dqlexpr` | reuses the DQL grammar                 |
| DQLPart  | `.dqlpart` | `pl.thedeem.intellij.dqlpart` | reuses the DQL grammar                 |

`DQLExpr` (only expressions, no command context) and `DQLPart` (fragments of a query) are separate IDE languages
built on the DQL lexer and parser (see `DQLPartParserDefinition`, `ExpressionDQLParser`). They have no grammar
files of their own. Changes to the DQL grammar or PSI affect all three.

## Tech stack

- Java 21 as the default language, Kotlin where the IntelliJ Platform API is Kotlin-first (see
  [Java or Kotlin](#java-or-kotlin)). Both live in `src/main/java`.
- Gradle (Kotlin DSL) with the IntelliJ Platform Gradle Plugin 2.x. Versions live in `gradle/libs.versions.toml`.
  Target platform and `sinceBuild` are in `gradle.properties`.
- Grammar-Kit (via the `org.jetbrains.intellij.platform.grammarkit` Gradle plugin) for parsers and JFlex lexers.
- Jackson for JSON, JFreeChart for result visualizations, Swing with IntelliJ UI components for UI.
- Tests: IntelliJ test framework on JUnit 4, plus Mockito.

## Build, run, test

Use the Gradle wrapper (`./gradlew` on Unix, `.\gradlew.bat` on Windows).

| Task                        | Command                                                           |
|-----------------------------|-------------------------------------------------------------------|
| Full build with tests       | `./gradlew build`                                                 |
| Tests only                  | `./gradlew check`                                                 |
| Run one test class          | `./gradlew test --tests "pl.thedeem.intellij.dql.DQLParsingTest"` |
| Sandbox IDE with the plugin | `./gradlew runIde`                                                |
| Plugin ZIP                  | `./gradlew buildPlugin`                                           |
| Plugin verifier             | `./gradlew verifyPlugin`                                          |
| Markdown lint (Docker)      | `make markdownlint`                                               |

CI (`.github/workflows/build.yml`) runs `buildPlugin`, `check` and `verifyPlugin` on every PR.

Notes for running these non-interactively:

- The first Gradle run downloads the IntelliJ Platform distribution and can take many minutes. Allow long
  timeouts, and run in the background where possible.
- `runIde` opens a GUI IDE and does not exit on its own. Don't use it to validate changes. Rely on tests.
- Tests run headless and mock all Dynatrace HTTP calls, so no tenant, token or network access is needed.

### Generated code

The parsers and lexers are generated by Gradle tasks (`generateParser`, `generateLexer`, `generateDplParser`,
`generateDplLexer`) into `src/main/gen/` and are wired into the main source set. This runs automatically
before compilation.

- `src/main/gen/` is git-ignored. Never edit it by hand. Change the `.bnf`/`.flex` file and rebuild.
- If PSI classes such as `DQLTypes`, `DQLCommand` or `DPLExpression` seem to be missing, run a build first.
- The "Building" section in `README.md` predates this setup. No manual Grammar-Kit generation is needed.

## Repository layout

```text
src/main/grammar/            .bnf and .flex files for DQL and DPL
src/main/java/pl/thedeem/intellij/
  common/                    shared code across languages
    annotator/ completion/ documentation/ psi/ quickFixes/   base classes and helpers
    components/              reusable UI (tables, charts, panels)
    sdk/                     Dynatrace REST/OAuth client, request/response models, errors
  dql/                       DQL language
    psi/                     PSI helpers, token sets; psi/elements/ holds mixins and interfaces used by the grammar
    completion/ annotator/ highlighting/ style/ structure/ code/ documentation/ indexing/
    inspections/             inspections grouped by topic (commands, expressions, fields, functions, ...);
                             fixes/ holds quick fixes, external/ the live tenant validation annotator
    intentions/
    services/                project/application services (see below)
    exec/                    query execution, run configurations, result panels
    editor/                  editor toolbar, gutter icons, breadcrumbs
    fileProviders/           virtual files and editors for the console and results
    settings/                plugin settings and tenant management
  dpl/                       DPL language (same structure as dql/, smaller)
  dqlexpr/ dqlpart/          partial-DQL languages
src/main/resources/
  META-INF/plugin.xml        all extension point registrations
  definition/                language definitions (see below)
  messages/                  DQLBundle.properties, DPLBundle.properties
  inspectionDescriptions/    one HTML file per inspection (named after its shortName)
  intentionDescriptions/     one folder per intention (description.html, before/after templates)
  schemas/                   JSON schema for dql-variables.json
  oauth/                     OAuth client config and callback pages
src/test/java                tests, mirroring the main package layout
src/test/testData            fixture files: parsing/, completion/, indexing/, services/
docs/wiki/                   user documentation (synced to the GitHub wiki)
openspec/                    spec-driven change proposals (see below)
```

## Key architectural concepts

### Language definitions drive semantics

The grammar is intentionally generic: it parses commands, parameters, functions and operators without
knowing which ones exist. The actual knowledge (commands, their parameters, functions, signatures, data types,
operators) comes from JSON:

- `definition/dql.json` – the large upstream DQL definition. Treat it as data synced from Dynatrace and
  avoid hand-editing it.
- `definition/dql.overrides.json` – plugin-specific additions and corrections merged on top of `dql.json`
  (`DefinitionUtils.mergeDefinitions`). Put local fixes here. Know how the merge works before editing it:
  - Top-level maps (`commands`, `functions`, `operators`, `dataTypes`, ...) merge by key. Within an existing
    entry, fields you leave out of the override keep their upstream value.
  - Lists are **replaced, not merged**. This includes a command's `parameters` (see
    `ParameterListMergingDeserializer`) and any other list, such as a function's `signatures` or a parameter's
    `valueTypes`. An override list must therefore always be complete, or the missing entries silently disappear
    along with their completion and validation.
  - If the override file already defines the list, edit that list in place. Never replace it with the upstream list,
    because that discards plugin-specific corrections: the `dql.command.fetch` override, for instance,
    replaces the upstream `name` parameter with `bucket`.
  - Copy the upstream list from `dql.json` only when creating a new override for a list that isn't overridden
    yet, then apply your change to the copy.
- `definition/dpl.json` – DPL commands, expressions and POSIX classes.

These are loaded by `DQLDefinitionService` / `DPLDefinitionService` (project services) and mapped to models in
`dql/services/definition/model` and `dpl/definition/model`. Completion, inspections, documentation and parameter
validation all query
these services instead of hardcoding language knowledge. If you need "is X a valid command/function/parameter",
use the definition service.

### Services

Most logic lives in IntelliJ services registered in `plugin.xml` (interface plus `*Impl`), retrieved with
`getService(...)`. Notable ones:

- `DQLParametersCalculatorService` maps command and function arguments to definition parameters.
  `DQLParameterValueTypesValidator` and `services/parameters/validators/*` validate the values.
- `DQLFieldsCalculatorService` computes fields available at each point of a query.
- `DQLQuerySelectorService` picks the query text to run from a file or the editor context (whole file, a
  subquery, an injected fragment).
- `DQLQueryParserService` substitutes `dql-variables.json` values into a query and keeps an offset mapping, so
  errors reported by the tenant can be placed back at the right location in the original text.
- `DQLQueryConfigurationService` manages per-query execution config: tenant, timeframe, scan and result limits.
- `DQLVariablesService` handles `dql-variables.json` loading, live reload and find usages.
- `DynatraceRestService` and `DynatraceOAuthService` handle tenant communication. `DynatraceTenantsService`
  stores tenant settings.

In tests, replace services with mocks via `ServiceContainerUtil.registerOrReplaceServiceInstance` (see existing
completion tests).

### PSI

Generated PSI classes extend handwritten mixins in `dql/psi/elements/impl` and `dpl/psi/elements/impl` and
implement interfaces in `.../psi/elements`, declared through `mixin=`/`implements=` in the `.bnf`. Put custom PSI
behavior (names, references, presentation) in those mixins or in `DQLPsiImplUtil`/`DPLPsiImplUtil`, not in
generated code.

## Common tasks

**Add an inspection:**

1. Create the class under `dql/inspections/<group>/` (or `dpl/inspections/`), following neighboring inspections.
2. Register a `<localInspection>` in `plugin.xml` with `shortName`, `bundle`, `key`, `groupKey`, level.
3. Add `src/main/resources/inspectionDescriptions/<shortName>.html`.
4. Add the display name and messages to `DQLBundle.properties` / `DPLBundle.properties`.
5. Reuse quick fix bases from `common/quickFixes` where possible.

**Add an intention:** class in `.../intentions/`, register in `plugin.xml`, add
`intentionDescriptions/<ClassName>/` with `description.html`, `before.*.template`, `after.*.template`.

**Change syntax:** edit the `.bnf`/`.flex`, rebuild, then update or add parsing tests and their expected
PSI dumps (`src/test/testData/parsing/<lang>/*.txt`).

**Add or fix command/function knowledge:** change `dql.overrides.json` (or `dpl.json`), not Java code. Lists in
overrides replace the upstream list, so keep them complete. Edit an existing override list in place, and copy
from upstream only when adding a new override (see
[Language definitions drive semantics](#language-definitions-drive-semantics)).

## Testing

- Parsing tests extend `ParsingTestCase`. Each test method `testX` reads `testData/parsing/<lang>/X.<ext>`
  and compares against `X.txt`. To create or refresh an expected dump, run the test once and copy the actual
  output. Check the PSI tree diff carefully; don't blindly accept it.
- Completion, reference and indexing tests extend `LightPlatformCodeInsightFixture4TestCase`, using fixture
  files in `testData/completion` and `testData/indexing`. `DQLTestsUtils` / `DPLTestsUtils` provide helpers to
  build mocked definitions.
- Service and validator tests live under `src/test/java/.../dql/services`.
- Add tests for any bug fix or new behavior that can be reproduced headlessly.

## Coding conventions

### Java or Kotlin

Follow the plugin development approach JetBrains currently recommends in the
[IntelliJ Platform SDK docs](https://plugins.jetbrains.com/docs/intellij/). Java is the first choice. Use Kotlin
for code built on platform APIs that are designed for Kotlin and awkward or unidiomatic from Java, for example:

- coroutines: an injected service `CoroutineScope`, `suspend` functions, `readAction { }`, `withBackgroundProgress`
- `suspend`-based extension points such as `ProjectActivity`
- [Kotlin UI DSL](https://plugins.jetbrains.com/docs/intellij/kotlin-ui-dsl-version-2.html) for settings pages
  and dialog forms

Keep the Kotlin part as small as the API requires, and expose a Java-friendly interface where Java code calls it.
`DynatraceRestServiceImpl.kt` and `DynatraceOAuthServiceImpl.kt` follow this pattern: a Java service interface
with a Kotlin implementation that uses coroutines. Don't implement Kotlin-first APIs from Java by hand-writing
`Continuation` parameters (as the existing `ServicesAutoRegistrationStartupActivity` does). Don't convert existing
Java code to Kotlin unless the change requires one of these APIs.

### General

- Follow the style of surrounding code. Always use braces, even for single-line `if`/`for` bodies.
- Annotate parameters and return values with `@NotNull` / `@Nullable` (`org.jetbrains.annotations`).
- Use modern Java (records, pattern matching, switch expressions) where it reads well.
- Prefer the current, recommended platform API over older alternatives, even if older ones still appear in the
  codebase. When you touch code that uses a deprecated or discouraged approach, mention it rather than spreading it.
- All user-facing strings go into `DQLBundle.properties` / `DPLBundle.properties` and are read via
  `DQLBundle.message(...)` / `DPLBundle.message(...)`. Follow JetBrains capitalization rules (title case for
  actions and titles, sentence case for descriptions).
- Prefer IntelliJ UI classes (`JBLabel`, `JBTable`, `JBScrollPane`, `JBUI`, popups, notifications) and existing
  components in `common/components` over raw Swing. Build new forms with the Kotlin UI DSL rather than
  `FormBuilder` or handwritten layouts.
- Never block the EDT. For new background work, prefer coroutines (in Kotlin, see above). In Java,
  `Task.Backgroundable` and `ReadAction` are acceptable. PSI access off the EDT always needs a read action.
- Language analysis runs constantly. Cache derived results (`CachedValuesManager`, service caches) instead of
  recomputing on every call.
- Only use public, non-deprecated, non-internal IntelliJ APIs. See
  [the critical rule](#critical-rule-no-deprecated-or-internal-intellij-platform-api).
- Fix warnings instead of adding `@SuppressWarnings`.
- Keep comments for non-obvious constraints only, not for narrating code.

## Changelog

Every user-visible change needs an entry in `CHANGELOG.md` under `## [Unreleased]`, in the existing
`### Features` or `### Bug fixes` sections (add the section if missing). Write entries from the user's
perspective, one bullet per change, wrapped at about 120 characters. The changelog feeds the plugin's
Marketplace release notes through the Gradle changelog plugin, so keep the format intact.

If a change alters user-visible behavior, update the matching page in `docs/wiki/` too.

## OpenSpec workflow

Larger changes may be planned with [OpenSpec](https://github.com/Fission-AI/OpenSpec): proposals live in
`openspec/changes/`, accepted specs in `openspec/specs/`, and finished changes are archived under
`openspec/changes/archive/`. Each change folder has `proposal.md`, `design.md`, `tasks.md` and delta specs.
When implementing an existing change, follow its `tasks.md` and tick off items as you complete them.

Step-by-step instructions for proposing, applying and archiving changes are in
`.claude/skills/openspec-*/SKILL.md` (mirrored in `.github/skills/`). They are plain Markdown, so any agent can
read and follow them, not only the tools that load those folders automatically.

## Before you finish

1. Run `./gradlew build` (or at least `./gradlew check` for test-only changes) and make sure it passes.
2. Confirm the change introduces no deprecated, internal or experimental platform API. If you touched platform
   API, run `./gradlew verifyPlugin` and check `build/reports/pluginVerifier`.
3. Remove unused imports, dead code and duplication introduced by the change.
4. Add or update the changelog entry, and the wiki page if behavior changed.
5. Don't create extra summary or notes Markdown files. Put the result in code, tests, changelog and docs.

If these instructions contradict what the codebase actually does, follow the codebase and point out the
discrepancy so this file can be updated. The exception is the platform API rule: never follow existing code
into deprecated or internal API.
