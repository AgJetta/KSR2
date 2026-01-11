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

    @Test
    public void testConstructor() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

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
    public void testSetMeasureWeights_wrongSize() {
        List<Double> weights = Arrays.asList(0.2, 0.2, 0.2, 0.2, 0.2);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            LinguisticSummary.setMeasureWeights(weights);
        });

        assertTrue(exception.getMessage().contains("10"));
    }

    @Test
    public void testSetMeasureWeights_wrongSum() {
        List<Double> weights = Arrays.asList(0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.05);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            LinguisticSummary.setMeasureWeights(weights);
        });

        assertTrue(exception.getMessage().contains("sum"));
    }

    @Test
    public void testCalculateT1_simple() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> dataset = createDataset(10, 20, 22, 24, 26, 28, 30, 35, 40, 50, 60);

        double t1 = summary.calculateT1(dataset);

        assertTrue(t1 >= 0.0 && t1 <= 1.0, "T1 should be in [0,1]");
    }

    @Test
    public void testCalculateT1_emptyDataset() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> emptyDataset = new ArrayList<>();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            summary.calculateT1(emptyDataset);
        });

        assertTrue(exception.getMessage().contains("empty"));
    }

    @Test
    public void testCalculateT1_compound() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        FuzzySet richFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.trapezoidal(6000, 7000, 10000, 10000));

        List<String> fieldNames = Arrays.asList("age", "salary");
        List<FuzzySet> fuzzySets = Arrays.asList(youngFuzzy, richFuzzy);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.AND);
        List<String> lingVars = Arrays.asList("age", "salary");

        Summarizer youngAndRich = new Summarizer("young_and_rich", fieldNames, fuzzySets, connectives, lingVars);

        LinguisticSummary summary = new LinguisticSummary(most, "people", youngAndRich);

        List<SongRecord> dataset = createDataset(10);

        double t1 = summary.calculateT1(dataset);

        assertTrue(t1 >= 0.0 && t1 <= 1.0, "T1 should be in [0,1]");
    }

    @Test
    public void testCalculateT2_simple() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> dataset = createDataset(10);

        double t2 = summary.calculateT2(dataset);

        assertTrue(t2 >= 0.0 && t2 <= 1.0, "T2 should be in [0,1]");
    }

    @Test
    public void testCalculateT2_compound() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        FuzzySet richFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.trapezoidal(6000, 7000, 10000, 10000));

        List<String> fieldNames = Arrays.asList("age", "salary");
        List<FuzzySet> fuzzySets = Arrays.asList(youngFuzzy, richFuzzy);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.AND);
        List<String> lingVars = Arrays.asList("age", "salary");

        Summarizer youngAndRich = new Summarizer("young_and_rich", fieldNames, fuzzySets, connectives, lingVars);

        LinguisticSummary summary = new LinguisticSummary(most, "people", youngAndRich);

        List<SongRecord> dataset = createDataset(10);

        double t2 = summary.calculateT2(dataset);

        assertTrue(t2 >= 0.0 && t2 <= 1.0, "T2 should be in [0,1]");
    }

    @Test
    public void testCalculateT3() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> dataset = createDataset(10);

        double t3 = summary.calculateT3(dataset);

        assertTrue(t3 >= 0.0 && t3 <= 1.0, "T3 should be in [0,1]");
    }

    @Test
    public void testCalculateT4() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> dataset = createDataset(10);

        double t4 = summary.calculateT4(dataset);

        assertTrue(t4 >= 0.0 && t4 <= 1.0, "T4 should be in [0,1]");
    }

    @Test
    public void testCalculateT5_simple() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> dataset = createDataset(10);

        double t5 = summary.calculateT5(dataset);

        assertEquals(1.0, t5, 0.001, "T5 should be 1.0 for single component (2 * 0.5^1)");
    }

    @Test
    public void testCalculateT5_compound() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        FuzzySet richFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.trapezoidal(6000, 7000, 10000, 10000));

        List<String> fieldNames = Arrays.asList("age", "salary");
        List<FuzzySet> fuzzySets = Arrays.asList(youngFuzzy, richFuzzy);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.AND);
        List<String> lingVars = Arrays.asList("age", "salary");

        Summarizer youngAndRich = new Summarizer("young_and_rich", fieldNames, fuzzySets, connectives, lingVars);

        LinguisticSummary summary = new LinguisticSummary(most, "people", youngAndRich);

        List<SongRecord> dataset = createDataset(10);

        double t5 = summary.calculateT5(dataset);

        assertEquals(0.5, t5, 0.001, "T5 should be 0.5 for 2 components (2 * 0.5^2)");
    }

    @Test
    public void testCalculateT6() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> dataset = createDataset(10);

        double t6 = summary.calculateT6(dataset);

        assertTrue(t6 >= 0.0 && t6 <= 1.0, "T6 should be in [0,1]");
    }

    @Test
    public void testCalculateT7() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> dataset = createDataset(10);

        double t7 = summary.calculateT7(dataset);

        assertTrue(t7 >= 0.0 && t7 <= 1.0, "T7 should be in [0,1]");
    }

    @Test
    public void testCalculateT8() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> dataset = createDataset(10);

        double t8 = summary.calculateT8(dataset);

        assertTrue(t8 >= 0.0 && t8 <= 1.0, "T8 should be in [0,1]");
    }

    @Test
    public void testCalculateT9() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> dataset = createDataset(10);

        double t9 = summary.calculateT9(dataset);

        assertEquals(0.0, t9, 0.001, "T9 should be 0.0 for Form 1 summaries");
    }

    @Test
    public void testCalculateT10() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> dataset = createDataset(10);

        double t10 = summary.calculateT10(dataset);

        assertEquals(0.0, t10, 0.001, "T10 should be 0.0 for Form 1 summaries");
    }

    @Test
    public void testCalculateT11() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> dataset = createDataset(10);

        double t11 = summary.calculateT11(dataset);

        assertEquals(1.0, t11, 0.001, "T11 should be 1.0 for Form 1 summaries");
    }

    @Test
    public void testCalculateOptimal() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> dataset = createDataset(10);

        double optimal = summary.calculateOptimal(dataset);

        assertTrue(optimal >= 0.0 && optimal <= 1.0, "Optimal should be in [0,1]");
    }

    @Test
    public void testGenerateSummary_simple() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        String summaryStr = summary.generateSummary();

        assertTrue(summaryStr.contains("MOST"));
        assertTrue(summaryStr.contains("people"));
        assertTrue(summaryStr.contains("young"));
    }

    @Test
    public void testGenerateSummary_compound() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        FuzzySet richFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.trapezoidal(6000, 7000, 10000, 10000));

        List<String> fieldNames = Arrays.asList("age", "salary");
        List<FuzzySet> fuzzySets = Arrays.asList(youngFuzzy, richFuzzy);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.AND);
        List<String> lingVars = Arrays.asList("age", "salary");

        Summarizer youngAndRich = new Summarizer("young_and_rich", fieldNames, fuzzySets, connectives, lingVars);

        LinguisticSummary summary = new LinguisticSummary(most, "people", youngAndRich);

        String summaryStr = summary.generateSummary();

        assertTrue(summaryStr.contains("MOST"));
        assertTrue(summaryStr.contains("people"));
        assertTrue(summaryStr.contains("COMPOUND"));
    }

    @Test
    public void testGenerateSummaryWithMeasures() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> dataset = createDataset(10);

        String summaryWithMeasures = summary.generateSummaryWithMeasures(dataset);

        assertNotNull(summaryWithMeasures);
        assertFalse(summaryWithMeasures.isEmpty());
    }

    @Test
    public void testToString() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        String str = summary.toString();

        assertNotNull(str);
        assertFalse(str.isEmpty());
    }

    @Test
    public void testAllMeasuresInRange() {
        Universe qUniverse = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(qUniverse, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true);

        Universe sUniverse = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(sUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        LinguisticSummary summary = new LinguisticSummary(most, "people", young);

        List<SongRecord> dataset = createDataset(10);

        double t1 = summary.calculateT1(dataset);
        double t2 = summary.calculateT2(dataset);
        double t3 = summary.calculateT3(dataset);
        double t4 = summary.calculateT4(dataset);
        double t5 = summary.calculateT5(dataset);
        double t6 = summary.calculateT6(dataset);
        double t7 = summary.calculateT7(dataset);
        double t8 = summary.calculateT8(dataset);
        double t9 = summary.calculateT9(dataset);
        double t10 = summary.calculateT10(dataset);
        double t11 = summary.calculateT11(dataset);
        double optimal = summary.calculateOptimal(dataset);

        assertTrue(t1 >= 0.0 && t1 <= 1.0, "T1 out of range: " + t1);
        assertTrue(t2 >= 0.0 && t2 <= 1.0, "T2 out of range: " + t2);
        assertTrue(t3 >= 0.0 && t3 <= 1.0, "T3 out of range: " + t3);
        assertTrue(t4 >= 0.0 && t4 <= 1.0, "T4 out of range: " + t4);
        assertTrue(t5 >= 0.0 && t5 <= 1.0, "T5 out of range: " + t5);
        assertTrue(t6 >= 0.0 && t6 <= 1.0, "T6 out of range: " + t6);
        assertTrue(t7 >= 0.0 && t7 <= 1.0, "T7 out of range: " + t7);
        assertTrue(t8 >= 0.0 && t8 <= 1.0, "T8 out of range: " + t8);
        assertTrue(t9 >= 0.0 && t9 <= 1.0, "T9 out of range: " + t9);
        assertTrue(t10 >= 0.0 && t10 <= 1.0, "T10 out of range: " + t10);
        assertTrue(t11 >= 0.0 && t11 <= 1.0, "T11 out of range: " + t11);
        assertTrue(optimal >= 0.0 && optimal <= 1.0, "Optimal out of range: " + optimal);
    }
}