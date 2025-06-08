package org.example.membershipFunctions;

import org.example.membershipFunctions.MembershipFunction;

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
}
