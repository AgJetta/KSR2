package org.fuzzy.summaries;

import org.fuzzy.FuzzySet;
import org.fuzzy.SongRecord;
import org.fuzzy.Universe;
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

        if (!quantifier.isRelative()) {
            System.err.println("Second order summaries only support relative quantifiers!");
            System.exit(1);
        }

        // Calculate r - sum of membership degrees in summarizer
        FuzzySet intersection = summarizer.getFuzzySet().intersection(
                qualifier.getFuzzySet());
        double upperSum = intersection.cardinalNumber();
        double lowerSum = qualifier.getFuzzySet().cardinalNumber();
        double r = upperSum / lowerSum;

        // Calculate quantifier membership based on r and m
        return quantifier.getMembership(r, 1);
    }

    @Override
    public double calculateT3(List<SongRecord> dataset){
        FuzzySet intersection = summarizer.getFuzzySet().intersection(
                qualifier.getFuzzySet());
        double t = intersection.support().cardinalNumber();
        double h = qualifier.getFuzzySet().support().cardinalNumber();
        double t3 = t / h;
        return t3;
    }

    @Override
    public double calculateT9(List<SongRecord> dataset) {
        double product = qualifier.getFuzzySet().degreeOfFuzziness();
        return 1 - product;
    }

    @Override
    public double calculateT10(List<SongRecord> dataset) {
        double qualifierCardinality = 0.0;
        for (SongRecord record : dataset) {
            double membership = qualifier.getFuzzySet().getMembership(record.getAttribute(qualifier.getFieldName()));
            qualifierCardinality += membership;
        }
        Universe universe = qualifier.getFuzzySet().getUniverse();
        double universeSize = universe.getCardinalNumber();
        double ratio = qualifierCardinality / universeSize;

        return 1 - ratio;
    }

    @Override
    public double calculateT11(List<SongRecord> dataset) {
        double base = 0.5; // Placeholder for base value
        double exponent = Math.pow(base, 1); // Placeholder for exponent
        return 2 * exponent;
    }

    @Override
    public String generateSummary() {
        return String.format("%s %s które są/mają [%s %s] są/mają [%s %s]",
                quantifier.getName(),
                predicate,
                qualifier.getName(),
                qualifier.linguisiticVariable,
                summarizer.getName(),
                summarizer.linguisiticVariable);
    }
}
