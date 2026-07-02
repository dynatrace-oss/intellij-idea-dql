## ADDED Requirements

### Requirement: dql-variables.json is recognised as a plugin-owned file

The plugin SHALL recognise any file named `dql-variables.json` as a DQL variables definition file and SHALL
associate it with a bundled JSON schema that describes valid variable definitions. Recognition SHALL be by file
name and SHALL NOT require the file to be opened from a DQL query first.

#### Scenario: Schema is applied to a dql-variables.json file

- **WHEN** a file named `dql-variables.json` is opened anywhere in the project
- **THEN** the plugin's bundled DQL variables JSON schema is applied to it, providing completion and validation

#### Scenario: Unrelated JSON files are not affected

- **WHEN** a JSON file with any other name is opened
- **THEN** the DQL variables JSON schema is NOT applied to it

### Requirement: Top level must be an object of variable definitions

The schema SHALL require the top-level JSON value to be an object. Each property key is a variable name and each
property value is a variable definition. A non-object top-level value SHALL be reported as a schema violation.

#### Scenario: Object top level is valid

- **WHEN** a `dql-variables.json` contains a JSON object whose properties are valid variable definitions
- **THEN** no schema violation is reported for the top-level structure

#### Scenario: Non-object top level is invalid

- **WHEN** a `dql-variables.json` contains a top-level array, string, number, boolean, or null
- **THEN** a schema violation is reported on the top-level value

### Requirement: Variable values conform to supported definition types

A variable definition value SHALL be one of: a string, a number, a boolean, a null, an array, a plain object
(record), or a DQL fragment object. A DQL fragment object SHALL be an object with a `$type` property equal to the
string `"dql"` and a `dql` property whose value is a string. Values outside these forms SHALL be reported as
schema violations. Array elements and record properties SHALL themselves conform to the supported value types.

#### Scenario: Scalar, array, and record values are valid

- **WHEN** a variable is defined as a string, number, boolean, null, array of supported values, or a record object
- **THEN** no schema violation is reported for that variable's value

#### Scenario: DQL fragment object is valid

- **WHEN** a variable is defined as `{"$type": "dql", "dql": "fetch logs"}`
- **THEN** no schema violation is reported for that variable's value

#### Scenario: DQL fragment missing the dql string is invalid

- **WHEN** a variable is defined as `{"$type": "dql"}` or with a non-string `dql` value
- **THEN** a schema violation is reported indicating the `dql` string property is required

### Requirement: Schema violations surface in the editor

Schema violations in a `dql-variables.json` file SHALL be reported as editor annotations/inspections at the
location of the offending value, using the standard JSON schema validation mechanism, so that authors see errors
before a DQL query is executed.

#### Scenario: Violation is highlighted in place

- **WHEN** a `dql-variables.json` contains a value that violates the schema
- **THEN** the offending element is highlighted in the editor with a descriptive message
