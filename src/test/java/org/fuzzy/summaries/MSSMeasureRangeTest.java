package org.fuzzy.summaries;

import org.fuzzy.SongRecord;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.dataImport.ConfigImporter.loadQuantifiersFromConfig;
import static org.dataImport.ConfigImporter.loadSummarizersFromConfig;
import static org.dataImport.CsvSongImporter.importSongs;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MSSMeasureRangeTest {
    private static List<Quantifier> relativeQuantifiers;
    private static List<Summarizer> summarizers;
    private static List<SongRecord> dataset;
    private static List<SubjectPair> subjectPairs;

    private static class SubjectPair {
        String name1;
        String name2;
        double value1;
        double value2;

        SubjectPair(String name1, double value1, String name2, double value2) {
            this.name1 = name1;
            this.value1 = value1;
            this.name2 = name2;
            this.value2 = value2;
        }
    }

    @BeforeAll
    public static void setup() {
        List<Quantifier> allQuantifiers = loadQuantifiersFromConfig();
        summarizers = loadSummarizersFromConfig();
        dataset = importSongs(10000);

        relativeQuantifiers = allQuantifiers.stream()
                .filter(Quantifier::isRelative)
                .toList();

        subjectPairs = createSubjectPairs();

        System.out.println("Loaded " + relativeQuantifiers.size() + " relative quantifiers");
        System.out.println("Loaded " + summarizers.size() + " summarizers");
        System.out.println("Loaded " + dataset.size() + " songs");
        System.out.println("Created " + subjectPairs.size() + " subject pairs");
    }

    private static List<SubjectPair> createSubjectPairs() {
        Map<Double, String> genreNames = new HashMap<>();
        genreNames.put(0.0, "pop");
        genreNames.put(1.0, "rock");
        genreNames.put(2.0, "rap");
        genreNames.put(3.0, "edm");
        genreNames.put(4.0, "r&b");
        genreNames.put(5.0, "latin");

        Map<Double, Integer> genreCounts = new HashMap<>();
        for (SongRecord record : dataset) {
            double genre = record.getAttribute("playlist_genre");
            genreCounts.put(genre, genreCounts.getOrDefault(genre, 0) + 1);
        }

        List<Double> availableGenres = genreCounts.entrySet().stream()
                .filter(e -> e.getValue() >= 50)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();

        System.out.println("\nGenre distribution (only genres with 50+ songs used for testing):");
        for (Double genre : availableGenres) {
            String name = genreNames.get(genre);
            int count = genreCounts.get(genre);
            System.out.printf("  %s: %d songs\n", name, count);
        }

        List<SubjectPair> pairs = new ArrayList<>();
        for (int i = 0; i < availableGenres.size(); i++) {
            for (int j = i + 1; j < availableGenres.size(); j++) {
                double genre1 = availableGenres.get(i);
                double genre2 = availableGenres.get(j);
                pairs.add(new SubjectPair(
                        genreNames.get(genre1), genre1,
                        genreNames.get(genre2), genre2
                ));
            }
        }

        return pairs;
    }

    @Test
    public void testMSS1MeasuresInRange() {
        int summaryCount = 0;
        int failureCount = 0;

        for (SubjectPair pair : subjectPairs) {
            for (Quantifier quantifier : relativeQuantifiers) {
                for (int i = 0; i < Math.min(10, summarizers.size()); i++) {
                    Summarizer summarizer = summarizers.get(i);
                    summaryCount++;

                    try {
                        MSS1 mss = new MSS1("playlist_genre", pair.name1, pair.name2,
                                pair.value1, pair.value2, quantifier, summarizer);

                        double t1 = mss.calculateT1(dataset);
                        assertT1InRange(mss, t1);
                    } catch (AssertionError e) {
                        failureCount++;
                        System.err.println("MSS1 FAILURE #" + failureCount + ": " + e.getMessage());
                    }
                }
            }
        }

        System.out.println("\nTested " + summaryCount + " MSS1 summaries");
        System.out.println("Failures: " + failureCount);

        if (failureCount > 0) {
            throw new AssertionError("Found " + failureCount + " T1 value(s) out of range in MSS1");
        }
    }

    @Test
    public void testMSS2MeasuresInRange() {
        int summaryCount = 0;
        int failureCount = 0;

        for (SubjectPair pair : subjectPairs) {
            for (Quantifier quantifier : relativeQuantifiers) {
                for (int i = 0; i < Math.min(8, summarizers.size()); i++) {
                    Summarizer summarizer1 = summarizers.get(i);
                    for (int j = 0; j < Math.min(8, summarizers.size()); j++) {
                        if (i == j) continue;
                        Summarizer summarizer2 = summarizers.get(j);
                        summaryCount++;

                        try {
                            MSS2 mss = new MSS2("playlist_genre", pair.name1, pair.name2,
                                    pair.value1, pair.value2, quantifier, summarizer1, summarizer2);

                            double t1 = mss.calculateT1(dataset);
                            assertT1InRange(mss, t1);
                        } catch (AssertionError e) {
                            failureCount++;
                            System.err.println("MSS2 FAILURE #" + failureCount + ": " + e.getMessage());
                        }
                    }
                }
            }
        }

        System.out.println("\nTested " + summaryCount + " MSS2 summaries");
        System.out.println("Failures: " + failureCount);

        if (failureCount > 0) {
            throw new AssertionError("Found " + failureCount + " T1 value(s) out of range in MSS2");
        }
    }

    @Test
    public void testMSS3MeasuresInRange() {
        int summaryCount = 0;
        int failureCount = 0;

        for (SubjectPair pair : subjectPairs) {
            for (Quantifier quantifier : relativeQuantifiers) {
                for (int i = 0; i < Math.min(8, summarizers.size()); i++) {
                    Summarizer summarizer1 = summarizers.get(i);
                    for (int j = 0; j < Math.min(8, summarizers.size()); j++) {
                        if (i == j) continue;
                        Summarizer summarizer2 = summarizers.get(j);
                        summaryCount++;

                        try {
                            MSS3 mss = new MSS3("playlist_genre", pair.name1, pair.name2,
                                    pair.value1, pair.value2, quantifier, summarizer1, summarizer2);

                            double t1 = mss.calculateT1(dataset);
                            assertT1InRange(mss, t1);
                        } catch (AssertionError e) {
                            failureCount++;
                            System.err.println("MSS3 FAILURE #" + failureCount + ": " + e.getMessage());
                        }
                    }
                }
            }
        }

        System.out.println("\nTested " + summaryCount + " MSS3 summaries");
        System.out.println("Failures: " + failureCount);

        if (failureCount > 0) {
            throw new AssertionError("Found " + failureCount + " T1 value(s) out of range in MSS3");
        }
    }

    @Test
    public void testMSS4MeasuresInRange() {
        int summaryCount = 0;
        int failureCount = 0;

        for (SubjectPair pair : subjectPairs) {
            for (int i = 0; i < Math.min(15, summarizers.size()); i++) {
                Summarizer summarizer = summarizers.get(i);
                summaryCount++;

                try {
                    MSS4 mss = new MSS4("playlist_genre", pair.name1, pair.name2,
                            pair.value1, pair.value2, summarizer);

                    double t1 = mss.calculateT1(dataset);
                    assertT1InRange(mss, t1);
                } catch (AssertionError e) {
                    failureCount++;
                    System.err.println("MSS4 FAILURE #" + failureCount + ": " + e.getMessage());
                }
            }
        }

        System.out.println("\nTested " + summaryCount + " MSS4 summaries");
        System.out.println("Failures: " + failureCount);

        if (failureCount > 0) {
            throw new AssertionError("Found " + failureCount + " T1 value(s) out of range in MSS4");
        }
    }

    @Test
    public void testMSS1RejectsAbsoluteQuantifier() {
        List<Quantifier> absoluteQuantifiers = loadQuantifiersFromConfig().stream()
                .filter(q -> !q.isRelative())
                .toList();

        if (absoluteQuantifiers.isEmpty()) {
            System.out.println("No absolute quantifiers to test");
            return;
        }

        Quantifier absolute = absoluteQuantifiers.get(0);
        Summarizer summarizer = summarizers.get(0);
        SubjectPair pair = subjectPairs.get(0);

        try {
            new MSS1("playlist_genre", pair.name1, pair.name2,
                    pair.value1, pair.value2, absolute, summarizer);
            throw new AssertionError("MSS1 should reject absolute quantifiers");
        } catch (IllegalArgumentException e) {
            System.out.println("Correctly rejected absolute quantifier: " + e.getMessage());
        }
    }

    @Test
    public void testAllMSSTypesWithEmptySubjects() {
        Quantifier quantifier = relativeQuantifiers.get(0);
        Summarizer summarizer = summarizers.get(0);

        List<SongRecord> emptyDataset = new ArrayList<>();

        MSS1 mss1 = new MSS1("playlist_genre", "nonexistent1", "nonexistent2",
                99.0, 98.0, quantifier, summarizer);
        double t1_mss1 = mss1.calculateT1(emptyDataset);
        assertTrue(t1_mss1 >= 0.0 && t1_mss1 <= 1.0, "MSS1 T1 should be in [0,1] for empty dataset");

        MSS2 mss2 = new MSS2("playlist_genre", "nonexistent1", "nonexistent2",
                99.0, 98.0, quantifier, summarizer, summarizers.get(1));
        double t1_mss2 = mss2.calculateT1(emptyDataset);
        assertTrue(t1_mss2 >= 0.0 && t1_mss2 <= 1.0, "MSS2 T1 should be in [0,1] for empty dataset");

        MSS3 mss3 = new MSS3("playlist_genre", "nonexistent1", "nonexistent2",
                99.0, 98.0, quantifier, summarizer, summarizers.get(1));
        double t1_mss3 = mss3.calculateT1(emptyDataset);
        assertTrue(t1_mss3 >= 0.0 && t1_mss3 <= 1.0, "MSS3 T1 should be in [0,1] for empty dataset");

        MSS4 mss4 = new MSS4("playlist_genre", "nonexistent1", "nonexistent2",
                99.0, 98.0, summarizer);
        double t1_mss4 = mss4.calculateT1(emptyDataset);
        assertTrue(t1_mss4 >= 0.0 && t1_mss4 <= 1.0, "MSS4 T1 should be in [0,1] for empty dataset");

        System.out.println("All MSS types handle empty subjects correctly");
    }

    private void assertT1InRange(Object mss, double t1) {
        String summaryType = mss.getClass().getSimpleName();
        String summaryText = mss.toString();

        assertTrue(t1 >= 0.0 && t1 <= 1.0,
                String.format("T1 for %s summary '%s' is out of range [0,1]: %.6f",
                        summaryType, summaryText, t1));
    }
}