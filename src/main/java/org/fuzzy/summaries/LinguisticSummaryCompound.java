package org.fuzzy.summaries;

import org.fuzzy.FuzzySet;
import org.fuzzy.SongRecord;
import org.fuzzy.Universe;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.CompoundSummarizer;
import org.fuzzy.summarizer.Summarizer;

import javax.swing.plaf.ComponentUI;
import java.util.List;

public class LinguisticSummaryCompound extends LinguisticSummary {
    private final CompoundSummarizer compoundSummarizer;

    public LinguisticSummaryCompound(Quantifier quantifier, String predicate, CompoundSummarizer compoundSummarizer) {
        super(quantifier, predicate, new Summarizer("", "",
                new FuzzySet(new Universe(0., 1. , false), MembershipFunctions.triangular(0.5, 0.5, 0.5))));
        this.compoundSummarizer = compoundSummarizer;
    }

    public CompoundSummarizer getCompoundSummarizer() {
        return compoundSummarizer;
    }

    public double calculateT1(List<SongRecord> dataset) {
        double r = 0.0;
        double minMembership = 1.0; // Initialize to maximum possible membership value
        for (SongRecord song : dataset) {
            for (Summarizer summarizer : compoundSummarizer.getSummarizers()) {
                double summarizerMembership = summarizer.getFuzzySet().getMembership(song.getAttribute(summarizer.getFieldName()));
                if (summarizerMembership < minMembership && summarizerMembership > 0) {
                    minMembership = summarizerMembership; // Find the minimum membership across all summarizers
                }
            }
            r += minMembership;
        }
        int m = dataset.size();

        // Calculate quantifier membership based on r and m
        return quantifier.getMembership(r, m);
    }

    public double calculateT2(List<SongRecord> dataset){
        if (dataset.isEmpty()) {
            System.err.println("Can't calculate a measure for an empty dataset!");
            System.exit(1);
        }

        double summarizerFuzziness = 1.0;
        for (Summarizer summarizer : compoundSummarizer.getSummarizers()) {
            summarizerFuzziness *= summarizer.getFuzzySet().degreeOfFuzziness();
        }
        double product = Math.pow(summarizerFuzziness, 1.0 / compoundSummarizer.getSummarizers().size());
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
        double productOfRs = 1.0;
        for (Summarizer summarizer : compoundSummarizer.getSummarizers()) {
            double g = summarizer.getFuzzySet().support().cardinalNumber();
            double m = dataset.size();
            double r = g / m;
            productOfRs *= r; // Multiply the r values for each summarizer
        }

        double t4 = Math.abs(productOfRs - calculateT3(dataset));
//        if (t4 > 1) {
//            System.err.println("g = " + g + ", m = " + m + ", r = " + r + ", t3 = " + calculateT3(dataset) + ", t4 = " + t4);
//            throw new RuntimeException("T4 value is greater than 1, which is not expected!");
//        }
        return t4;
    }

    public double calculateT5(List<SongRecord> dataset){
        double base = (1./2.);
        int power = this.compoundSummarizer.getSummarizers().size();
        double exponent = Math.pow(base, power);
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

        double quantifierCardinality = 0.0;
//        for (SongRecord record : dataset) {
//            double membership = quantifier.getFuzzySet().getMembership(record.getAttribute(quantifier.getFieldName()));
//            quantifierCardinality += membership;
//        }

        quantifierCardinality = quantifier.getEnd() - quantifier.getStart();
        Universe universe = quantifier.getFuzzySet().getUniverse();
        double universeSize = universe.getCardinalNumber();

        return 1 - (quantifierCardinality / universeSize);
    }

    public double calculateT8(List<SongRecord> dataset) {
        double productOfCardinalities = 1.0;
        for (Summarizer summarizer : compoundSummarizer.getSummarizers()) {
            double summarizerCardinality = 0.0;
            for (SongRecord record : dataset) {
                double membership = summarizer.getFuzzySet().getMembership(record.getAttribute(summarizer.getFieldName()));
                summarizerCardinality += membership;
            }
            Universe universe = summarizer.getFuzzySet().getUniverse();
            double universeSize = universe.getCardinalNumber();
            productOfCardinalities *= (summarizerCardinality / universeSize);
        }
        return 1 - Math.pow(productOfCardinalities, 1.0 / compoundSummarizer.getSummarizers().size());
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
        return String.format("%s %s są/mają [%s]",
                quantifier.getName(),
                predicate,
                compoundSummarizer.getName()
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
}
