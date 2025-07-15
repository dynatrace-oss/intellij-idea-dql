package pl.thedeem.intellij.dql.definition;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface DQLDefinitionLoader {
   static DQLDefinitionLoader getInstance() {
      return ApplicationManager.getApplication().getService(DQLDefinitionLoader.class);
   }

   @NotNull Map<String, DQLCommandDefinition> loadCommands();
   @NotNull Map<String, DQLFunctionDefinition> loadFunctions();
   @NotNull List<DQLParameterDefinition> loadTimeSeriesParameters();
   @NotNull Map<IElementType, DQLOperationTarget> loadOperations();
}
