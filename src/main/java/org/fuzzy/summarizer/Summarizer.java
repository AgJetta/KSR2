package org.fuzzy.summarizer;

import org.fuzzy.FuzzySet;
import org.fuzzy.LogicalConnective;
import org.fuzzy.SongRecord;

import java.util.ArrayList;
import java.util.List;

public class Summarizer {
    private final String name;
    private final List<String> fieldNames;
    private final List<FuzzySet> fuzzySets;
    private final List<LogicalConnective> connectives;
    private final List<String> linguisticVariables;

    public Summarizer(String name, String fieldName, FuzzySet fuzzySet) {
        this.name = name;
        this.fieldNames = new ArrayList<>();
        this.fieldNames.add(fieldName);
        this.fuzzySets = new ArrayList<>();
        this.fuzzySets.add(fuzzySet);
        this.connectives = new ArrayList<>();
        this.linguisticVariables = new ArrayList<>();
        this.linguisticVariables.add("");
    }

    public Summarizer(String name, List<String> fieldNames, List<FuzzySet> fuzzySets,
                      List<LogicalConnective> connectives, List<String> linguisticVariables) {
        if (fieldNames.size() != fuzzySets.size()) {
            throw new IllegalArgumentException(
                    "Number of field names must match number of fuzzy sets");
        }
        if (fuzzySets.size() > 1 && connectives.size() != fuzzySets.size() - 1) {
            throw new IllegalArgumentException(
                    "Number of connectives must be one less than number of fuzzy sets");
        }
        if (linguisticVariables.size() != fuzzySets.size()) {
            throw new IllegalArgumentException(
                    "Number of linguistic variables must match number of fuzzy sets");
        }

        this.name = name;
        this.fieldNames = new ArrayList<>(fieldNames);
        this.fuzzySets = new ArrayList<>(fuzzySets);
        this.connectives = new ArrayList<>(connectives);
        this.linguisticVariables = new ArrayList<>(linguisticVariables);
    }

    public String getName() {
        return name;
    }

    public boolean isCompound() {
        return fuzzySets.size() > 1;
    }

    public int getComponentCount() {
        return fuzzySets.size();
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

    public double getMembership(SongRecord record) {
        if (fuzzySets.size() == 1) {
            double fieldValue = record.getAttribute(fieldNames.get(0));
            return fuzzySets.get(0).getMembership(fieldValue);
        }

        double result = Double.NaN;
        for (int i = 0; i < fuzzySets.size(); i++) {
            double fieldValue = record.getAttribute(fieldNames.get(i));
            double membership = fuzzySets.get(i).getMembership(fieldValue);

            if (i == 0) {
                result = membership;
            } else {
                LogicalConnective connective = connectives.get(i - 1);
                result = applyConnective(result, membership, connective);
            }
        }
        return result;
    }

    private double applyConnective(double a, double b, LogicalConnective connective) {
        switch (connective) {
            case AND:
                return Math.min(a, b);
            case OR:
                return Math.max(a, b);
            case NOT:
                throw new UnsupportedOperationException("NOT connective not supported in this context");
            default:
                throw new IllegalArgumentException("Unknown connective: " + connective);
        }
    }

    public double calculateR(List<SongRecord> dataset) {
        return dataset.stream()
                .mapToDouble(this::getMembership)
                .sum();
    }

    public String generateDescription() {
        if (!isCompound()) {
            return name + " " + linguisticVariables.get(0);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fuzzySets.size(); i++) {
            sb.append(getName()).append(" ").append(linguisticVariables.get(i));
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

    public void setLinguisticVariable(int index, String linguisticVariable) {
        if (index >= 0 && index < linguisticVariables.size()) {
            linguisticVariables.set(index, linguisticVariable);
        }
    }
}