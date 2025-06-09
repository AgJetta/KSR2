package org.fuzzy.summaries;

import org.fuzzy.SongRecord;
import org.fuzzy.Universe;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.*;

import java.util.*;


// First-order linguistic summary
public class LinguisticSummary {
    protected final Quantifier quantifier;
    protected final String predicate;
    protected final Summarizer summarizer;

    public LinguisticSummary(Quantifier quantifier, String predicate, Summarizer summarizer) {
        this.quantifier = quantifier;
        this.predicate = predicate;
        this.summarizer = summarizer;
    }

    public Quantifier getQuantifier() {
        return quantifier;
    }

    public String getPredicate() {
        return predicate;
    }

    public Summarizer getSummarizer() {
        return summarizer;
    }

    // Calculate T1 (degree of truth)
    public double calculateT1(List<SongRecord> dataset) {
        if (dataset.isEmpty()) {
            System.err.println("Can't calculate a measure for an empty dataset!");
            System.exit(1);
        }

        // Calculate r - sum of membership degrees in summarizer
        double r = summarizer.calculateR(dataset);
        int m = dataset.size();

        // Calculate quantifier membership based on r and m
        return quantifier.getMembership(r, m);
    }

    public double calculateT2(List<SongRecord> dataset){
        if (dataset.isEmpty()) {
            System.err.println("Can't calculate a measure for an empty dataset!");
            System.exit(1);
        }

        // Jak dla Second Order???
        double summarizerFuzziness = summarizer.getFuzzySet().degreeOfFuzziness();
        double product = Math.pow(summarizerFuzziness, 1); // placeholder for future compound summarizer

        return (1 - product);
    }

    public double calculateT3(List<SongRecord> dataset){
        double t = summarizer.getFuzzySet().support().cardinality();
        double h = dataset.size();

        return t / h;
    }

    public double calculateT5(List<SongRecord> dataset){
        double base = (1./2.);
        double exponent = Math.pow(base, 1);
        return 2 * exponent;
    }

    // Generate natural language summary
    public String generateSummary() {
        return String.format("%s %s są/mają [%s %s]",
                quantifier.getName(),
                predicate,
                summarizer.getName(),
                summarizer.linguisiticVariable
        );
    }

    // Generate summary with T1 value
    public String generateSummaryWithMeasures(List<SongRecord> dataset) {
        double t1 = calculateT1(dataset);
        double t2 = calculateT2(dataset);
        double t3 = calculateT3(dataset);
        double t4 = calculateT4(dataset);
        double t5 = calculateT5(dataset);
        double t6 = calculateT6(dataset);
        double t7 = calculateT7(dataset);
        double t8 = calculateT8(dataset);
        double t9 = calculateT9(dataset);
        double t10 = calculateT10(dataset);
        return String.format("%s" + "(T1: %.7f | T2: %.7f | T3: %.7f | T4: %.7f | T5: %.7f | T6: %.7f | T7: %.7f | T8: %.7f | T9: %.7f | T10: %.7f)",
                generateSummary(), t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }

    public double calculateT4(List<SongRecord> dataset) {
        double t = summarizer.getFuzzySet().support().cardinality();
        double h = dataset.size();

        return (t / h) - calculateT3(dataset);
    }

    public double calculateT6(List<SongRecord> dataset) {
        double quantifierSupportCardinality = quantifier.getFuzzySet().support().cardinality();
        Universe universe = quantifier.getFuzzySet().getUniverse();
        double universeSize = universe.getLength();

        return 1 - (quantifierSupportCardinality / universeSize);
    }

    public double calculateT7(List<SongRecord> dataset) {
        // How cardinality of a quantifier is supposed to be less than 1 ??!!!
        double quantifierCardinality = 0.0;
        for (SongRecord record : dataset) {
            double membership = quantifier.getFuzzySet().getMembership(record.getAttribute(quantifier.getName()));
            quantifierCardinality += membership;
        }
        Universe universe = quantifier.getFuzzySet().getUniverse();
        double universeSize = universe.getLength();

        return 1 - (quantifierCardinality / universeSize);
    }

    public double calculateT8(List<SongRecord> dataset) {
        double summarizerCardinality = 0.0;
        for (SongRecord record : dataset) {
            double membership = summarizer.getFuzzySet().getMembership(record.getAttribute(summarizer.getName()));
            summarizerCardinality += membership;
        }
        Universe universe = summarizer.getFuzzySet().getUniverse();
        double universeSize = universe.getLength();
        return 1 - (summarizerCardinality / universeSize);
    }

    public double calculateT9(List<SongRecord> dataset) {
        return 0.0; // First-order summary doesn't have a qualifier
    }

    public double calculateT10(List<SongRecord> dataset) {
        return 0.0; // First-order summary doesn't have a qualifier
    }





    @Override
    public String toString() {
        return generateSummary();
    }

}

// Example usage and testing
