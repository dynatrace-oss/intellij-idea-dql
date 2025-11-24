package pl.thedeem.intellij.dpl.indexing;

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
import pl.thedeem.intellij.dpl.DPLUtil;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;

import java.util.List;
import java.util.Objects;

public class DPLChooseByNameContributor implements ChooseByNameContributorEx {
    @Override
    public void processNames(@NotNull Processor<? super String> processor, @NotNull GlobalSearchScope scope, @Nullable IdFilter idFilter) {
        Project project = Objects.requireNonNull(scope.getProject());
        List<String> fields = ContainerUtil.map(DPLUtil.findFields(project), DPLFieldName::getName);
        ContainerUtil.process(fields, processor);
    }

    @Override
    public void processElementsWithName(@NotNull String name, @NotNull Processor<? super NavigationItem> processor, @NotNull FindSymbolParameters parameters) {
        List<NavigationItem> properties = ContainerUtil.map(DPLUtil.findFields(parameters.getProject(), name), property -> (NavigationItem) property);
        ContainerUtil.process(properties, processor);
    }
}
