## Context

The DQL IntelliJ plugin (platform `sinceBuild=251`, version 2025.3) provides full PSI support for Dynatrace Query
Language. Queries are represented as a `DQLQuery` containing a list of `DQLCommand` nodes. The first command in each
query is the "entry command" — always a data source command as defined in `dql.overrides.json` under the `dataSources`
key: `fetch`, `data`, `timeseries`, `metrics`, `describe`, `fieldsSnapshot`, `load`, `smartscapeEdges`,
`smartscapeNodes`. Subqueries are embedded via `DQLSubqueryExpression` — a PSI element that wraps a nested `DQLQuery`
and is itself a child of the outer `DQLCommand` that triggers it (e.g., `append`, `join`, `joinNested`, `lookup`).

The IntelliJ platform drives sticky lines via breadcrumbs. Plugins register a `BreadcrumbsProvider` via the
`<breadcrumbsInfoProvider>` extension point. The platform walks PSI ancestors from the caret and pins each accepted
element's line at the viewport top as the user scrolls past it.

All existing editor features in this plugin live under `pl.thedeem.intellij.dql.editor`. Source is Java.

## Goals / Non-Goals

**Goals:**

- Pin the entry command of the top-level query as a sticky line.
- For subquery scopes: pin only the subquery's own entry command (e.g., `fetch` inside `append [...]`). The outer operator command is not shown.
- Display multiple sticky lines when the cursor is inside a nested subquery (one per scope level).

**Non-Goals:**

- Sticky lines for non-entry commands (`filter`, `summarize`, `sort`, etc.).
- Sticky lines in injected DQL fragments (e.g., DQL injected inside JSON/YAML) — keep scope to native `.dql` files for
  now.
- Custom label rendering beyond what the platform's sticky lines mechanism provides (labels come from the line text
  itself).

## Decisions

### 1. PSI traversal to collect scope chain
The provider accepts only `DQLQuery` nodes. The IntelliJ Platform walks PSI ancestors from the caret position upward, calling `acceptElement` on each — so every enclosing `DQLQuery` that has at least one command produces exactly one sticky line showing that query's entry command (the first `DQLCommand`).

**Traversal logic:**

```
for each DQLQuery ancestor (innermost first):
    entry = query.getCommandList().first()   // guard: skip if empty
    emit breadcrumb(entry.name)
    emit sticky(entry.textOffset)
```
**Why operator commands are not included:** accepting `DQLCommand` nodes for operators like `append`/`join`/`lookup` was considered but removed — showing only the entry command of each scope level is sufficient to orient the user, and keeps the sticky header concise.
O(depth) rather than O(file), and depth rarely exceeds 3.

### 2. Extension point and class placement

Register via `<breadcrumbsInfoProvider implementation="pl.thedeem.intellij.dql.editor.DQLBreadcrumbsProvider"/>` in `plugin.xml`. New class:
`pl.thedeem.intellij.dql.editor.DQLBreadcrumbsProvider`.

**Alternative considered:** A `StructureViewTreeElement`-based approach that reuses the structure view. Rejected because
the `BreadcrumbsProvider` EP is simpler and directly drives sticky-line behavior without requiring a full structure tree.

### 3. Verifying the platform API before implementation

IntelliJ does not expose a public `StickyLinesProvider` EP — sticky lines are driven entirely by the breadcrumbs
mechanism. The interface used is `com.intellij.ui.breadcrumbs.BreadcrumbsProvider`, registered via the
`<breadcrumbsInfoProvider>` tag. This is a stable public API; no `@SuppressWarnings("UnstableApiUsage")` is required.

### 4. Null-safety at PSI boundaries

`DQLQuery.getCommandList()` may return an empty list during incomplete edits. The provider must guard against this and
return an empty collection rather than throwing.

## Risks / Trade-offs

- **API stability** → `BreadcrumbsProvider` is a stable public API in 2025.3; no `@SuppressWarnings("UnstableApiUsage")` is required.
- **Performance** → Ancestor traversal is cheap, but calling `getCommandList()` triggers PSI reads. Mitigation: the
  platform already throttles sticky-line computation to avoid blocking the EDT.
- **Empty subquery during editing** → `DQLSubqueryExpression.getQuery()` can return `null` on incomplete input.
  Mitigation: null-check before traversal.
- **Injected DQL** → Subqueries in injected fragments have a different PSI root. Excluding injected fragments (Non-Goal)
  avoids this complexity for the initial implementation.
