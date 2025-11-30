package pl.thedeem.intellij.dpl.completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import pl.thedeem.intellij.dpl.DPLTestsUtils;
import pl.thedeem.intellij.dpl.definition.DPLDefinitionService;
import pl.thedeem.intellij.dpl.definition.model.Command;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.definition.model.Expression;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationCompletionTest extends LightPlatformCodeInsightFixture4TestCase {
    @Mock
    private DPLDefinitionService serviceMock;

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/completion/dpl";
    }

    @Before
    public void createService() {
        serviceMock = mock(DPLDefinitionService.class);
        ServiceContainerUtil.registerOrReplaceServiceInstance(
                getProject(),
                DPLDefinitionService.class,
                serviceMock,
                getTestRootDisposable()
        );
    }

    @After
    public void cleanup() {
        DQLDefinitionService service = myFixture.getProject().getService(DQLDefinitionService.class);
        service.invalidateCache();
    }

    @Test
    public void testInsideACommandWithoutConfigurationShouldSuggestCommands() {
        when(serviceMock.commands()).thenReturn(DPLTestsUtils.createMockedCommands(
                new Command("INT", null, "integer", null, null, null, null),
                new Command("LONG", null, "integer", null, null, null, null)
        ));
        myFixture.configureByFiles("empty-command-configuration.dpl");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "INT", "LONG");
    }

    @Test
    public void testInsideACommandWithEmptyConfigurationShouldSuggestCommandsAndParameters() {
        when(serviceMock.commands()).thenReturn(DPLTestsUtils.createMockedCommands(
                new Command("INT", null, "integer", null, null, null, Map.of(
                        "min", new Configuration("min", null, null, "integer", Set.of(), Set.of()),
                        "max", new Configuration("max", null, null, "integer", Set.of(), Set.of())
                )),
                new Command("LONG", null, "integer", null, null, null, null)
        ));
        myFixture.configureByFiles("empty-command-configuration.dpl");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "INT", "LONG", "min", "max");
    }

    @Test
    public void testInsideACommandWithExpressionConfigurationShouldSuggestCommandsAndParameters() {
        when(serviceMock.commands()).thenReturn(DPLTestsUtils.createMockedCommands(
                new Command("INT", null, "integer", null, null, null, null),
                new Command("LONG", null, "integer", null, null, null, null)
        ));
        when(serviceMock.expressions()).thenReturn(Map.of(
                "sequence", new Expression(Map.of(
                        "charset", new Configuration("charset", null, null, "integer", Set.of(), Set.of()),
                        "locale", new Configuration("locale", null, null, "integer", Set.of(), Set.of())
                ))
        ));
        myFixture.configureByFiles("empty-expression-configuration.dpl");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "INT", "LONG", "charset", "locale");
    }

    @Test
    public void testInsideACommandWithFilledConfigurationShouldSuggestUnusedParameters() {
        when(serviceMock.commands()).thenReturn(DPLTestsUtils.createMockedCommands(
                new Command("INT", null, "integer", null, null, null, Map.of(
                        "min", new Configuration("min", null, null, "integer", Set.of(), Set.of()),
                        "max", new Configuration("max", null, null, "integer", Set.of(), Set.of())
                )),
                new Command("LONG", null, "integer", null, null, null, null)
        ));
        myFixture.configureByFiles("filled-command-configuration.dpl");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "max");
    }

    @Test
    public void testInsideACommandWithConfigurationForAllParametersShouldNotSuggestAnything() {
        when(serviceMock.commands()).thenReturn(DPLTestsUtils.createMockedCommands(
                new Command("INT", null, "integer", null, null, null, Map.of(
                        "min", new Configuration("min", null, null, "integer", Set.of(), Set.of())
                )),
                new Command("LONG", null, "integer", null, null, null, null)
        ));
        myFixture.configureByFiles("filled-command-configuration.dpl");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertEmpty(lookupElementStrings);
    }

    @Test
    public void testInsideACommandConfigurationFinishedWithCommaShouldSuggestUnusedParameters() {
        when(serviceMock.commands()).thenReturn(DPLTestsUtils.createMockedCommands(
                new Command("INT", null, "integer", null, null, null, Map.of(
                        "min", new Configuration("min", null, null, "integer", Set.of(), Set.of()),
                        "max", new Configuration("max", null, null, "integer", Set.of(), Set.of())
                )),
                new Command("LONG", null, "integer", null, null, null, null)
        ));
        myFixture.configureByFiles("filled-command-configuration-with-comma.dpl");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "max");
    }
}
