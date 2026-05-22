# intellij-idea-dql

<!-- Plugin description -->
The plugin adds support for
[Dynatrace Query Language (DQL)](https://docs.dynatrace.com/docs/discover-dynatrace/references/dynatrace-query-language)
and
[Dynatrace Pattern Language (DPL)](https://docs.dynatrace.com/docs/discover-dynatrace/platform/grail/dynatrace-pattern-language)
in IntelliJ IDEA.

> **Note**
> This product is a community-driven open-source plugin, helping users write and execute DQL & DPL statements within
> JetBrains IDEs.
> It's not officially supported by Dynatrace, please report any errors directly
> via [GitHub Issues](https://github.com/dynatrace-oss/intellij-idea-dql/issues).

## Features

The list of features provided by this extension can be divided into two categories: **fully local features** that do not
require any external connection, and **remote features** that require an authenticated connection to a Dynatrace tenant.

Local features are always enabled, and you can optionally configure a connection to a Dynatrace tenant to improve the
integration.

You can find more insights on the features in the [Wiki](https://github.com/dynatrace-oss/intellij-idea-dql/wiki).

### Local features

Local features are covered completely by the plugin and do not require any kind of connection to the Dynatrace tenant.
This also means that not all functionalities provided by Dynatrace services and languages are supported. For example,
without the connection the extension is not able to provide any information about the data stored in the
tenant, so no validations and autocompletion for field identifiers can be performed.

The list of local features includes:

- Highly customizable syntax highlighting and code style formatting
- Detecting references between DQL fields, functions, statements, and parameters
- Code completion for commands, fields, functions, and parameters
- Hover documentation for commands, fields, functions, and parameters
- Contextual issues detection, with quick fixes where possible
- Partial DQL support for files containing only parts of the query (both smaller fragments & just expressions)
- Support for DQL variables defined in Dynatrace Dashboards
- Nested languages support (DPL inside DQL commands & functions, JSON etc.)

### Remote features

After connecting the plugin to a Dynatrace tenant, you can benefit from a wide range of features. You can authorize
into multiple tenants and easily switch between them, so you can work with different environments without leaving the
IDE.

Supported authentication methods include simple browser OAuth flow and API tokens.

The list of remote features includes:

- DQL execution on a specific tenant, with results presented as a table, JSON response, or simple visualization
- Live validations, reporting any kind of errors Dynatrace Notebooks would show
- Live autocompletion for field identifiers and data objects supported by the tenant
- Run configurations allowing to save parameters for DQL execution and run them with a single click
- DQL query console, allowing to execute DQL statements without the need to create a file for them
- DQL execution toolbar & gutter icons, allowing to easily provide parameters and execute smaller parts of the query

## More information

You can find detailed documentation for all features inside the
plugin's [Wiki pages](https://github.com/dynatrace-oss/intellij-idea-dql/wiki).

<!-- Plugin description end -->

## Building

This plugin uses the [GrammarKit](https://github.com/JetBrains/Grammar-Kit/tree/master) plugin to generate all
necessary PSI resources.
You'll need to generate them for both `.flex` and `.bnf` files from the [grammar](src/main/grammar)package.
