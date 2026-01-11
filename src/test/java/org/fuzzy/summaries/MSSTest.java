package org.fuzzy.summaries;

import org.fuzzy.*;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class MSSTest {
    private List<SongRecord> testDataset;
    private Quantifier aboutHalf;
    private Summarizer energyHigh;
    private Summarizer loudness;

    @BeforeEach
    public void setUp() {
        testDataset = createTestDataset();
        aboutHalf = createQuantifier();
        energyHigh = createSummarizer("High Energy", "energy");
        loudness = createSummarizer("Loud", "loudness");
    }

    private List<SongRecord> createTestDataset() {
        List<SongRecord> dataset = new ArrayList<>();

        // 5 rap songs (genre=2.0) with higher energy
        for (int i = 0; i < 5; i++) {
            Map<String, Double> attrs = new HashMap<>();
            attrs.put("playlist_genre", 2.0);
            attrs.put("track_popularity", 50.0 + i * 10);
            attrs.put("danceability", 0.7 + i * 0.05);
            attrs.put("energy", 0.8 + i * 0.03);
            attrs.put("loudness", -5.0 + i);
            attrs.put("acousticness", 0.2);
            attrs.put("instrumentalness", 0.1);
            attrs.put("liveness", 0.2);
            attrs.put("valence", 0.6);
            attrs.put("tempo", 120.0);
            attrs.put("duration_ms", 180000.0);
            dataset.add(new SongRecord(attrs));
        }

        // 5 pop songs (genre=0.0) with lower energy
        for (int i = 0; i < 5; i++) {
            Map<String, Double> attrs = new HashMap<>();
            attrs.put("playlist_genre", 0.0);
            attrs.put("track_popularity", 60.0 + i * 8);
            attrs.put("danceability", 0.6 + i * 0.04);
            attrs.put("energy", 0.5 + i * 0.08);
            attrs.put("loudness", -8.0 + i);
            attrs.put("acousticness", 0.3);
            attrs.put("instrumentalness", 0.05);
            attrs.put("liveness", 0.15);
            attrs.put("valence", 0.7);
            attrs.put("tempo", 110.0);
            attrs.put("duration_ms", 200000.0);
            dataset.add(new SongRecord(attrs));
        }

        return dataset;
    }

    private Quantifier createQuantifier() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet quantifierSet = new FuzzySet(qUniverse,
                MembershipFunctions.triangular(0.3, 0.5, 0.7));
        return new Quantifier("OKOŁO POŁOWY", quantifierSet, true);
    }

    private Summarizer createSummarizer(String name, String fieldName) {
        Universe universe = new Universe(0.0, 1.0, true);
        FuzzySet fuzzySet = new FuzzySet(universe,
                MembershipFunctions.triangular(0.6, 0.8, 1.0));
        return new Summarizer(name, fieldName, fuzzySet);
    }

    @Test
    public void testMSS1Constructor() {
        MSS1 mss1 = new MSS1("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh);

        assertEquals("rap", mss1.getPredicate1());
        assertEquals("pop", mss1.getPredicate2());
        assertEquals(aboutHalf, mss1.getQuantifier());
        assertEquals(energyHigh, mss1.getSummarizer());
    }

    @Test
    public void testMSS1ThrowsOnAbsoluteQuantifier() {
        Universe absUniverse = new Universe(0.0, 100.0, false);
        FuzzySet absFuzzy = new FuzzySet(absUniverse,
                MembershipFunctions.triangular(40, 50, 60));
        Quantifier absolute = new Quantifier("ABOUT 50", absFuzzy, false);

        assertThrows(IllegalArgumentException.class, () -> {
            new MSS1("playlist_genre", "rap", "pop", 2.0, 0.0,
                    absolute, energyHigh);
        });
    }

    @Test
    public void testMSS1CalculateT1() {
        MSS1 mss1 = new MSS1("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh);

        double t1 = mss1.calculateT1(testDataset);

        assertTrue(t1 >= 0.0 && t1 <= 1.0, "T1 should be in [0,1]");
        assertFalse(Double.isNaN(t1), "T1 should not be NaN");
    }

    @Test
    public void testMSS1GenerateSummary() {
        MSS1 mss1 = new MSS1("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh);

        String summary = mss1.generateSummary();

        assertTrue(summary.contains("MSS1"));
        assertTrue(summary.contains("OKOŁO POŁOWY"));
        assertTrue(summary.contains("RAP"));
        assertTrue(summary.contains("POP"));
    }

    @Test
    public void testMSS1WithEmptySubject() {
        List<SongRecord> emptyDataset = new ArrayList<>();

        MSS1 mss1 = new MSS1("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh);

        double t1 = mss1.calculateT1(emptyDataset);
        assertEquals(0.0, t1, "T1 should be 0 for empty subjects");
    }

    @Test
    public void testMSS2Constructor() {
        MSS2 mss2 = new MSS2("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh, loudness);

        assertEquals("rap", mss2.getPredicate1());
        assertEquals("pop", mss2.getPredicate2());
        assertEquals(aboutHalf, mss2.getQuantifier());
        assertEquals(energyHigh, mss2.getSummarizer());
        assertEquals(loudness, mss2.getSummarizer2());
    }

    @Test
    public void testMSS2CalculateT1() {
        MSS2 mss2 = new MSS2("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh, loudness);

        double t1 = mss2.calculateT1(testDataset);

        assertTrue(t1 >= 0.0 && t1 <= 1.0, "T1 should be in [0,1]");
        assertFalse(Double.isNaN(t1), "T1 should not be NaN");
    }

    @Test
    public void testMSS2GenerateSummary() {
        MSS2 mss2 = new MSS2("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh, loudness);

        String summary = mss2.generateSummary();

        assertTrue(summary.contains("MSS2"));
        assertTrue(summary.contains("będących"));
    }

    @Test
    public void testMSS3Constructor() {
        MSS3 mss3 = new MSS3("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh, loudness);

        assertEquals("rap", mss3.getPredicate1());
        assertEquals("pop", mss3.getPredicate2());
    }

    @Test
    public void testMSS3CalculateT1() {
        MSS3 mss3 = new MSS3("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh, loudness);

        double t1 = mss3.calculateT1(testDataset);

        assertTrue(t1 >= 0.0 && t1 <= 1.0, "T1 should be in [0,1]");
        assertFalse(Double.isNaN(t1), "T1 should not be NaN");
    }

    @Test
    public void testMSS3GenerateSummary() {
        MSS3 mss3 = new MSS3("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh, loudness);

        String summary = mss3.generateSummary();

        assertTrue(summary.contains("MSS3"));
        assertTrue(summary.contains("będących"));
        assertTrue(summary.contains("w odniesieniu do"));
    }

    @Test
    public void testMSS2AndMSS3ProduceDifferentResults() {
        MSS2 mss2 = new MSS2("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh, loudness);
        MSS3 mss3 = new MSS3("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh, loudness);

        double t1_mss2 = mss2.calculateT1(testDataset);
        double t1_mss3 = mss3.calculateT1(testDataset);

        // They should produce different results due to different denominators
        assertNotEquals(t1_mss2, t1_mss3, 0.001,
                "MSS2 and MSS3 should produce different T1 values");
    }

    @Test
    public void testMSS4Constructor() {
        MSS4 mss4 = new MSS4("playlist_genre", "rap", "pop", 2.0, 0.0, energyHigh);

        assertEquals("rap", mss4.getPredicate1());
        assertEquals("pop", mss4.getPredicate2());
        assertNull(mss4.getQuantifier(), "MSS4 should not have a quantifier");
        assertEquals(energyHigh, mss4.getSummarizer());
    }

    @Test
    public void testMSS4CalculateT1() {
        MSS4 mss4 = new MSS4("playlist_genre", "rap", "pop", 2.0, 0.0, energyHigh);

        double t1 = mss4.calculateT1(testDataset);

        assertTrue(t1 >= 0.0 && t1 <= 1.0, "T1 should be in [0,1]");
        assertFalse(Double.isNaN(t1), "T1 should not be NaN");
    }

    @Test
    public void testMSS4GenerateSummary() {
        MSS4 mss4 = new MSS4("playlist_genre", "rap", "pop", 2.0, 0.0, energyHigh);

        String summary = mss4.generateSummary();

        assertTrue(summary.contains("MSS4"));
        assertTrue(summary.contains("Więcej"));
        assertTrue(summary.contains("niż"));
        assertFalse(summary.contains("OKOŁO"), "MSS4 should not contain quantifier");
    }

    @Test
    public void testMSS4UsesMinimumSize() {
        // Create dataset with unequal subject sizes
        List<SongRecord> unequalDataset = new ArrayList<>();

        // 3 rap songs
        for (int i = 0; i < 3; i++) {
            Map<String, Double> attrs = new HashMap<>();
            attrs.put("playlist_genre", 2.0);
            attrs.put("energy", 0.8);
            unequalDataset.add(new SongRecord(attrs));
        }

        // 7 pop songs
        for (int i = 0; i < 7; i++) {
            Map<String, Double> attrs = new HashMap<>();
            attrs.put("playlist_genre", 0.0);
            attrs.put("energy", 0.5);
            unequalDataset.add(new SongRecord(attrs));
        }

        MSS4 mss4 = new MSS4("playlist_genre", "rap", "pop", 2.0, 0.0, energyHigh);
        double t1 = mss4.calculateT1(unequalDataset);

        // Should use min(3, 7) = 3 for comparison
        assertTrue(t1 >= 0.0 && t1 <= 1.0, "T1 should handle unequal sizes");
    }

    @Test
    public void testAllMSSFormsWithSameData() {
        MSS1 mss1 = new MSS1("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh);
        MSS2 mss2 = new MSS2("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh, loudness);
        MSS3 mss3 = new MSS3("playlist_genre", "rap", "pop", 2.0, 0.0,
                aboutHalf, energyHigh, loudness);
        MSS4 mss4 = new MSS4("playlist_genre", "rap", "pop", 2.0, 0.0, energyHigh);

        double t1_mss1 = mss1.calculateT1(testDataset);
        double t1_mss2 = mss2.calculateT1(testDataset);
        double t1_mss3 = mss3.calculateT1(testDataset);
        double t1_mss4 = mss4.calculateT1(testDataset);

        // All should produce valid T1 values
        assertTrue(t1_mss1 >= 0.0 && t1_mss1 <= 1.0);
        assertTrue(t1_mss2 >= 0.0 && t1_mss2 <= 1.0);
        assertTrue(t1_mss3 >= 0.0 && t1_mss3 <= 1.0);
        assertTrue(t1_mss4 >= 0.0 && t1_mss4 <= 1.0);

        // All should be different (except possibly MSS1 vs MSS4)
        assertNotEquals(t1_mss1, t1_mss2, 0.001);
        assertNotEquals(t1_mss2, t1_mss3, 0.001);
    }
}