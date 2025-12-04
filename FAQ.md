# Frequently Asked Questions

## IDE reports errors, but Dynatrace Notebook does not for the same DQL query

Probably the plugin is lacking support for the functionality you are using. Please consider
creating [GitHub issue](https://github.com/dynatrace-oss/intellij-idea-dql/issues) containing the description of what
you expect to be working with an example DQL query included.

## No errors are reported, but the DQL does not compile

Because the local execution does not have any access to the DQL engine on your tenant, not every inspection can be
implemented (like the correct value type stored in a field), and some inspections were just not yet implemented.

To show all problems, you can enable _live validations_ in IDE's settings:
`File -> Settings -> Tools -> Dynatrace Query Language`. Before it starts working, you need to configure
a connection to your Dynatrace Tenant.

After enabling live validations feature the plugin will verify the DQL query calling REST API used by
Dynatrace Notebooks, so the same errors will be reported in the editor.

## The query does not execute due to authorization issues

There are two possible root causes for authorization issues when executing the DQL query: an invalid token was provided
or the user does not have proper privileges.

The easiest way of starting working with the plugin is to configure
a [platform token](https://docs.dynatrace.com/docs/manage/identity-access-management/access-tokens-and-oauth-clients/platform-tokens).
By design, it's executing REST API calls as the user that created the token, meaning that you can easily verify if you
have proper privileges by executing the same DQL query in Dynatrace Notebooks.

If the DQL query produces a result in the Notebook, the next step is to verify if your token has all required
privileges. It should require `storage:buckets:read` (to be able to access the data stored in Grail) and all
`storage:*:read` you will use, like `storage:logs:read` or `storage:bizevents:read`.
You can find the list of supported permissions
in [Dynatrace documentation](https://docs.dynatrace.com/docs/discover-dynatrace/platform/grail/organize-data/assign-permissions-in-grail#grail-permissions-table).

## The query executes, but the result is empty

The first step is to verify that the query produces a result in Dynatrace Notebooks, as maybe the DQL just does not
produce any data (or your user does not have permission to fetch it).

If you can see the result in the Notebook, but not inside the plugin execution, please verify if the time range for the
query is the same (using either `from`/`to` in the data source command or via the _Default timeframe start/end_ option
in the Run Configuration for the file). You can check which time range was used in the _Show query metadata_ button
in the result's panel header.

If the time range is correct, then most probably your token is missing required permission to access this sort of data.

## The plugin reports a lot of errors in Monaco templates

If your Monaco templates contain interpolation (`{{ }}`) or code injections like `for`/`if`, then the DQL/DPL file will
report errors, as this is not a correct syntax for the language.

Instead of using the `.dql` extension directly, you need to parse it using a templating engine first, specifying
the extension - for example - `.dql.templ` or `.godql`. The engine will automatically strip out all interpolations of
the Monaco injections and leave the rest to the DQL while providing the proper support for template commands, allowing
you to validate the code as well.

The templating engine must be provided by another JetBrains plugin, like
[Go template](https://plugins.jetbrains.com/plugin/10581-go-template). It allows you to register your own _File Type_
(in `File -> Settings -> Editor -> File Types -> Go template files`) and map a specific file extension to the original
language (works with JSON too).

## How to parse strings in other languages as DQL

If you want to parse strings in any other language (like Java, YAML, Typescript) as DQL queries, you need to add
a comment just before the string definition, containing `language=DQL` (no additional spaces are allowed). Some
examples:

```java
public static void test() {
    String myDql = /* language=DQL */ """
            fetch logs, from: -2h
            """;
    // language=DQL
    String myDql2 = """
            fetch logs, from: -2h
            """;
}
```

```yaml
parameters:
  # language=DQL
  dql: |-
    fetch logs, from: -2h
```

```typescript
const parameters = {
    dql: /* language=DQL */ `
     fetch logs, from: -2h
    `
}
```

You can learn more about this feature by reading the
[Add language injections](https://www.jetbrains.com/help/idea/using-language-injections.html#kffd9g_8)
documentation.
