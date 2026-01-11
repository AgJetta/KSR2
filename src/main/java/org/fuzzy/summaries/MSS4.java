package org.fuzzy.summaries;

import org.fuzzy.SongRecord;
import org.fuzzy.summarizer.Summarizer;

import java.util.List;

public class MSS4 extends MSS1 {

    public MSS4(String subjectAttribute, String predicate1, String predicate2,
                double predicate1Value, double predicate2Value, Summarizer summarizer) {
        super(subjectAttribute, predicate1, predicate2, predicate1Value, predicate2Value,
                null, summarizer);
        this.summaryType = "MSS4";
    }

    private double reichenbachImplicator(double a, double b) {
        return 1.0 - a + (a * b);
    }

    @Override
    public double calculateT1(List<SongRecord> dataset) {
        List<SongRecord> p1Records = filterSubject(dataset, predicate1Value);
        List<SongRecord> p2Records = filterSubject(dataset, predicate2Value);

        int m_p1 = p1Records.size();
        int m_p2 = p2Records.size();

        if (m_p1 == 0 || m_p2 == 0) {
            System.err.println("Warning: One of the subjects is empty!");
            return 0.0;
        }

        int minSize = Math.min(m_p1, m_p2);
        double sumImplicator = 0.0;

        for (int i = 0; i < minSize; i++) {
            double membershipP1 = summarizer.getMembership(p1Records.get(i));
            double membershipP2 = summarizer.getMembership(p2Records.get(i));
            double implicatorValue = reichenbachImplicator(membershipP2, membershipP1);
            sumImplicator += implicatorValue;
        }

        return 1.0 - (sumImplicator / minSize);
    }

    @Override
    public String generateSummary() {
        return String.format("%s | Więcej %s niż %s jest [%s]",
                summaryType,
                predicate1.toUpperCase(),
                predicate2.toUpperCase(),
                summarizer.generateDescription()
        );
    }
}