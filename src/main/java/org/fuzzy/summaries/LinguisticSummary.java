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

    public void setMeasureWeights(List<Double> measureWeights) {
        if (measureWeights.size() != 10) {
            throw new IllegalArgumentException("Measure weights must contain exactly 10 values.");
        }
        // Should sum up to 1
        double sum = measureWeights.stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - 1.0) > 1e-9) {
            throw new IllegalArgumentException("Measure weights must sum up to 1.");
        }
        this.measureWeights = measureWeights;
    }

    protected static List<Double> measureWeights = Arrays.asList(0.2, 0.05, 0.05, 0.2, 0.05, 0.05, 0.1, 0.1, 0.1, 0.1);

    protected String summaryType = "1S F1";

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
        double t2 = (1 - product);
        // debug
        if (0 > t2 || t2 > 1) {
            System.err.println("t2 = " + t2 + " is not in the range (0, 1]!");
            System.err.println("Summarizer Fuzziness= " + summarizerFuzziness + " product = " + product);
        }


        return t2;
    }

    public double calculateT3(List<SongRecord> dataset){
        double t = dataset.size();
        double h = dataset.size();
        return t / h;
    }

    public double calculateT4(List<SongRecord> dataset) {
        double g = summarizer.getFuzzySet().support().cardinalNumber();
        double m = dataset.size();
//        double universeSize = summarizer.getFuzzySet().getUniverse().getEnd() - summarizer.getFuzzySet().getUniverse().getStart();
//        double m = universeSize;
        double r = g / m;

        // TODO - Summation of "r" for every element in a complex summarizer (we don't have one yet)
        double t4 = Math.abs(r - calculateT3(dataset));
        // debug
        if (t4 > 1) {
            System.err.println("g = " + g + ", m = " + m + ", r = " + r + ", t3 = " + calculateT3(dataset) + ", t4 = " + t4);
            throw new RuntimeException("T4 value is greater than 1, which is not expected!");
        }
        return t4;
    }

    public double calculateT5(List<SongRecord> dataset){
        double base = (1./2.);
        double exponent = Math.pow(base, 1);
        return 2 * exponent;
    }

    public double calculateT6(List<SongRecord> dataset) {
        double quantifierSupportCardinality = quantifier.getSupportCardinalNumber();
        Universe universe = quantifier.getFuzzySet().getUniverse();
        double m = quantifier.isRelative() ? 1.0 : universe.getCardinalNumber();
        double t6 = 1 - (quantifierSupportCardinality / m);
        if (0 > t6 || t6 > 1 || Double.isNaN(t6)) {
            System.err.println("t6 = " + t6 + " is not in the range (0, 1]!");
            System.err.println("Quantifier Support Cardinality = " + quantifierSupportCardinality + ", m = " + m);
        }
        return t6;
    }

    public double calculateT7(List<SongRecord> dataset) {

        // How cardinality of a quantifier is supposed to be less than 1 ??!!!
        double quantifierCardinality = 0.0;
        for (SongRecord record : dataset) {
            double membership = quantifier.getFuzzySet().getMembership(record.getAttribute(quantifier.getFieldName()));
            quantifierCardinality += membership;
        }
        Universe universe = quantifier.getFuzzySet().getUniverse();
        double universeSize = universe.getCardinalNumber();

        return 1 - (quantifierCardinality / universeSize);
    }

    public double calculateT8(List<SongRecord> dataset) {
        double summarizerCardinality = 0.0;
        for (SongRecord record : dataset) {
            double membership = summarizer.getFuzzySet().getMembership(record.getAttribute(summarizer.getFieldName()));
            summarizerCardinality += membership;
        }
        Universe universe = summarizer.getFuzzySet().getUniverse();
        double universeSize = universe.getCardinalNumber();
        return 1 - (summarizerCardinality / universeSize);
    }

    public double calculateT9(List<SongRecord> dataset) {
        return 0.0;
    }

    public double calculateT10(List<SongRecord> dataset) {
        return 0.0;
    }

    public double calculateT11(List<SongRecord> dataset) {
        return 1.0;
    }

    public double calculateOptimal(List<SongRecord> dataset){
        return calculateT1(dataset) * measureWeights.get(0) +
                calculateT2(dataset) * measureWeights.get(1) +
                calculateT3(dataset) * measureWeights.get(2) +
                calculateT4(dataset) * measureWeights.get(3) +
                calculateT5(dataset) * measureWeights.get(4) +
                calculateT6(dataset) * measureWeights.get(5) +
                calculateT7(dataset) * measureWeights.get(6) +
                calculateT8(dataset) * measureWeights.get(7) +
                calculateT9(dataset) * measureWeights.get(8) +
                calculateT10(dataset) * measureWeights.get(9);
    }

    // Generate natural language summary
    public String generateSummary() {
        return String.format("%s | %s %s są/mają [%s %s]",
                summaryType,
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
        double t11 = calculateT11(dataset);
        double optimal = calculateOptimal(dataset);

        String summaryString = generateSummary();
        return String.format("%-113s" + " %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f",
                summaryString, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, optimal);
    }

    @Override
    public String toString() {
        return generateSummary();
    }

    public void printLatexFuzzySummaryResults(List<SongRecord> dataset) {

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
        double t11 = calculateT11(dataset);
        double optimal = calculateOptimal(dataset);

        String summaryString = generateSummary();
        System.out.printf("%s", summaryString);
        System.out.printf(" & %.4f", t1);
        System.out.printf(" & %.4f", t2);
        System.out.printf(" & %.4f", t3);
        System.out.printf(" & %.4f", t4);
        System.out.printf(" & %.4f", t5);
        System.out.printf(" & %.4f", t6);
        System.out.printf(" & %.4f", t7);
        System.out.printf(" & %.4f", t8);
        System.out.printf(" & %.4f", t9);
        System.out.printf(" & %.4f", t10);
        System.out.printf(" & %.4f", t11);
        System.out.printf(" & %.4f", optimal);
        System.out.printf(" \\\\ %n");
        System.out.println("\\midrule");
    }


}

