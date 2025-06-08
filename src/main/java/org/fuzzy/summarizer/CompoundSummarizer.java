package org.fuzzy.summarizer;

import org.fuzzy.FuzzySet;
import org.fuzzy.LogicalConnective;

public class CompoundSummarizer extends Summarizer{

    FuzzySet setA;
    FuzzySet setB;

    public CompoundSummarizer(String name, String linguisticVariableName, FuzzySet fuzzySet) {
        super(name, linguisticVariableName, fuzzySet);
    }

    // Constructor taking in a Summarizer returns NOT Summarizer
    public CompoundSummarizer(Summarizer summarizer) {
        super(
                LogicalConnective.NOT.name() + " " + summarizer.getName(),
                summarizer.getFieldName(),
                LogicalConnective.NOT.apply(summarizer.getFuzzySet())
        );
    }

    // Constructor taking in a Summarizer A and Summarizer B and a logical connective, returns Compound Summarizer with AND or OR
    public CompoundSummarizer(Summarizer summarizerA, Summarizer summarizerB, LogicalConnective connective) {
        super(
                summarizerA.getName() + " " + connective.name() + " " + summarizerB.getName(),
                "Compound Summarizer",
                connective.apply(summarizerA.getFuzzySet(), summarizerB.getFuzzySet())
        );
    }
}
