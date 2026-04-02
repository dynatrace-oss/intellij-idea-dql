package pl.thedeem.intellij.dql.services.parameters;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import pl.thedeem.intellij.dql.DQLTestsUtils;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.model.MappedParameter;
import pl.thedeem.intellij.dqlpart.DQLPartFileType;

import java.util.List;

public class DQLParametersCalculatorServiceImplTest extends LightPlatformCodeInsightFixture4TestCase {
    private final DQLParametersCalculatorServiceImpl calculator = new DQLParametersCalculatorServiceImpl();

    @Test
    public void correctlyMapsSimpleParametersToDefinitions() {
        List<Parameter> definitions = List.of(
                DQLTestsUtils.createParameter("source", true, false, false),
                DQLTestsUtils.createParameter("from", false, true, false),
                DQLTestsUtils.createParameter("to", false, true, false)
        );

        DQLCommand command = parseCommand("fetch to: -2h, from: -5d, logs");
        List<MappedParameter> result = calculator.mapParameters(command, definitions);

        assertParametersEqual(result,
                new ParamMatcher("to", true, List.of("-2h")),
                new ParamMatcher("from", true, List.of("-5d")),
                new ParamMatcher("source", true, List.of("logs"))
        );
    }


    @Test
    public void groupsAdditionalUnnamedExpressionsIntoVariadicParameter() {
        List<Parameter> definitions = List.of(
                DQLTestsUtils.createParameter("field", true, false, true)
        );

        DQLCommand command = parseCommand("""
                fieldsAdd field1, field2 = concat("hello", "world"), field3 < 5
                """);
        List<MappedParameter> result = calculator.mapParameters(command, definitions);

        assertParametersEqual(result,
                new ParamMatcher("field", true, List.of(
                        "field1",
                        "field2 = concat(\"hello\", \"world\")",
                        "field3 < 5"
                ))
        );
    }

    @Test
    public void doesNotDetectAnAdditionalNotDefinedParameter() {
        List<Parameter> definitions = List.of(
                DQLTestsUtils.createParameter("source", true, false, false),
                DQLTestsUtils.createParameter("from", false, true, false),
                DQLTestsUtils.createParameter("to", false, true, false)
        );

        DQLCommand command = parseCommand("""
                fetch to: -2h, from: -5d, logs, bucket: "default"
                """);
        List<MappedParameter> result = calculator.mapParameters(command, definitions);

        assertParametersEqual(result,
                new ParamMatcher("to", true, List.of("-2h")),
                new ParamMatcher("from", true, List.of("-5d")),
                new ParamMatcher("source", true, List.of("logs")),
                new ParamMatcher("bucket", false, List.of("\"default\""))
        );
    }

    @Test
    public void correctlyAssignsUnusedUnnamedParameters() {
        List<Parameter> definitions = List.of(
                DQLTestsUtils.createParameter("expression", true, false, false),
                DQLTestsUtils.createParameter("pattern", true, false, false),
                DQLTestsUtils.createParameter("preserveFieldsOnFailure", false, true, false),
                DQLTestsUtils.createParameter("parsingPrerequisite", false, true, false)
        );

        DQLCommand command = parseCommand("""
                parse pattern: "JSON:v", preserveFieldsOnFailure: true, parsingPrerequisite: true, field
                """);
        List<MappedParameter> result = calculator.mapParameters(command, definitions);

        assertParametersEqual(result,
                new ParamMatcher("pattern", true, List.of("\"JSON:v\"")),
                new ParamMatcher("preserveFieldsOnFailure", true, List.of("true")),
                new ParamMatcher("parsingPrerequisite", true, List.of("true")),
                new ParamMatcher("expression", true, List.of("field"))
        );
    }

    @Test
    public void continuesVariadicParameterSplitWithOtherParameters() {
        List<Parameter> definitions = List.of(
                DQLTestsUtils.createParameter("aggregation", true, false, true),
                DQLTestsUtils.createParameter("by", false, true, true)
        );

        DQLCommand command = parseCommand("""
                summarize takeAny(field1), field2 = max(field2), by: {field3}, takeLast(field4)
                """);
        List<MappedParameter> result = calculator.mapParameters(command, definitions);

        assertParametersEqual(result,
                new ParamMatcher("aggregation", true, List.of(
                        "takeAny(field1)",
                        "field2 = max(field2)",
                        "takeLast(field4)"
                )),
                new ParamMatcher("by", true, List.of("field3"))
        );
    }

