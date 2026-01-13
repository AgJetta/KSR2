package org.fuzzy.quantifiers;

import org.fuzzy.FuzzySet;
import org.fuzzy.Universe;

import java.util.Arrays;

public class Quantifier {
    private final String name;
    private final FuzzySet fuzzySet;
    private final boolean isRelative;

    private final String functionType;
    private final double[] parameters;

    public Quantifier(String name, FuzzySet fuzzySet, boolean isRelative,
                      String functionType, double[] parameters) {
        this.name = name;
        this.fuzzySet = fuzzySet;
        this.isRelative = isRelative;
        this.functionType = functionType;
        this.parameters = parameters.clone(); // defensive copy
        validateUniverse();
    }

    private void validateUniverse() {
        Universe universe = fuzzySet.getUniverse();

        if (isRelative) {
            if (!universe.isDense()) {
                throw new IllegalArgumentException(
                        "Relative quantifiers must have continuous (dense) universe");
            }
            if (Math.abs(universe.getStart() - 0.0) > 1e-9 ||
                    Math.abs(universe.getEnd() - 1.0) > 1e-9) {
                throw new IllegalArgumentException(
                        "Relative quantifiers must have universe [0,1], got [" +
                                universe.getStart() + "," + universe.getEnd() + "]");
            }
        } else {
            if (universe.isDense()) {
                throw new IllegalArgumentException(
                        "Absolute quantifiers must have discrete (non-dense) universe");
            }
            if (universe.getStart() < 0) {
                throw new IllegalArgumentException(
                        "Absolute quantifiers must have non-negative universe");
            }
        }
    }

    public String getName() {
        return name;
    }

    public FuzzySet getFuzzySet() {
        return fuzzySet;
    }

    public boolean isRelative() {
        return isRelative;
    }

    public String getFunctionType() {
        return functionType;
    }

    public double[] getParameters() {
        return parameters.clone(); // return copy to keep immutability
    }

    public double getMembership(double r, int m) {
        if (isRelative) {
            double proportion = m > 0 ? r / m : 0.0;
            return fuzzySet.getMembershipFunction().apply(proportion);
        } else {
            return fuzzySet.getMembershipFunction().apply(r);
        }
    }

    public double getSupportMeasure() {
        return fuzzySet.supportMeasure();
    }

    public double getCardinality() {
        return fuzzySet.cardinalNumber();
    }

    @Override
    public String toString() {
        return name + " (" + (isRelative ? "relative" : "absolute") + ")";
    }

    public String parametersToString() {
        return Arrays.stream(parameters)
                .mapToObj(d -> String.format("%.2f", d))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
