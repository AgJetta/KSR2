package org.fuzzy.summaries;

import org.fuzzy.*;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class LinguisticSummaryTest {

    private List<SongRecord> createDataset(int size, double... ageValues) {
        List<SongRecord> dataset = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Map<String, Double> attrs = new HashMap<>();
            double age = (i < ageValues.length) ? ageValues[i] : 20.0 + i * 5;
            attrs.put("age", age);
            attrs.put("salary", 5000.0 + i * 500);
            dataset.add(new SongRecord(attrs));
        }
        return dataset;
    }

    // Helper to create a single-component Summarizer
    private Summarizer createSingleSummarizer(String name, String field, FuzzySet fs) {
        return new Summarizer(
                name,
                Collections.singletonList(field),
                Collections.singletonList(fs),
                Collections.emptyList(), // no connectives
                Collections.singletonList(field)
        );
    }

    // Helper to create a compound Summarizer
    private Summarizer createCompoundSummarizer(String name, List<String> fields, List<FuzzySet> sets, List<LogicalConnective> connectives) {
        return new Summarizer(name, fields, sets, connectives, fields);
    }

    // Helper to create a Quantifier
    private Quantifier createRelativeQuantifier(String name, double a, double b, double c, double d) {
        Universe universe = new Universe(0.0, 1.0, true);
        FuzzySet fs = new FuzzySet(universe, MembershipFunctions.trapezoidal(a, b, c, d));
        return new Quantifier(name, fs, true, "trapezoidal", new double[]{a, b, c, d});
    }

    @Test
    public void testConstructor() {
        Quantifier most = createRelativeQuantifier("MOST", 0.5, 0.7, 0.9, 1.0);
        FuzzySet youngFuzzy = new FuzzySet(new Universe(0, 100, true), MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = createSingleSummarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        assertEquals(most, summary.getQuantifier());
        assertEquals("people", summary.getPredicate());
        assertEquals(young, summary.getSummarizer());
    }

    @Test
    public void testSetMeasureWeights_valid() {
        List<Double> weights = Arrays.asList(0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1);
        assertDoesNotThrow(() -> LinguisticSummary.setMeasureWeights(weights));
    }

    @Test
    public void testCalculateT1_simple() {
        Quantifier most = createRelativeQuantifier("MOST", 0.5, 0.7, 0.9, 1.0);
        FuzzySet youngFuzzy = new FuzzySet(new Universe(0, 100, true), MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = createSingleSummarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);
        List<SongRecord> dataset = createDataset(10, 20, 22, 24, 26, 28, 30, 35, 40, 50, 60);

        double t1 = summary.calculateT1(dataset);
        assertTrue(t1 >= 0.0 && t1 <= 1.0, "T1 should be in [0,1]");
    }

    @Test
    public void testCalculateT1_compound() {
        Quantifier most = createRelativeQuantifier("MOST", 0.5, 0.7, 0.9, 1.0);

        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        FuzzySet richFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.trapezoidal(6000, 7000, 10000, 10000));

        Summarizer youngAndRich = createCompoundSummarizer(
                "young_and_rich",
                Arrays.asList("age", "salary"),
                Arrays.asList(youngFuzzy, richFuzzy),
                Arrays.asList(LogicalConnective.AND)
        );

        LinguisticSummary summary = new LinguisticSummary(most, "people", youngAndRich);
        List<SongRecord> dataset = createDataset(10);

        double t1 = summary.calculateT1(dataset);
        assertTrue(t1 >= 0.0 && t1 <= 1.0, "T1 should be in [0,1]");
    }

    @Test
    public void testGenerateSummary_simple() {
        Quantifier most = createRelativeQuantifier("MOST", 0.5, 0.7, 0.9, 1.0);
        FuzzySet youngFuzzy = new FuzzySet(new Universe(0, 100, true), MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = createSingleSummarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        String summaryStr = summary.generateSummary();
        assertTrue(summaryStr.contains("MOST"));
        assertTrue(summaryStr.contains("people"));
        assertTrue(summaryStr.contains("young"));
    }

    @Test
    public void testGenerateSummary_compound() {
        Quantifier most = createRelativeQuantifier("MOST", 0.5, 0.7, 0.9, 1.0);

        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        FuzzySet richFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.trapezoidal(6000, 7000, 10000, 10000));

        Summarizer youngAndRich = createCompoundSummarizer(
                "young_and_rich",
                Arrays.asList("age", "salary"),
                Arrays.asList(youngFuzzy, richFuzzy),
                Arrays.asList(LogicalConnective.AND)
        );

        LinguisticSummary summary = new LinguisticSummary(most, "people", youngAndRich);
        String summaryStr = summary.generateSummary();

        assertTrue(summaryStr.contains("MOST"));
        assertTrue(summaryStr.contains("people"));
        assertTrue(summaryStr.contains("COMPOUND"));
    }

    @Test
    public void testAllMeasuresInRange() {
        Quantifier most = createRelativeQuantifier("MOST", 0.5, 0.7, 0.9, 1.0);
        FuzzySet youngFuzzy = new FuzzySet(new Universe(0, 100, true), MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = createSingleSummarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);
        List<SongRecord> dataset = createDataset(10);

        double[] measures = new double[]{
                summary.calculateT1(dataset),
                summary.calculateT2(dataset),
                summary.calculateT3(dataset),
                summary.calculateT4(dataset),
                summary.calculateT5(dataset),
                summary.calculateT6(dataset),
                summary.calculateT7(dataset),
                summary.calculateT8(dataset),
                summary.calculateT9(dataset),
                summary.calculateT10(dataset),
                summary.calculateT11(dataset),
                summary.calculateOptimal(dataset)
        };

        for (int i = 0; i < measures.length; i++) {
            assertTrue(measures[i] >= 0.0 && measures[i] <= 1.0, "Measure T" + (i + 1) + " out of range: " + measures[i]);
        }
    }
}
