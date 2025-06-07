package org.example;

import java.util.List;

public class FuzzySet extends Set {

    private boolean isComplex;
    private double height;
    private MembershipFunction mf;


    public FuzzySet(UniverseOfDiscourse universeOfDiscourse, List<element> support, boolean isComplex, double height, MembershipFunction mf) {
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
    public List<element> getSupport() {
        // Return all elements where membership > 0
        List<element> nonZeroSupport = new ArrayList<>();
        for (element e : support) {
            if (getMembership(e.getValue()) > 0.0) {
                nonZeroSupport.add(e);
            }
        }
        return nonZeroSupport;
    }

    public List<element> getAlphaCut(double alpha) {
        List<element> alphaSet = new ArrayList<>();
        for (element e : support) {
            if (getMembership(e.getValue()) >= alpha) {
                alphaSet.add(e);
            }
        }
        return alphaSet;
    }
}
