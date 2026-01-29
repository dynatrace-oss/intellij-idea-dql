# Dynatrace Pattern Language

The plugin adds support of
the [Dynatrace Pattern Language (DPL)](https://docs.dynatrace.com/docs/discover-dynatrace/platform/grail/dynatrace-pattern-language).
inside the `.dpl` files.

## Features

### Code style settings

The plugin adds a support for customizing the DPL code style. You can find the settings under a dedicated page in
`Settings` > `Editor` > `Code Style` > `Dynatrace Pattern Language`.

### Custom color scheme

The color scheme used for DPL files can be customized in `Settings` > `Editor` > `Color Scheme` >
`Dynatrace Pattern Language`.

### Code completion

Depending on the context of the DPL pattern, the plugin will suggest relevant options for code completion.
It supports commands and configuration parameters.

### Inspections

The plugin adds a lot of fully local inspections, verifying the DPL syntax and context without the need to connect to
a Dynatrace tenant. You can configure which issues should be reported by the plugin inside the `Settings` > `Editor` >
`Inspections` > `Dynatrace Pattern Language` view.

### Intentions

The list of supported intentions is available in the `Settings` > `Editor` >
`Intentions` > `Dynatrace Pattern Language` view.

### On-hover documentation

You can hover over almost any pattern parts to see more information about the element.

The plugin also implements structure with navbar for IntelliJ, so it's straightforward to track the context of the
pattern.

![An example documentation popup](images/dpl-support.png)

Additional to showing just the information loaded from Dynatrace documentation, the plugin also calculates the
expression information, like the maximum allowed occurrences, the exported field name and whether the field is optional
or required inside the pattern.