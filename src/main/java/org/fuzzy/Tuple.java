package org.fuzzy;

// Tuple class for (x, membership) pairs
public record Tuple(double x, double membership) {
    public Tuple {
        if (membership < 0.0 || membership > 1.0) {
            throw new IllegalArgumentException("Membership must be in [0, 1]");
        }
    }

    public boolean isInSet() {
        return membership > 0.0;
    }

    public boolean isFullyInSet() {
        return membership == 1.0;
    }
}
