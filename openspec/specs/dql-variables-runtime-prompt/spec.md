# Spec: dql-variables-runtime-prompt

## Purpose

Defines how unresolved DQL `$variable` references are handled at query execution time via an interactive runtime prompt dialog, how supplied values are persisted for the editing session, and how those values are merged with file-based definitions and consumed silently by background processes such as autocomplete and external validation.

## Requirements

### Requirement: A plugin setting controls whether the runtime variable dialog is shown

The DQL settings page SHALL include a boolean setting **"Show modal for unresolved variables"** that defaults to `true`. When set to `false`, unresolved variables SHALL be silently substituted with `null` on execution (preserving the behavior that existed before this feature), and the dialog SHALL NOT be shown.

#### Scenario: Setting enabled (default) — dialog appears for unresolved variables on execution

- **WHEN** "Show modal for unresolved variables" is `true` and the user explicitly executes a query with at least one unresolved variable that has no stored runtime value
- **THEN** the runtime variable prompt dialog is shown

#### Scenario: Setting disabled — execution falls back to null substitution

- **WHEN** "Show modal for unresolved variables" is `false` and the user executes a query with unresolved variables
- **THEN** no dialog is shown and unresolved variables are substituted with `null` as before

### Requirement: The runtime variable dialog is shown only on explicit user-initiated execution

The runtime variable prompt dialog SHALL appear exclusively when the user explicitly triggers query execution (the Execute Query action). It SHALL NOT be shown for any background or automated process, including autocomplete and external validation.

#### Scenario: Fully resolved query executes without a dialog

- **WHEN** all `$variable` references in the DQL query have definitions in a governing `dql-variables.json`
- **THEN** the execution proceeds directly without showing a variable-input dialog

#### Scenario: Unresolved variable triggers the dialog on explicit execution

- **WHEN** the user explicitly executes a DQL query that contains at least one `$variable` with no definition in any governing `dql-variables.json` and no stored runtime value
- **THEN** the runtime variable prompt dialog is shown before execution continues

#### Scenario: Autocomplete does not trigger the dialog

- **WHEN** autocomplete is invoked for a DQL query containing unresolved variables
- **THEN** no dialog is shown; unresolved variables fall back to `null` as before (or use stored runtime values if available)

#### Scenario: External validation does not trigger the dialog

- **WHEN** external validation runs for a DQL file containing unresolved variables
- **THEN** no dialog is shown; unresolved variables fall back to `null` as before (or use stored runtime values if available)

#### Scenario: Execution without an open editor skips the dialog

- **WHEN** a DQL query is executed from a run configuration that has no associated open `PsiFile`
- **THEN** the dialog is skipped and execution proceeds using only file-based variable definitions (same behavior as today)

### Requirement: User is prompted to supply a type and value for each unresolved variable

The runtime variable prompt dialog SHALL present a row for each unresolved variable containing: the variable name (read-only), a type selector with options **string**, **number**, **boolean**, **null**, and **DQL fragment**, and a value text field.

#### Scenario: Dialog shows one row per unresolved variable

- **WHEN** the dialog is shown for a query with N unresolved variables
- **THEN** the dialog contains exactly N rows, each labelled with the corresponding variable name

#### Scenario: User provides values and confirms

- **WHEN** the user fills in type and value for all rows and presses OK
- **THEN** execution proceeds using the provided values substituted for the unresolved variables

#### Scenario: User cancels the dialog

- **WHEN** the user presses Cancel in the dialog
- **THEN** execution is aborted and the query is not sent to the API

### Requirement: Runtime values are persisted on the PsiFile for the session

Supplied runtime variable values SHALL be stored in `PsiFile` user data so that subsequent executions of the same file can reuse them without re-prompting.

#### Scenario: Re-execution pre-populates dialog with previous values

- **WHEN** the user executes the same DQL file a second time after having provided runtime values
- **THEN** the dialog is shown with the previously supplied type and value pre-populated for each variable

#### Scenario: Values are not persisted across IDE restarts

- **WHEN** the IDE is restarted
- **THEN** previously supplied runtime variable values are no longer available and the dialog starts blank

### Requirement: Stored runtime values are used silently by background processes

When autocomplete or external validation loads variable definitions, they SHALL merge any stored runtime values from PSI user data into the definition set without prompting. Variables that have no file-based definition and no stored runtime value SHALL continue to be substituted with `null` as before.

#### Scenario: Autocomplete uses stored runtime value

- **WHEN** autocomplete is invoked and a stored runtime value exists for an otherwise unresolved variable
- **THEN** the stored value is included in the variable set passed to the substituted query used for the autocomplete API call

#### Scenario: External validation uses stored runtime value

- **WHEN** external validation runs and a stored runtime value exists for an otherwise unresolved variable
- **THEN** the stored value is included in the variable set passed to the substituted query used for the validation API call

#### Scenario: No stored value falls back to null in background processes

- **WHEN** autocomplete or external validation encounters a variable with no file-based definition and no stored runtime value
- **THEN** the variable is substituted with `null` (unchanged from current behavior)

### Requirement: Runtime values are merged with file-based definitions at execution time

File-based variable definitions from `dql-variables.json` SHALL take precedence over runtime values. A variable that is resolved from a file SHALL NOT appear in the runtime prompt dialog, even if a runtime value for it exists in PSI user data.

#### Scenario: File-based variable is not overridden by a stored runtime value

- **WHEN** a variable is defined in `dql-variables.json` and a runtime value for the same name is stored on the PsiFile
- **THEN** the file-based value is used in the substituted query and the variable does not appear in the dialog

#### Scenario: Runtime value supplements file-based definitions

- **WHEN** a query has one variable resolved by `dql-variables.json` and one unresolved variable with a stored runtime value
- **THEN** the dialog is skipped (the only unresolved variable already has a stored runtime value) and both values are used in substitution
