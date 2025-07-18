package org.fuzzy.quantifiers;

import org.fuzzy.FuzzySet;
import org.fuzzy.Universe;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.fuzzy.summarizer.Summarizer;

// Quantifier class inheriting from Summarizer
public class Quantifier extends Summarizer {
    private final boolean isRelative;

    // Constructor for relative quantifiers (operates on [0,1] range)
    public Quantifier(String name, FuzzySet fuzzySet) {
        super(name, "proportion", fuzzySet);
        this.isRelative = true;
    }

    // Constructor for absolute quantifiers
    public Quantifier(String name, FuzzySet fuzzySet, boolean isRelative) {
        super(name, isRelative ? "proportion" : "count", fuzzySet);
        this.isRelative = isRelative;
    }

    public boolean isRelative() {
        return isRelative;
    }

    // Calculate quantifier membership for given r and m
    public double getMembership(double r, int m) {
        if (isRelative) {
            double proportion = m > 0 ? r / m : 0.0;
            return fuzzySet.getMembership(proportion);
        } else {
            return fuzzySet.getMembership(r);
        }
    }

    // Factory methods for common quantifiers
    public static Quantifier most() {
        Universe universe = new Universe(0.0, 1.0, true);
        FuzzySet fuzzySet = new FuzzySet(universe, MembershipFunctions.triangular(0.25, 0.5, 1.));
        return new Quantifier("OKOŁO POŁOWY", fuzzySet);
    }

    public static Quantifier few() {
        Universe universe = new Universe(0.0, 1.0, true);
        FuzzySet fuzzySet = new FuzzySet(universe, MembershipFunctions.trapezoidal(0.0, 0.0, 0.1, 0.3));
        return new Quantifier("few", fuzzySet);
    }

    public static Quantifier about(double count) {
        Universe universe = new Universe(0.0, count * 2, true);
        FuzzySet fuzzySet = new FuzzySet(universe, MembershipFunctions.triangular(
                Math.max(0, count - count * 0.3), count, count + count * 0.3));
        return new Quantifier("about " + (int) count, fuzzySet, false);
    }

    public static Quantifier around(double count) {
        return about(count); // Alias for about
    }


    // Factory methods for absolute qualifiers:

    public static Quantifier moreThan(double threshold, double maxExpectedCount) {
        Universe universe = new Universe(0.0, maxExpectedCount, true);
        FuzzySet fuzzySet = new FuzzySet(universe,
                MembershipFunctions.trapezoidal(threshold, threshold + 0.05 * maxExpectedCount, maxExpectedCount, maxExpectedCount));
        return new Quantifier("więcej niż " + threshold, fuzzySet, false);
    }

    public static Quantifier lessThan(double threshold, double maxExpectedCount) {
        Universe universe = new Universe(0.0, maxExpectedCount, true);
        FuzzySet fuzzySet = new FuzzySet(universe,
                MembershipFunctions.trapezoidal(0.0, 0.0, threshold - 0.05 * maxExpectedCount, threshold));
        return new Quantifier("mniej niż " + threshold, fuzzySet, false);
    }
}