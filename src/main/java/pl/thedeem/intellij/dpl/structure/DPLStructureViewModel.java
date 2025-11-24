package pl.thedeem.intellij.dpl.structure;

import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.psi.*;

public class DPLStructureViewModel extends StructureViewModelBase implements com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider {
    public DPLStructureViewModel(@Nullable Editor editor, @NotNull PsiFile psiFile) {
        super(psiFile, editor, new DPLStructureViewElement(psiFile));
    }

    @NotNull
    public Sorter @NotNull [] getSorters() {
        return new Sorter[]{Sorter.ALPHA_SORTER};
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement structureViewTreeElement) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        return element.getValue() instanceof DPLFieldName
                || element.getValue() instanceof DPLVariable
                || element.getValue() instanceof DPLSimpleExpression
                || element.getValue() instanceof DPLQuantifierLimit
                || element.getValue() instanceof DPLCommandKeyword;
    }

    @Override
    protected Class<?> @NotNull [] getSuitableClasses() {
        return new Class[]{
                DPLExpressionDefinition.class,
                DPLMacroDefinitionExpression.class,
                DPLExpression.class
        };
    }
}
