package pl.thedeem.intellij.dql.annotator;

import pl.thedeem.intellij.common.annotator.AnnotatorEngine;
import pl.thedeem.intellij.common.annotator.BaseAnnotator;
import pl.thedeem.intellij.dql.annotator.highlights.*;

import java.util.List;


final class DQLAnnotator extends BaseAnnotator {
    public static final List<AnnotatorEngine> ENGINES = List.of(
            new HighlightStatementKeywords(),
            new HighlightSortDirection(),
            new HighlightFunctions(),
            new HighlightEnumValues(),
            new HighlightFields(),
            new HighlightOperatorKeywords(),
            new HighlightDurations(),
            new HighlightTimeAlignmentExpressions()
    );

    @Override
    public List<AnnotatorEngine> getEngines() {
        return ENGINES;
    }
}
