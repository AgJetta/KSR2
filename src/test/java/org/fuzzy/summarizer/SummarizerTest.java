package org.fuzzy.summarizer;

import org.fuzzy.*;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SummarizerTest {

    private SongRecord createRecord(String fieldName, double value) {
        Map<String, Double> attributes = new HashMap<>();
        attributes.put(fieldName, value);
        return new SongRecord(attributes);
    }

    private SongRecord createRecord(Map<String, Double> attributes) {
        return new SongRecord(attributes);
    }

    // ------------------- Atomic summarizer tests -------------------

    @Test
    public void testSimpleSummarizer_creation() {
        Universe universe = new Universe(0, 100, true);
        FuzzySet fuzzy = new FuzzySet(universe, MembershipFunctions.trapezoidal(0, 0, 25, 40));

        Summarizer summarizer = new Summarizer(
                "young",
                "age",
                fuzzy,
                "trapezoidal",
                new double[]{0, 0, 25, 40},
                universe
        );

        assertEquals("young", summarizer.getName());
        assertFalse(summarizer.isCompound());
        assertEquals(1, summarizer.getComponentCount());
        assertEquals("age", summarizer.getFieldName(0));
        assertNotNull(summarizer.getFuzzySet(0));
        assertEquals("trapezoidal", summarizer.getFunctionType());
        assertArrayEquals(new double[]{0, 0, 25, 40}, summarizer.getParameters());
    }

    @Test
    public void testSimpleSummarizer_getMembership() {
        Universe universe = new Universe(0, 100, true);
        FuzzySet fuzzy = new FuzzySet(universe, MembershipFunctions.trapezoidal(0, 0, 25, 40));

        Summarizer summarizer = new Summarizer(
                "young",
                "age",
                fuzzy,
                "trapezoidal",
                new double[]{0, 0, 25, 40},
                universe
        );

        SongRecord record1 = createRecord("age", 20.0);
        assertEquals(1.0, summarizer.getMembership(record1), 0.001);

        SongRecord record2 = createRecord("age", 32.5);
        assertEquals(0.5, summarizer.getMembership(record2), 0.001);

        SongRecord record3 = createRecord("age", 50.0);
        assertEquals(0.0, summarizer.getMembership(record3), 0.001);
    }

    @Test
    public void testSimpleSummarizer_calculateR() {
        Universe universe = new Universe(0, 100, true);
        FuzzySet fuzzy = new FuzzySet(universe, MembershipFunctions.trapezoidal(0, 0, 25, 40));

        Summarizer summarizer = new Summarizer(
                "young",
                "age",
                fuzzy,
                "trapezoidal",
                new double[]{0, 0, 25, 40},
                universe
        );

        List<SongRecord> dataset = Arrays.asList(
                createRecord("age", 20.0),  // μ = 1.0
                createRecord("age", 30.0),  // μ ≈ 0.667
                createRecord("age", 50.0)   // μ = 0.0
        );

        double r = summarizer.calculateR(dataset);
        assertTrue(r >= 1.0 && r <= 2.0);
    }

    // ------------------- Compound summarizer tests -------------------

    @Test
    public void testCompoundSummarizer_creation() {
        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        FuzzySet richFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.trapezoidal(6000, 7000, 10000, 10000));

        List<String> fieldNames = Arrays.asList("age", "salary");
        List<FuzzySet> fuzzySets = Arrays.asList(youngFuzzy, richFuzzy);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.AND);
        List<String> lingVars = Arrays.asList("young", "rich");

        Summarizer compound = new Summarizer("young_and_rich", fieldNames, fuzzySets, connectives, lingVars);

        assertEquals("young_and_rich", compound.getName());
        assertTrue(compound.isCompound());
        assertEquals(2, compound.getComponentCount());
        assertEquals("age", compound.getFieldName(0));
        assertEquals("salary", compound.getFieldName(1));
    }

    @Test
    public void testCompoundSummarizer_AND_membership() {
        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        FuzzySet richFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.trapezoidal(6000, 7000, 10000, 10000));

        List<String> fieldNames = Arrays.asList("age", "salary");
        List<FuzzySet> fuzzySets = Arrays.asList(youngFuzzy, richFuzzy);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.AND);
        List<String> lingVars = Arrays.asList("young", "rich");

        Summarizer compound = new Summarizer("young_and_rich", fieldNames, fuzzySets, connectives, lingVars);

        SongRecord record = createRecord(Map.of("age", 30.0, "salary", 8000.0));
        double membership = compound.getMembership(record);

        double expected = Math.min(youngFuzzy.getMembership(30.0), richFuzzy.getMembership(8000.0));
        assertEquals(expected, membership, 0.001);
    }

    @Test
    public void testCompoundSummarizer_OR_membership() {
        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        FuzzySet richFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.trapezoidal(6000, 7000, 10000, 10000));

        List<String> fieldNames = Arrays.asList("age", "salary");
        List<FuzzySet> fuzzySets = Arrays.asList(youngFuzzy, richFuzzy);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.OR);
        List<String> lingVars = Arrays.asList("young", "rich");

        Summarizer compound = new Summarizer("young_or_rich", fieldNames, fuzzySets, connectives, lingVars);

        SongRecord record = createRecord(Map.of("age", 50.0, "salary", 8000.0));
        double membership = compound.getMembership(record);

        double expected = Math.max(youngFuzzy.getMembership(50.0), richFuzzy.getMembership(8000.0));
        assertEquals(expected, membership, 0.001);
    }

    @Test
    public void testGenerateDescription_compound() {
        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.triangular(0, 25, 50));
        FuzzySet richFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.triangular(5000, 7500, 10000));

        List<String> fieldNames = Arrays.asList("age", "salary");
        List<FuzzySet> fuzzySets = Arrays.asList(youngFuzzy, richFuzzy);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.AND);
        List<String> lingVars = Arrays.asList("young", "rich");

        Summarizer compound = new Summarizer("young_and_rich", fieldNames, fuzzySets, connectives, lingVars);

        String description = compound.generateDescription();
        assertTrue(description.contains("AND"));
        assertTrue(description.contains("young"));
        assertTrue(description.contains("rich"));
    }
}
