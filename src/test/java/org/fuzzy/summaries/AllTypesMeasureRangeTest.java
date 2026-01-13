package org.fuzzy.summaries;

import org.fuzzy.LogicalConnective;
import org.fuzzy.SongRecord;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.dataImport.ConfigImporter.loadQuantifiersFromConfig;
import static org.dataImport.ConfigImporter.loadSummarizersFromConfig;
import static org.dataImport.CsvSongImporter.importSongs;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AllTypesMeasureRangeTest {
    private static List<Quantifier> quantifiers;
    private static List<Quantifier> relativeQuantifiers;
    private static List<Summarizer> summarizers;
    private static List<Summarizer> compoundSummarizers;
    private static List<SongRecord> dataset;

    @BeforeAll
    public static void setup() {
        quantifiers = loadQuantifiersFromConfig();
        summarizers = loadSummarizersFromConfig();
        dataset = importSongs(1000);

        relativeQuantifiers = quantifiers.stream()
                .filter(Quantifier::isRelative)
                .toList();

        compoundSummarizers = createCompoundSummarizers(summarizers);

        System.out.println("Loaded " + quantifiers.size() + " quantifiers (" +
                relativeQuantifiers.size() + " relative)");
        System.out.println("Loaded " + summarizers.size() + " simple summarizers");
        System.out.println("Created " + compoundSummarizers.size() + " compound summarizers");
        System.out.println("Loaded " + dataset.size() + " songs");
    }

    private static List<Summarizer> createCompoundSummarizers(List<Summarizer> simple) {
        List<Summarizer> compounds = new ArrayList<>();
        int maxPairs = 20;
        int count = 0;

        for (int i = 0; i < simple.size() && count < maxPairs; i++) {
            for (int j = i + 1; j < simple.size() && count < maxPairs; j++) {
                Summarizer s1 = simple.get(i);
                Summarizer s2 = simple.get(j);

                Summarizer andCompound = new Summarizer(
                        s1.getName() + " AND " + s2.getName(),
                        Arrays.asList(s1.getFieldName(0), s2.getFieldName(0)),
                        Arrays.asList(s1.getFuzzySet(0), s2.getFuzzySet(0)),
                        Arrays.asList(LogicalConnective.AND),
                        Arrays.asList(s1.getLinguisticVariable(0), s2.getLinguisticVariable(0))
                );
                compounds.add(andCompound);
                count++;

                if (count >= maxPairs) break;
            }
        }

        return compounds;
    }

    @Test
    public void testCompoundSummariesMeasuresInRange() {
        int summaryCount = 0;
        int failureCount = 0;

        for (Quantifier quantifier : quantifiers) {
            for (Summarizer compound : compoundSummarizers) {
                summaryCount++;
                LinguisticSummary summary = new LinguisticSummary(quantifier, "songs", compound);

                try {
                    assertAllMeasuresInRange(summary);
                } catch (AssertionError e) {
                    failureCount++;
                    System.err.println("COMPOUND FAILURE #" + failureCount + ": " + e.getMessage());
                }
            }
        }

        System.out.println("\nTested " + summaryCount + " compound summaries");
        System.out.println("Failures: " + failureCount);

        if (failureCount > 0) {
            throw new AssertionError("Found " + failureCount + " measure(s) out of range in compound summaries");
        }
    }

    @Test
    public void testSecondOrderSimpleSummariesMeasuresInRange() {
        int summaryCount = 0;
        int failureCount = 0;

        for (Quantifier quantifier : relativeQuantifiers) {
            for (int i = 0; i < Math.min(10, summarizers.size()); i++) {
                Summarizer summarizer = summarizers.get(i);
                for (int j = 0; j < Math.min(10, summarizers.size()); j++) {
                    if (i == j) continue;
                    Summarizer qualifier = summarizers.get(j);

                    summaryCount++;
                    LinguisticSummary summary = new LinguisticSummary(
                            quantifier, "songs", summarizer, qualifier);

                    try {
                        assertAllMeasuresInRange(summary);
                    } catch (AssertionError e) {
                        failureCount++;
                        System.err.println("F2 SIMPLE FAILURE #" + failureCount + ": " + e.getMessage());
                    }
                }
            }
        }

        System.out.println("\nTested " + summaryCount + " second-order (simple+simple) summaries");
        System.out.println("Failures: " + failureCount);

        if (failureCount > 0) {
            throw new AssertionError("Found " + failureCount + " measure(s) out of range in F2 simple summaries");
        }
    }

    @Test
    public void testSecondOrderCompoundSummarizerMeasuresInRange() {
        int summaryCount = 0;
        int failureCount = 0;

        for (Quantifier quantifier : relativeQuantifiers) {
            for (int i = 0; i < Math.min(5, compoundSummarizers.size()); i++) {
                Summarizer compoundSummarizer = compoundSummarizers.get(i);
                for (int j = 0; j < Math.min(5, summarizers.size()); j++) {
                    Summarizer qualifier = summarizers.get(j);

                    summaryCount++;
                    LinguisticSummary summary = new LinguisticSummary(
                            quantifier, "songs", compoundSummarizer, qualifier);

                    try {
                        assertAllMeasuresInRange(summary);
                    } catch (AssertionError e) {
                        failureCount++;
                        System.err.println("F2 COMPOUND SUMMARIZER FAILURE #" + failureCount + ": " + e.getMessage());
                    }
                }
            }
        }

        System.out.println("\nTested " + summaryCount + " second-order (compound summarizer) summaries");
        System.out.println("Failures: " + failureCount);

        if (failureCount > 0) {
            throw new AssertionError("Found " + failureCount + " measure(s) out of range in F2 compound summarizer summaries");
        }
    }

    @Test
    public void testSecondOrderCompoundQualifierMeasuresInRange() {
        int summaryCount = 0;
        int failureCount = 0;

        for (Quantifier quantifier : relativeQuantifiers) {
            for (int i = 0; i < Math.min(5, summarizers.size()); i++) {
                Summarizer summarizer = summarizers.get(i);
                for (int j = 0; j < Math.min(5, compoundSummarizers.size()); j++) {
                    Summarizer compoundQualifier = compoundSummarizers.get(j);

                    summaryCount++;
                    LinguisticSummary summary = new LinguisticSummary(
                            quantifier, "songs", summarizer, compoundQualifier);

                    try {
                        assertAllMeasuresInRange(summary);
                    } catch (AssertionError e) {
                        failureCount++;
                        System.err.println("F2 COMPOUND QUALIFIER FAILURE #" + failureCount + ": " + e.getMessage());
                    }
                }
            }
        }

        System.out.println("\nTested " + summaryCount + " second-order (compound qualifier) summaries");
        System.out.println("Failures: " + failureCount);

        if (failureCount > 0) {
            throw new AssertionError("Found " + failureCount + " measure(s) out of range in F2 compound qualifier summaries");
        }
    }

    @Test
    public void testSecondOrderBothCompoundMeasuresInRange() {
        int summaryCount = 0;
        int failureCount = 0;

        for (Quantifier quantifier : relativeQuantifiers) {
            for (int i = 0; i < Math.min(3, compoundSummarizers.size()); i++) {
                Summarizer compoundSummarizer = compoundSummarizers.get(i);
                for (int j = 0; j < Math.min(3, compoundSummarizers.size()); j++) {
                    if (i == j) continue;
                    Summarizer compoundQualifier = compoundSummarizers.get(j);

                    summaryCount++;
                    LinguisticSummary summary = new LinguisticSummary(
                            quantifier, "songs", compoundSummarizer, compoundQualifier);

                    try {
                        assertAllMeasuresInRange(summary);
                    } catch (AssertionError e) {
                        failureCount++;
                        System.err.println("F2 BOTH COMPOUND FAILURE #" + failureCount + ": " + e.getMessage());
                    }
                }
            }
        }

        System.out.println("\nTested " + summaryCount + " second-order (both compound) summaries");
        System.out.println("Failures: " + failureCount);

        if (failureCount > 0) {
            throw new AssertionError("Found " + failureCount + " measure(s) out of range in F2 both compound summaries");
        }
    }

    private void assertAllMeasuresInRange(LinguisticSummary summary) {
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
    }

    private void assertMeasureInRange(LinguisticSummary summary, String measureName, double value) {
        assertTrue(value >= 0.0 && value <= 1.0,
                String.format("%s for summary '%s' is out of range [0,1]: %.6f",
                        measureName, summary.generateSummary(), value));
    }
}