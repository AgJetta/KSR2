package org.fuzzy.summarizer;

import java.util.List;

public class CompoundSummarizer {
    private final List<Summarizer> summarizers;
    private String name;

    public CompoundSummarizer(List<Summarizer> summarizers) {
        this.summarizers = summarizers;
        this.setName();
    }

    public void setName() {
        this.name = "";
        for (int i = 0; i < summarizers.size(); i++) {
            this.name += summarizers.get(i).getName();
            this.name += " " + summarizers.get(i).linguisiticVariable;
            if (i < summarizers.size() - 1) {
                this.name += " ORAZ ";
            }
        }
    }

    public String getName() {
        return name;
    }

    public List<Summarizer> getSummarizers() {
        return this.summarizers;
    }
}
