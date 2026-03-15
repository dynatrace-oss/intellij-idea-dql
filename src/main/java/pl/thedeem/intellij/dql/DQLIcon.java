package pl.thedeem.intellij.dql;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
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

    Icon QUERY_CONSOLE = IconLoader.getIcon("/icons/dt-execute.svg", Icons.class);
    Icon DT_SETTINGS = IconLoader.getIcon("/icons/dt-settings.svg", Icons.class);
    Icon DT_TENANT = IconLoader.getIcon("/icons/dt-tenant.svg", Icons.class);
    Icon QUERY_METADATA = IconLoader.getIcon("/icons/query-metadata.svg", Icons.class);
    Icon QUERY_USED = IconLoader.getIcon("/icons/used-query.svg", Icons.class);

    Icon EXTERNAL_VALIDATION_ENABLED = Icons.scaleToBottomRight(AllIcons.General.InspectionsOK, AllIcons.Actions.Lightning, 0.5f);

    Icon LINE_CHART = IconLoader.getIcon("/icons/charts/line-chart.svg", Icons.class);
    Icon BAR_CHART = IconLoader.getIcon("/icons/charts/bar-chart.svg", Icons.class);
    Icon PIE_CHART = IconLoader.getIcon("/icons/charts/pie-chart.svg", Icons.class);
    Icon LEGEND_SHOW = AllIcons.Actions.Show;
    Icon LEGEND_HIDE = IconLoader.getIcon("/icons/charts/legend-hide.svg", Icons.class);
    Icon LEGEND_HIDE_OTHERS = IconLoader.getIcon("/icons/charts/legend-hide-others.svg", Icons.class);
    Icon LEGEND_SHOW_ALL = IconLoader.getIcon("/icons/charts/legend-show-all.svg", Icons.class);
}
