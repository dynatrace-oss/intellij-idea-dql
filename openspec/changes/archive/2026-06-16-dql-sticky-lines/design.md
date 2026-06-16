## Context

The DQL IntelliJ plugin (platform `sinceBuild=251`, version 2025.3) provides full PSI support for Dynatrace Query
Language. Queries are represented as a `DQLQuery` containing a list of `DQLCommand` nodes. The first command in each
query is the "entry command" — always a data source command as defined in `dql.overrides.json` under the `dataSources`
key: `fetch`, `data`, `timeseries`, `metrics`, `describe`, `fieldsSnapshot`, `load`, `smartscapeEdges`,
`smartscapeNodes`. Subqueries are embedded via `DQLSubqueryExpression` — a PSI element that wraps a nested `DQLQuery`
and is itself a child of the outer `DQLCommand` that triggers it (e.g., `append`, `join`, `joinNested`, `lookup`).

The IntelliJ platform has supported sticky lines since 2023.1. Plugins register a `StickyLinesProvider` via the
`codeInsight.stickyLinesProvider` extension point. The provider returns a collection of line offsets that the editor
pins at the viewport top as the user scrolls past them.

All existing editor features in this plugin live under `pl.thedeem.intellij.dql.editor`. Source is Java.

## Goals / Non-Goals

**Goals:**

- Pin the entry command of the top-level query as a sticky line.
- For subquery scopes: pin both the outer operator command and the inner entry command as a pair (e.g., `append`, then
  `fetch`).
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
  paired display ("append" then "fetch").

**Traversal logic:**

for each DQLQuery ancestor (innermost first):
    entry = query.getCommandList().first()   // guard: skip if empty
    emit breadcrumb(entry.name)
    emit sticky(entry.textOffset)
```
**Why operator commands are not included:** accepting `DQLCommand` nodes for operators like `append`/`join`/`lookup` was considered but removed — showing only the entry command of each scope level is sufficient to orient the user, and keeps the sticky header concise.
O(depth) rather than O(file), and depth rarely exceeds 3.

### 2. Extension point and class placement

Register as `codeInsight.stickyLinesProvider` with `language="DQL"` in `plugin.xml`. New class:
`pl.thedeem.intellij.dql.editor.DqlStickyLinesProvider`.

**Alternative considered:** A `StructureViewTreeElement`-based approach that reuses the structure view. Rejected because
the sticky lines EP accepts line offsets directly and doesn't require a full structure tree.

### 3. Verifying the platform API before implementation

The sticky lines EP was introduced in 2023.1 (build 231); `sinceBuild=251` means it's available. The interface is
`com.intellij.codeInsight.stickyLines.StickyLinesProvider`. **Confirm the exact package and method signature** by
checking IntelliJ platform sources or SDK docs before writing the class — the API has been refined across minor
releases.

### 4. Null-safety at PSI boundaries

`DQLQuery.getCommandList()` may return an empty list during incomplete edits. The provider must guard against this and
return an empty collection rather than throwing.

## Risks / Trade-offs

- **API stability** → `StickyLinesProvider` was `@ApiStatus.Internal` in early 2023 releases; verify it is public in the
  2025.3 SDK. Mitigation: check platform API docs before implementing; if internal, use
  `@SuppressWarnings("UnstableApiUsage")` and track for promotion.
- **Performance** → Ancestor traversal is cheap, but calling `getCommandList()` triggers PSI reads. Mitigation: the
  platform already throttles sticky-line computation to avoid blocking the EDT.
- **Empty subquery during editing** → `DQLSubqueryExpression.getQuery()` can return `null` on incomplete input.
  Mitigation: null-check before traversal.
- **Injected DQL** → Subqueries in injected fragments have a different PSI root. Excluding injected fragments (Non-Goal)
  avoids this complexity for the initial implementation.
