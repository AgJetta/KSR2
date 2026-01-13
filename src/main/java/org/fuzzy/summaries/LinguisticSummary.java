package org.fuzzy.summaries;

import org.fuzzy.SongRecord;
import org.fuzzy.Universe;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;

import java.util.Arrays;
import java.util.List;

public class LinguisticSummary {
    protected final Quantifier quantifier;
    protected final String predicate;
    protected final Summarizer summarizer;
    protected final Summarizer qualifier;
    protected static List<Double> measureWeights = Arrays.asList(
            0.2, 0.05, 0.05, 0.2, 0.05, 0.05, 0.1, 0.1, 0.1, 0.1
    );

    public LinguisticSummary(Quantifier quantifier, String predicate, Summarizer summarizer) {
        this(quantifier, predicate, summarizer, null);
    }

    public LinguisticSummary(Quantifier quantifier, String predicate,
                             Summarizer summarizer, Summarizer qualifier) {
        this.quantifier = quantifier;
        this.predicate = predicate;
        this.summarizer = summarizer;
        this.qualifier = qualifier;

        if (qualifier != null && !quantifier.isRelative()) {
            throw new IllegalArgumentException(
                    "Second-order summaries only support relative quantifiers");
        }
    }

    public boolean isSecondOrder() {
        return qualifier != null;
    }

