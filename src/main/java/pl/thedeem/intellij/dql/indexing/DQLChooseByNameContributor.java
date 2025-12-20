package pl.thedeem.intellij.dql.indexing;

import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;

import java.util.List;

public class DQLChooseByNameContributor implements ChooseByNameContributorEx {
    @Override
    public void processNames(@NotNull Processor<? super String> processor, @NotNull GlobalSearchScope scope, @Nullable IdFilter idFilter) {
        Project project = scope.getProject();
        if (project == null) {
            return;
        }
        List<String> dqlFields = ContainerUtil.map(DQLUtil.findFieldsInProject(project), DQLFieldExpression::getName);
        ContainerUtil.process(dqlFields, processor);
    }

    @Override
    public void processElementsWithName(@NotNull String name, @NotNull Processor<? super NavigationItem> processor, @NotNull FindSymbolParameters parameters) {
        List<NavigationItem> properties = ContainerUtil.map(DQLUtil.findFieldsInProject(parameters.getProject(), name), property -> (NavigationItem) property);
        ContainerUtil.process(properties, processor);
    }
}
