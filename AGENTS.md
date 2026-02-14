# AI Agent Instructions

This file contains instructions for AI coding assistants (GitHub Copilot, Cursor, etc.) working on this project.

## Project Overview

This is an IntelliJ IDEA plugin for Dynatrace Query Language (DQL) and Dynatrace Pattern Language (DPL). The project is
written in Java and uses Gradle as the build system.

## Code Style and Best Practices

### General Rules

- Follow existing code patterns and conventions in the project
- Use Java 17+ features when appropriate
- Prefer IntelliJ Platform APIs over standard Swing components
- Use `@NotNull` and `@Nullable` annotations consistently
- Keep methods focused and single-purpose
- Write self-documenting code with clear variable and method names
- Clean the code after making changes - remove duplications, unused imports, variables, and methods

### Critical Rules

- **NEVER use `@SuppressWarnings` annotations** - fix warnings properly instead of suppressing them
- **DO NOT generate markdown summaries of changes** - just make the changes directly using the appropriate tools
- Always use `get_errors` tool after editing files to verify changes
- Fix all errors and warnings that your changes introduce

### Naming Conventions

- Classes: PascalCase (e.g., `DQLExecutor`, `ChartLegendPanel`)
- Methods: camelCase (e.g., `refreshChart`, `createDataset`)
- Constants: UPPER_SNAKE_CASE (e.g., `SETTING_ORIENTATION`, `EMPTY_COMBO_OPTION`)
- Private fields: camelCase with descriptive names (e.g., `chartGenerator`, `selectedSeriesColumn`)

## Project Structure

```
src/main/java/pl/thedeem/intellij/
├── common/              # Reusable components and utilities
├── dql/                 # DQL language support
└── dpl/                 # DPL language support
```

## Common Patterns

## Testing

- Run tests with `./gradlew test`
- Build plugin with `./gradlew buildPlugin`
- Run IDE with plugin with `./gradlew runIde`

## Bundle Messages

All user-facing strings should be externalized to `DQLBundle.properties` or `DPLBundle.properties`:

```java
DQLBundle.message("key.name",param1, param2)
```

## Performance Considerations

- Avoid EDT blocking operations - use background threads with `ProgressManager`
- Cache expensive computations when possible
- Use `ReadAction` and `WriteAction` for PSI modifications
- Prefer BGT (Background Thread) for action updates when possible

## When Making Changes

1. **Search first**: Use semantic search to understand existing patterns before implementing
2. **Follow patterns**: Look at similar existing code and follow the same approach
3. **Validate**: Always check for errors after making changes
4. **Be thorough**: Fix all warnings your changes introduce - never suppress them
5. **Test**: Consider edge cases and test your changes
6. **No summaries**: Apply changes directly using tools, don't write markdown summaries

## Common Mistakes to Avoid

- ❌ Using `@SuppressWarnings` instead of fixing warnings
- ❌ Blocking EDT with long-running operations
- ❌ Not using `@NotNull`/`@Nullable` annotations
- ❌ Hardcoding user-facing strings instead of using bundle messages
- ❌ Using standard Swing components when IntelliJ alternatives exist
- ❌ Creating unnecessary boilerplate classes instead of using existing utilities
- ❌ Creating custom layouts when existing layout managers can do the job
- ❌ Not validating changes with `get_errors` tool

## Questions?

When in doubt:

1. Search the codebase for similar implementations
2. Check IntelliJ Platform SDK documentation
3. Look at the existing patterns in this project
4. Ask for clarification only if you truly cannot find the answer

Remember: **Action over discussion** - make the changes, validate them, and iterate if needed.
