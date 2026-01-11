package org.fuzzy;

import org.fuzzy.membershipFunctions.MembershipFunction;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class FuzzySetTest {

    @Test
    public void testGetMembership_cached() {
        Universe u = new Universe(0, 10, false, 1.0);
        Map<Double, Double> m = Map.of(5.0, 0.8);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, m);

        assertEquals(0.8, fs.getMembership(5.0), 0.001);
    }

    @Test
    public void testGetMembership_evaluateFunction() {
        Universe u = new Universe(0, 10, true);
        MembershipFunction f = x -> x / 10.0;
        FuzzySet fs = new FuzzySet(u, f);

        assertEquals(0.5, fs.getMembership(5.0), 0.001);
        assertEquals(0.8, fs.getMembership(8.0), 0.001);
    }

    @Test
    public void testGetMembership_noFunction() {
        Universe u = new Universe(0, 10, false, 1.0);
        FuzzySet fs = new FuzzySet(u, null, new HashMap<>());

        assertEquals(0.0, fs.getMembership(5.0), 0.001);
    }

    @Test
    public void testCacheMembershipsForPoints() {
        Universe u = new Universe(0, 10, false, 1.0);
        MembershipFunction f = MembershipFunctions.triangular(2, 5, 8);
        FuzzySet fs = new FuzzySet(u, f);

        Set<Double> points = Set.of(2.0, 5.0, 8.0);
        fs.cacheMembershipsForPoints(points);

        assertEquals(0.0, fs.getMembership(2.0), 0.001);
        assertEquals(1.0, fs.getMembership(5.0), 0.001);
        assertEquals(0.0, fs.getMembership(8.0), 0.001);
    }

    @Test
    public void testIsEmpty_true() {
        Universe u = new Universe(0, 10, false, 1.0);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, new HashMap<>());

        assertTrue(fs.isEmpty());
    }

    @Test
    public void testIsEmpty_false() {
        Universe u = new Universe(0, 10, false, 1.0);
        Map<Double, Double> m = Map.of(5.0, 0.5);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, m);

        assertFalse(fs.isEmpty());
    }

    @Test
    public void testIsNormal_true() {
        Universe u = new Universe(0, 10, false, 1.0);
        Map<Double, Double> m = Map.of(5.0, 1.0, 6.0, 0.5);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, m);

        assertTrue(fs.isNormal());
    }

    @Test
    public void testIsNormal_false() {
        Universe u = new Universe(0, 10, false, 1.0);
        Map<Double, Double> m = Map.of(5.0, 0.9, 6.0, 0.5);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, m);

        assertFalse(fs.isNormal());
    }

    @Test
    public void testHeight() {
        Universe u = new Universe(0, 10, false, 1.0);
        Map<Double, Double> m = Map.of(5.0, 0.9, 6.0, 0.5, 7.0, 0.7);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, m);

        assertEquals(0.9, fs.height(), 0.001);
    }

    @Test
    public void testComplement_continuous() {
        Universe u = new Universe(0, 10, true);
        MembershipFunction f = x -> 0.6;
        FuzzySet fs = new FuzzySet(u, f);

        FuzzySet complement = fs.complement();
        assertEquals(0.4, complement.getMembership(5.0), 0.001);
    }

    @Test
    public void testComplement_discrete() {
        Universe u = new Universe(0, 4, false, 1.0);
        u.setCardinalNumber(5);
        Map<Double, Double> m = Map.of(2.0, 0.7);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, m);

        FuzzySet complement = fs.complement();
        assertEquals(0.3, complement.getMembership(2.0), 0.001);
        assertEquals(1.0, complement.getMembership(0.0), 0.001);
    }

    @Test
    public void testUnion_continuous() {
        Universe u = new Universe(0, 10, true);
        FuzzySet fs1 = new FuzzySet(u, x -> 0.3);
        FuzzySet fs2 = new FuzzySet(u, x -> 0.7);

        FuzzySet union = fs1.union(fs2);
        assertEquals(0.7, union.getMembership(5.0), 0.001);
    }

    @Test
    public void testUnion_discrete() {
        Universe u = new Universe(0, 10, false, 1.0);
        Map<Double, Double> m1 = Map.of(2.0, 0.3, 5.0, 0.6);
        Map<Double, Double> m2 = Map.of(2.0, 0.7, 8.0, 0.4);
        FuzzySet fs1 = new FuzzySet(u, x -> 0.0, m1);
        FuzzySet fs2 = new FuzzySet(u, x -> 0.0, m2);

        FuzzySet union = fs1.union(fs2);
        assertEquals(0.7, union.getMembership(2.0), 0.001);
        assertEquals(0.6, union.getMembership(5.0), 0.001);
        assertEquals(0.4, union.getMembership(8.0), 0.001);
    }

    @Test
    public void testIntersection_continuous() {
        Universe u = new Universe(0, 10, true);
        FuzzySet fs1 = new FuzzySet(u, x -> 0.3);
        FuzzySet fs2 = new FuzzySet(u, x -> 0.7);

        FuzzySet intersection = fs1.intersection(fs2);
        assertEquals(0.3, intersection.getMembership(5.0), 0.001);
    }

    @Test
    public void testIntersection_discrete() {
        Universe u = new Universe(0, 10, false, 1.0);
        Map<Double, Double> m1 = Map.of(2.0, 0.3, 5.0, 0.6);
        Map<Double, Double> m2 = Map.of(2.0, 0.7, 5.0, 0.4, 8.0, 0.9);
        FuzzySet fs1 = new FuzzySet(u, x -> 0.0, m1);
        FuzzySet fs2 = new FuzzySet(u, x -> 0.0, m2);

        FuzzySet intersection = fs1.intersection(fs2);
        assertEquals(0.3, intersection.getMembership(2.0), 0.001);
        assertEquals(0.4, intersection.getMembership(5.0), 0.001);
        assertEquals(0.0, intersection.getMembership(8.0), 0.001);
    }

    @Test
    public void testAlphaCut() {
        Universe u = new Universe(0, 10, false, 1.0);
        Map<Double, Double> m = Map.of(2.0, 0.3, 5.0, 0.6, 8.0, 0.9);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, m);

        FuzzySet alphaCut = fs.alphaCut(0.5);
        assertTrue(alphaCut.isClassic());
        assertEquals(1.0, alphaCut.getMembership(5.0), 0.001);
        assertEquals(1.0, alphaCut.getMembership(8.0), 0.001);
        assertEquals(0.0, alphaCut.getMembership(2.0), 0.001);
    }

    @Test
    public void testSupport_discrete() {
        Universe u = new Universe(0, 10, false, 1.0);
        Map<Double, Double> m = Map.of(2.0, 0.3, 5.0, 0.6, 8.0, 0.0);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, m);

        FuzzySet support = fs.support();
        assertTrue(support.isClassic());
        assertEquals(1.0, support.getMembership(2.0), 0.001);
        assertEquals(1.0, support.getMembership(5.0), 0.001);
        assertEquals(0.0, support.getMembership(8.0), 0.001);
    }

    @Test
    public void testSupport_continuous() {
        Universe u = new Universe(0, 10, true);
        MembershipFunction f = MembershipFunctions.triangular(2, 5, 8);
        FuzzySet fs = new FuzzySet(u, f);

        FuzzySet support = fs.support();
        assertTrue(support.isClassic());
        assertTrue(support.getMembership(5.0) > 0);
    }

    @Test
    public void testCore() {
        Universe u = new Universe(0, 10, false, 1.0);
        Map<Double, Double> m = Map.of(2.0, 0.3, 5.0, 1.0, 8.0, 1.0);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, m);

        FuzzySet core = fs.core();
        assertTrue(core.isClassic());
        assertEquals(1.0, core.getMembership(5.0), 0.001);
        assertEquals(1.0, core.getMembership(8.0), 0.001);
        assertEquals(0.0, core.getMembership(2.0), 0.001);
    }

    @Test
    public void testCardinalNumber_discreteFuzzy() {
        Universe u = new Universe(0, 10, false, 1.0);
        Map<Double, Double> m = Map.of(1.0, 1.0, 3.0, 1.0, 5.0, 0.7, 7.0, 0.3);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, m);

        assertEquals(3.0, fs.cardinalNumber(), 0.001);
    }

    @Test
    public void testCardinalNumber_discreteCrisp() {
        Universe u = new Universe(0, 10, false, 1.0);
        Set<Double> elements = Set.of(1.0, 3.0, 5.0, 7.0);
        FuzzySet fs = FuzzySet.classicSet(u, 0, 10, elements);

        assertEquals(4.0, fs.cardinalNumber(), 0.001);
    }

    @Test
    public void testCardinalNumber_continuous_triangular() {
        Universe u = new Universe(0, 10, true);
        MembershipFunction f = MembershipFunctions.triangular(2, 5, 8);
        FuzzySet fs = new FuzzySet(u, f);

        double area = fs.cardinalNumber();
        assertEquals(3.0, area, 0.1);
    }

    @Test
    public void testCardinalNumber_continuous_trapezoidal() {
        Universe u = new Universe(0, 100, true);
        MembershipFunction f = MembershipFunctions.trapezoidal(10, 30, 70, 90);
        FuzzySet fs = new FuzzySet(u, f);

        double area = fs.cardinalNumber();
        assertEquals(60.0, area, 1.0);
    }

    @Test
    public void testCardinalNumber_relativeQuantifier() {
        Universe u = new Universe(0.0, 1.0, true);
        MembershipFunction f = MembershipFunctions.triangular(0.3, 0.5, 0.7);
        FuzzySet fs = new FuzzySet(u, f);

        double area = fs.cardinalNumber();
        assertEquals(0.2, area, 0.01);
    }

    @Test
    public void testSupportMeasure_discrete() {
        Universe u = new Universe(0, 10, false, 1.0);
        Map<Double, Double> m = Map.of(1.0, 1.0, 3.0, 0.5, 5.0, 0.7, 7.0, 0.3);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, m);

        assertEquals(4.0, fs.supportMeasure(), 0.001);
    }

    @Test
    public void testSupportMeasure_continuous() {
        Universe u = new Universe(0, 10, true);
        MembershipFunction f = MembershipFunctions.triangular(2, 5, 8);
        FuzzySet fs = new FuzzySet(u, f);

        double length = fs.supportMeasure();
        assertEquals(6.0, length, 0.1);
    }

    @Test
    public void testSupportMeasure_relativeQuantifier() {
        Universe u = new Universe(0.0, 1.0, true);
        MembershipFunction f = MembershipFunctions.triangular(0.2, 0.5, 0.8);
        FuzzySet fs = new FuzzySet(u, f);

        double length = fs.supportMeasure();
        assertEquals(0.6, length, 0.05);
    }

    @Test
    public void testCardinalityVsSupportMeasure_distinction() {
        Universe u = new Universe(0, 10, false, 1.0);
        Map<Double, Double> m = Map.of(1.0, 1.0, 2.0, 0.7, 3.0, 0.3);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, m);

        double cardinality = fs.cardinalNumber();
        double supportMeasure = fs.supportMeasure();

        assertEquals(2.0, cardinality, 0.001);
        assertEquals(3.0, supportMeasure, 0.001);
        assertNotEquals(cardinality, supportMeasure);
    }

    @Test
    public void testCentroid() {
        Universe u = new Universe(0, 10, false, 1.0);
        Map<Double, Double> m = Map.of(2.0, 1.0, 6.0, 1.0);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, m);

        double centroid = fs.centroid();
        assertEquals(4.0, centroid, 0.001);
    }

    @Test
    public void testDegreeOfFuzziness_discrete() {
        Universe u = new Universe(0, 9, false, 1.0);
        u.setCardinalNumber(10);
        Map<Double, Double> m = Map.of(1.0, 1.0, 3.0, 1.0, 5.0, 0.7, 7.0, 0.3);
        FuzzySet fs = new FuzzySet(u, x -> 0.0, m);

        double fuzziness = fs.degreeOfFuzziness();
        assertEquals(0.4, fuzziness, 0.001);
    }

    @Test
    public void testDegreeOfFuzziness_continuous() {
        Universe u = new Universe(0, 10, true);
        MembershipFunction f = MembershipFunctions.triangular(2, 5, 8);
        FuzzySet fs = new FuzzySet(u, f);

        double fuzziness = fs.degreeOfFuzziness();
        assertEquals(0.6, fuzziness, 0.05);
    }

    @Test
    public void testDegreeOfFuzziness_relativeQuantifier() {
        Universe u = new Universe(0.0, 1.0, true);
        MembershipFunction f = MembershipFunctions.triangular(0.3, 0.5, 0.7);
        FuzzySet fs = new FuzzySet(u, f);

        double fuzziness = fs.degreeOfFuzziness();
        assertEquals(0.4, fuzziness, 0.05);
    }

    @Test
    public void testClassicSet_noElements() {
        Universe u = new Universe(0, 10, false, 1.0);
        FuzzySet fs = FuzzySet.classicSet(u, 0, 10);

        assertTrue(fs.isClassic());
    }

    @Test
    public void testClassicSet_withElements() {
        Universe u = new Universe(0, 10, false, 1.0);
        Set<Double> elements = Set.of(3.0, 5.0, 7.0);
        FuzzySet fs = FuzzySet.classicSet(u, 0, 10, elements);

        assertTrue(fs.isClassic());
        assertEquals(1.0, fs.getMembership(3.0), 0.001);
        assertEquals(1.0, fs.getMembership(5.0), 0.001);
        assertEquals(0.0, fs.getMembership(2.0), 0.001);
    }

    @Test
    public void testBookExample_smallOddDigit() {
        Universe u = new Universe(0, 9, false, 1.0);
        u.setCardinalNumber(10);

        Map<Double, Double> m = Map.of(1.0, 1.0, 3.0, 1.0, 5.0, 0.7, 7.0, 0.3);
        FuzzySet D = new FuzzySet(u, x -> 0.0, m);

        assertEquals(3.0, D.cardinalNumber(), 0.001);

        FuzzySet support = D.support();
        assertTrue(support.isClassic());
        assertEquals(1.0, support.getMembership(1.0), 0.001);
        assertEquals(1.0, support.getMembership(3.0), 0.001);
        assertEquals(1.0, support.getMembership(5.0), 0.001);
        assertEquals(1.0, support.getMembership(7.0), 0.001);

        assertEquals(4.0, D.supportMeasure(), 0.001);
        assertEquals(0.4, D.degreeOfFuzziness(), 0.001);
    }
}