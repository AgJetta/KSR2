package org.example.fuzzyQuantifiers;

import org.example.FuzzySet;
import org.example.Universe;
import org.example.membershipFunctions.MembershipFunctions;

public class AbsoluteFuzzyQuantifier extends FuzzyQuantifier {

    //     public WHATA??? what it shoudl be??? absoluteQuanitfy(FuzzySet set, double percentage) {
    public FuzzySet absoluteQuanitfy(FuzzySet set, double percentage) {

        // essentially, this should take like chosen percentage of fuzzyset and quantify it.
        // So we take all elemeents of fuzzy set ( support ) and than take almost all of them
        // Wchich is support * 95% for example - almost all
        // Needs to be implemented somehow

        return new FuzzySet(new Universe(1, 2, false, 0.5), MembershipFunctions.triangular(1,2 ,3)) ;
    }

}
