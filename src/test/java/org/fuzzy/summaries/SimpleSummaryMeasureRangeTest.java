package org.fuzzy.summaries;

import org.fuzzy.SongRecord;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.dataImport.ConfigImporter.loadQuantifiersFromConfig;
import static org.dataImport.ConfigImporter.loadSummarizersFromConfig;
import static org.dataImport.CsvSongImporter.importSongs;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleSummaryMeasureRangeTest {
    private static List<Quantifier> quantifiers;
    private static List<Summarizer> summarizers;
    private static List<SongRecord> dataset;

    @BeforeAll
    public static void setup() {
        quantifiers = loadQuantifiersFromConfig();
        summarizers = loadSummarizersFromConfig();
        dataset = importSongs(1000);

        System.out.println("Loaded " + quantifiers.size() + " quantifiers");
        System.out.println("Loaded " + summarizers.size() + " summarizers");
        System.out.println("Loaded " + dataset.size() + " songs");
        System.out.println("Total simple summaries to test: " + (quantifiers.size() * summarizers.size()));
    }

    @Test
    public void testAllSimpleSummariesMeasuresInRange() {
        int summaryCount = 0;
        int failureCount = 0;

        for (Quantifier quantifier : quantifiers) {
            for (Summarizer summarizer : summarizers) {
                summaryCount++;
                LinguisticSummary summary = new LinguisticSummary(quantifier, "songs", summarizer);

                try {
                    assertMeasureInRange(summary, "T1", summary.calculateT1(dataset));
                    assertMeasureInRange(summary, "T2", summary.calculateT2(dataset));
                    assertMeasureInRange(summary, "T3", summary.calculateT3(dataset));
                    assertMeasureInRange(summary, "T4", summary.calculateT4(dataset));
                    assertMeasureInRange(summary, "T5", summary.calculateT5(dataset));
                    assertMeasureInRange(summary, "T6", summary.calculateT6(dataset));
                    assertMeasureInRange(summary, "T7", summary.calculateT7(dataset));
                    assertMeasureInRange(summary, "T8", summary.calculateT8(dataset));
                    assertMeasureInRange(summary, "T9", summary.calculateT9(dataset));
                    assertMeasureInRange(summary, "T10", summary.calculateT10(dataset));
                    assertMeasureInRange(summary, "T11", summary.calculateT11(dataset));
                    assertMeasureInRange(summary, "Optimal", summary.calculateOptimal(dataset));
                } catch (AssertionError e) {
                    failureCount++;
                    System.err.println("FAILURE #" + failureCount + ": " + e.getMessage());
                }
            }
        }

        System.out.println("\nTested " + summaryCount + " simple summaries");
        System.out.println("Failures: " + failureCount);

        if (failureCount > 0) {
            throw new AssertionError("Found " + failureCount + " measure(s) out of range");
        }
    }

    @Test
    public void testT4IsZeroForSimpleSummaries() {
        int summaryCount = 0;
        int failureCount = 0;
        double maxT4 = 0.0;

        for (Quantifier quantifier : quantifiers) {
            for (Summarizer summarizer : summarizers) {
                summaryCount++;
                LinguisticSummary summary = new LinguisticSummary(quantifier, "songs", summarizer);

                double t3 = summary.calculateT3(dataset);
                double t4 = summary.calculateT4(dataset);

                double product = computeProductForT4(summarizer, dataset);

                if (Math.abs(t4) > 1e-6) {
                    failureCount++;
                    System.err.println("T4 FAILURE #" + failureCount + ": Summary '" +
                            summary.generateSummary() + "' has T4=" + t4 + " (expected ≈0)");
                }

                if (Math.abs(product - t3) > 1e-6) {
                    failureCount++;
                    System.err.println("PRODUCT FAILURE #" + failureCount + ": Summary '" +
                            summary.generateSummary() + "' has product=" + product +
                            " but T3=" + t3 + " (difference=" + Math.abs(product - t3) + ")");
                }

                maxT4 = Math.max(maxT4, Math.abs(t4));
            }
        }

        System.out.println("\nTested " + summaryCount + " simple summaries for T4=0");
        System.out.println("Maximum |T4| value: " + maxT4);
        System.out.println("Failures: " + failureCount);

        if (failureCount > 0) {
            throw new AssertionError("Found " + failureCount + " T4 violations");
        }
    }

    private double computeProductForT4(Summarizer summarizer, List<SongRecord> dataset) {
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

        return product;
    }

    private void assertMeasureInRange(LinguisticSummary summary, String measureName, double value) {
        assertTrue(value >= 0.0 && value <= 1.0,
                String.format("%s for summary '%s' is out of range [0,1]: %.6f",
                        measureName, summary.generateSummary(), value));
    }
}