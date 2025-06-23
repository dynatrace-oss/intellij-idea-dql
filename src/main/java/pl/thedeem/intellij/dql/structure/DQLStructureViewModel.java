package pl.thedeem.intellij.dql.structure;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.impl.ExpressionOperatorImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DQLStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
    public DQLStructureViewModel(@Nullable Editor editor, @NotNull PsiFile psiFile) {
        super(psiFile, editor, new DQLStructureViewElement(psiFile));
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
        return element.getValue() instanceof DQLFieldExpression
                || element.getValue() instanceof DQLVariableExpression
                || element.getValue() instanceof DQLSimpleExpression;
    }

    @Override
    protected Class<?> @NotNull [] getSuitableClasses() {
        return new Class[]{
                DQLExpression.class,
                ExpressionOperatorImpl.class,
                DQLQuery.class,
                DQLQueryStatement.class
        };
    }
}
