## Why

DQL queries can reference `$variables` but currently the only way to supply values is by committing a `dql-variables.json` file to disk. Users who want to run a query with ad-hoc values — or who are sharing a query that has no committed variable file — have no way to provide values without first creating that file, which is unnecessary friction during exploration and development.

## What Changes

- `DQLVariablesService` gains a new `getUndefinedVariables(PsiFile)` method that returns the names of `$variables` referenced in a DQL file that have no definition in any governing `dql-variables.json`.
- A new plugin setting **"Show modal for unresolved variables"** (default: on) is added to the DQL settings page. When off, behavior reverts to the previous silent `null` substitution.
- When the setting is on and a DQL query is explicitly executed with one or more unresolved variables, a modal dialog is shown before execution proceeds.
- The dialog presents each unresolved variable in a table: variable name (read-only), a type combobox (string, number, boolean, null, DQL fragment), and a value text field.
- On confirmation the supplied values are stored on the DQL `PsiFile` via a `UserData` key so subsequent executions in the same IDE session reuse and pre-populate them.
- On cancel execution is aborted.
- Runtime values overlay `dql-variables.json` values: if a variable is defined in the JSON file it does not appear in the dialog; only genuinely unresolved variables are prompted for.

## Capabilities

### New Capabilities

- `dql-variables-runtime-prompt`: Detects unresolved variables at execution time, prompts the user for values via a modal dialog, persists the values in PSI user data for the session, and merges them into the variable set used for query substitution.

### Modified Capabilities

*(none — existing variable resolution and schema specs are unchanged)*

## Impact

- `DQLVariablesService` — new `getUndefinedVariables(PsiFile)` method and `Key<List<VariableDefinition>> RUNTIME_VALUES_KEY` constant; no new model types.
- `DQLVariablesServiceImpl` — implements `getUndefinedVariables`; `getDefinedVariables()` now also appends stored runtime values from `RUNTIME_VALUES_KEY`.
- `DQLExecutionService` — `preparePayload()` uses `getUndefinedVariables()` to decide whether to show the dialog before calling `loadVariables()` as normal.
- `DQLVerificationAnnotator`, `DQLDynatraceAutocomplete` — no changes; stored runtime values are picked up automatically via `getDefinedVariables()`.
- `DQLSettingsState` / `DQLSettings` / `DQLSettingsComponent` / `DQLSettingsConfigurable` — new `showModalForUnresolvedVariables` boolean (default `true`).
- New `DQLRuntimeVariableDialog` (DialogWrapper) for the variable value input UI.
- No changes to DQL grammar, `dql-variables.json` schema, or existing inspection/completion logic.
- No breaking changes.
