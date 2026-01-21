package pl.thedeem.intellij.dql.exec.panel;

import com.intellij.icons.AllIcons;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.InformationComponent;
import pl.thedeem.intellij.common.components.TransparentScrollPane;
import pl.thedeem.intellij.common.sdk.errors.*;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

public class DQLExecutionErrorPanel extends BorderLayoutPanel {
    protected DQLExecutionErrorPanel() {
        super();
        setOpaque(false);
        setBorder(JBUI.Borders.empty());
    }

    public DQLExecutionErrorPanel(@NotNull Exception e) {
        this(getErrorMessage(e), getErrorDetails(e));
    }

    public DQLExecutionErrorPanel(@NotNull String message) {
        this(message, List.of());
    }

    public DQLExecutionErrorPanel(@NotNull String message, @NotNull List<String> details) {
        this();
        JPanel errorPanel = new JPanel();
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
        errorPanel.setOpaque(false);
        errorPanel.setBorder(JBUI.Borders.empty());
        InformationComponent header = new InformationComponent(DQLBundle.message("error.detailed.title"), AllIcons.General.Error);
        header.setAlignmentX(InformationComponent.CENTER_ALIGNMENT);
        errorPanel.add(header);
        errorPanel.add(Box.createVerticalStrut(1));
        errorPanel.add(createHtmlField("<p>" + message + "</p>"));

        if (!details.isEmpty()) {
            errorPanel.add(Box.createVerticalStrut(1));
            errorPanel.add(createHtmlField("<ul>" + String.join("", details.stream().map(p -> "<li>" + p + "</li>").toList()) + "</ul>"));
        }

        JPanel centeringWrapper = new JPanel(new GridBagLayout());
        centeringWrapper.setOpaque(false);
        centeringWrapper.add(errorPanel, new GridBagConstraints());
        addToCenter(new TransparentScrollPane(centeringWrapper));
    }

    private static String getErrorMessage(@NotNull Exception exception) {
        return switch (exception) {
            case DQLErrorResponseException error ->
                    DQLBundle.message("runConfiguration.executeDQL.errors.execution", error.getApiMessage());
            case DQLNotAuthorizedException error ->
                    DQLBundle.message("runConfiguration.executeDQL.errors.unauthorized", error.getApiMessage());
            case DQLResponseParsingException error ->
                    DQLBundle.message("runConfiguration.executeDQL.errors.parsing", error.getApiMessage());
            case DQLResponseRedirectedException error ->
                    DQLBundle.message("runConfiguration.executeDQL.errors.redirected", error.getApiMessage());
            case InterruptedException ignored ->
                    DQLBundle.message("runConfiguration.executeDQL.indicator.cancelled", exception.getMessage());
            default -> DQLBundle.message("runConfiguration.executeDQL.errors.unknown", exception.getMessage());
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
}
