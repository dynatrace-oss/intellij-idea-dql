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

There are currently two supported authentication methods for connecting to a Dynatrace tenant: SSO and platform token.
The first tenant on the list will become the default one, used for features like live validation and autocompletion.

#### SSO

Using the SSO method is straight-forward. IDE will open a browser window where you can log in to your tenant and grant
the necessary permissions to the plugin.

You will be notified each time the session expires, and you will be able to easily sign in again.

#### Platform token

The plugin is able to connect to a Dynatrace tenant using a platform token. This method of authentication requires
manual token creation (you can read more about that
[in the official docs](https://docs.dynatrace.com/docs/discover-dynatrace/references/dynatrace-api/basics/dynatrace-api-authentication)).
By default, the token should include the `storage:buckets:read` permission, but you will also need to add permissions to
the specific data types, like: `storage:logs:read`, `storage:spans:read`, `storage:events:read`,
`storage:bizevents:read`, `storage:entities:read` etc., depending on your needs.