    public static void setMeasureWeights(List<Double> weights) {
        if (weights.size() != 10) {
            throw new IllegalArgumentException("Measure weights must contain exactly 10 values.");
        }
        double sum = weights.stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - 1.0) > 1e-9) {
            throw new IllegalArgumentException("Measure weights must sum up to 1.");
        }
        measureWeights = weights;
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

    public Summarizer getQualifier() {
        return qualifier;
    }

    public double calculateT1(List<SongRecord> dataset) {
        if (dataset.isEmpty()) {
            throw new IllegalArgumentException("Can't calculate a measure for an empty dataset!");
        }

        if (qualifier == null) {
            double r = summarizer.calculateR(dataset);
            int m = dataset.size();
            return quantifier.getMembership(r, m);
        } else {
            double numerator = 0.0;
            double denominator = 0.0;

            for (SongRecord record : dataset) {
                double qMembership = qualifier.getMembership(record);
                denominator += qMembership;

                double sMembership = summarizer.getMembership(record);
                double intersection = Math.min(sMembership, qMembership);
                numerator += intersection;
            }

            double r = denominator > 0 ? numerator / denominator : 0.0;
            return quantifier.getMembership(r, 1);
        }
    }

    public double calculateT2(List<SongRecord> dataset) {
        if (dataset.isEmpty()) {
            throw new IllegalArgumentException("Can't calculate a measure for an empty dataset!");
        }

        int n = summarizer.getComponentCount();
        double product = 1.0;

        for (int i = 0; i < n; i++) {
            double fuzziness = summarizer.getFuzzySet(i).degreeOfFuzziness();
            product *= fuzziness;
        }

        double geometricMean = Math.pow(product, 1.0 / n);
        double t2 = 1 - geometricMean;

        if (t2 < 0 || t2 > 1) {
            System.err.println("WARNING: t2 = " + t2 + " is not in the range [0, 1]!");
        }

        return t2;
    }

    public double calculateT3(List<SongRecord> dataset) {
        if (dataset.isEmpty()) {
            throw new IllegalArgumentException("Can't calculate a measure for an empty dataset!");
        }

        if (qualifier == null) {
            int n = summarizer.getComponentCount();
            int t = 0;
            int h = dataset.size();

            for (SongRecord record : dataset) {
                boolean allSupported = true;
                for (int i = 0; i < n; i++) {
                    double fieldValue = record.getAttribute(summarizer.getFieldName(i));
                    double membership = summarizer.getFuzzySet(i).getMembership(fieldValue);
                    if (membership <= 0) {
                        allSupported = false;
                        break;
                    }
                }
                if (allSupported) {
                    t++;
                }
            }

            return (double) t / h;
        } else {
            int t = 0;
            int h = 0;

            for (SongRecord record : dataset) {
                double qMembership = qualifier.getMembership(record);
                if (qMembership > 0) {
                    h++;
                    if (summarizer.getMembership(record) > 0) {
                        t++;
                    }
                }
            }

            return h > 0 ? (double) t / h : 0.0;
        }
    }

    public double calculateT4(List<SongRecord> dataset) {
        if (dataset.isEmpty()) {
            throw new IllegalArgumentException("Can't calculate a measure for an empty dataset!");
        }

        int n = summarizer.getComponentCount();
        double m = dataset.size();

        double product = 1.0;
        for (int j = 0; j < n; j++) {
            int g = 0;
            for (SongRecord record : dataset) {
                double fieldValue = record.getAttribute(summarizer.getFieldName(j));
                double membership = summarizer.getFuzzySet(j).getMembership(fieldValue);
                if (membership > 0) {
                    g++;
                }
            }
            double r_j = g / m;
            product *= r_j;
        }

        double t3 = calculateT3(dataset);
        double t4 = Math.abs(product - t3);

        if (t4 > 1) {
            System.err.println("WARNING: t4 = " + t4 + " is greater than 1!");
        }

        return t4;
    }

    public double calculateT5(List<SongRecord> dataset) {
        int n = summarizer.getComponentCount();
        return 2 * Math.pow(0.5, n);
    }

    public double calculateT6(List<SongRecord> dataset) {
        double supportMeasure = quantifier.getSupportMeasure();
        Universe universe = quantifier.getFuzzySet().getUniverse();
        double universeMeasure = quantifier.isRelative() ? 1.0 : universe.getMeasure();

        double t6 = 1 - (supportMeasure / universeMeasure);

        if (t6 < 0 || t6 > 1 || Double.isNaN(t6)) {
            System.err.println("WARNING: t6 = " + t6 + " is not in the range [0, 1]!");
            System.err.println("Support measure = " + supportMeasure + ", universe measure = " + universeMeasure);
        }

        return t6;
    }

    public double calculateT7(List<SongRecord> dataset) {
        double quantifierCardinality = quantifier.getCardinality();
        Universe universe = quantifier.getFuzzySet().getUniverse();
        double universeMeasure = universe.getMeasure();

        return 1 - (quantifierCardinality / universeMeasure);
    }

    public double calculateT8(List<SongRecord> dataset) {
        int n = summarizer.getComponentCount();

        double product = 1.0;
        for (int i = 0; i < n; i++) {
            double cardinality = summarizer.getFuzzySet(i).cardinalNumber();
            Universe universe = summarizer.getFuzzySet(i).getUniverse();
            double universeMeasure = universe.getMeasure();
            product *= (cardinality / universeMeasure);
        }

        double geometricMean = Math.pow(product, 1.0 / n);
        return 1 - geometricMean;
    }

    public double calculateT9(List<SongRecord> dataset) {
        if (qualifier == null) {
            return 0.0;
        }

        int n = qualifier.getComponentCount();
        double product = 1.0;

        for (int i = 0; i < n; i++) {
            double fuzziness = qualifier.getFuzzySet(i).degreeOfFuzziness();
            product *= fuzziness;
        }

        double geometricMean = Math.pow(product, 1.0 / n);
        return 1 - geometricMean;
    }

    public double calculateT10(List<SongRecord> dataset) {
        if (qualifier == null) {
            return 0.0;
        }

        int n = qualifier.getComponentCount();
        double product = 1.0;

        for (int i = 0; i < n; i++) {
            double cardinality = qualifier.getFuzzySet(i).cardinalNumber();
            Universe universe = qualifier.getFuzzySet(i).getUniverse();
            double universeMeasure = universe.getMeasure();
            product *= (cardinality / universeMeasure);
        }

        double geometricMean = Math.pow(product, 1.0 / n);
        return 1 - geometricMean;
    }

    public double calculateT11(List<SongRecord> dataset) {
        if (qualifier == null) {
            return 1.0;
        }

        int n = qualifier.getComponentCount();
        return 2 * Math.pow(0.5, n);
    }

    public double calculateOptimal(List<SongRecord> dataset) {
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

    public String generateSummary() {
        String summaryType;
        if (isSecondOrder()) {
            summaryType = summarizer.isCompound() ? "COMPOUND F2" : "F2";
        } else {
            summaryType = summarizer.isCompound() ? "COMPOUND" : "SIMPLE";
        }

        if (qualifier == null) {
            return String.format("%s | %s %s są/mają [%s]",
                    summaryType,
                    quantifier.getName(),
                    predicate,
                    summarizer.generateDescription()
            );
        } else {
            return String.format("%s | %s %s które są/mają [%s] są/mają [%s]",
                    summaryType,
                    quantifier.getName(),
                    predicate,
                    qualifier.generateDescription(),
                    summarizer.generateDescription()
            );
        }
    }

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
        return String.format("%-113s %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f",
                summaryString, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, optimal);
    }

    @Override
    public String toString() {
        return generateSummary();
    }
}