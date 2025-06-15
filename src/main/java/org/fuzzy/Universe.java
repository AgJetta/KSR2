package org.fuzzy;

import java.util.ArrayList;
import java.util.List;

public class Universe {
    private final double start;
    private final double end;
    private int cardinalNumber = 30000;
    private final boolean isDense;
    private final double step; // for discrete universes

    public Universe(double start, double end, boolean isDense) {
        this(start, end, isDense, 1.0);
    }

    public Universe(double start, double end, boolean isDense, double step) {
        if (start >= end) {
            throw new IllegalArgumentException("Start must be less than end");
        }
        if (!isDense && step <= 0) {
            throw new IllegalArgumentException("Step must be positive for discrete universe");
        }

        this.start = start;
        this.end = end;
        this.isDense = isDense;
        this.step = step;
    }

    public double getStart() { return start; }
    public double getEnd() { return end; }
    public boolean isDense() { return isDense; }
    public double getStep() { return step; }
    public double getCardinalNumber() { return cardinalNumber; }
    public void setCardinalNumber(int cardinalNumber) {
        if (cardinalNumber <= 0) {
            throw new IllegalArgumentException("Cardinal number must be positive");
        }
        this.cardinalNumber = cardinalNumber;
    }

    public boolean contains(double x) {
        return x >= start && x <= end;
    }

    // Get all discrete points in the universe
    public List<Double> getDiscretePoints() {
        if (isDense) {
            throw new IllegalStateException("Cannot get discrete points from dense universe");
        }

        List<Double> points = new ArrayList<>();
        for (double x = start; x <= end; x += step) {
            points.add(x);
        }
        return points;
    }
}


