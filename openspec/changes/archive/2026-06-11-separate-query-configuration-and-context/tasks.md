## 1. Audit QueryContext usages before deleting

- [x] 1.1 Grep for all accesses of `context.query()` — list every call site
- [x] 1.2 Grep for all accesses of `context.definedVariables()` — determine which consumers actually use this field
- [x] 1.3 Grep for all accesses of `context.configuration()` — list every call site
- [x] 1.4 Grep for `context.getRunConfigName()` / `context.setRelatedRunConfiguration()` — list every call site
- [x] 1.5 Grep for `new QueryContext()` — list every construction site

## 2. Update QueryConfiguration model

- [x] 2.1 Add `runConfigName` field with accessor `runConfigName()` and mutator `setRunConfigName(String)` to
  `QueryConfiguration`
- [x] 2.2 Update `QueryConfiguration.copy()` to include `runConfigName`

## 3. Update ExecuteDQLRunConfiguration

- [x] 3.1 Change `getConfiguration()` return type to `QueryConfiguration`; set `runConfigName` on the config instead of
  on a `QueryContext`; remove all `QueryContext` construction from this method
- [x] 3.2 Update `getState()`: replace the `QueryContext payload = getConfiguration()` pattern — pass the
  `QueryConfiguration` directly to `DQLExecutionService` (no `QueryContext` needed)
- [x] 3.3 Update `loadFromConfiguration()`: read run config name from `QueryConfiguration.runConfigName()` if needed;
  remove any remaining `QueryContext` references

## 4. Update DQLExecutionService

- [x] 4.1 Change constructor to
  `(String name, QueryConfiguration configuration, @Nullable String selectedQuery, Project project, DQLProcessHandler handler)` —
  remove the `QueryContext params` parameter
- [x] 4.2 Replace the `context` and `configurationCopy` fields (both `QueryContext`) with a single
  `QueryConfiguration configuration` field (and a `configurationCopy` of the same type) plus
  `@Nullable String selectedQuery`
- [x] 4.3 Update `getParentGroups()`, `equals()`, and `hashCode()` to read `tenant` from `configuration` directly
- [x] 4.4 Update `getNavigatable()` to read `originalFile` from `configuration` directly
- [x] 4.5 Update `uiDataSnapshot()` to set individual per-property data keys (handled fully in task 7.4)
- [x] 4.6 Rewrite `preparePayload()`: resolve query text via `DQLQuerySelectorService` and variables via
  `DQLVariablesService` using the file at `configuration.originalFile()`; remove the `context` parameter
- [x] 4.7 Update `startExecution()`: pass the resolved query text `String` directly to `DQLExecutionErrorPanel` and
  `DQLExecutionResult` instead of passing `context`

## 5. Update DQLExecutionResult and DQLExecutionErrorPanel

- [x] 5.1 Audit `DQLExecutionResult` constructor — replace `QueryContext` parameter with query text `String`
- [x] 5.2 Audit `DQLExecutionErrorPanel` constructor — already accepts `String query` (no change needed)

## 6. Remove QueryContext

- [x] 6.1 Delete `QueryContext.java`
- [x] 6.2 Confirm no remaining imports or references to `QueryContext` anywhere in the codebase

## 7. Replace DATA_QUERY_CONFIGURATION with per-property DataKeys

- [x] 7.1 In `DQLQueryConfigurationService`, declare individual `DataKey<T>` constants for each field: `DATA_TENANT`,
  `DATA_TIMEFRAME_START`, `DATA_TIMEFRAME_END`, `DATA_DEFAULT_SCAN_LIMIT`, `DATA_MAX_RESULT_BYTES`,
  `DATA_MAX_RESULT_RECORDS`, `DATA_ORIGINAL_FILE`, `DATA_RUN_CONFIG_NAME`
- [x] 7.2 Add `QueryConfiguration fromDataContext(DataContext context)` to `DQLQueryConfigurationService` (and implement
  in `DQLQueryConfigurationServiceImpl`)
- [x] 7.3 Remove `DATA_QUERY_CONFIGURATION: DataKey<QueryConfiguration>` from `DQLQueryConfigurationService`
- [x] 7.4 Update every `uiDataSnapshot()` / `DataProvider` implementation that currently sets
  `DATA_QUERY_CONFIGURATION` — replace with individual `dataSink.set(DATA_<FIELD>, value)` /
  `dataSink.lazy(DATA_<FIELD>, supplier)` calls
- [x] 7.5 Update every consumer that reads `DATA_QUERY_CONFIGURATION` — replace with either `fromDataContext()` or a
  direct read of the relevant per-property key

## 8. Verify compilation and behaviour

- [x] 8.1 Confirm project compiles with no errors
- [x] 8.2 Run a DQL query via the run configuration and verify it executes against the correct tenant
- [x] 8.3 Run a DQL query via the editor gutter action and verify it executes against the correct tenant
- [x] 8.4 Confirm the DQL toolbar renders without triggering file reads
- [x] 8.5 Open the "Navigate to source" action from an execution result and verify it navigates to the correct file
- [x] 8.6 Change only the tenant from the toolbar and verify other configuration fields are unaffected
