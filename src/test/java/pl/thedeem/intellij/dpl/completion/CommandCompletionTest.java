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
import pl.thedeem.intellij.dpl.definition.model.CommandMatcher;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandCompletionTest extends LightPlatformCodeInsightFixture4TestCase {
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
    public void testWhenDefinitionIsEmptyShouldSuggestCommands() {
        when(serviceMock.commands()).thenReturn(DPLTestsUtils.createMockedCommands(
                new Command("INT", null, "integer", null, null, null, null),
                new Command("LONG", null, "integer", null, null, null, null)
        ));
        myFixture.configureByFiles("empty.dpl");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "INT", "LONG");
    }

    @Test
    public void testWhenAnExpressionIsAlreadyDefinedShouldSuggestCommands() {
        when(serviceMock.commands()).thenReturn(DPLTestsUtils.createMockedCommands(
                new Command("INT", null, "integer", null, null, null, null),
                new Command("LONG", null, "integer", null, null, null, null)
        ));
        myFixture.configureByFiles("continuation.dpl");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "INT", "LONG");
    }

    @Test
    public void testWheInsideExpressionMatchersShouldSuggestCommands() {
        when(serviceMock.commands()).thenReturn(DPLTestsUtils.createMockedCommands(
                new Command("JSON", null, "integer", null, null, new CommandMatcher("members_list", null, null, false), null),
                new Command("INT", null, "integer", null, null, null, null)
        ));
        myFixture.configureByFiles("command-with-matchers.dpl");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "JSON", "INT");
    }

    @Test
    public void testInsideAnEmptyGroupShouldSuggestCommands() {
        when(serviceMock.commands()).thenReturn(DPLTestsUtils.createMockedCommands(
                new Command("INT", null, "integer", null, null, null, null),
                new Command("LONG", null, "integer", null, null, null, null)
        ));
        myFixture.configureByFiles("empty-group.dpl");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "INT", "LONG");
    }

    @Test
    public void testInsideAGroupShouldSuggestCommands() {
        when(serviceMock.commands()).thenReturn(DPLTestsUtils.createMockedCommands(
                new Command("INT", null, "integer", null, null, null, null),
                new Command("LONG", null, "integer", null, null, null, null)
        ));
        myFixture.configureByFiles("group.dpl");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "INT", "LONG");
    }

    @Test
    public void testInsideAMacroShouldSuggestCommands() {
        when(serviceMock.commands()).thenReturn(DPLTestsUtils.createMockedCommands(
                new Command("INT", null, "integer", null, null, null, null),
                new Command("LONG", null, "integer", null, null, null, null)
        ));
        myFixture.configureByFiles("macro.dpl");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "INT", "LONG");
    }

}
