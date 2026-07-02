## 1. Schema recognition & validation

- [x] 1.1 Author `src/main/resources/schemas/dql-variables.schema.json` (Draft-07): object top level; shared
      `$defs/variableValue` = oneOf [string, number, boolean, null, array of variableValue, dql-fragment object,
      record object]; dql-fragment requires `$type` == `"dql"` and a string `dql`.
- [x] 1.2 Add `DQLVariablesJsonSchemaProviderFactory` implementing `JsonSchemaProviderFactory`, matching files
      named `dql-variables.json`, serving the bundled schema with `SchemaType.embeddedSchema`.
- [x] 1.3 Register the factory in `plugin.xml` under `com.intellij.json.jsonSchemaProviderFactory`.
- [x] 1.4 Add a test verifying the schema applies only to `dql-variables.json` and flags: non-object top level,
      an unsupported value form, and a dql-fragment missing/typed-wrong `dql`; and accepts scalars, arrays,
      records, and a valid dql-fragment.

## 2. Live reload of definitions

- [x] 2.1 Change `VariableElementImpl.getDefinition()` so the `CachedValue` always depends on
      `PsiModificationTracker.MODIFICATION_COUNT` (including the unresolved/null branch), so create/edit/delete of
      a governing `dql-variables.json` invalidates the cached definition.
- [x] 2.2 Verify `getValue()` / `getDataType()` (which read `getDefinition()`) reflect changes after invalidation;
      adjust if any additional cached state holds a stale `JsonProperty`.
- [x] 2.3 Add tests: (a) editing a value updates resolved value/data type; (b) removing a definition unresolves
      the variable; (c) creating the file / adding a property resolves a previously unresolved variable.

## 3. Reverse lookup & Find Usages

- [x] 3.1 Add a method to `DQLVariablesService` (impl in `DQLVariablesServiceImpl`) that, given a variable
      `JsonProperty` in a `dql-variables.json`, returns the `DQLVariableExpression` usages it governs — reusing
      `findVariableDefinitionFiles`/`findClosestDefinition` so only queries whose closest definition is this file
      are included.
- [x] 3.2 Implement a `referencesSearch` `QueryExecutorBase` that, when the search target is such a `JsonProperty`,
      yields the corresponding `DQLVariableReference`s; register it in `plugin.xml`.
- [x] 3.3 Ensure Find Usages is offered on the definition property (find-usages target handling) and results are
      categorised under the existing DQL variable usage type (`findUsages.types.variables` / `DQLUsageTypeProvider`).
- [x] 3.4 Add tests: all in-scope usages found; usages in a nested scope with a closer `dql-variables.json`
      excluded; a defined-but-unused variable yields no usages and no error.

## 4. Integration, docs & verification

- [x] 4.1 Extend `DQLVariableReferencesTest` (or add a sibling test) to cover the reverse direction alongside the
      existing forward resolution cases.
- [x] 4.2 Update `docs/wiki/DQL.md` variables section to document schema validation, live reload, and Find Usages.
- [x] 4.3 Update `CHANGELOG.md` with the new two-sided `dql-variables.json` integration.
- [x] 4.4 Run the full build/test suite (`./gradlew test`) and confirm all new and existing tests pass.
