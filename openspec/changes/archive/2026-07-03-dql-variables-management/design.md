## Context

Three code paths load variable definitions and call `DQLQueryParserService.getSubstitutedQuery()`:

1. **Execution** (`DQLExecutionService.preparePayload()`): calls `loadVariables()` which resolves `dql-variables.json`
   for the original file path and returns `List<VariableDefinition>`.
2. **External validation** (`DQLVerificationAnnotator.doAnnotate()`): calls
   `DQLVariablesService.getDefinedVariables(psiFile)` directly.
3. **Autocomplete** (`DQLDynatraceAutocomplete`): calls `DQLVariablesService.getDefinedVariables(originalFile)`
   directly.

In all three paths, `getSubstitutedQuery()` substitutes any variable not present in the definitions map with the literal
string `"null"` (`DQLQueryParserServiceImpl` lines 34-36: `if (value == null) { value = "null"; }`). Users without a
`dql-variables.json` file therefore get silent `null` substitutions across execution, validation, and autocomplete.

There is no mechanism to supply values interactively for any of these paths.

PSI-level user data (`PsiFile.putUserData` / `getUserData` with `Key<T>`) is already the established pattern in this
plugin for per-file session state (see `ToggleExternalValidationAction`, `QueryElementImpl` variable caching).

## Goals / Non-Goals

**Goals:**

- Expose `DQLVariablesService.getUndefinedVariables(PsiFile)` to detect variables with no file-based definition.
- Detect unresolved `$variables` at explicit execution time and prompt the user to supply values via a modal dialog (when the setting is on).
- Persist supplied values on the `PsiFile` user data so re-executions reuse them without re-prompting.
- Merge runtime values with file-based values (file always wins; runtime only fills gaps) — for all three call sites.
- Autocomplete and external validation silently use stored runtime values without ever showing a dialog; variables with no stored value continue to substitute as `null`.
- Provide a global plugin setting "Show modal for unresolved variables" (default on) to opt out of the prompt entirely.

**Non-Goals:**

- Writing runtime values back to `dql-variables.json` on disk.
- Deep value validation (e.g., checking a DQL-fragment value is syntactically valid DQL).
- Persisting values across IDE restarts (session-only).
- Replacing or modifying the existing `dql-variables.json` workflow.

## Decisions

### 1. Where to detect unresolved variables

**Decision**: Add `Set<String> getUndefinedVariables(PsiFile dqlFile)` to `DQLVariablesService` (implemented in `DQLVariablesServiceImpl`). The implementation uses `PsiTreeUtil` to collect all `DQLVariableExpression` names in the DQL file and returns those whose `getValue()` is `null` (meaning `getDefinition()` returns `null` — no `dql-variables.json` covers them). `DQLExecutionService.preparePayload()` calls this method to decide whether the dialog is needed.

**Why in the service, not in `DQLExecutionService`**: Variable resolution already lives in `DQLVariablesService`; knowing which variables are unresolved is a natural extension of that responsibility, not an execution concern. It is also independently testable and reusable.

**Why not diff query text against the definitions list**: String-scanning requires re-parsing logic already present in the PSI tree and is error-prone for edge cases (escaped `$`, variable names in strings).

**Why not run after `getSubstitutedQuery()`**: By that point unresolved variables have been silently replaced with
`"null"`; the original names are no longer present in the output text, so detection would require a separate parse pass.

### 2. Runtime value storage

**Decision**: The `Key<List<VariableDefinition>>` constant for PsiFile user data lives directly on `DQLVariablesService` — following the same pattern as `DQLSettings.EXTERNAL_VALIDATION_ENABLED`. The stored type is the existing `VariableDefinition` record, which already holds the substitution-ready `value` string. No new model types are introduced.

The dialog converts user input (type selector + raw text) into a `VariableDefinition` value string at submission time, using the same formatting rules as `getVariableValue()` (string → quoted, number → raw, boolean → true/false, null → null, DQL fragment → raw). The type selector is an internal enum of the dialog class and is not stored.

**Why PsiFile**: Consistent with how external-validation toggle and other per-file states are stored. Survives reloads of the editor panel but not IDE restarts — acceptable for session state.

### 3. Where the merge happens

**Decision**: `getDefinedVariables(PsiFile)` in `DQLVariablesServiceImpl` reads the stored `List<VariableDefinition>` from `RUNTIME_VALUES_KEY` on the DQL PsiFile and appends entries whose name is not already covered by a file-based definition (file wins). This makes autocomplete and external validation completely transparent — they call `getDefinedVariables()` as today and automatically receive the merged result. No changes to those call sites are required.

### 4. Dialog design

**Decision**: Standard IntelliJ `DialogWrapper` containing a `JBTable` with three columns: **Variable** (read-only label), **Type** (`ComboBox<VariableType>`), **Value** (text field).

One row per unresolved variable. Rows are pre-populated from stored PSI user data if values were supplied in a previous execution.

OK action stores results via the `Key` on the PsiFile and proceeds. Cancel aborts execution without modifying stored data.

### 5. How runtime values reach the three call sites

All three call sites invoke `getDefinedVariables()`, which now transparently includes stored runtime values. `DQLVerificationAnnotator` and `DQLDynatraceAutocomplete` require no code changes — they pick up stored values automatically.

`DQLExecutionService.preparePayload()` adds one pre-step: it calls `getUndefinedVariables()`, subtracts names already covered by stored runtime values, and if any remain shows the dialog to collect them before calling `loadVariables()` / `getDefinedVariables()` as normal.

### 6. Plugin setting for opt-out

**Decision**: Add `showModalForUnresolvedVariables` boolean (default `true`) to `DQLSettingsState` with the standard accessor pair on `DQLSettings` (`isShowModalForUnresolvedVariables()` / `setShowModalForUnresolvedVariables()`). A corresponding checkbox "Show modal for unresolved variables" is added to `DQLSettingsComponent` and wired in `DQLSettingsConfigurable`. The check is performed in `DQLExecutionService.preparePayload()` before calling `getUndefinedVariables()` — if the setting is off the entire prompt flow is skipped.

**Why a global setting and not per-file**: Consistency with all other DQL settings (`performLiveValidation`, `useDynatraceAutocomplete`, etc.), which are all application-level. The per-file stored runtime values already give file-level granularity for the session.

## Risks / Trade-offs

- **DQL fragment values are not validated** → Mitigation: The existing execution path already tolerates malformed
  substitutions; the user will see an API error, which is acceptable.
- **PSI file identity**: `PsiFile` objects can be recreated on VFS events, losing stored data → Mitigation: This is
  consistent with all other PSI user data in the plugin; no special handling needed.
- **Null PsiFile at execution time**: When the execution is launched from a run configuration without an open editor,
  `preparePayload()` already handles null PsiFile gracefully — in that case the prompt is skipped and execution proceeds
  with file-based variables only (same behaviour as today).
