# Dynatrace Query Language support plugin

## [Unreleased]

### Features

- DQL now supports HEX representations (for example `0x1A3F`) for numbers
- Autocompleting aliases for commands in DPL (for example, both short notation `INT` and the long one `INTEGER` will be
  shown during the autocompletion). Added the missing alias `BOOL` for the `BOOLEAN` command.
- DPL now supports line (`//`) and block (`/* */`) comments.
- DPL now supports SI number (scientific notation, for example (`1e23`)) and HEX representations (for example,
  `0x1A3F`).
- DPL now supports "meta" expressions that allow converting the found value into a different type, for example:
  `(('t' <true>:is_found) | ('f' <false>:is_found))` will convert `t` into `true` and `f` into `false`. Added code
  style & highlighting settings for such expressions.
- When a part of DQL query is selected when executed, the selected fragment will be shown as the default option

## [1.3.0] - 2026-01-11

### Features

- Major rework for the DQL execution
  - **Executing DQLs no longer requires the user to create a Run Configuration** - each file will have its own context
    stored in memory. The user can still create a run configuration to save settings inside IntelliJ project. If they
    do, the initial configuration for the file will be loaded from the correlated run configuration.
  - Adding a DQL toolbar at the top of each DQL file (similar to `.sql` files). The toolbar allows the user to set up
    the Dynatrace context used for the file (to run, verify & autocomplete the query). For injected DQL fragments, the
    gutter icon will be shown, allowing the user to execute the query. Both are optional and can be enabled/disabled
    in the plugin's settings.
  - The user can now show the executed DQL fragment and switch between the JSON and table mode to show the execution
    result.
  - Allowing the user to execute a smaller DQL fragment via text selection and nested DQL queries. In such cases
    a popup menu will be shown to the user allowing to select which part of the DQL file should be executed.
- Autocompleting parameter values in all suitable places (for example, in nested expressions of different types)
- Added autocompleting string quotes in suitable places
- Adding support for scientific notation numbers in DQL (for example, `1.3e20`)
- Adding support for calculating the expressions data types for supported operators (if possible, for example: `long` +
  `long`)
- Added support for fields named via the `alias` parameter. Added an intention to replace `alias` with
  assigned expression (`=`)
- Replacing manual inspections with automatically generated based on the DQL JSON definition
- Added support for known Dynatrace Dashboards variables (`$variable`) replacement for live Notebooks autocompletion
  and inspections
- Adding support for replacing DQL variables with DQL query fragments (for example: `now() - 4h`). To limit the risk of
  code injections, the fragment must be explicitly set up using the object with the `$type: dql` property:

  ```json lines
  {
    "variableName": {
      "$type": "dql",
      "dql": "now() - 4h"
    }
  }
  ```

  If the `dql` property is not a string, the whole object will be parsed as DQL object.
- When the plugin encounters any issues during the DQL query verification or autocompletion (connecting to the Dynatrace
  tenant), an error balloon notification will be shown to the user containing the error details. Previously it was just
  silently ignored by printing the error in IntelliJ logs.

## [1.2.0] - 2025-12-17

### Features

- Reworked DQL engine to support a better JSON format with the language definition
- Added more inspections related to:
  - nested negations in conditions (`not (not true)` could be simplified to just `true`)
  - negated filtering conditions (`filter not true` could be replaced with `filterOut true`)
  - parameter value types (subqueries, fields identifier etc.)
  - disjointed variadic parameters definitions
  - migration from `fetch metric.series` into `metrics` command
- DPL will now be injected within all supported DQL function strings
- Added support for experimental DQL features (to be enabled in plugin's tool settings)
- Added search operator `~` code style settings
- Added intentions to DQL:
  - Splitting and joining consecutive `filter` and `filterOut` commands

### Bug fixes

- Field accessor (for example, `field[0]`) will now be properly indented

## [1.1.0] - 2025-12-03

### Features

- Added support for DQL comments style settings
- Added support for Smartscape functions to DQL
- Added support for other JetBrains tools (like WebStorm)
- Added support for a `bucket` parameter inside DQL `fetch` and `timeseries` commands
- Added support for Dynatrace Pattern Language files (`.dpl`)
  - The support is also automatically added to the DQL `parse` command
  - Customizable syntax highlighting
  - Local code inspections with quick fixes if possible
  - Customizable code style support — enforcing indents, line breaking and spaces around elements
  - Advanced code completion
  - Hover documentation
  - Useful intentions for managing DPL expressions

## [1.0.4] - 2025-10-28

### Bug fixes

- #11: Fixed the issue with an invalid expressions precedences

### Other

- #12: Adding support for the `load` command
- Added support for the `<>` operator which is an alternative to `!=`

## [1.0.3] - 2025-09-04

### Bug fixes

- #7: Fixing the
  `ClassCastException: PsiPlainTextFileImpl cannot be cast to class pl.thedeem.intellij.dql.DQLFile`
  error that occurred when injecting DQL into other types of files like Markdown (DQL blocks) or Java
  (`/* language=DQL */` injections).

### Other

- Improving error handling for Dynatrace REST API calls. The user will now see more details for errors like an invalid
  url or an unknown HTML response. Errors will now also be logged, which will be helpful when supporting users

## [1.0.2] - 2025-08-12

### Bug fixes

- #3: Fixing the
  `Resolving the Legacy configurable id calculation mode from localizable name will be used for configurable class`
  error that started appearing in IDEA 2025.2

### Other

- Rebranded the plugin from "unofficial" to "community-driven open-source" Dynatrace plugin

## [1.0.1] - 2025-08-11

### Bug fixes

- Fixing the
  `Cannot invoke "java.util.Set.iterator()" because the return value of "pl.thedeem.intellij.dql.definition.DQLOperationTarget.getOperatorType()" is null`
  error

## [1.0.0] - 2025-08-11

### Added

- Initial version of the plugin
- Adding support for Dynatrace Query Language files (`.dql`):
  - Customizable syntax highlighting
  - Local code inspections with quick fixes if possible
  - Customizable code style support — enforcing indents, line breaking and spaces around elements
  - Advanced code completion
  - Hover documentation for commands, parameters, and functions
- Added an option to connect to a Dynatrace tenant, which allows to:
  - perform live validations on all DQL files (meaning detecting the same issues as in Dynatrace Notebooks)
  - execute DQL queries and show results as a table directly in IntelliJ IDEA
  - autocomplete DQL fields
- Partial files support `NAME.partial.dql`:
  - disables live validations and inspections related to query completeness (like missing the data source command or
    starting with the `|` symbol)
  - very helpful for projects containing a lot of dynamically joined DQL queries
- Variables support for DQL queries:
  - special support for DQL queries stored for Dynatrace Dashboards which allows the user to define global variables
    that are automatically replaced within queries
  - the plugin adds support for `dql-variables.json` file that contains the static placeholder value that will replace
    the variable
  - the file is loaded in a hierarchical order: from the same directory, then to the parent one, etc.
  - the file supports specifying `record` (JSON object), `array` (JSON array), `string`, `boolean`, `number`, and
    `null` types.

[Unreleased]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.3.0...HEAD

[1.3.0]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.2.0...v1.3.0

[1.2.0]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.1.0...v1.2.0

[1.1.0]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.0.4...v1.1.0

[1.0.4]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.0.3...v1.0.4

[1.0.3]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.0.2...v1.0.3

[1.0.2]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.0.1...v1.0.2

[1.0.1]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.0.0...v1.0.1

[1.0.0]: https://github.com/dynatrace-oss/intellij-idea-dql/commits/v1.0.0
