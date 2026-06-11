## Why

`QueryConfiguration` currently bundles two unrelated concerns: user-tunable execution parameters (scan limits,
timeframes) and environmental context (`tenant`, `originalFile`, or the file text content). This makes
`QueryConfiguration` a leaky abstraction that must carry location information to be usable,
and forces callers to drill through two levels(`context.configuration().tenant()`) to reach data that is conceptually
a top-level property of the execution context.

Beyond naming, a deeper problem is `QueryContext`: it is a transport object that pre-fetches expensive data (query
text from file, variables from PSI analysis) and carries it alongside cheap user settings. This forces the expensive
work to happen eagerly, at the wrong time, in the wrong place. Services that already know how to fetch this data
on demand (`DQLQuerySelectorService`, `DQLVariablesService`) are bypassed in favour of a bag object that duplicates
their responsibility.

## What Changes

- `QueryContext` is **dropped entirely**
- `QueryConfiguration` becomes the sole model: user-driven settings only — tenant, scan limits, result size limits,
  timeframe, source file path, and run config name
- `DQLExecutionService` accepts `QueryConfiguration` and fetches query text and variables itself, from the existing
  services, at the point of execution
- All APIs that currently accept `QueryContext` are updated to accept `QueryConfiguration` directly
- Eager query text and variable loading at configuration time is eliminated
- Each property of `QueryConfiguration` is exposed as an **individual `DataKey`** in the IntelliJ data context —
  so toolbar actions and panels can provide or update a single field without replacing the whole configuration object;
  `DQLQueryConfigurationService` assembles these individual keys into a `QueryConfiguration` when a full object is
  needed

## Capabilities

### New Capabilities

_(none — this is a pure structural refactoring, no new capabilities are introduced)_

### Modified Capabilities

_(none)_

## Impact

- **`QueryContext`**: deleted
- **`QueryConfiguration`**: gains `runConfigName` (was on `QueryContext`); all other fields unchanged
- **`DQLExecutionService`**: constructor takes `QueryConfiguration` only; `preparePayload()` calls
  `DQLQuerySelectorService` and `DQLVariablesService` directly instead of reading from a `QueryContext`
- **`ExecuteDQLRunConfiguration.getConfiguration()`**: return type changes to `QueryConfiguration`; no longer builds
  a `QueryContext`
- **`DQLExecutionResult`** and **`DQLExecutionErrorPanel`**: receive query text as a plain `String` parameter rather
  than extracting it from `QueryContext`
- **`DQLQueryConfigurationService`**: gains individual `DataKey<T>` constants for each `QueryConfiguration` field
  (e.g. `DATA_TENANT`, `DATA_TIMEFRAME_START`, …); gains a method that reads these keys from a `DataContext` and
  assembles a `QueryConfiguration`; the existing single-object `DATA_QUERY_CONFIGURATION` key is removed
- **Providers** (`DQLExecutionService`, editor toolbar, etc.): set individual data keys instead of a whole
  `QueryConfiguration` object, enabling partial updates (e.g. only tenant changes)
