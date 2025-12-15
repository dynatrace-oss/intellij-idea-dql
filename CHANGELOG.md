# Dynatrace Query Language support plugin

## [Unreleased]

### Features

- Reworked DQL engine to support a better JSON format with the language definition
- Added more inspections related to parameter value types (subqueries, fields identifier etc.)
- DPL will now be injected within all supported DQL function strings
- Added support for experimental DQL features (to be enabled in plugin's tool settings)
- Added search operator `~` code style settings

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

[Unreleased]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.1.0...HEAD

[1.1.0]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.0.4...v1.1.0

[1.0.4]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.0.3...v1.0.4

[1.0.3]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.0.2...v1.0.3

[1.0.2]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.0.1...v1.0.2

[1.0.1]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.0.0...v1.0.1

[1.0.0]: https://github.com/dynatrace-oss/intellij-idea-dql/commits/v1.0.0
