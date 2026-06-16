## Why

JetBrains editors support "sticky lines" — a feature that pins structural scope headers at the top of the viewport as
you scroll, helping users keep track of where they are in large files. DQL queries can grow long and complex, with
multiple pipeline stages and nested subqueries. Without sticky lines, users lose context about which pipeline command
they are editing and which subquery scope they are inside. Implementing sticky lines for DQL makes large queries
significantly more readable and navigable.

## What Changes

- Add a `DqlStickyLinesProvider` that identifies sticky line candidates in a DQL document.
- The top-level pipeline entry command (any data source command: `fetch`, `data`, `timeseries`, `metrics`, `describe`,
  `fieldsSnapshot`, `load`, `smartscapeEdges`, `smartscapeNodes`) is pinned as the first sticky line.
- Subquery entry points pin two sticky lines: the enclosing operator (`append`, `join`, `lookup`, etc.) and the
  subquery's own entry command, displayed as `append → fetch` or `join → data`.
- Register the provider with IntelliJ's sticky-lines extension point for the DQL language.

## Capabilities

### New Capabilities
- `dql-sticky-lines`: Sticky lines support for DQL editors — pins the active pipeline entry command and each subquery's entry command as the user scrolls through a query.
  headers as the user scrolls through a query.

### Modified Capabilities

<!-- No existing spec-level requirements are changing. -->

## Impact

- **New source files**: `DqlStickyLinesProvider` (Kotlin), likely under the editor or language support package.
- **Plugin descriptor**: `plugin.xml` needs a new extension registration for the sticky-lines extension point.
- **DQL PSI / structure**: The provider will walk the PSI tree produced by the existing DQL grammar to locate pipeline
  commands and subquery blocks — read-only usage of existing PSI nodes.
- **No API changes** and no breaking changes.
