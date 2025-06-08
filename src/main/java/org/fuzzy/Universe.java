package org.fuzzy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Universe {
    private final double start;
    private final double end;
    private final boolean isDense;
    private final double step; // for discrete universes>
    private List<Double> actualValues = null;

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

    // For database operations
    public Universe(double start, double end, boolean isDense, List<Double> actualValues) {
        this(start, end, isDense, 1.0);
        this.actualValues = actualValues;
    }

    // Methods to get actual values that exist in the dataset
    public List<Double> getActualValues() {
        return actualValues != null ? actualValues : List.of();
    }

    public void setActualValues(List<Double> actualValues) {
        this.actualValues = actualValues;
    }

    public double getStart() { return start; }
    public double getEnd() { return end; }
    public boolean isDense() { return isDense; }
    public double getStep() { return step; }
    public double getLength() { return end - start; }

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
    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Universe)) return false;

        Universe universe = (Universe) o;

        if (Double.compare(universe.start, start) != 0) return false;
        if (Double.compare(universe.end, end) != 0) return false;
        if (isDense != universe.isDense) return false;
        return Double.compare(universe.step, step) == 0;
    }

}


