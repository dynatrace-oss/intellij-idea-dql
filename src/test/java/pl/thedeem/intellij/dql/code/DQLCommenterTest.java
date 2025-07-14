package pl.thedeem.intellij.dql.code;

import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Test;
import pl.thedeem.intellij.dql.DQLFileType;

public class DQLCommenterTest extends LightPlatformCodeInsightFixture4TestCase {
   @Test
   public void testCommentsWholeLineWhenItsNotCommented() {
      myFixture.configureByText(DQLFileType.INSTANCE, """
          fetch logs, from: -5d
          | filter <caret>isNotNull(content)
          | fieldsAdd valid = matchesValue(content, "myValue")
          """);
      CommentByLineCommentAction commentAction = new CommentByLineCommentAction();
      commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
      myFixture.checkResult("""
          fetch logs, from: -5d
          // | filter isNotNull(content)
          | fieldsAdd valid = matchesValue(content, "myValue")
          """);
   }

   @Test
   public void testRemovesCommentOnWholeLineWhenItsCommented() {
      myFixture.configureByText(DQLFileType.INSTANCE, """
          fetch logs, from: -5d
          // | <caret>filter isNotNull(content)
          | fieldsAdd valid = matchesValue(content, "myValue")
          """);
      CommentByLineCommentAction commentAction = new CommentByLineCommentAction();
      commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
      myFixture.checkResult("""
          fetch logs, from: -5d
          | filter isNotNull(content)
          | fieldsAdd valid = matchesValue(content, "myValue")
          """);
   }
}
