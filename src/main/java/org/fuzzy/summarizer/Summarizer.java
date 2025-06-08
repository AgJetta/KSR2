package org.fuzzy.summarizer;

import org.fuzzy.FuzzySet;
import org.fuzzy.SongRecord;

import java.util.List;

// Base Summarizer class
public class Summarizer {
    protected final String name;
    protected final String linguisticVariableName;

    protected final FuzzySet fuzzySet;

    public Summarizer(String name, String linguisticVariableName, FuzzySet fuzzySet) {
        this.name = name;
        this.linguisticVariableName = linguisticVariableName;
        this.fuzzySet = fuzzySet;
    }

    public String getName() {
        return name;
    }

    public String getFieldName() {
        return linguisticVariableName;
    }

    public FuzzySet getFuzzySet() {
        return fuzzySet;
    }

    // Calculate membership degree for a record
    public double getMembership(SongRecord record) {
        double fieldValue = record.getAttribute(linguisticVariableName);
        return fuzzySet.getMembership(fieldValue);
    }

    // Calculate total membership sum for dataset
    public double calculateR(List<SongRecord> dataset) {
        return dataset.stream()
                .mapToDouble(this::getMembership)
                .sum();
    }

    @Override
    public String toString() {
        return name;
    }
}
