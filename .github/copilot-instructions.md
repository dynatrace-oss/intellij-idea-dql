# GitHub Copilot Instructions

**Project Purpose**: A Java-based plugin for JetBrains IDEs adding support for Dynatrace Query Language (DQL)
and Dynatrace Pattern Language (DPL).

**Tech Stack**: Java, Gradle, Swing, JFreeChart.

## 1. PROJECT STRUCTURE & ARCHITECTURE

- **GrammarKit Plugin Used For Languages**: all supported languages define their syntax & parsing rules using the
  GrammarKit plugin, with grammar files located in `src/main/grammar/`. Classes generated from these grammar files are
  stored in `src/main/gen/`. They are generated automatically during Gradle build process and should not be manually
  edited.
- **Feature-Based Architecture**: each supported language has its own dedicated java package (like
  `src/main/java/pl/thedeem/intellij/dpl`). There is also a common package (`src/main/java/pl/thedeem/intellij/common`)
  for shared utilities and base classes.
- The `dqlpart` and `dqlexpr` packages contain a special types of language which are subset of DQL. They intentionally
  do not specify their own grammar files, but instead reuse DQL grammar with some limitations. This allows to reuse a
  lot of code and features between these languages, while still treating them as separate languages in the IDE.
- **Key Directories**:
  - `src/main/java`: Root for source code.
  - `src/main/grammar`: Contains `.bnf` and `.flex` grammar files for each supported language.
  - `src/main/gen`: Contains generated code classes for languages. Should not be edited manually.
  - `src/main/resources`: Contains non-code resources loaded in runtime by the plugin.
  - `src/main/resources/definition`: Contains JSON definitions for language engine (like allowed functions, commands,
    and parameters).
  - `CHANGELOG.md`: Contains a summary for changes made within each release, based on changesets included in PRs.

## 2. CODING STANDARDS & LINTING

- **Use JetBrains UI**: For UI components, use JetBrains' built-in UI components and follow their design guidelines
  instead of native Swing classes, if possible.
- **Avoid using code that produces warnings**: Do NOT use `@SuppressWarnings` without a very good reason. Fix warnings
  properly instead of suppressing them.
- **Use latest Java features**: Use Java 21+ features where appropriate, such as records, sealed classes, and pattern
  matching.
- **Annotate parameters**: Use `@NotNull` and `@Nullable` annotations consistently to indicate nullability of parameters
  and return values.
- **Naming Conventions**: Standard Java approach for classes, methods, variables. Follow existing code patterns and
  conventions in the project
- **Internationalize messages**: All user-facing strings should be externalized to `DQLBundle.properties` or
  `DPLBundle.properties`. Use `DQLBundle.message("key.name", param1, param2)` to retrieve messages.
- **Avoid EDT blocking operations**: Use background threads with `ProgressManager`
- **Focus on performance optimization**: Cache expensive computations when possible, especially in language parsing and
  analysis.
- **Avoid inline code blocks**: Do not use statements without braces.

## 3. TESTING SUMMARY

All tests are stored in the `src/test/java` directory. They use JetBrains framework to test IDE integration.

- **Unit tests**: Focus on testing individual components, utilities, and language parsing logic. Use JUnit 5 for unit
  tests.
- **Parsing tests**: Test the correctness of language parsing using the generated parser classes. Ensure that valid code
  is parsed correctly and invalid code produces appropriate errors. Add testing language files in
  `src/test/testData/parsing`.
- **Completion tests**: Test code completion features for languages. Ensure that the correct suggestions are provided
  based on the context. Add testing language files in `src/test/testData/completion`.
- **Indexing tests**: Test the indexing and reference resolution features. Add testing language files in
  `src/test/testData/indexing`.

## 4. QUICK DOs & DON'Ts

### ✅ DO

- Include a changelog entry with a short summary for the change. Follow the existing format in `CHANGELOG.md` file and
  decide whether the change is a fix, feature, or breaking change.
- Clean the code after making changes - remove duplications, unused imports, variables, and methods.
- Use `get_errors` tool after editing files to verify changes and fix all errors and warnings.

### ❌ DON'T

- Create Markdown summary files after completing tasks. Just make the necessary code changes directly.
- Hardcode user-facing strings instead of using bundle messages.
- Use standard Swing components when IntelliJ alternatives exist.
- Create unnecessary boilerplate classes instead of using existing utilities from JetBrains utilities.
- Avoid adding unnecessary comments that explain what the code does. Instead, focus on writing clear and
  self-explanatory code.

## 5. TASK COMPLETION VALIDATION

**Before stating that a task is complete, ALWAYS run validation after changing the code**.
Run `./gradlew build` to validate. **All commands must pass** before considering the task done. If any command fails:

1. Fix the errors
2. Re-run validation
3. Only then report completion

## 6. CLARIFICATION & COMMUNICATION

- **Always ask clarifying questions** before starting work until the requirements are crystal clear. Do not assume
  ambiguous details — ask.
- **Ask questions during work** whenever new uncertainties arise. Do not proceed with guesses on important decisions.

## 7. INSTRUCTIONS SELF-HEALING

- **Proactively identify misconfigurations**: When you notice that these instructions prescribe pattern A, but the
  codebase consistently follows pattern B, flag the discrepancy to the user and propose an update to the instructions
  file.
- **Report confusing or outdated instructions**: If any instruction is ambiguous, contradictory, or no longer matches
  the codebase reality, raise it immediately rather than silently working around it.
- **Goal**: Keep instructions files accurate and aligned with the actual codebase at all times.
