package pl.thedeem.intellij.dql.components;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;
import java.text.ParseException;

public class DQLQueryExecutionComponent {
  private final JFormattedTextField defaultScanLimit;
  private final JFormattedTextField maxResultBytes;
  private final JFormattedTextField maxResultRecords;
  private final JBTextField timeframeStart;
  private final JBTextField timeframeEnd;
  private final JPanel myPanel;

  public DQLQueryExecutionComponent() {
    defaultScanLimit = new JFormattedTextField(createNumberFormat());
    maxResultBytes = new JFormattedTextField(createNumberFormat());
    maxResultRecords = new JFormattedTextField(createNumberFormat());
    timeframeStart = new JBTextField();
    timeframeEnd = new JBTextField();

    defaultScanLimit.setComponentPopupMenu(DQLComponentUtils.createDefaultPopupMenu());
    maxResultBytes.setComponentPopupMenu(DQLComponentUtils.createDefaultPopupMenu());
    maxResultRecords.setComponentPopupMenu(DQLComponentUtils.createDefaultPopupMenu());
    timeframeStart.setComponentPopupMenu(generateTimestampPopupMenu(timeframeStart));
    timeframeEnd.setComponentPopupMenu(generateTimestampPopupMenu(timeframeEnd));

    myPanel = FormBuilder.createFormBuilder()
        .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.defaultScanLimit"), defaultScanLimit)
        .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.maxResultBytes"), maxResultBytes)
        .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.maxResultRecords"), maxResultRecords)
        .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.timeframeStart"), timeframeStart)
        .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.timeframeEnd"), timeframeEnd)
        .getPanel();
  }

  private NumberFormatter createNumberFormat() {
    NumberFormat format = NumberFormat.getIntegerInstance();
    NumberFormatter formatter = new NumberFormatter(format) {
      @Override
      public Object stringToValue(String text) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
          return null;
        }
        return super.stringToValue(text);
      }
    };
    formatter.setValueClass(Long.class);
    formatter.setAllowsInvalid(true);
    formatter.setMinimum(null);
    formatter.setMaximum(Long.MAX_VALUE);
    formatter.setCommitsOnValidEdit(true);
    return formatter;
  }

  public @NotNull JComponent getPanel() {
    return myPanel;
  }

  private JPopupMenu generateTimestampPopupMenu(JBTextField field) {
    JPopupMenu popupMenu = DQLComponentUtils.createDefaultPopupMenu();

    popupMenu.addSeparator();
    JMenuItem currentTimestampAction = new JMenuItem(DQLBundle.message("components.queryExecution.actions.getCurrentTimestamp"));
    currentTimestampAction.setIcon(AllIcons.General.SelectIn);
    currentTimestampAction.setBorder(DQLComponentUtils.DEFAULT_BORDER);
    currentTimestampAction.addActionListener(e -> field.setText(DQLUtil.getCurrentTimeTimestamp()));

    popupMenu.add(currentTimestampAction);
    return popupMenu;
  }

  public void setDefaultScanLimit(Long defaultScanLimit) {
    this.defaultScanLimit.setValue(defaultScanLimit);
  }

  public Long getDefaultScanLimit() {
    return (Long) this.defaultScanLimit.getValue();
  }

  public void setMaxResultBytes(Long maxResultBytes) {
    this.maxResultBytes.setValue(maxResultBytes);
  }

  public Long getMaxResultBytes() {
    return (Long) this.maxResultBytes.getValue();
  }

  public void setMaxResultRecords(Long maxResultRecords) {
    this.maxResultRecords.setValue(maxResultRecords);
  }

  public Long getMaxResultRecords() {
    return (Long) this.maxResultRecords.getValue();
  }

  public void setTimeframeStart(String timeframeStart) {
    this.timeframeStart.setText(timeframeStart);
  }

  public String getTimeframeStart() {
    return this.timeframeStart.getText();
  }

  public void setTimeframeEnd(String timeframeEnd) {
    this.timeframeEnd.setText(timeframeEnd);
  }

  public String getTimeframeEnd() {
    return this.timeframeEnd.getText();
  }
}
