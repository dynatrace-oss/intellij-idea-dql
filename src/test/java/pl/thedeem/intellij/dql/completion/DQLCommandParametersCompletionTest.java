package pl.thedeem.intellij.dql.completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import pl.thedeem.intellij.dql.DQLTestsUtils;
import pl.thedeem.intellij.dql.definition.DQLCommandGroup;
import pl.thedeem.intellij.dql.definition.DQLDefinitionLoader;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DQLCommandParametersCompletionTest extends LightPlatformCodeInsightFixture4TestCase {
   @Mock
   private DQLDefinitionLoader loaderMock;

   @Override
   protected String getTestDataPath() {
      return "src/test/testData/completion";
   }

   @Before
   public void createService() {
      loaderMock = mock(DQLDefinitionLoader.class);
      ServiceContainerUtil.registerOrReplaceServiceInstance(
          ApplicationManager.getApplication(),
          DQLDefinitionLoader.class,
          loaderMock,
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
      when(loaderMock.loadCommands()).thenReturn(DQLTestsUtils.createMockedCommands(List.of(
          DQLTestsUtils.createCommand("fetch", DQLCommandGroup.DATA_SOURCE, List.of(
              DQLTestsUtils.createParameter("source", List.of(DQLDataType.DATA_OBJECT), true, List.of(), List.of(), List.of(), null, false),
              DQLTestsUtils.createParameter("from", List.of(DQLDataType.TIMESTAMP)),
              DQLTestsUtils.createParameter("to", List.of(DQLDataType.TIMESTAMP))
          ))
      )));
      myFixture.configureByFiles("command-without-parameters.dql");
      myFixture.complete(CompletionType.BASIC);

      List<String> lookupElementStrings = myFixture.getLookupElementStrings();

      assertNotNull(lookupElementStrings);
      assertSameElements(lookupElementStrings, "from", "to");
   }

   @Test
   public void testInsideCommandShouldNotSuggestAlreadyProvidedParameters() {
      when(loaderMock.loadCommands()).thenReturn(DQLTestsUtils.createMockedCommands(List.of(
          DQLTestsUtils.createCommand("fetch", DQLCommandGroup.DATA_SOURCE, List.of(
              DQLTestsUtils.createParameter("source", List.of(DQLDataType.DATA_OBJECT), true, List.of(), List.of(), List.of(), null, false),
              DQLTestsUtils.createParameter("from", List.of(DQLDataType.TIMESTAMP)),
              DQLTestsUtils.createParameter("to", List.of(DQLDataType.TIMESTAMP))
          ))
      )));
      myFixture.configureByFiles("command-with-some-parameters.dql");
      myFixture.complete(CompletionType.BASIC);

      List<String> lookupElementStrings = myFixture.getLookupElementStrings();

      assertNotNull(lookupElementStrings);
      assertSameElements(lookupElementStrings, "to");
   }
}
