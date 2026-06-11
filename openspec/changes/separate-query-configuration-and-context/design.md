## Context

`QueryContext` wraps a `QueryConfiguration` and adds query text, defined variables, and run config name. It acts as
a pre-fetched bundle: callers load file content and parse variables upfront, then hand the result around as an
opaque object.

The problem is that two services already own this data:
- `DQLQuerySelectorService` — retrieves the query text for a given `PsiFile`
- `DQLVariablesService` — parses and returns variable definitions from a `PsiFile`

`QueryContext` duplicates their responsibility, forces eager computation (including PSI analysis on the EDT via
`WriteCommandAction` in `preparePayload()`), and adds an indirection layer (`context.configuration().tenant()`)
that obscures what is really cheap (user settings in `QueryConfiguration`) vs. what requires work.

## Goals / Non-Goals

**Goals:**

- `QueryContext` is deleted; `QueryConfiguration` is the only query model
- `QueryConfiguration` holds only cheap, user-set values: tenant, limits, timeframe, source file path, run config name
- `DQLExecutionService` calls `DQLQuerySelectorService` and `DQLVariablesService` directly when building the
  execution payload — no pre-fetched bundle needed
- Eager query text and variable loading at configuration time is eliminated
- All APIs that previously accepted `QueryContext` accept `QueryConfiguration` instead
- Each field of `QueryConfiguration` is individually addressable in the IntelliJ data context via its own `DataKey`;
  `DQLQueryConfigurationService` assembles these keys into a `QueryConfiguration` when a full object is needed

**Non-Goals:**

- Changing the persistence format (`ExecuteDQLRunConfigurationOptions`) — XML option keys stay the same
- Adding new features or changing visible UI behaviour
- Making `QueryConfiguration` immutable — deferred

## Decisions

### D1 — Drop QueryContext entirely; use services at the execution boundary

**Decision:** `QueryContext` is deleted. `DQLExecutionService` calls `DQLQuerySelectorService` and
`DQLVariablesService` directly inside `preparePayload()` using `configuration.originalFile()` to locate the file.

**Rationale:** The services already exist and are the canonical source of this data. `QueryContext` was a transport
layer that added no logic of its own. Removing it eliminates the eager-loading pattern, simplifies the call graph,
and makes it obvious that file access happens at execution time, not at configuration time.

**Alternative considered:** Keep `QueryContext` but make it a lazy wrapper (compute fields on first access).
Rejected — deferred computation in a mutable POJO is hard to reason about and doesn't eliminate the EDT risk.
Services with explicit `ReadAction` calls are clearer.

---

### D2 — `runConfigName` moves to `QueryConfiguration`

**Decision:** `QueryConfiguration` gains a `runConfigName` field (previously on `QueryContext`).

**Rationale:** It is a cheap string that identifies which run configuration produced this configuration. It requires
no I/O and belongs with the other user-set metadata.

---

### D3 — `ExecuteDQLRunConfiguration.getConfiguration()` returns `QueryConfiguration`

**Decision:** The method returns `QueryConfiguration` only. No `QueryContext` is built here.

**Rationale:** `ExecuteDQLRunConfiguration` stores user settings. Building a `QueryContext` from it required
reading the DQL file synchronously. That load is now deferred to `preparePayload()` inside `DQLExecutionService`,
which already runs within a `WriteCommandAction`/`ReadAction` context.

---

### D4 — `DQLExecutionResult` and `DQLExecutionErrorPanel` receive query text as a plain String

**Decision:** Sites that need the query text for display (error panels, result panels) receive it as a `String`
parameter, resolved at the point `preparePayload()` runs, rather than from a `QueryContext`.

**Rationale:** These components need the resolved/substituted query string, which `preparePayload()` already
produces via `DQLQueryParserService`. Passing it as a `String` is direct and does not require the callers to hold
a `QueryContext`.

---

### D5 — `DQLExecutionService` constructor takes `QueryConfiguration` only

**Decision:** Constructor signature: `(String name, QueryConfiguration configuration, Project project,
DQLProcessHandler handler)`.

**Rationale:** Mirrors D1 — configuration is what the service needs to know at construction time. The query text
and variables are fetched during `startExecution()`.

---

### D6 — Per-property `DataKey`s replace the single `DATA_QUERY_CONFIGURATION` key

**Decision:** `DQLQueryConfigurationService` declares one `DataKey<T>` per `QueryConfiguration` field (e.g.
`DATA_TENANT: DataKey<String>`, `DATA_TIMEFRAME_START: DataKey<String>`,
`DATA_DEFAULT_SCAN_LIMIT: DataKey<Long>`, …). The existing `DATA_QUERY_CONFIGURATION: DataKey<QueryConfiguration>`
is removed. `DQLQueryConfigurationService` gains a method
`QueryConfiguration fromDataContext(DataContext)` that reads all individual keys and assembles the object.

**Rationale:** The single-object key forces providers to supply a complete `QueryConfiguration` even when only one
field changes (e.g. a toolbar action that only controls the tenant). Per-property keys let each provider set
exactly what it owns. Consumers that need a full config call `fromDataContext()` once; consumers that need a single
field read their one key directly. Partial updates (changing just the timeframe from the toolbar, or hiding the
tenant selector in the execution result panel) become trivial — no object copy required.

**Key naming:** Prefer `DATA_<FIELD_NAME>` convention, matching the existing style (`DATA_QUERY_CONFIGURATION`).
Example set: `DATA_TENANT`, `DATA_TIMEFRAME_START`, `DATA_TIMEFRAME_END`, `DATA_DEFAULT_SCAN_LIMIT`,
`DATA_MAX_RESULT_BYTES`, `DATA_MAX_RESULT_RECORDS`, `DATA_ORIGINAL_FILE`, `DATA_RUN_CONFIG_NAME`.

**Visibility flags** (currently `SHOW_TIMEFRAME`, `SHOW_TENANT_SELECTION`, etc. on `QueryConfigurationAction`) are
not part of `QueryConfiguration` and are unaffected by this decision.

**Alternative considered:** Keep `DATA_QUERY_CONFIGURATION` and add per-property keys alongside it. Rejected —
two parallel representations of the same state creates sync problems. A single canonical representation (individual
keys, assembled on demand) is cleaner.

## Risks / Trade-offs

- **Risk:** `preparePayload()` currently uses `WriteCommandAction.runWriteCommandAction()` to parse the query — this
  is a write action, which is heavier than necessary and runs on the EDT. Dropping `QueryContext` does not fix this,
  but it makes the boundary clearer. Consider switching to `ReadAction` as a follow-up.  
  → **Acceptable:** out of scope for this change, but worth noting.

- **Risk:** If any current consumer of `QueryContext` uses fields beyond `query` and `configuration` (e.g.,
  `definedVariables` for display in `DQLExecutionResult`), those sites need explicit handling after `QueryContext`
  is deleted.  
  → **Mitigation:** Audit all `QueryContext` field accesses before deleting the class.

## Open Questions

- Does `DQLExecutionResult` actually display `definedVariables`? If so, it needs a `List<VariableDefinition>`
  parameter sourced from `DQLVariablesService` at execution time. Needs audit.
