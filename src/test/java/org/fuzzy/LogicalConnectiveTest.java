package org.fuzzy;

import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;

class LogicalConnectiveTest {

    private Universe universe;
    private FuzzySet setA;
    private FuzzySet setB;

    @BeforeEach
    void setUp() {
        // Define a discrete universe
        universe = new Universe(0.0, 5.0, true);

        // Define two fuzzy sets with explicit memberships for testing
        setA = new FuzzySet(universe, MembershipFunctions.triangular(1, 3, 5), Map.of(
                1.0, 0.0,
                2.0, 0.5,
                3.0, 1.0,
                4.0, 0.5,
                5.0, 0.0
        ));

        setB = new FuzzySet(universe, MembershipFunctions.triangular(2, 4, 6), Map.of(
                1.0, 0.0,
                2.0, 0.5,
                3.0, 0.75,
                4.0, 1.0,
                5.0, 0.5
        ));
    }

    @Test
    void testAnd() {
        FuzzySet result = LogicalConnective.AND.apply(setA, setB);

        assertEquals(universe, result.getUniverse());

        // Intersection is min of memberships
        result.getTuples().forEach(tuple -> {
            double expectedMembership = Math.min(setA.getMembership(tuple.x()), setB.getMembership(tuple.x()));
            assertEquals(expectedMembership, tuple.membership(), 1e-6);
        });
    }

    @Test
    void testOr() {
        FuzzySet result = LogicalConnective.OR.apply(setA, setB);

        assertEquals(universe, result.getUniverse());

        // Union is max of memberships
        result.getTuples().forEach(tuple -> {
            double expectedMembership = Math.max(setA.getMembership(tuple.x()), setB.getMembership(tuple.x()));
            assertEquals(expectedMembership, tuple.membership(), 1e-6);
        });
    }

    @Test
    void testNot() {
        FuzzySet result = LogicalConnective.NOT.apply(setA);

        assertEquals(universe, result.getUniverse());

        // Complement is 1 - membership
        result.getTuples().forEach(tuple -> {
            double expectedMembership = 1.0 - setA.getMembership(tuple.x());
            assertEquals(expectedMembership, tuple.membership(), 1e-6);
        });

    }

    @Test
    void testUnaryOnTwoOperandsThrows() {
        assertThrows(UnsupportedOperationException.class, () -> LogicalConnective.NOT.apply(setA, setB));
    }

    @Test
    void testBinaryOnOneOperandThrows() {
        assertThrows(UnsupportedOperationException.class, () -> LogicalConnective.AND.apply(setA));
    }
}
