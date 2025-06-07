package org.example.membershipFunctions;

public class TriangularMF extends MembershipFunction {

    private double a;
    private double b;
    private double c;

    public TriangularMF(double a, double b, double c) {
        if (!(a <= b && b <= c)) {
            throw new IllegalArgumentException("Triangle parameters must satisfy a ≤ b ≤ c");
        }
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public double evaluate(double x) {
        if (x <= a || x >= c) {
            return 0.0;
        } else if (x == b) {
            return 1.0;
        } else if (x > a && x < b) {
            return (x - a) / (b - a);
        } else if (x > b && x < c) {
            return (c - x) / (c - b);
        } else {
            return 0.0;
        }
    }
}
