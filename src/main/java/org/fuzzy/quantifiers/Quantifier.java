package org.fuzzy.quantifiers;

import org.fuzzy.FuzzySet;
import org.fuzzy.Universe;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.fuzzy.summarizer.Summarizer;

// Quantifier class inheriting from Summarizer
public class Quantifier extends Summarizer {
    private final boolean isRelative;

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
        this.end = end;
    }

    private double start;
    private double end;

    // Constructor for relative quantifiers (operates on [0,1] range)
    public Quantifier(String name, FuzzySet fuzzySet) {
        super(name, "proportion", fuzzySet);
        this.isRelative = true;
    }

    // Constructor for absolute quantifiers
    public Quantifier(String name, FuzzySet fuzzySet, boolean isRelative, double start, double end) {
        super(name, isRelative ? "proportion" : "count", fuzzySet);
        this.isRelative = isRelative;
        this.start = start;
        this.end = end;
    }


    public boolean isRelative() {
        return isRelative;
    }

    // Calculate quantifier membership for given r and m
    public double getMembership(double r, int m) {
        if (isRelative) {
            double proportion = m > 0 ? r / m : 0.0;
            return fuzzySet.getMembershipFunction().apply(proportion);
        } else {
            return fuzzySet.getMembershipFunction().apply(r);
        }
    }

    public double getSupportCardinalNumber() {
        // Integral of the membership function over the universe of discourse
        // For relative, this is the area under the curve in [0,1]
        // For absolute, this is the area under the curve in [0, max] == [0, 30000] for our dataset
        double supportInterval = this.end - this.start;
        return supportInterval;
    }
}