    @Test
    public void continuesVariadicParameterEventWithBracketValue() {
        List<Parameter> definitions = List.of(
                DQLTestsUtils.createParameter("aggregation", true, false, true),
                DQLTestsUtils.createParameter("by", false, true, true)
        );

        DQLCommand command = parseCommand("summarize {sum(f1), max(f1)}, by: f2, count(f1)");
        List<MappedParameter> result = calculator.mapParameters(command, definitions);

        assertParametersEqual(result,
                new ParamMatcher("aggregation", true, List.of(
                        "sum(f1)",
                        "max(f1)",
                        "count(f1)"
                )),
                new ParamMatcher("by", true, List.of("f2"))
        );
    }

    @Test
    public void recognizesParametersEmbeddedInBrackets() {
        List<Parameter> definitions = List.of(
                DQLTestsUtils.createParameter("metric", true, false, true),
                DQLTestsUtils.createParameter("nonempty", false, true, false),
                DQLTestsUtils.createParameter("filter", false, true, false),
                DQLTestsUtils.createParameter("by", false, true, true)
        );

        DQLCommand command = parseCommand("""
                   timeseries {
                       oom_kills = sum(dt.kubernetes.container.oom_kills, default: 0, rollup: sum),
                       nonempty: true,
                       filter: k8s.namespace.name == "vulnerability-scan",
                       by: { cluster_name=k8s.cluster.name, k8s.pod.name },
                       max_oom_kills = max(dt.kubernetes.container.oom_kills, default: 0, rollup: sum)
                   }
                """);
        List<MappedParameter> result = calculator.mapParameters(command, definitions);

        assertParametersEqual(result,
                new ParamMatcher("metric", true, List.of(
                        "oom_kills = sum(dt.kubernetes.container.oom_kills, default: 0, rollup: sum)",
                        "max_oom_kills = max(dt.kubernetes.container.oom_kills, default: 0, rollup: sum)"
                )),
                new ParamMatcher("nonempty", true, List.of("true")),
                new ParamMatcher("filter", true, List.of("k8s.namespace.name == \"vulnerability-scan\"")),
                new ParamMatcher("by", true, List.of("cluster_name=k8s.cluster.name", "k8s.pod.name"))
        );
    }

    @Test
    public void joinsParametersValuesFromBrackets() {
        List<Parameter> definitions = List.of(
                DQLTestsUtils.createParameter("metric", true, false, true),
                DQLTestsUtils.createParameter("nonempty", false, true, false),
                DQLTestsUtils.createParameter("filter", false, true, false),
                DQLTestsUtils.createParameter("by", false, true, true)
        );

        DQLCommand command = parseCommand("""
                   timeseries {
                       oom_kills = sum(dt.kubernetes.container.oom_kills, default: 0, rollup: sum),
                       nonempty: true
                   },
                   {
                       max_oom_kills = max(dt.kubernetes.container.oom_kills, default: 0, rollup: sum),
                       filter: k8s.namespace.name == "vulnerability-scan"
                   },
                   by: { cluster_name=k8s.cluster.name, k8s.pod.name }
                """);
        List<MappedParameter> result = calculator.mapParameters(command, definitions);

        assertParametersEqual(result,
                new ParamMatcher("metric", true, List.of(
                        "oom_kills = sum(dt.kubernetes.container.oom_kills, default: 0, rollup: sum)",
                        "max_oom_kills = max(dt.kubernetes.container.oom_kills, default: 0, rollup: sum)"
                )),
                new ParamMatcher("nonempty", true, List.of("true")),
                new ParamMatcher("filter", true, List.of("k8s.namespace.name == \"vulnerability-scan\"")),
                new ParamMatcher("by", true, List.of("cluster_name=k8s.cluster.name", "k8s.pod.name"))
        );
    }

    @Test
    public void allowsCreatingAliasParameterForField() {
        List<Parameter> definitions = List.of(
                DQLTestsUtils.createParameter("field", true, false, true)
        );

        DQLCommand command = parseCommand("""
                   fields field1, alias: field2, field3, field4, alias: field5
                """);
        List<MappedParameter> result = calculator.mapParameters(command, definitions);

        assertParametersEqual(result,
                new ParamMatcher("field", true, List.of(
                        "field1",
                        "alias: field2",
                        "field3",
                        "field4",
                        "alias: field5"
                ))
        );
    }

