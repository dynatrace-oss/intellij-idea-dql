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
import pl.thedeem.intellij.dql.definition.DQLFunctionGroup;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import static org.mockito.Mockito.*;

import java.util.List;

public class DQLCommandCompletionTest extends LightPlatformCodeInsightFixture4TestCase {
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
   public void testAfterStartingQueryFinishedShouldSuggestMoreCommands() {
      when(loaderMock.loadCommands()).thenReturn(DQLTestsUtils.createMockedCommands(List.of(
          DQLTestsUtils.createCommand("data", DQLCommandGroup.DATA_SOURCE, List.of()),
          DQLTestsUtils.createCommand("timeseries", DQLCommandGroup.METRIC, List.of()),
          DQLTestsUtils.createCommand("filter", DQLCommandGroup.FILTERING, List.of()),
          DQLTestsUtils.createCommand("sort", DQLCommandGroup.ORDERING, List.of())
      )));
      when(loaderMock.loadFunctions()).thenReturn(DQLTestsUtils.createMockedFunctions(List.of(
          DQLTestsUtils.createFunction("record", DQLFunctionGroup.RECORDS_LIST, List.of(DQLDataType.RECORD), List.of())
      )));
      myFixture.configureByFiles("single-data-record-query.dql");
      myFixture.complete(CompletionType.BASIC);

      List<String> lookupElementStrings = myFixture.getLookupElementStrings();

      assertNotNull(lookupElementStrings);
      assertSameElements(lookupElementStrings, "filter", "sort");
   }

   @Test
   public void testInsideEmptyFileShouldSuggestQueryStartCommands() {
      when(loaderMock.loadCommands()).thenReturn(DQLTestsUtils.createMockedCommands(List.of(
          DQLTestsUtils.createCommand("data", DQLCommandGroup.DATA_SOURCE, List.of()),
          DQLTestsUtils.createCommand("timeseries", DQLCommandGroup.METRIC, List.of()),
          DQLTestsUtils.createCommand("filter", DQLCommandGroup.FILTERING, List.of()),
          DQLTestsUtils.createCommand("sort", DQLCommandGroup.ORDERING, List.of())
      )));
      myFixture.configureByFiles("empty.dql");
      myFixture.complete(CompletionType.BASIC);

      List<String> lookupElementStrings = myFixture.getLookupElementStrings();

      assertNotNull(lookupElementStrings);
      assertSameElements(lookupElementStrings, "data", "timeseries");
   }

   @Test
   public void testInsideSubqueryShouldSuggestQueryStartCommands() {
      when(loaderMock.loadCommands()).thenReturn(DQLTestsUtils.createMockedCommands(List.of(
          DQLTestsUtils.createCommand("data", DQLCommandGroup.DATA_SOURCE, List.of()),
          DQLTestsUtils.createCommand("timeseries", DQLCommandGroup.METRIC, List.of()),
          DQLTestsUtils.createCommand("append", DQLCommandGroup.CORRELATION_AND_JOIN, List.of(
              DQLTestsUtils.createParameter("subquery", List.of(DQLDataType.SUBQUERY_EXPRESSION))
          )),
          DQLTestsUtils.createCommand("sort", DQLCommandGroup.ORDERING, List.of())
      )));
      myFixture.configureByFiles("subquery-append.dql");
      myFixture.complete(CompletionType.BASIC);

      List<String> lookupElementStrings = myFixture.getLookupElementStrings();

      assertNotNull(lookupElementStrings);
      assertSameElements(lookupElementStrings, "data", "timeseries");
   }
}
