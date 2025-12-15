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

public class DQLCommandParametersCompletionTest extends LightPlatformCodeInsightFixture4TestCase {
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
    public void testAtTheEndOfCommandShouldSuggestCommandParameters() {
        when(serviceMock.getCommandByName("fetch")).thenReturn(
                DQLTestsUtils.createCommand("fetch", "data_source", List.of(
                        DQLTestsUtils.createParameter("source", List.of("dql.dataType.string"), true, List.of(), List.of(), null, false),
                        DQLTestsUtils.createParameter("from", List.of("dql.dataType.timestamp")),
                        DQLTestsUtils.createParameter("to", List.of("dql.dataType.timestamp"))
                ))
        );
        myFixture.configureByFiles("command-without-parameters.dql");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "source", "from", "to");
    }

    @Test
    public void testInsideCommandShouldNotSuggestAlreadyProvidedParameters() {
        when(serviceMock.getCommandByName("fetch")).thenReturn(DQLTestsUtils.createCommand("fetch", "data_source", List.of(
                DQLTestsUtils.createParameter("source", List.of("dql.dataType.string"), true, List.of(), List.of(), null, false),
                DQLTestsUtils.createParameter("from", List.of("dql.dataType.timestamp")),
                DQLTestsUtils.createParameter("to", List.of("dql.dataType.timestamp"))
        )));
        myFixture.configureByFiles("command-with-some-parameters.dql");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "to");
    }
}
