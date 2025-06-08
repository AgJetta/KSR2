package org.fuzzy;

import org.fuzzy.fuzzyQuantifiers.Quantifier;
import org.fuzzy.summarizer.*;

import java.util.*;


// First-order linguistic summary
public class LinguisticSummary {
    private final Quantifier quantifier;
    private final String predicate;
    private final Summarizer summarizer;

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
            return 0.0;
        }

        // Calculate r - sum of membership degrees in summarizer
        double r = summarizer.calculateR(dataset);
        int m = dataset.size();

        // Calculate quantifier membership based on r and m
        return quantifier.getMembership(r, m);
    }

    // Generate natural language summary
    public String generateSummary() {
        return String.format("%s %s are/have %s",
                quantifier.getName(),
                predicate,
                summarizer.getName());
    }

    // Generate summary with T1 value
    public String generateSummaryWithT1(List<SongRecord> dataset) {
        double t1 = calculateT1(dataset);
        return String.format("%s (T1: %.3f)", generateSummary(), t1);
    }

    // TODO: Methods for future T-measures
    public double calculateT2(List<SongRecord> dataset) {
        // TODO: Implement degree of imprecision
        return 0.0;
    }

    public double calculateT3(List<SongRecord> dataset) {
        // TODO: Implement degree of covering
        return 0.0;
    }

    public double calculateT4(List<SongRecord> dataset) {
        // TODO: Implement degree of appropriateness
        return 0.0;
    }

    // TODO: T5-T11 placeholder methods

    @Override
    public String toString() {
        return generateSummary();
    }
}

// Example usage and testing
