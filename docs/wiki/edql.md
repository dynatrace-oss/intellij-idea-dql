# Expressions of Dynatrace Query Language

In some cases you might want to write only the expression part of the Dynatrace Query Language (DQL) statements,
without the full command context - for example providing matchers for Dynatrace Monaco or OpenPipeline.

As a solution for this problem, the plugin adds support for `.edql` files which allow writing only the DQL expressions
without any commands.

Such files naturally cannot be executed and do not fire any additional verification when a connection to the Dynatrace
tenant is configured.

## Features

The `.edql` files inherit features from the [Dynatrace Query Language (`.dql`)](dql.md) files. Inspections related to
the expression context (like assignment support) are disabled for such files.

## Example `.edql` file

```edql
matchesPhrase(event.provider, \"my-provider-*\") OR matchesPhrase(event.provider, \"*.my.host\")
```
