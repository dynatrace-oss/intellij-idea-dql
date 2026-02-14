package pl.thedeem.intellij.dql;

import com.intellij.icons.AllIcons;
import pl.thedeem.intellij.common.Icons;

import javax.swing.*;

public interface DQLIcon extends Icons {
    Icon DQL_FIELD = AllIcons.Nodes.Field;
    Icon DQL_OPERATOR = AllIcons.Nodes.Lambda;
    Icon DQL_FUNCTION = AllIcons.Nodes.Function;
    Icon DQL_QUERY_COMMAND = AllIcons.Nodes.Class;
    Icon DQL_VARIABLE = AllIcons.Nodes.Variable;
    Icon DQL_STATEMENT_PARAMETER = AllIcons.Nodes.Parameter;
    Icon DQL_BOOLEAN = AllIcons.Nodes.Static;
    Icon DQL_TIME_FIELD = AllIcons.Debugger.Watch;
    Icon NULL = AllIcons.Nodes.C_private;
    Icon DQL_SORT_DIRECTION = AllIcons.Nodes.Target;
    Icon DQL_NUMBER = AllIcons.Nodes.PropertyReadStatic;
    Icon DQL_STRING = AllIcons.Nodes.Word;
    Icon DQL_OPERAND = AllIcons.Nodes.AbstractMethod;
    Icon DQL_EXPRESSION = AllIcons.Nodes.Method;
    Icon DQL_SUBQUERY = AllIcons.Gutter.RecursiveMethod;
    Icon DQL_ARRAY = AllIcons.Debugger.Db_array;
    Icon DQL_RECORD = AllIcons.FileTypes.Json;
    Icon INTENTION = AllIcons.Actions.Edit;
    Icon GUTTER_EXECUTE_DQL = Icons.scaleToBottomRight(AllIcons.Actions.Execute, DYNATRACE_LOGO, 0.5f);
    Icon GUTTER_EXECUTE_SETTINGS = Icons.scaleToBottomRight(AllIcons.General.GearPlain, DYNATRACE_LOGO, 0.5f);
    Icon QUERY_CONSOLE = Icons.scaleToBottomRight(AllIcons.General.ProjectTab, DYNATRACE_LOGO, 0.5f);
    Icon MANAGE_TENANTS = Icons.scaleToBottomRight(AllIcons.Actions.Annotate, DYNATRACE_LOGO, 0.5f);
    Icon EXTERNAL_VALIDATION_ENABLED = Icons.scaleToBottomRight(AllIcons.General.InspectionsOK, AllIcons.Actions.Lightning, 0.5f);
}
