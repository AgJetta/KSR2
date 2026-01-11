package org.fuzzy.summaries;

import org.fuzzy.SongRecord;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;

import java.util.List;

public class MSS3 extends MSS2 {

    public MSS3(String subjectAttribute, String predicate1, String predicate2,
                double predicate1Value, double predicate2Value,
                Quantifier quantifier, Summarizer summarizer, Summarizer summarizer2) {
        super(subjectAttribute, predicate1, predicate2, predicate1Value, predicate2Value,
                quantifier, summarizer, summarizer2);
        this.summaryType = "MSS3";
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

        double sigmaCountS1S2P1 = 0.0;
        for (SongRecord record : p1Records) {
            double membershipS1 = summarizer.getMembership(record);
            double membershipS2 = summarizer2.getMembership(record);
            double intersection = Math.min(membershipS1, membershipS2);
            sigmaCountS1S2P1 += intersection;
        }

        double sigmaCountS1P1 = 0.0;
        for (SongRecord record : p1Records) {
            sigmaCountS1P1 += summarizer.getMembership(record);
        }

        double sigmaCountS1P2 = 0.0;
        for (SongRecord record : p2Records) {
            sigmaCountS1P2 += summarizer.getMembership(record);
        }

        double numerator = 2.0 * sigmaCountS1S2P1 / m_p1;
        double denominator = (sigmaCountS1P1 / m_p1) + (sigmaCountS1P2 / m_p2);

        if (denominator == 0) {
            System.err.println("Warning: Denominator is zero in T1 calculation!");
            return 0.0;
        }

        double proportion = numerator / denominator;
        return quantifier.getMembership(proportion, 1);
    }

    @Override
    public String generateSummary() {
        return String.format("%s | %s %s będących [%s] w odniesieniu do %s jest [%s]",
                summaryType,
                quantifier.getName(),
                predicate1.toUpperCase(),
                summarizer2.generateDescription(),
                predicate2.toUpperCase(),
                summarizer.generateDescription()
        );
    }
}