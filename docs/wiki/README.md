# ![Dynatrace logo](../../src/main/resources/icons/dynatrace.png) Dynatrace DSL for JetBrains IDE

This plugin adds support for Dynatrace domain-specific languages:
[Dynatrace Query Language (DQL)](https://docs.dynatrace.com/docs/discover-dynatrace/references/dynatrace-query-language)
and
[Dynatrace Pattern Language (DPL)](https://docs.dynatrace.com/docs/discover-dynatrace/platform/grail/dynatrace-pattern-language).

> ℹ️ **Note**:
> This product is a community-driven open-source plugin, helping users write and execute DQL statements within JetBrains
> IDEs.
> It's not officially supported by Dynatrace, please report any errors directly
> via [GitHub Issues](https://github.com/dynatrace-oss/intellij-idea-dql/issues).

## Supported IDEs

The plugin works with most IDEs in JetBrains stack (you can find the exact list of compatible IDEs in the
plugins [marketplace page](https://plugins.jetbrains.com/plugin/28135-dynatrace-query-language/versions?noRedirect=true)).

It does not work with other IDEs like Visual Studio Code, as it heavily uses JetBrains SDK features.

## Getting started

You can find the information about installation and plugin configuration inside the
dedicated [installation guide](installation.md).

## Usage

Because the supported features differ between supported languages, you can find the usage details in dedicated
subpages:

- [Dynatrace Query Language (DQL)](dql.md)
- [Dynatrace Pattern Language (DPL)](dpl.md)
- [Expressions DQL (EDQL)](edql.md) - smaller subset of DQL files, allowing to specify only expressions without the DQL
  command context

## Changelog

You can see the list of changes in the
dedicated [changelog](https://github.com/dynatrace-oss/intellij-idea-dql/blob/main/CHANGELOG.md) page.
