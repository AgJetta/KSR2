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
}