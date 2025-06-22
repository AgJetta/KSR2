package org.fuzzy.summaries;

import org.fuzzy.FuzzySet;
import org.fuzzy.SongRecord;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;

import java.util.List;

public class MSS2 extends MSS1 {

    protected Summarizer summarizer2;
    protected String summaryType = "2S F2";

    public MSS2(String predicate1, String predicate2, Quantifier quantifier, Summarizer summarizer, Summarizer summarizer2) {
        super(predicate1, predicate2, quantifier, summarizer);
        this.summarizer2 = summarizer2;
    }

    public Summarizer getSummarizer2() {
        return summarizer2;
    }

    // Calculate T1 (degree of truth)
    public double calculateT1(List<SongRecord> dataset) {
        if (!quantifier.isRelative()) {
            System.err.println("MSS1 only supports relative quantifiers!");
            System.exit(1);
        }

        // Filter in records which have value SongRecord.genreStringToDouble(predicate1) against key 'playlist_genre'

        List<SongRecord> songsPredicate1 = dataset.stream()
                .filter(song -> song.getAttribute("playlist_genre") == SongRecord.genreStringtoDouble(predicate1))
                .toList();
        List<SongRecord> songsPredicate2 = dataset.stream()
                .filter(song -> song.getAttribute("playlist_genre") == SongRecord.genreStringtoDouble(predicate2))
                .toList();
        int M_p1 = songsPredicate1.size();
        int M_p2 = songsPredicate2.size();

        double sigma_count_s1s2p1 = 0.0;
        for (SongRecord song : songsPredicate1) {
            double membershipValueToS1 = summarizer.getFuzzySet().getMembership(song.getAttribute(summarizer.getFieldName()));
            double membershipValueToS2 = summarizer2.getFuzzySet().getMembership(song.getAttribute(summarizer2.getFieldName()));
            double intersectionMembership = Math.min(membershipValueToS1, membershipValueToS2);
            sigma_count_s1s2p1 += intersectionMembership;
        }
        double sigma_count_s2p1 = songsPredicate1.stream()
                .mapToDouble(song -> summarizer2.getFuzzySet().getMembership(song.getAttribute(summarizer2.getFieldName())))
                .sum();
        double sigma_count_s1p2 = songsPredicate2.stream()
                .mapToDouble(song -> summarizer.getFuzzySet().getMembership(song.getAttribute(summarizer.getFieldName())))
                .sum();
        double nominator = sigma_count_s1s2p1 / M_p1;
        double denominator = sigma_count_s2p1 / M_p1 + sigma_count_s1p2 / M_p2;
        if (denominator == 0) {
            System.err.println("Denominator is zero, cannot calculate T1!");
            return 0.0;
        }
        double proportion = nominator / denominator;
        double t1 = quantifier.getMembership(proportion, 1);
        return t1;
    }

    // Generate natural language summary
    @Override
    public String generateSummary() {
        return String.format("%s utworów %s w odniesieniu do %s będących [%s %s] jest [%s %s]",
                quantifier.getName(),
                predicate1.toUpperCase(),
                predicate2.toUpperCase(),
                summarizer2.getName(),
                summarizer2.linguisiticVariable,
                summarizer.getName(),
                summarizer.linguisiticVariable
        );
    }

    // Generate summary with T1 value
    @Override
    public String generateSummaryWithMeasures(List<SongRecord> dataset) {
        double t1 = calculateT1(dataset);

        String summaryString = generateSummary();
        return String.format("%-140s" + " %.4f",
                summaryString, t1);
    }
}
