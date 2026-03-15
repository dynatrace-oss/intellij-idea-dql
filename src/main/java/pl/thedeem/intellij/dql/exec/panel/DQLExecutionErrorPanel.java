package pl.thedeem.intellij.dql.exec.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.FormattedLanguageText;
import pl.thedeem.intellij.common.components.InformationComponent;
import pl.thedeem.intellij.common.components.TransparentScrollPane;
import pl.thedeem.intellij.common.sdk.errors.*;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

public class DQLExecutionErrorPanel extends BorderLayoutPanel implements Disposable {
    private FormattedLanguageText queryViewer;

    protected DQLExecutionErrorPanel() {
        super();
        withBorder(JBUI.Borders.empty()).andTransparent();
    }

    public DQLExecutionErrorPanel(@NotNull Exception e, @Nullable String query, @Nullable Project project) {
        this(getErrorMessage(e), getErrorDetails(e), query, project);
    }

    public DQLExecutionErrorPanel(@NotNull String message, @Nullable String query, @Nullable Project project) {
        this(message, List.of(), query, project);
    }

    public DQLExecutionErrorPanel(@NotNull String message, @NotNull List<String> details, @Nullable String query, @Nullable Project project) {
        this();
        if (query != null && project != null) {
            this.queryViewer = new FormattedLanguageText(DynatraceQueryLanguage.INSTANCE, project, true);
        }

        JBPanel<?> errorPanel = new JBPanel<>()
                .withBorder(JBUI.Borders.empty())
                .andTransparent();
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
        InformationComponent header = new InformationComponent(DQLBundle.message("error.detailed.title"), AllIcons.General.Error);
        header.setAlignmentX(InformationComponent.CENTER_ALIGNMENT);
        errorPanel.add(header);
        errorPanel.add(Box.createVerticalStrut(1));
        errorPanel.add(createHtmlField("<p>" + message + "</p>"));

        if (!details.isEmpty()) {
            errorPanel.add(Box.createVerticalStrut(1));
            errorPanel.add(createHtmlField("<ul>" + String.join("", details.stream().map(p -> "<li>" + p + "</li>").toList()) + "</ul>"));
        }

        JBPanel<?> centeringWrapper = new JBPanel<>(new GridBagLayout())
                .andTransparent();
        centeringWrapper.add(errorPanel, new GridBagConstraints());

        if (query != null && project != null) {
            OnePixelSplitter splitter = new OnePixelSplitter(false, 0.45f);
            splitter.setFirstComponent(new TransparentScrollPane(centeringWrapper));
            JButton showQueryButton = createQueryToggleButton(query, splitter);
            errorPanel.add(Box.createVerticalStrut(JBUI.scale(8)));
            errorPanel.add(showQueryButton);
            addToCenter(splitter);
        } else {
            addToCenter(new TransparentScrollPane(centeringWrapper));
        }
    }

    private @NotNull JButton createQueryToggleButton(@NotNull String query, @NotNull OnePixelSplitter splitter) {
        JButton showQueryButton = new JButton(DQLBundle.message("components.executionError.showQuery.show"), DQLIcon.QUERY_USED);
        showQueryButton.setAlignmentX(CENTER_ALIGNMENT);
        showQueryButton.addActionListener(e -> {
            boolean nowVisible = splitter.getSecondComponent() != null;
            if (!nowVisible) {
                splitter.setSecondComponent(queryViewer);
                queryViewer.showResult(() -> query);
            } else {
                splitter.setSecondComponent(null);
            }

            showQueryButton.setText(nowVisible ?
                    DQLBundle.message("components.executionError.showQuery.show")
                    : DQLBundle.message("components.executionError.showQuery.hide")
            );
        });
        return showQueryButton;
    }

    private static String getErrorMessage(@NotNull Exception exception) {
        return switch (exception) {
            case DQLErrorResponseException error ->
                    DQLBundle.message("components.executionError.errors.executionFailed", error.getApiMessage());
            case DQLNotAuthorizedException error ->
                    DQLBundle.message("components.executionError.errors.unauthorized", error.getApiMessage());
            case DQLResponseParsingException error ->
                    DQLBundle.message("components.executionError.errors.unparseableResponse", error.getApiMessage());
            case DQLResponseRedirectedException error ->
                    DQLBundle.message("components.executionError.errors.requestRedirected", error.getApiMessage());
            case InterruptedException ignored ->
                    DQLBundle.message("components.executionError.errors.executionCancelled", exception.getMessage());
            default -> DQLBundle.message("components.executionError.errors.unexpectedError", exception.getMessage());
        };
    }

    private static @NotNull List<String> getErrorDetails(@NotNull Exception exception) {
        if (!(exception instanceof DQLDetailedErrorException detailedError)) {
            return List.of();
        }
        List<String> printedDetails = new ArrayList<>();
        if (detailedError.getResponse() != null && detailedError.getResponse().error != null && detailedError.getResponse().error.code() != null) {
            printedDetails.add(DQLBundle.message("error.detailed.errorCode", detailedError.getResponse().error.code()));
        }
        Map<String, Object> details = detailedError.getErrorDetails();
        if (details.get("queryId") instanceof String id) {
            printedDetails.add(DQLBundle.message("error.detailed.queryId", id));
        }
        if (details.get("errorRef") instanceof String id) {
            printedDetails.add(DQLBundle.message("error.detailed.errorRef", id));
        }
        if (details.get("traceId") instanceof String id) {
            printedDetails.add(DQLBundle.message("error.detailed.traceId", id));
        }
        if (details.get("syntaxErrorPosition") instanceof Map<?, ?> position) {
            if (position.get("start") instanceof Map<?, ?> start && position.get("end") instanceof Map<?, ?> end) {
                printedDetails.add(DQLBundle.message("error.detailed.column", getRangeSummary(start.get("column"), end.get("column"))));
                printedDetails.add(DQLBundle.message("error.detailed.line", getRangeSummary(start.get("line"), end.get("line"))));
            }
        }

        return printedDetails;
    }

    private static @NotNull JComponent createHtmlField(@NotNull String htmlText) {
        JTextPane field = new JTextPane() {
            @Override
            public Dimension getMaximumSize() {
                Container parent = getParent();
                if (parent != null) {
                    return new Dimension((int) (parent.getWidth() * 0.9), getPreferredSize().height);
                }
                return super.getMaximumSize();
            }
        };
        field.setContentType("text/html");
        field.setText("<html><body>" + htmlText + "</body></html>");
        field.setEditable(false);
        field.setFont(JBUI.Fonts.label());
        Font font = JBUI.Fonts.label();
        HTMLDocument document = (HTMLDocument) field.getDocument();
        String bodyRule = "body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; }";
        document.getStyleSheet().addRule(bodyRule);
        document.getStyleSheet().addRule("p { text-align: center; }");
        field.setOpaque(false);
        field.setFocusable(true);
        field.setAlignmentX(JTextPane.CENTER_ALIGNMENT);
        field.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        return new TransparentScrollPane(field, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
    }

    private static @NotNull String getRangeSummary(@Nullable Object start, @Nullable Object end) {
        if (Objects.equals(start, end)) {
            return String.valueOf(start);
        }
        return start + " - " + end;
    }

    @Override
    public void dispose() {
        if (queryViewer != null) {
            queryViewer.dispose();
        }
    }
}
