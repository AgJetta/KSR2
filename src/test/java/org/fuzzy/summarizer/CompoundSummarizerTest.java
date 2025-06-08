package org.fuzzy.summarizer;

import org.fuzzy.FuzzySet;
import org.fuzzy.LogicalConnective;
import org.fuzzy.Universe;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CompoundSummarizerTest {

    private Universe universe;
    private FuzzySet setA;
    private FuzzySet setB;
    private Summarizer summarizerA;
    private Summarizer summarizerB;

    @BeforeEach
    void setUp() {
        universe = new Universe(0.0, 5.0, true);

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

        summarizerA = new Summarizer("Tall", "Height", setA);
        summarizerB = new Summarizer("Heavy", "Height", setB); // same field for simplicity
    }

    @Test
    void testNegationConstructor() {
        CompoundSummarizer notA = new CompoundSummarizer(summarizerA);

        assertEquals("NOT Tall", notA.getName());
        assertEquals("Height", notA.getFieldName());

        notA.getFuzzySet().getTuples().forEach(tuple -> {
            double expected = 1.0 - summarizerA.getFuzzySet().getMembership(tuple.x());
            assertEquals(expected, tuple.membership(), 1e-6);
        });
    }

    @Test
    void testAndConstructor() {
        CompoundSummarizer andSummarizer = new CompoundSummarizer(summarizerA, summarizerB, LogicalConnective.AND);

        assertEquals("Tall AND Heavy", andSummarizer.getName());
        assertEquals("Compound Summarizer", andSummarizer.getFieldName());

        andSummarizer.getFuzzySet().getTuples().forEach(tuple -> {
            double expected = Math.min(
                    summarizerA.getFuzzySet().getMembership(tuple.x()),
                    summarizerB.getFuzzySet().getMembership(tuple.x())
            );
            assertEquals(expected, tuple.membership(), 1e-6);
        });
    }

    @Test
    void testOrConstructor() {
        CompoundSummarizer orSummarizer = new CompoundSummarizer(summarizerA, summarizerB, LogicalConnective.OR);

        assertEquals("Tall OR Heavy", orSummarizer.getName());
        assertEquals("Compound Summarizer", orSummarizer.getFieldName());

        orSummarizer.getFuzzySet().getTuples().forEach(tuple -> {
            double expected = Math.max(
                    summarizerA.getFuzzySet().getMembership(tuple.x()),
                    summarizerB.getFuzzySet().getMembership(tuple.x())
            );
            assertEquals(expected, tuple.membership(), 1e-6);
        });
    }
}
