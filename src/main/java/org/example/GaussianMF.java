package org.example;

public class GaussianMF extends MembershipFunction {

    private double a; // mean
    private double b; // standard deviation

    public GaussianMF(double a, double b) {
        if (b <= 0) {
            throw new IllegalArgumentException("Standard deviation must be positive");
        }
        this.a = a;
        this.b = b;
    }

    @Override
    public double evaluate(double x) {
        return Math.exp(-0.5 * Math.pow((x - a) / b, 2));
    }
}
