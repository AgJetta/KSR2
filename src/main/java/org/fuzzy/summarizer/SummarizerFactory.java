package org.fuzzy.summarizer;

import org.fuzzy.FuzzySet;
import org.fuzzy.Universe;
import org.fuzzy.membershipFunctions.MembershipFunction;
import org.fuzzy.membershipFunctions.MembershipFunctions;

// Factory class for creating common summarizers
public class SummarizerFactory {

    // Custom summarizer factory method
    public static Summarizer custom(String name, String fieldName, Universe universe, MembershipFunction membershipFunction) {
        FuzzySet fuzzySet = new FuzzySet(universe, membershipFunction);
        return new Summarizer(name, fieldName, fuzzySet);
    }
}
