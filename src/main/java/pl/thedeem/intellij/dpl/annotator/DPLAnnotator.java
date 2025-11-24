package pl.thedeem.intellij.dpl.annotator;

import pl.thedeem.intellij.common.annotator.AnnotatorEngine;
import pl.thedeem.intellij.common.annotator.BaseAnnotator;
import pl.thedeem.intellij.dpl.annotator.highlights.*;

import java.util.List;


final class DPLAnnotator extends BaseAnnotator {
    public static final List<AnnotatorEngine> ENGINES = List.of(
            new HighlightFields(),
            new HighlightKeywords(),
            new HighlightConfigurationParameters()
    );

    @Override
    public List<AnnotatorEngine> getEngines() {
        return ENGINES;
    }
}
