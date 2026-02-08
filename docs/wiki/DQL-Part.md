# Dynatrace Query Language parts

The `DQLPart` language is a DQL query that disables validation for the query context. This allows you to write DQL
queries in smaller parts without raising errors related to a wrong DQL command usage.

This is especially useful for dynamic DQL generators, where you define a base part of the query and then add more
commands to it based on some conditions.

## Features

The `.dqlpart` files inherit features from the [Dynatrace Query Language (`.dql`)](./DQL) files.
You will not be able to execute such files and automatic tenant verification will be disabled for them, as they will
most likely never be a valid DQL query on their own.

## Usage

### Example `.dqlpart` file

```DQLPart
| filter event.provider = "my-provider"
```

### Example dynamic generator

```Java
public class Example {
    public static final String BASE_QUERY = /* language=DQL */ """
            fetch events, bucket: {"my-bucket"}, from: -1h
            """;

    public String generateQuery(boolean withFilter) {
        String query = BASE_QUERY;
        if (withFilter) {
            query += /* language=DQLPart */ """
                      | filter event.provider == "my-provider"
                    """;
        }
        return query;
    }
}
```