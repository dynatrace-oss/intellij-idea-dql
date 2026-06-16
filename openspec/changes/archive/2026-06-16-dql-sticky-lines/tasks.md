## 1. Verify Platform API

- [x] 1.1 Confirm the exact interface name and package for `StickyLinesProvider` in the 2025.3 SDK (check platform
  sources or IntelliJ SDK docs for `codeInsight.stickyLinesProvider`)
- [x] 1.2 Confirm whether the API requires `@SuppressWarnings("UnstableApiUsage")` and note any required `@ApiStatus`
  handling

> Outcome: IntelliJ does not expose a public `StickyLinesProvider` extension point — sticky lines are
> driven entirely by `com.intellij.ui.breadcrumbs.BreadcrumbsProvider` (extension point
> `com.intellij.ui.breadcrumbs`). Implementation pivoted to a `BreadcrumbsProvider`; no unstable-API
> suppression is required.

## 2. Implement DQLBreadcrumbsProvider

- [x] 2.1 Create `src/main/java/pl/thedeem/intellij/dql/editor/DQLBreadcrumbsProvider.java` implementing the platform
  `BreadcrumbsProvider` interface
- [x] 2.2 Implement PSI ancestor handling: accept every `DQLQuery` that has at least one command so its first command's
  line is pinned (sticky lines walk PSI ancestors automatically)
- [x] 2.3 In `getElementInfo`, return the first `DQLCommand` name for `DQLQuery` (guarding against an empty
  `getCommandList()`)
- [x] 2.4 Accept any `DQLCommand` whose subtree contains a `DQLSubqueryExpression` so the operator (`append`/`join`/
  `lookup`) line sticks immediately before the subquery's entry sticky line
- [x] 2.5 Return empty info / `false` from `acceptElement` when the PSI is incomplete so the editor never throws

## 3. Register Extension

- [x] 3.1 Add `<breadcrumbsInfoProvider implementation="pl.thedeem.intellij.dql.editor.DQLBreadcrumbsProvider"/>` to
  `src/main/resources/META-INF/plugin.xml`

## 4. Verify Behaviour

- [x] 4.1 Open a multi-stage DQL query (≥ 10 lines) and scroll past the entry command — confirm it sticks at the
  viewport top
- [x] 4.2 Place the cursor inside an `append [...]` subquery — confirm one sticky line appears for the subquery's entry
  command (`fetch`/`data`/etc.)
- [x] 4.3 Place the cursor inside a doubly-nested subquery — confirm sticky lines accumulate for each scope's entry
  command in order (one per scope level)
- [x] 4.4 Verify that `filter`, `sort`, and other non-entry commands do not appear as sticky lines
- [x] 4.5 Type an incomplete subquery (`| append [`) and confirm the editor does not throw an exception
