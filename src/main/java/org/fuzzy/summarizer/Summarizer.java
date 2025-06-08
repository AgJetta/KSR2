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

    public void calculateAllMemberships(List<SongRecord> dataset){
        // Call FuzzySet.getmembership() on all dataset records, which are SongRecord
        for (SongRecord record: dataset){
            this.fuzzySet.calculateMembershipAndAdd(record.getAttribute(linguisticVariableName));
        }
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

    public double getMembership(double value) {
        return fuzzySet.getMembership(value);
    }

    // Calculate total membership sum for dataset
    public double calculateR(List<SongRecord> dataset) {
        return fuzzySet.cardinality();
    }

    @Override
    public String toString() {
        return name;
    }
}
