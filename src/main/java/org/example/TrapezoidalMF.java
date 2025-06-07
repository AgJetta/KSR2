package org.example;

public class TrapezoidalMF extends MembershipFunction {

    private double a;
    private double b;
    private double c;
    private double d;

    public TrapezoidalMF(double a, double b, double c, double d) {
        if (!(a <= b && b <= c && c <= d)) {
            throw new IllegalArgumentException("Trapezoid parameters must satisfy a ≤ b ≤ c ≤ d");
        }
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    @Override
    public double evaluate(double x) {
        if (x <= a || x >= d) {
            return 0.0;
        } else if (x > a && x <= b) {
            return (x - a) / (b - a);
        } else if (x > b && x <= c) {
            return 1.0;
        } else if (x > c && x < d) {
            return (d - x) / (d - c);
        } else {
            return 0.0;
        }
    }
}