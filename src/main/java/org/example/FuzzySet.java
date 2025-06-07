package org.example;

import org.example.membershipFunctions.MembershipFunction;

import java.util.ArrayList;
import java.util.List;

public class FuzzySet extends Set {

    private boolean isComplex;
    private double height;
    private MembershipFunction mf;

    public FuzzySet(UniverseOfDiscourse universeOfDiscourse, List<Element> support, boolean isComplex, double height, MembershipFunction mf) {
        super(universeOfDiscourse, support);
        this.isComplex = isComplex;
        this.height = height;
        this.mf = mf;
    }


    public double getHeight() {
        return height;
    }

    public double getMembership(double x) {
        return height * mf.evaluate(x);
    }

    public MembershipFunction getMembershipFunction() {
        return mf;
    }
    @Override
    public List<Element> getSupport() {
    }

    public List<Element> getAlphaCut(double alpha) {
    }
}
