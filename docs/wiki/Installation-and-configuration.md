# Installation

You can install the extension from
the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/28135-dynatrace-query-language/)
or directly from the IDE by going to `Settings` > `Plugins` > `Marketplace`.

## Configuration

After the plugin is installed, the support for Dynatrace domain-specific languages will be added automatically.
While lots of features like syntax highlighting, advanced code completion and code formatting will work out of the box
completely locally, some features require a live Dynatrace tenant connection to provide the best experience.

The plugin during the first execution will show a notification inviting you to simply add a Dynatrace tenant connection.
Alternatively, you can provide the connection details in the IDE settings under `Settings` > `Tools` >
`Dynatrace Query Language` -> `Connected Tenants` page.

### Adding a Dynatrace tenant connection

Currently, the only supported authentication method is providing a token. You can see more about that
[in the official docs](https://docs.dynatrace.com/docs/discover-dynatrace/references/dynatrace-api/basics/dynatrace-api-authentication).
By default, the token should include the `storage:buckets:read` permission, but you will also need to add permissions to
the specific data types, like: `storage:logs:read`, `storage:spans:read`, `storage:events:read`,
`storage:bizevents:read`, `storage:entities:read` etc., depending on your needs.

The first tenant on the list will become the default one, used for features like live validation and autocompletion.
