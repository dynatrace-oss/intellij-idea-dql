package pl.thedeem.intellij.dql.completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import pl.thedeem.intellij.dql.DQLTestsUtils;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DQLCommandCompletionTest extends LightPlatformCodeInsightFixture4TestCase {
    @Mock
    private DQLDefinitionService serviceMock;

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/completion/dql";
    }

    @Before
    public void createService() {
        serviceMock = mock(DQLDefinitionService.class);
        ServiceContainerUtil.registerOrReplaceServiceInstance(
                getProject(),
                DQLDefinitionService.class,
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
    public void testAfterStartingQueryFinishedShouldSuggestMoreCommands() {
        when(serviceMock.getCommandsByCategory(DQLDefinitionService.STARTING_COMMANDS)).thenReturn(List.of(
                DQLTestsUtils.createCommand("data", "data_source", List.of()),
                DQLTestsUtils.createCommand("timeseries", "data_source", List.of())
        ));
        when(serviceMock.getCommandsByCategory(DQLDefinitionService.EXTENSION_COMMANDS)).thenReturn(List.of(
                DQLTestsUtils.createCommand("filter", "filter", List.of()),
                DQLTestsUtils.createCommand("sort", "order", List.of())
        ));
        myFixture.configureByFiles("single-data-record-query.dql");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "filter", "sort");
    }

    @Test
    public void testInsideEmptyFileShouldSuggestQueryStartCommands() {
        when(serviceMock.getCommandsByCategory(DQLDefinitionService.STARTING_COMMANDS)).thenReturn(List.of(
                DQLTestsUtils.createCommand("data", "data_source", List.of()),
                DQLTestsUtils.createCommand("timeseries", "data_source", List.of())
        ));
        when(serviceMock.getCommandsByCategory(DQLDefinitionService.EXTENSION_COMMANDS)).thenReturn(List.of(
                DQLTestsUtils.createCommand("filter", "filter", List.of()),
                DQLTestsUtils.createCommand("sort", "order", List.of())
        ));
        myFixture.configureByFiles("empty.dql");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "data", "timeseries");
    }

    @Test
    public void testInsideSubqueryShouldSuggestQueryStartCommands() {
        when(serviceMock.getCommandsByCategory(DQLDefinitionService.STARTING_COMMANDS)).thenReturn(List.of(
                DQLTestsUtils.createCommand("data", "data_source", List.of()),
                DQLTestsUtils.createCommand("timeseries", "data_source", List.of())
        ));
        when(serviceMock.getCommandsByCategory(DQLDefinitionService.EXTENSION_COMMANDS)).thenReturn(List.of(
                DQLTestsUtils.createCommand("append", "subquery", List.of(
                        DQLTestsUtils.createParameter("subquery", List.of())
                )),
                DQLTestsUtils.createCommand("sort", "order", List.of())
        ));
        myFixture.configureByFiles("subquery-append.dql");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "data", "timeseries");
    }
}
