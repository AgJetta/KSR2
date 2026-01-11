package org.fuzzy.summarizer;

import org.fuzzy.*;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class SummarizerTest {

    private SongRecord createRecord(String fieldName, double value) {
        Map<String, Double> attributes = new HashMap<>();
        attributes.put(fieldName, value);
        return new SongRecord(attributes);
    }

    private SongRecord createRecord(Map<String, Double> attributes) {
        return new SongRecord(attributes);
    }

    @Test
    public void testSimpleSummarizer_creation() {
        Universe universe = new Universe(0, 100, true);
        FuzzySet fuzzy = new FuzzySet(universe, MembershipFunctions.trapezoidal(0, 0, 25, 40));

        Summarizer summarizer = new Summarizer("young", "age", fuzzy);

        assertEquals("young", summarizer.getName());
        assertFalse(summarizer.isCompound());
        assertEquals(1, summarizer.getComponentCount());
        assertEquals("age", summarizer.getFieldName(0));
        assertNotNull(summarizer.getFuzzySet(0));
    }

    @Test
    public void testSimpleSummarizer_setLinguisticVariable() {
        Universe universe = new Universe(0, 100, true);
        FuzzySet fuzzy = new FuzzySet(universe, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer summarizer = new Summarizer("young", "age", fuzzy);

        summarizer.setLinguisticVariable(0, "age_var");

        assertEquals("age_var", summarizer.getLinguisticVariable(0));
    }

    @Test
    public void testSimpleSummarizer_getMembership() {
        Universe universe = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(universe, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        SongRecord record1 = createRecord("age", 20.0);
        double membership1 = young.getMembership(record1);
        assertEquals(1.0, membership1, 0.001, "Age 20 should be fully young");

        SongRecord record2 = createRecord("age", 32.5);
        double membership2 = young.getMembership(record2);
        assertEquals(0.5, membership2, 0.001, "Age 32.5 should be 0.5 young");

        SongRecord record3 = createRecord("age", 50.0);
        double membership3 = young.getMembership(record3);
        assertEquals(0.0, membership3, 0.001, "Age 50 should not be young");
    }

    @Test
    public void testSimpleSummarizer_calculateR() {
        Universe universe = new Universe(0, 100, true);
        FuzzySet youngFuzzy = new FuzzySet(universe, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        Summarizer young = new Summarizer("young", "age", youngFuzzy);

        List<SongRecord> dataset = Arrays.asList(
                createRecord("age", 20.0),  // μ = 1.0
                createRecord("age", 30.0),  // μ = 0.667
                createRecord("age", 50.0)   // μ = 0.0
        );

        double r = young.calculateR(dataset);

        assertTrue(r >= 1.0, "R should be at least 1.0");
        assertTrue(r <= 2.0, "R should be at most 2.0");
    }

    @Test
    public void testCompoundSummarizer_creation() {
        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        FuzzySet highSalaryFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.trapezoidal(6000, 7000, 10000, 10000));

        List<String> fieldNames = Arrays.asList("age", "salary");
        List<FuzzySet> fuzzySets = Arrays.asList(youngFuzzy, highSalaryFuzzy);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.AND);
        List<String> lingVars = Arrays.asList("age", "salary");

        Summarizer compound = new Summarizer("young_and_rich", fieldNames, fuzzySets, connectives, lingVars);

        assertEquals("young_and_rich", compound.getName());
        assertTrue(compound.isCompound());
        assertEquals(2, compound.getComponentCount());
        assertEquals("age", compound.getFieldName(0));
        assertEquals("salary", compound.getFieldName(1));
    }

    @Test
    public void testCompoundSummarizer_validationFieldNames() {
        Universe universe = new Universe(0, 100, true);
        FuzzySet fuzzy1 = new FuzzySet(universe, MembershipFunctions.triangular(0, 50, 100));
        FuzzySet fuzzy2 = new FuzzySet(universe, MembershipFunctions.triangular(0, 50, 100));

        List<String> fieldNames = Arrays.asList("field1");
        List<FuzzySet> fuzzySets = Arrays.asList(fuzzy1, fuzzy2);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.AND);
        List<String> lingVars = Arrays.asList("var1", "var2");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Summarizer("invalid", fieldNames, fuzzySets, connectives, lingVars);
        });

        assertTrue(exception.getMessage().contains("field names"));
    }

    @Test
    public void testCompoundSummarizer_validationConnectives() {
        Universe universe = new Universe(0, 100, true);
        FuzzySet fuzzy1 = new FuzzySet(universe, MembershipFunctions.triangular(0, 50, 100));
        FuzzySet fuzzy2 = new FuzzySet(universe, MembershipFunctions.triangular(0, 50, 100));

        List<String> fieldNames = Arrays.asList("field1", "field2");
        List<FuzzySet> fuzzySets = Arrays.asList(fuzzy1, fuzzy2);
        List<LogicalConnective> connectives = Arrays.asList();  // Empty!
        List<String> lingVars = Arrays.asList("var1", "var2");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Summarizer("invalid", fieldNames, fuzzySets, connectives, lingVars);
        });

        assertTrue(exception.getMessage().contains("connectives"));
    }

    @Test
    public void testCompoundSummarizer_validationLingVars() {
        Universe universe = new Universe(0, 100, true);
        FuzzySet fuzzy1 = new FuzzySet(universe, MembershipFunctions.triangular(0, 50, 100));
        FuzzySet fuzzy2 = new FuzzySet(universe, MembershipFunctions.triangular(0, 50, 100));

        List<String> fieldNames = Arrays.asList("field1", "field2");
        List<FuzzySet> fuzzySets = Arrays.asList(fuzzy1, fuzzy2);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.AND);
        List<String> lingVars = Arrays.asList("var1");  // Too few!

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Summarizer("invalid", fieldNames, fuzzySets, connectives, lingVars);
        });

        assertTrue(exception.getMessage().contains("linguistic variables"));
    }

    @Test
    public void testCompoundSummarizer_AND_membership() {
        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        FuzzySet highSalaryFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.trapezoidal(6000, 7000, 10000, 10000));

        List<String> fieldNames = Arrays.asList("age", "salary");
        List<FuzzySet> fuzzySets = Arrays.asList(youngFuzzy, highSalaryFuzzy);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.AND);
        List<String> lingVars = Arrays.asList("age", "salary");

        Summarizer youngAndRich = new Summarizer("young_and_rich", fieldNames, fuzzySets, connectives, lingVars);

        Map<String, Double> attrs1 = new HashMap<>();
        attrs1.put("age", 30.0);
        attrs1.put("salary", 8000.0);
        SongRecord record1 = createRecord(attrs1);

        double membership1 = youngAndRich.getMembership(record1);

        double youngMembership = youngFuzzy.getMembership(30.0);
        double richMembership = highSalaryFuzzy.getMembership(8000.0);
        double expectedMin = Math.min(youngMembership, richMembership);

        assertEquals(expectedMin, membership1, 0.001, "AND should use min");
    }

    @Test
    public void testCompoundSummarizer_AND_membershipZero() {
        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        FuzzySet highSalaryFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.trapezoidal(6000, 7000, 10000, 10000));

        List<String> fieldNames = Arrays.asList("age", "salary");
        List<FuzzySet> fuzzySets = Arrays.asList(youngFuzzy, highSalaryFuzzy);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.AND);
        List<String> lingVars = Arrays.asList("age", "salary");

        Summarizer youngAndRich = new Summarizer("young_and_rich", fieldNames, fuzzySets, connectives, lingVars);

        Map<String, Double> attrs = new HashMap<>();
        attrs.put("age", 50.0);  // Not young
        attrs.put("salary", 9000.0);  // Rich
        SongRecord record = createRecord(attrs);

        double membership = youngAndRich.getMembership(record);

        assertEquals(0.0, membership, 0.001, "Should be 0 when one component is 0");
    }

    @Test
    public void testCompoundSummarizer_OR_membership() {
        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        FuzzySet highSalaryFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.trapezoidal(6000, 7000, 10000, 10000));

        List<String> fieldNames = Arrays.asList("age", "salary");
        List<FuzzySet> fuzzySets = Arrays.asList(youngFuzzy, highSalaryFuzzy);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.OR);
        List<String> lingVars = Arrays.asList("age", "salary");

        Summarizer youngOrRich = new Summarizer("young_or_rich", fieldNames, fuzzySets, connectives, lingVars);

        Map<String, Double> attrs = new HashMap<>();
        attrs.put("age", 50.0);  // Not young (μ = 0)
        attrs.put("salary", 8000.0);  // Rich (μ = 1)
        SongRecord record = createRecord(attrs);

        double membership = youngOrRich.getMembership(record);

        assertEquals(1.0, membership, 0.001, "OR should use max");
    }

    @Test
    public void testCompoundSummarizer_threeComponents() {
        Universe universe = new Universe(0, 100, true);

        FuzzySet fuzzy1 = new FuzzySet(universe, MembershipFunctions.triangular(0, 25, 50));
        FuzzySet fuzzy2 = new FuzzySet(universe, MembershipFunctions.triangular(25, 50, 75));
        FuzzySet fuzzy3 = new FuzzySet(universe, MembershipFunctions.triangular(50, 75, 100));

        List<String> fieldNames = Arrays.asList("field1", "field2", "field3");
        List<FuzzySet> fuzzySets = Arrays.asList(fuzzy1, fuzzy2, fuzzy3);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.AND, LogicalConnective.AND);
        List<String> lingVars = Arrays.asList("var1", "var2", "var3");

        Summarizer compound = new Summarizer("triple", fieldNames, fuzzySets, connectives, lingVars);

        assertEquals(3, compound.getComponentCount());

        Map<String, Double> attrs = new HashMap<>();
        attrs.put("field1", 25.0);
        attrs.put("field2", 50.0);
        attrs.put("field3", 75.0);
        SongRecord record = createRecord(attrs);

        double membership = compound.getMembership(record);
        assertTrue(membership >= 0.0 && membership <= 1.0);
    }

    @Test
    public void testCompoundSummarizer_calculateR() {
        Universe ageUniverse = new Universe(0, 100, true);
        Universe salaryUniverse = new Universe(0, 10000, true);

        FuzzySet youngFuzzy = new FuzzySet(ageUniverse, MembershipFunctions.trapezoidal(0, 0, 25, 40));
        FuzzySet highSalaryFuzzy = new FuzzySet(salaryUniverse, MembershipFunctions.trapezoidal(6000, 7000, 10000, 10000));

        List<String> fieldNames = Arrays.asList("age", "salary");
        List<FuzzySet> fuzzySets = Arrays.asList(youngFuzzy, highSalaryFuzzy);
        List<LogicalConnective> connectives = Arrays.asList(LogicalConnective.AND);
        List<String> lingVars = Arrays.asList("age", "salary");

        Summarizer youngAndRich = new Summarizer("young_and_rich", fieldNames, fuzzySets, connectives, lingVars);

        List<SongRecord> dataset = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Double> attrs = new HashMap<>();
            attrs.put("age", 20.0 + i * 5);
            attrs.put("salary", 7000.0 + i * 500);
            dataset.add(createRecord(attrs));
        }

        double r = youngAndRich.calculateR(dataset);

        assertTrue(r >= 0.0, "R should be non-negative");
        assertTrue(r <= dataset.size(), "R should not exceed dataset size");
    }

    @Test
    public void testGenerateDescription_simple() {
        Universe universe = new Universe(0, 100, true);
        FuzzySet fuzzy = new FuzzySet(universe, MembershipFunctions.triangular(0, 50, 100));
        Summarizer summarizer = new Summarizer("young", "age", fuzzy);
        summarizer.setLinguisticVariable(0, "age");

        String description = summarizer.generateDescription();

        assertTrue(description.contains("young"));
        assertTrue(description.contains("age"));
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
        List<String> lingVars = Arrays.asList("age", "salary");

        Summarizer compound = new Summarizer("young_and_rich", fieldNames, fuzzySets, connectives, lingVars);

        String description = compound.generateDescription();

        assertTrue(description.contains("AND"));
    }

    @Test
    public void testToString() {
        Universe universe = new Universe(0, 100, true);
        FuzzySet fuzzy = new FuzzySet(universe, MembershipFunctions.triangular(0, 50, 100));
        Summarizer summarizer = new Summarizer("young", "age", fuzzy);
        summarizer.setLinguisticVariable(0, "age");

        String str = summarizer.toString();

        assertNotNull(str);
        assertFalse(str.isEmpty());
    }
}