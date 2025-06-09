package org.fuzzy.membershipFunctions;

public class MembershipFunctions {

    public static MembershipFunction triangular(double a, double b, double c) {
        return x -> {
            if (x <= a || x >= c) return 0.0;
            if (x <= b) return (x - a) / (b - a);
            return (c - x) / (c - b);
        };
    }

    public static MembershipFunction trapezoidal(double a, double b, double c, double d) {
        return x -> {
            if (x <= a || x >= d) return 0.0;
            if (x <= b) return (x - a) / (b - a);
            if (x <= c) return 1.0;
            return (d - x) / (d - c);
        };
    }

    public static MembershipFunction gaussian(double center, double sigma) {
        return x -> Math.exp(-0.5 * Math.pow((x - center) / sigma, 2));
    }

    // Classic set membership (crisp)
    public static MembershipFunction crisp(double start, double end) {
        return x -> (x >= start && x <= end) ? 1.0 : 0.0;
    }

    public static MembershipFunction rampDown(double start, double end) {
        return x -> {
            if (x <= start) return 1.0;
            if (x >= end) return 0.0;
            return (end - x) / (end - start);
        };
    }

    public static MembershipFunction rampUp(double start, double end) {
        return x -> {
            if (x <= start) return 0.0;
            if (x >= end) return 1.0;
            return (x - start) / (end - start);
        };
    }


}
