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
        return String.format("%s %s are/have %s",
                quantifier.getName(),
                predicate,
                summarizer.getName());
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
        return String.format("%s" + "(T1: %.7f | T2: %.7f | T3: %.7f | T4: %.7f | T5: %.7f | T6: %.7f | T7: %.7f)",
                generateSummary(), t1, t2, t3, t4, t5, t6, t7);
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
        double quantifierCardinality = quantifier.getFuzzySet().cardinality();
        Universe universe = quantifier.getFuzzySet().getUniverse();
        double universeSize = universe.getLength();

        return 1 - (quantifierCardinality / universeSize);
    }



    @Override
    public String toString() {
        return generateSummary();
    }

}

// Example usage and testing
