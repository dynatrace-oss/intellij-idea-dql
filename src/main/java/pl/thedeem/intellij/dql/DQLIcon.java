package pl.thedeem.intellij.dql;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class DQLIcon {
    public static final Icon DYNATRACE_LOGO = IconLoader.getIcon("/icons/dynatrace.png", DQLIcon.class);
    public static final Icon DQL_FIELD = AllIcons.Nodes.Field;
    public static final Icon DQL_OPERATOR = AllIcons.Nodes.Lambda;
    public static final Icon DQL_FUNCTION = AllIcons.Nodes.Function;
    public static final Icon DQL_QUERY_COMMAND = AllIcons.Nodes.Class;
    public static final Icon DQL_VARIABLE = AllIcons.Nodes.Variable;
    public static final Icon DQL_STATEMENT_PARAMETER = AllIcons.Nodes.Parameter;
    public static final Icon DQL_BOOLEAN = AllIcons.Nodes.Static;
    public static final Icon DQL_TIME_FIELD = AllIcons.Debugger.Watch;
    public static final Icon NULL = AllIcons.Nodes.C_private;
    public static final Icon DQL_SORT_DIRECTION = AllIcons.Nodes.Target;
    public static final Icon DQL_NUMBER = AllIcons.Nodes.PropertyReadStatic;
    public static final Icon DQL_STRING = AllIcons.Nodes.Word;
    public static final Icon DQL_OPERAND = AllIcons.Nodes.AbstractMethod;
    public static final Icon DQL_EXPRESSION = AllIcons.Nodes.Method;
    public static final Icon DQL_SUBQUERY = AllIcons.Gutter.RecursiveMethod;
    public static final Icon DQL_ARRAY = AllIcons.Debugger.Db_array;
    public static final Icon DQL_RECORD = AllIcons.FileTypes.Json;
}
