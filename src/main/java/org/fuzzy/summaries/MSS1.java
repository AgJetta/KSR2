package org.fuzzy.summaries;

import org.fuzzy.SongRecord;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;

import java.util.List;

public class MSS1 {
    protected final String subjectAttribute;
    protected final String predicate1;
    protected final String predicate2;
    protected final double predicate1Value;
    protected final double predicate2Value;
    protected final Quantifier quantifier;
    protected final Summarizer summarizer;
    protected String summaryType = "MSS1";

    public MSS1(String subjectAttribute, String predicate1, String predicate2,
                double predicate1Value, double predicate2Value,
                Quantifier quantifier, Summarizer summarizer) {
        this.subjectAttribute = subjectAttribute;
        this.predicate1 = predicate1;
        this.predicate2 = predicate2;
        this.predicate1Value = predicate1Value;
        this.predicate2Value = predicate2Value;
        this.quantifier = quantifier;
        this.summarizer = summarizer;

        if (quantifier != null && !quantifier.isRelative()) {
            throw new IllegalArgumentException("MSS1 only supports relative quantifiers!");
        }
    }

    protected List<SongRecord> filterSubject(List<SongRecord> dataset, double subjectValue) {
        return dataset.stream()
                .filter(song -> song.getAttribute(subjectAttribute) == subjectValue)
                .toList();
    }

    public double calculateT1(List<SongRecord> dataset) {
        List<SongRecord> p1Records = filterSubject(dataset, predicate1Value);
        List<SongRecord> p2Records = filterSubject(dataset, predicate2Value);

        int m_p1 = p1Records.size();
        int m_p2 = p2Records.size();

        if (m_p1 == 0 || m_p2 == 0) {
            System.err.println("Warning: One of the subjects is empty!");
            return 0.0;
        }

        double sigmaCountS1P1 = 0.0;
        for (SongRecord record : p1Records) {
            sigmaCountS1P1 += summarizer.getMembership(record);
        }

        double sigmaCountS1P2 = 0.0;
        for (SongRecord record : p2Records) {
            sigmaCountS1P2 += summarizer.getMembership(record);
        }

        double numerator = sigmaCountS1P1 / m_p1;
        double denominator = (sigmaCountS1P1 / m_p1) + (sigmaCountS1P2 / m_p2);

        if (denominator == 0) {
            System.err.println("Warning: Denominator is zero in T1 calculation!");
            return 0.0;
        }

        double proportion = numerator / denominator;
        return quantifier.getMembership(proportion, 1);
    }

    public String generateSummary() {
        return String.format("%s | %s %s w odniesieniu do %s jest [%s]",
                summaryType,
                quantifier.getName(),
                predicate1.toUpperCase(),
                predicate2.toUpperCase(),
                summarizer.generateDescription()
        );
    }

    public String generateSummaryWithMeasures(List<SongRecord> dataset) {
        double t1 = calculateT1(dataset);
        String summaryString = generateSummary();
        return String.format("%-80s | %.4f", summaryString, t1);
    }

    public Quantifier getQuantifier() {
        return quantifier;
    }

    public String getPredicate1() {
        return predicate1;
    }

    public String getPredicate2() {
        return predicate2;
    }

    public Summarizer getSummarizer() {
        return summarizer;
    }

    @Override
    public String toString() {
        return generateSummary();
    }
}