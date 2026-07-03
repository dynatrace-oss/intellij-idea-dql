## 1. Service additions

- [x] 1.1 Add `Key<List<VariableDefinition>> RUNTIME_VALUES_KEY = Key.create("DQL_RUNTIME_VARIABLE_VALUES")` as a constant on `DQLVariablesService` (following the pattern of `DQLSettings.EXTERNAL_VALIDATION_ENABLED`).
- [x] 1.2 Add `Set<String> getUndefinedVariables(PsiFile dqlFile)` to `DQLVariablesService`; implement in `DQLVariablesServiceImpl` using `PsiTreeUtil.findChildrenOfType` to collect all `DQLVariableExpression` names and returning those whose `getValue()` is `null`.
- [x] 1.3 Update `DQLVariablesServiceImpl.getDefinedVariables()` to read `RUNTIME_VALUES_KEY` from the PsiFile and append stored `VariableDefinition` entries whose name is not already covered by a file-based definition (file wins).

## 3. Plugin setting

- [x] 3.1 Add `@OptionTag public boolean showModalForUnresolvedVariables = true;` to `DQLSettingsState` and update its `equals`/`hashCode`.
- [x] 3.2 Add `isShowModalForUnresolvedVariables()` / `setShowModalForUnresolvedVariables(boolean)` accessors to `DQLSettings`.
- [x] 3.3 Add a "Show modal for unresolved variables" checkbox to `DQLSettingsComponent` and wire it in `DQLSettingsConfigurable` (`isModified`, `apply`, `reset`).
- [x] 3.4 Add the corresponding bundle key to `DQLBundle.properties`.

## 4. Runtime variable prompt dialog

- [x] 4.1 Create `DQLRuntimeVariableDialog extends DialogWrapper` in `pl.thedeem.intellij.dql.exec` with a `JBTable` showing columns: Variable (read-only), Type (combobox with string/number/boolean/null/DQL-fragment options), Value (`JTextField`).
- [x] 4.2 Accept a `List<VariableDefinition>` (previously stored values) in the constructor and pre-populate rows from it (match by name).
- [x] 4.3 Expose `getResult(): List<VariableDefinition>` that converts each row's type + raw value into the appropriate substitution string and returns the resulting `VariableDefinition` list on OK; return `null` on Cancel.

## 5. Execution integration

- [x] 5.1 In `DQLExecutionService.preparePayload()`, if `DQLSettings.isShowModalForUnresolvedVariables()` and the PsiFile is available: call `getUndefinedVariables(psiFile)`, subtract names already covered by stored `VariableDefinition` values from the key; if any remain, show `DQLRuntimeVariableDialog`; abort if cancelled (return `null`); persist results via `psiFile.putUserData(DQLVariablesService.RUNTIME_VALUES_KEY, result)`.
- [x] 5.2 Confirm that `loadVariables()` in `DQLExecutionService` routes through `getDefinedVariables()`; if it does not, adjust it so that the merge added in task 1.3 takes effect for execution as well.
- [x] 5.3 Handle the case where the PsiFile is unavailable (run configuration without open editor): skip detection and dialog, proceed with file-based variables only.

## 6. Tests & verification

- [x] 6.1 Unit-test key storage: `putUserData` / `getUserData` round-trip with `RUNTIME_VALUES_KEY` on a mock PsiFile.
- [x] 6.3 Integration-test `getUndefinedVariables`: a DQL file with an unresolved `$variable` returns that name; a fully-resolved file returns an empty set.
- [x] 6.4 Integration-test the merge: runtime value for an unresolved variable is included in the final `VariableDefinition` list; a file-based variable is not overridden by a stored runtime value.
- [x] 6.5 Integration-test setting disabled: execution proceeds without a dialog and unresolved variables become `null` in the substituted query.
- [x] 6.6 Run the full test suite (`./gradlew test`) and confirm all new and existing tests pass.
