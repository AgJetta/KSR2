package org.fuzzy.summarizer;

import org.fuzzy.FuzzySet;
import org.fuzzy.LogicalConnective;
import org.fuzzy.SongRecord;
import org.fuzzy.Universe;

import java.util.ArrayList;
import java.util.List;

public class Summarizer {

    private final String name;
    private final List<String> fieldNames;
    private final List<FuzzySet> fuzzySets;
    private final List<LogicalConnective> connectives;
    private final List<String> linguisticVariables;

    private final String functionType;     // null for compound
    private final double[] parameters;     // null for compound
    private final Universe universe;        // null for compound


    public Summarizer(
            String name,
            String fieldName,
            FuzzySet fuzzySet,
            String functionType,
            double[] parameters,
            Universe universe
    ) {
        this.name = name;

        this.fieldNames = List.of(fieldName);
        this.fuzzySets = List.of(fuzzySet);
        this.connectives = new ArrayList<>();

        this.linguisticVariables = List.of(name);

        this.functionType = functionType;
        this.parameters = parameters.clone();
        this.universe = universe;
    }

    public Summarizer(
            String name,
            List<String> fieldNames,
            List<FuzzySet> fuzzySets,
            List<LogicalConnective> connectives,
            List<String> linguisticVariables
    ) {
        if (fieldNames.size() != fuzzySets.size()) {
            throw new IllegalArgumentException("Fields and fuzzy sets count mismatch");
        }
        if (fuzzySets.size() > 1 && connectives.size() != fuzzySets.size() - 1) {
            throw new IllegalArgumentException("Invalid number of connectives");
        }

        this.name = name;
        this.fieldNames = new ArrayList<>(fieldNames);
        this.fuzzySets = new ArrayList<>(fuzzySets);
        this.connectives = new ArrayList<>(connectives);
        this.linguisticVariables = new ArrayList<>(linguisticVariables);

        this.functionType = null;
        this.parameters = null;
        this.universe = null;
    }

    public String getName() {
        return name;
    }

    public String getFieldName(int index) {
        return fieldNames.get(index);
    }

    public FuzzySet getFuzzySet(int index) {
        return fuzzySets.get(index);
    }

    public String getLinguisticVariable(int index) {
        return linguisticVariables.get(index);
    }

    public boolean isCompound() {
        return fuzzySets.size() > 1;
    }

    public int getComponentCount() {
        return fuzzySets.size();
    }

    public String getFunctionType() {
        return functionType;
    }

    public double[] getParameters() {
        return parameters == null ? null : parameters.clone();
    }

    public Universe getUniverse() {
        return universe;
    }


    public double getMembership(SongRecord record) {
        double result = fuzzySets.get(0)
                .getMembership(record.getAttribute(fieldNames.get(0)));

        for (int i = 1; i < fuzzySets.size(); i++) {
            double membership = fuzzySets.get(i)
                    .getMembership(record.getAttribute(fieldNames.get(i)));

            LogicalConnective connective = connectives.get(i - 1);
            result = applyConnective(result, membership, connective);
        }
        return result;
    }

    private double applyConnective(double a, double b, LogicalConnective connective) {
        return switch (connective) {
            case AND -> Math.min(a, b);
            case OR -> Math.max(a, b);
            default -> throw new IllegalStateException("Unsupported connective");
        };
    }

    public double calculateR(List<SongRecord> dataset) {
        return dataset.stream()
                .mapToDouble(this::getMembership)
                .sum();
    }
    public String generateDescription() {
        if (!isCompound()) {
            return name;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fuzzySets.size(); i++) {
            sb.append(linguisticVariables.get(i));
            if (i < connectives.size()) {
                sb.append(" ").append(connectives.get(i).name()).append(" ");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return generateDescription();
    }
}