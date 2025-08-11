# Dynatrace Query Language support plugin

## [Unreleased]

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

[Unreleased]: https://github.com/dynatrace-oss/intellij-idea-dql/compare/v1.0.0...HEAD

[1.0.0]: https://github.com/dynatrace-oss/intellij-idea-dql/commits/v1.0.0
