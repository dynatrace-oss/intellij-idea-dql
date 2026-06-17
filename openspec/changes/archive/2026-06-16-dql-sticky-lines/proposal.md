## Why

JetBrains editors support "sticky lines" — a feature that pins structural scope headers at the top of the viewport as
you scroll, helping users keep track of where they are in large files. DQL queries can grow long and complex, with
multiple pipeline stages and nested subqueries. Without sticky lines, users lose context about which pipeline command
they are editing and which subquery scope they are inside. Implementing sticky lines for DQL makes large queries
significantly more readable and navigable.

## What Changes

- Add a `DQLBreadcrumbsProvider` that registers breadcrumbs for DQL queries, which the IDE uses to drive sticky lines.
- The top-level pipeline entry command (any data source command: `fetch`, `data`, `timeseries`, `metrics`, `describe`,
  `fieldsSnapshot`, `load`, `smartscapeEdges`, `smartscapeNodes`) is pinned as a sticky line.
- For subqueries: only the subquery's own entry command is shown as a sticky line (the outer operator command is not).
- Register the provider with IntelliJ's `<breadcrumbsInfoProvider>` extension point for the DQL language.

## Capabilities

### New Capabilities
- `dql-sticky-lines`: Sticky lines support for DQL editors — pins the active pipeline entry command and each subquery's entry command as the user scrolls through a query.

### Modified Capabilities

<!-- No existing spec-level requirements are changing. -->

## Impact

- **New source files**: `DQLBreadcrumbsProvider` (Java), under `pl.thedeem.intellij.dql.editor`.
- **Plugin descriptor**: `plugin.xml` needs a new `<breadcrumbsInfoProvider>` extension registration.
- **DQL PSI / structure**: The provider will walk the PSI tree produced by the existing DQL grammar to locate pipeline
  commands and subquery blocks — read-only usage of existing PSI nodes.
- **No API changes** and no breaking changes.
