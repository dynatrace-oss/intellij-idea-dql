## ADDED Requirements

### Requirement: A variable definition is a Find Usages target

A property in a `dql-variables.json` file that defines a variable SHALL be a valid Find Usages target. Invoking
Find Usages on the property SHALL be supported and SHALL produce results describing the variable's DQL usages.

#### Scenario: Find Usages is available on a definition property

- **WHEN** the caret is on a variable property in `dql-variables.json` and Find Usages is invoked
- **THEN** the action runs and reports usages rather than "no usages provider" / an empty unsupported result

### Requirement: Find Usages lists all DQL queries that reference the variable

Find Usages on a variable definition SHALL return every `DQLVariableExpression` whose name matches the property
name, within the directory scope governed by that `dql-variables.json` (the queries for which this file is the
closest definition source). Queries governed by a different, closer `dql-variables.json` SHALL NOT be reported.

#### Scenario: All matching usages in scope are found

- **WHEN** Find Usages is invoked on a variable defined in `dql-variables.json`
- **THEN** every DQL variable reference with the same name in that file's governed scope appears in the results

#### Scenario: Usages governed by a closer file are excluded

- **WHEN** a nested directory has its own `dql-variables.json` defining the same variable name
- **THEN** usages in that nested scope are attributed to the closer file and are NOT reported for the outer file

#### Scenario: A variable with no usages reports none

- **WHEN** Find Usages is invoked on a defined variable that no DQL query references
- **THEN** the result set is empty and no error occurs

### Requirement: Usages are labelled as DQL variables

Usage results for a variable definition SHALL be categorised using the existing DQL variable usage type so they
appear grouped consistently with other DQL variable usages.

#### Scenario: Results use the DQL variable usage type

- **WHEN** Find Usages results for a variable definition are displayed
- **THEN** the usages are grouped under the DQL variable usage category
