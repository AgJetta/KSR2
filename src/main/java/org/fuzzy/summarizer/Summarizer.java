package org.fuzzy.summarizer;

import org.fuzzy.FuzzySet;
import org.fuzzy.SongRecord;

import java.util.List;

// Base Summarizer class
public class Summarizer {
    protected final String name;
    protected final String fieldName;

    protected final FuzzySet fuzzySet;
    public String linguisiticVariable = "";

    private List<SongRecord> data = null; // List of song records for this fuzzy set

    public Summarizer(String name, String fieldName, FuzzySet fuzzySet) {
        this.name = name;
        this.fieldName = fieldName;
        this.fuzzySet = fuzzySet;
        this.fuzzySet.setFieldName(fieldName);
    }

    public void connectDataset(List<SongRecord> data) {
        this.data = data;
        this.getFuzzySet().connectDataset(data, fieldName);
    }

    public String getName() {
        return name;
    }

    public String getFieldName() {
        return fieldName;
    }

    public FuzzySet getFuzzySet() {
        return fuzzySet;
    }

    // Calculate membership degree for a record
    public double getMembership(SongRecord record) {
        double fieldValue = record.getAttribute(fieldName);
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