    @Test
    public void correctlyDetectsAliasParametersInBrackets() {
        List<Parameter> definitions = List.of(
                DQLTestsUtils.createParameter("field", true, false, true)
        );

        DQLCommand command = parseCommand("""
                   fields {field1, alias: field2}, field3, {alias: field5, field4}
                """);
        List<MappedParameter> result = calculator.mapParameters(command, definitions);

        assertParametersEqual(result,
                new ParamMatcher("field", true, List.of(
                        "field1",
                        "alias: field2",
                        "field3",
                        "alias: field5",
                        "field4"
                ))
        );
    }

    @Test
    public void assignsUnnamedParametersToEachUnnamedVariadic() {
        List<Parameter> definitions = List.of(
                DQLTestsUtils.createParameter("edgeType", true, false, true),
                DQLTestsUtils.createParameter("targetType", true, false, true),
                DQLTestsUtils.createParameter("fieldsKeep", false, true, true),
                DQLTestsUtils.createParameter("direction", false, true, false),
                DQLTestsUtils.createParameter("nodeId", false, false, false)
        );

        DQLCommand command = parseCommand("""
                  traverse runs_on, HOST, direction: forward, fieldsKeep: name
                """);
        List<MappedParameter> result = calculator.mapParameters(command, definitions);

        assertParametersEqual(result,
                new ParamMatcher("edgeType", true, List.of("runs_on")),
                new ParamMatcher("targetType", true, List.of("HOST")),
                new ParamMatcher("direction", true, List.of("forward")),
                new ParamMatcher("fieldsKeep", true, List.of("name"))
        );
    }

    @Test
    public void unpacksNestedBracketsOnlyForOneElementGroups() {
        List<Parameter> definitions = List.of(
                DQLTestsUtils.createParameter("aggregation", true, false, true),
                DQLTestsUtils.createParameter("by", false, true, true)
        );

        DQLCommand command = parseCommand("""
                  summarize {{{ sum(f1), {{max(f1), min(f1)}} }}}, by: {{ f2, count() }}
                """);
        List<MappedParameter> result = calculator.mapParameters(command, definitions);

        assertParametersEqual(result,
                new ParamMatcher("aggregation", true, List.of(
                        "sum(f1)",
                        "max(f1)",
                        "min(f1)"
                )),
                new ParamMatcher("by", true, List.of(
                        "f2",
                        "count()"
                ))
        );
    }

    @Test
    public void handlesRequiringNameVariadicElementWhenItsPlacedAtTheBeginning() {
        List<Parameter> definitions = List.of(
                DQLTestsUtils.createParameter("joinTable", true, false, false),
                DQLTestsUtils.createParameter("on", true, true, true),
                DQLTestsUtils.createParameter("executionOrder", false, true, false),
                DQLTestsUtils.createParameter("broadcast", false, true, false),
                DQLTestsUtils.createParameter("fields", false, true, true)
        );

        DQLCommand command = parseCommand("""
                  join on: left == right, executionOrder: auto, [data record()]
                """);
        List<MappedParameter> result = calculator.mapParameters(command, definitions);

        assertParametersEqual(result,
                new ParamMatcher("on", true, List.of("left == right")),
                new ParamMatcher("executionOrder", true, List.of("auto")),
                new ParamMatcher("joinTable", true, List.of("[data record()]"))
        );
    }

    private @NotNull DQLCommand parseCommand(@NotNull String content) {
        PsiFile file = myFixture.configureByText(DQLPartFileType.INSTANCE, content);
        DQLCommand command = PsiTreeUtil.findChildOfType(file, DQLCommand.class);
        assertNotNull("Expected DQLCommand in: " + content, command);
        return command;
    }

    private void assertParametersEqual(@NotNull List<MappedParameter> result, @NotNull ParamMatcher... matchers) {
        List<ParamMatcher> expected = List.of(matchers);
        List<ParamMatcher> actual = result.stream()
                .filter(MappedParameter::notPseudo)
                .map(p -> new ParamMatcher(
                        p.name(),
                        p.definition() != null,
                        p.values().stream()
                                .map(e -> e instanceof DQLParameterExpression named
                                        && !StringUtil.equalsIgnoreCase("alias", named.getName()) && named.getExpression() != null
                                        ? named.getExpression().getText()
                                        : e.getText())
                                .toList()
                ))
                .toList();
        assertContainsOrdered(actual, expected);
    }

    private record ParamMatcher(@Nullable String name, boolean definitionFound, @NotNull List<String> values) {
        @Override
        public @NotNull String toString() {
            return "Parameter '%s' with definition: %s, with values: %s".formatted(name, definitionFound, String.join(", ", values));
        }
    }
}
