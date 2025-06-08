package org.example;

public class Summarizer { // == LABEL == ETYKIETA == SUMARYZATOR

    protected String label;  // e.g., "tall", "young"
    protected FuzzySet fuzzySet;  // Fuzzy set associated with the label

    public Summarizer(String label) {
        this.label = label;
    }

}
