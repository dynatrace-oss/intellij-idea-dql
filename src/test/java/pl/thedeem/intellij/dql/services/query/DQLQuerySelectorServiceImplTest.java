package pl.thedeem.intellij.dql.services.query;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.IPopupChooserBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Before;
import org.junit.Test;
import pl.thedeem.intellij.dql.DQLFileType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DQLQuerySelectorServiceImplTest extends LightPlatformCodeInsightFixture4TestCase {
    private final DQLQuerySelectorServiceImpl service = new DQLQuerySelectorServiceImpl();
    private final JBPopupFactory factoryMock = mock(JBPopupFactory.class);
    private final IPopupChooserBuilder<?> builderMock = mock(IPopupChooserBuilder.class, RETURNS_SELF);
    private final JBPopup popupMock = mock(JBPopup.class);

    @Before
    public void setup() {
        ServiceContainerUtil.registerOrReplaceServiceInstance(
                ApplicationManager.getApplication(),
                JBPopupFactory.class,
                factoryMock,
                getTestRootDisposable()
        );
    }

    // DQL files
    @Test
    public void shouldReturnTheWholeQueryWhenNothingIsSelected() {
        PsiFile file = myFixture.configureByText(DQLFileType.INSTANCE, "fetch logs | filter true");
        assertEquals("fetch logs | filter true", service.getQueryText(file));
    }

    @Test
    public void shouldReturnTheWholeQueryWhenCaretIsInsideMainQuery() {
        PsiFile file = myFixture.configureByText(DQLFileType.INSTANCE, "fetch logs | <caret>filter true | append [data record()]");
        assertEquals("fetch logs | filter true | append [data record()]", service.getQueryText(file));
    }

    @Test
    public void shouldReturnTheWholeQueryWhenEditorIsNull() {
        PsiFile file = myFixture.configureByText(DQLFileType.INSTANCE, "fetch logs | filter true");
        List<String> results = new ArrayList<>();

        service.getQueryFromEditorContext(file, null, results::add);

        assertEquals(List.of("fetch logs | filter true"), results);
    }

    @Test
    public void shouldNotAskTheUserForSelectionAndReturnWholeQueryWhenCaretIsInMainQuery() {
        PsiFile file = myFixture.configureByText(DQLFileType.INSTANCE, "fetch logs | filter<caret> true");

        service.getQueryFromEditorContext(file, myFixture.getEditor(), query -> {
            assertEquals("fetch logs | filter true", query);
            verify(factoryMock, never()).createPopupChooserBuilder(any(List.class));
        });
    }

    @Test
    public void shouldReturnWholeQueryWhenUserSelectedPartOfContentAndWantsToUseWholeQuery() {
        PsiFile file = myFixture.configureByText(DQLFileType.INSTANCE, "<selection>fetch logs</selection> | filter true");
        mockUserSelectedPopupPosition(1);

        service.getQueryFromEditorContext(file, myFixture.getEditor(), query -> {
            assertEquals("fetch logs | filter true", query);
            verify(factoryMock, atMostOnce()).createPopupChooserBuilder(any(List.class));
        });
    }

    @Test
    public void shouldReturnSelectedQueryWhenUserSelectedPartOfContentAndWantsToUseSelectedPart() {
        PsiFile file = myFixture.configureByText(DQLFileType.INSTANCE, "<selection>fetch logs</selection> | filter true");
        mockUserSelectedPopupPosition(0);

        service.getQueryFromEditorContext(file, myFixture.getEditor(), query -> {
            assertEquals("fetch logs", query);
            verify(factoryMock, atMostOnce()).createPopupChooserBuilder(any(List.class));
        });
    }

    @Test
    public void shouldReturnWholeQueryWhenCaretIsInsideSubqueryAndTheUserWantsToUseWholeQuery() {
        PsiFile file = myFixture.configureByText(DQLFileType.INSTANCE, "fetch logs | filter true | append [data record(1) | append [data <caret>record(2)]]");
        mockUserSelectedPopupPosition(2);

        service.getQueryFromEditorContext(file, myFixture.getEditor(), query -> {
            assertEquals("fetch logs | filter true | append [data record(1) | append [data record(2)]]", query);
            verify(factoryMock, atMostOnce()).createPopupChooserBuilder(any(List.class));
        });
    }

    @Test
    public void shouldReturnSubqueryWhenCaretIsInsideSubqueryAndTheUserWantsToExecuteIt() {
        PsiFile file = myFixture.configureByText(DQLFileType.INSTANCE, "fetch logs | filter true | append [data record(1) | append [data <caret>record(2)]]");
        mockUserSelectedPopupPosition(1);

        service.getQueryFromEditorContext(file, myFixture.getEditor(), query -> {
            assertEquals("data record(1) | append [data record(2)]", query);
            verify(factoryMock, atMostOnce()).createPopupChooserBuilder(any(List.class));
        });
    }

    @Test
    public void shouldReturnNestedSubqueryWhenCaretIsInsideSubqueryAndTheUserWantsToExecuteIt() {
        PsiFile file = myFixture.configureByText(DQLFileType.INSTANCE, "fetch logs | filter true | append [data record(1) | append [data<caret> record(2)]]");
        mockUserSelectedPopupPosition(0);

        service.getQueryFromEditorContext(file, myFixture.getEditor(), query -> {
            assertEquals("data record(2)", query);
            verify(factoryMock, atMostOnce()).createPopupChooserBuilder(any(List.class));
        });
    }

    // Injected DQL fragments

    @Test
    public void shouldReturnUnescapedQueryWhenExecutingInjectedFragment() {
        PsiFile file = myFixture.configureByText(JavaFileType.INSTANCE, /* language=Java */ """
                public class TextClass {
                    public static String printInjectedFragment() {
                        System.out.println(/* language=DQL */ "<caret>data record(field = \\"value\\")");
                    }
                }
                """);
        assertEquals("data record(field = \"value\")", service.getQueryText(file));
    }

    @Test
    public void shouldNotCreatePopupWhenInsideInjectedFragmentMainQuery() {
        PsiFile file = myFixture.configureByText(JavaFileType.INSTANCE, /* language=Java */ """
                public class TextClass {
                    public static String printInjectedFragment() {
                        System.out.println(/* language=DQL */ "<caret>data record(field = \\"value\\")");
                    }
                }
                """);
        service.getQueryFromEditorContext(file, myFixture.getEditor(), query -> {
            assertEquals("data record(field = \"value\")", query);
            verify(factoryMock, never()).createPopupChooserBuilder(any(List.class));
        });
    }

    @Test
    public void shouldAllowTheUserToExecuteJustSubqueryInInjectedFragment() {
        PsiFile file = myFixture.configureByText(JavaFileType.INSTANCE, /* language=Java */ """
                public class TextClass {
                    public static String printInjectedFragment() {
                        System.out.println(/* language=DQL */ "fetch logs | append [<caret>data record(field = \\"value2\\") | filter true]");
                    }
                }
                """);
        mockUserSelectedPopupPosition(0);

        service.getQueryFromEditorContext(file, myFixture.getEditor(), query -> {
            assertEquals("data record(field = \"value2\") | filter true", query);
            verify(factoryMock, atMostOnce()).createPopupChooserBuilder(any(List.class));
        });
    }

    @Test
    public void shouldAllowTheUserToExecuteJustSelectedPartInInjectedFragment() {
        PsiFile file = myFixture.configureByText(JavaFileType.INSTANCE, /* language=Java */ """
                public class TextClass {
                    public static String printInjectedFragment() {
                        System.out.println(/* language=DQL */ "fetch logs | append [<caret><selection>data record(field = \\"value2\\")</selection> | filter true]");
                    }
                }
                """);
        mockUserSelectedPopupPosition(0);

        service.getQueryFromEditorContext(file, myFixture.getEditor(), query -> {
            assertEquals("data record(field = \"value2\")", query);
            verify(factoryMock, atMostOnce()).createPopupChooserBuilder(any(List.class));
        });
    }

    private void mockUserSelectedPopupPosition(int selectedItemIndex) {
        AtomicReference<List<DQLQuerySelectorServiceImpl.SelectionContext>> capturedItems = new AtomicReference<>();
        AtomicReference<Consumer<DQLQuerySelectorServiceImpl.SelectionContext>> capturedCallback = new AtomicReference<>();

        when(factoryMock.createPopupChooserBuilder(any(List.class))).thenAnswer(inv -> {
            capturedItems.set(inv.getArgument(0));
            return builderMock;
        });

        when(builderMock.setItemChosenCallback(any())).thenAnswer(inv -> {
            capturedCallback.set(inv.getArgument(0));
            return builderMock;
        });

        when(builderMock.createPopup()).thenReturn(popupMock);

        doAnswer(inv -> {
            capturedCallback.get().accept(capturedItems.get().get(selectedItemIndex));
            return null;
        }).when(popupMock).showInBestPositionFor(any(Editor.class));
    }
}
