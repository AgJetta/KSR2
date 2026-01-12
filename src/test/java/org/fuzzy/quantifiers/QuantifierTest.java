package org.fuzzy.quantifiers;

import org.fuzzy.FuzzySet;
import org.fuzzy.Universe;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QuantifierTest {

    @Test
    public void testRelativeQuantifier_validCreation() {
        Universe universe = new Universe(0.0, 1.0, true);
        FuzzySet fuzzy = new FuzzySet(universe, MembershipFunctions.triangular(0.3, 0.5, 0.7));

        Quantifier quantifier = new Quantifier(
                "ABOUT HALF",
                fuzzy,
                true,
                "triangular",
                new double[]{0.3, 0.5, 0.7}
        );

        assertEquals("ABOUT HALF", quantifier.getName());
        assertTrue(quantifier.isRelative());
        assertNotNull(quantifier.getFuzzySet());
        assertEquals("triangular", quantifier.getFunctionType());
        assertArrayEquals(new double[]{0.3, 0.5, 0.7}, quantifier.getParameters());
    }

    @Test
    public void testAbsoluteQuantifier_validCreation() {
        Universe universe = new Universe(0, 30000, false, 1.0);
        FuzzySet fuzzy = new FuzzySet(universe, MembershipFunctions.triangular(1000, 5000, 9000));

        Quantifier quantifier = new Quantifier(
                "ABOUT 5000",
                fuzzy,
                false,
                "triangular",
                new double[]{1000, 5000, 9000}
        );

        assertEquals("ABOUT 5000", quantifier.getName());
        assertFalse(quantifier.isRelative());
        assertNotNull(quantifier.getFuzzySet());
    }

    @Test
    public void testRelativeQuantifier_rejectsDiscreteUniverse() {
        Universe discreteUniverse = new Universe(0, 1000, false, 1.0);
        FuzzySet fuzzy = new FuzzySet(discreteUniverse, MembershipFunctions.triangular(100, 500, 900));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Quantifier("INVALID", fuzzy, true, "triangular", new double[]{100, 500, 900});
        });

        assertTrue(exception.getMessage().contains("continuous"));
    }

    @Test
    public void testRelativeQuantifier_rejectsWrongRange() {
        Universe wrongRange = new Universe(0.0, 2.0, true);
        FuzzySet fuzzy = new FuzzySet(wrongRange, MembershipFunctions.triangular(0.5, 1.0, 1.5));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Quantifier("INVALID", fuzzy, true, "triangular", new double[]{0.5, 1.0, 1.5});
        });

        assertTrue(exception.getMessage().contains("[0,1]"));
    }

    @Test
    public void testAbsoluteQuantifier_rejectsContinuousUniverse() {
        Universe continuousUniverse = new Universe(0.0, 1000.0, true);
        FuzzySet fuzzy = new FuzzySet(continuousUniverse, MembershipFunctions.triangular(100, 500, 900));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Quantifier("INVALID", fuzzy, false, "triangular", new double[]{100, 500, 900});
        });

        assertTrue(exception.getMessage().contains("discrete"));
    }

    @Test
    public void testAbsoluteQuantifier_rejectsNegativeUniverse() {
        Universe negativeUniverse = new Universe(-100, 1000, false, 1.0);
        FuzzySet fuzzy = new FuzzySet(negativeUniverse, MembershipFunctions.triangular(-50, 500, 900));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Quantifier("INVALID", fuzzy, false, "triangular", new double[]{-50, 500, 900});
        });

        assertTrue(exception.getMessage().contains("non-negative"));
    }

    @Test
    public void testRelativeQuantifier_membershipCalculation() {
        Universe universe = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(universe, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true, "trapezoidal", new double[]{0.5, 0.7, 0.9, 1.0});

        double r1 = 80.0;
        int m1 = 100;
        double membership1 = most.getMembership(r1, m1);

        assertEquals(0.8, r1 / m1, 0.001);
        assertTrue(membership1 > 0.0, "Should have non-zero membership for 80%");
        assertTrue(membership1 <= 1.0, "Membership should not exceed 1.0");
    }

    @Test
    public void testRelativeQuantifier_extremeValues() {
        Universe universe = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(universe, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true, "trapezoidal", new double[]{0.5, 0.7, 0.9, 1.0});

        double membershipZero = most.getMembership(0.0, 100);
        assertEquals(0.0, membershipZero, 0.001, "Should have zero membership for 0%");

        double membership80 = most.getMembership(80.0, 100);
        // NOTE: trapezoidal may peak at 1.0 around 0.7-0.9 proportion
        assertTrue(membership80 > 0.0 && membership80 <= 1.0);

        double membership100 = most.getMembership(100.0, 100);
        assertEquals(0.0, membership100, 0.001, "Should be 0 at 100%");
    }

    @Test
    public void testRelativeQuantifier_emptyDataset() {
        Universe universe = new Universe(0.0, 1.0, true);
        FuzzySet mostFuzzy = new FuzzySet(universe, MembershipFunctions.trapezoidal(0.5, 0.7, 0.9, 1.0));
        Quantifier most = new Quantifier("MOST", mostFuzzy, true, "trapezoidal", new double[]{0.5, 0.7, 0.9, 1.0});

        double membership = most.getMembership(0.0, 0);
        assertEquals(0.0, membership, 0.001, "Empty dataset should give 0 membership");
    }

    @Test
    public void testAbsoluteQuantifier_membershipCalculation() {
        Universe universe = new Universe(0, 10000, false, 1.0);
        FuzzySet aboutFuzzy = new FuzzySet(universe, MembershipFunctions.triangular(4000, 5000, 6000));
        Quantifier about5000 = new Quantifier("ABOUT 5000", aboutFuzzy, false, "triangular", new double[]{4000, 5000, 6000});

        double membership1 = about5000.getMembership(5000.0, 10000);
        assertEquals(1.0, membership1, 0.001, "Should have full membership at peak");

        double membership2 = about5000.getMembership(4500.0, 10000);
        assertEquals(0.5, membership2, 0.001, "Should have 0.5 membership at midpoint");

        double membership3 = about5000.getMembership(3000.0, 10000);
        assertEquals(0.0, membership3, 0.001, "Should have zero membership outside support");
    }

    @Test
    public void testGetSupportMeasure_relative() {
        Universe universe = new Universe(0.0, 1.0, true);
        FuzzySet fuzzy = new FuzzySet(universe, MembershipFunctions.triangular(0.3, 0.5, 0.7));
        Quantifier quantifier = new Quantifier("ABOUT HALF", fuzzy, true, "triangular", new double[]{0.3, 0.5, 0.7});

        double supportMeasure = quantifier.getSupportMeasure();

        assertTrue(supportMeasure > 0.0, "Support measure should be positive");
        assertTrue(supportMeasure <= 0.4, "Support should be approximately 0.4 for triangle (0.3 to 0.7)");
    }

    @Test
    public void testGetSupportMeasure_absolute() {
        Universe universe = new Universe(0, 10000, false, 1.0);
        FuzzySet fuzzy = new FuzzySet(universe, MembershipFunctions.triangular(4000, 5000, 6000));
        Quantifier quantifier = new Quantifier("ABOUT 5000", fuzzy, false, "triangular", new double[]{4000, 5000, 6000});

        double supportMeasure = quantifier.getSupportMeasure();

        assertTrue(supportMeasure > 0.0, "Support measure should be positive");
        assertTrue(supportMeasure <= 2100.0, "Support should be approximately 2000 (4000 to 6000)");
    }

    @Test
    public void testGetCardinality_relative() {
        Universe universe = new Universe(0.0, 1.0, true);
        FuzzySet fuzzy = new FuzzySet(universe, MembershipFunctions.triangular(0.3, 0.5, 0.7));
        Quantifier quantifier = new Quantifier("ABOUT HALF", fuzzy, true, "triangular", new double[]{0.3, 0.5, 0.7});

        double cardinality = quantifier.getCardinality();

        assertTrue(cardinality > 0.0, "Cardinality should be positive");
        assertTrue(cardinality <= 1.0, "Cardinality should not exceed universe measure");
    }

    @Test
    public void testGetCardinality_absolute() {
        Universe universe = new Universe(0, 10000, false, 1.0);
        universe.setCardinalNumber(10000);
        FuzzySet fuzzy = new FuzzySet(universe, MembershipFunctions.triangular(4000, 5000, 6000));
        Quantifier quantifier = new Quantifier("ABOUT 5000", fuzzy, false, "triangular", new double[]{4000, 5000, 6000});

        double cardinality = quantifier.getCardinality();

        assertTrue(cardinality > 0.0, "Cardinality should be positive");
    }

    @Test
    public void testToString() {
        Universe universe = new Universe(0.0, 1.0, true);
        FuzzySet fuzzy = new FuzzySet(universe, MembershipFunctions.triangular(0.3, 0.5, 0.7));
        Quantifier quantifier = new Quantifier("MOST", fuzzy, true, "triangular", new double[]{0.3, 0.5, 0.7});

        String str = quantifier.toString();

        assertTrue(str.contains("MOST"));
        assertTrue(str.contains("relative"));
    }
}
