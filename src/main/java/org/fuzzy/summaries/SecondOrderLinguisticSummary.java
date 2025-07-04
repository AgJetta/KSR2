package org.fuzzy.summaries;

import org.fuzzy.FuzzySet;
import org.fuzzy.SongRecord;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;

import java.util.List;

public class SecondOrderLinguisticSummary extends LinguisticSummary{
    private Summarizer qualifier;

    public SecondOrderLinguisticSummary(Quantifier quantifier, String predicate,
                                        Summarizer summarizer, Summarizer qualifier) {
        super(quantifier, predicate, summarizer);
        this.qualifier = qualifier;
    }

    @Override
    public double calculateT1(List<SongRecord> dataset) {
        if (dataset.isEmpty()) {
            System.err.println("Can't calculate a measure for an empty dataset!");
            System.exit(1);
        }

        // Calculate r - sum of membership degrees in summarizer
        FuzzySet intersection = summarizer.getFuzzySet().intersection(
                qualifier.getFuzzySet());
        double upperSum = intersection.cardinality();
        double lowerSum = qualifier.getFuzzySet().cardinality();
        double r = upperSum / lowerSum;

        // Calculate quantifier membership based on r and m
        return quantifier.getMembership(r, 1);
    }

    @Override
    public double calculateT3(List<SongRecord> dataset){
        FuzzySet intersection = summarizer.getFuzzySet().intersection(
                qualifier.getFuzzySet());
        double t = intersection.support().cardinality();
        double h = dataset.size();
        return t / h;
    }

    @Override
    public String generateSummary() {
        return String.format("%s %s that are/having %s are/have %s",
                quantifier.getName(),
                predicate,
                qualifier.getName(),
                summarizer.getName());
    }
}
