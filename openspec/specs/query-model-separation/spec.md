## Purpose

This capability defines the separation between query configuration (user-set values) and runtime query context (data
fetched at execution time). `QueryConfiguration` is the single model for query state, and all runtime data resolution
happens at execution time via services.

## Requirements

### Requirement: QueryConfiguration is the sole query model

`QueryConfiguration` SHALL be the only class representing query state. It SHALL contain only cheap, user-set
values: `tenant`, `defaultScanLimit`, `maxResultBytes`, `maxResultRecords`, `timeframeStart`, `timeframeEnd`,
`originalFile` (path reference), and `runConfigName`. No class named `QueryContext` SHALL exist in the codebase.

#### Scenario: QueryConfiguration is constructible without file access

- **WHEN** a `QueryConfiguration` is constructed from user-entered values
- **THEN** no file read or PSI analysis is required

#### Scenario: QueryConfiguration carries the run config name

- **WHEN** a run configuration builds a `QueryConfiguration`
- **THEN** `configuration.runConfigName()` returns the run configuration's name

---

### Requirement: DQLExecutionService fetches query data from services at execution time

`DQLExecutionService` SHALL obtain the query text by calling `DQLQuerySelectorService` and SHALL obtain variable
definitions by calling `DQLVariablesService`, using the `originalFile` from `QueryConfiguration` to locate the
source file. It SHALL NOT accept a `QueryContext` parameter.

#### Scenario: Execution service constructor accepts only QueryConfiguration

- **WHEN** `DQLExecutionService` is instantiated
- **THEN** its constructor signature is
  `(String name, QueryConfiguration configuration, @Nullable String selectedQuery, Project project, DQLProcessHandler handler)`

#### Scenario: Payload preparation resolves query text via service

- **WHEN** `DQLExecutionService.preparePayload()` runs
- **THEN** it calls `DQLQuerySelectorService` to obtain the query text; it does NOT read query text from a stored
  `QueryContext`

#### Scenario: Payload preparation resolves variables via service

- **WHEN** `DQLExecutionService.preparePayload()` runs
- **THEN** it calls `DQLVariablesService` to obtain defined variables; it does NOT read variables from a stored
  `QueryContext`

---

### Requirement: ExecuteDQLRunConfiguration produces QueryConfiguration only

`ExecuteDQLRunConfiguration.getConfiguration()` SHALL return `QueryConfiguration`. It SHALL NOT return `QueryContext`
or load query text from the file at this point.

#### Scenario: Run configuration returns QueryConfiguration

- **WHEN** `ExecuteDQLRunConfiguration.getConfiguration()` is called
- **THEN** the return type is `QueryConfiguration` and no file read occurs

---

### Requirement: UI and display components do not depend on QueryContext

Display components (`DQLExecutionResult`, `DQLExecutionErrorPanel`) SHALL accept query text as a plain `String`
parameter. They SHALL NOT depend on `QueryContext`.

#### Scenario: Error panel receives query text as String

- **WHEN** `DQLExecutionErrorPanel` is constructed
- **THEN** it accepts the query text as a `String`, not wrapped in a `QueryContext`

#### Scenario: Result panel receives query text as String

- **WHEN** `DQLExecutionResult` is constructed
- **THEN** it accepts the query text as a `String` (alongside project, result, and execution timestamp), not wrapped in
  a `QueryContext` or `QueryConfiguration`

---

### Requirement: Each QueryConfiguration field has an individual DataKey

`DQLQueryConfigurationService` SHALL declare one `DataKey<T>` per field of `QueryConfiguration`
(`DATA_TENANT`, `DATA_TIMEFRAME_START`, `DATA_TIMEFRAME_END`, `DATA_DEFAULT_SCAN_LIMIT`,
`DATA_MAX_RESULT_BYTES`, `DATA_MAX_RESULT_RECORDS`, `DATA_ORIGINAL_FILE`, `DATA_RUN_CONFIG_NAME`).
The existing `DATA_QUERY_CONFIGURATION: DataKey<QueryConfiguration>` SHALL be removed.

#### Scenario: Provider sets only tenant without touching other fields

- **WHEN** a UI component that owns only the tenant setting calls `dataSink.set(DATA_TENANT, value)`
- **THEN** no other configuration fields are affected

#### Scenario: Service assembles QueryConfiguration from individual keys

- **WHEN** `DQLQueryConfigurationService.fromDataContext(dataContext)` is called
- **THEN** it reads each per-property `DataKey` from the context and returns a fully populated `QueryConfiguration`

#### Scenario: Consumer reads a single field directly

- **WHEN** a component needs only the tenant name
- **THEN** it reads `DATA_TENANT` from the data context without needing to assemble a full `QueryConfiguration`
