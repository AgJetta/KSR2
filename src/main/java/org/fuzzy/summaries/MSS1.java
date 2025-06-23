package org.fuzzy.summaries;

import org.fuzzy.SongRecord;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;

import java.util.Arrays;
import java.util.List;

// MultiSubject Summary Form 1
public class MSS1 {
    protected final String predicate1;
    protected final String predicate2;

    protected final Quantifier quantifier;

    protected final Summarizer summarizer;
    protected String summaryType = "2S F1";

    public MSS1(String predicate1, String predicate2, Quantifier quantifier, Summarizer summarizer) {
        this.predicate1 = predicate1;
        this.predicate2 = predicate2;
        this.quantifier = quantifier;
        this.summarizer = summarizer;
    }
    public Quantifier getQuantifier() {
        return quantifier;
    }

    public String getPredicate1() {
        return predicate1;
    }
    public String getPredicate2() { return predicate2; }

    public Summarizer getSummarizer() {
        return summarizer;
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

        double sigma_count_s1p1 = songsPredicate1.stream()
                .mapToDouble(song -> summarizer.getFuzzySet().getMembership(song.getAttribute(summarizer.getFieldName())))
                .sum();
        double sigma_count_s1p2 = songsPredicate2.stream()
                .mapToDouble(song -> summarizer.getFuzzySet().getMembership(song.getAttribute(summarizer.getFieldName())))
                .sum();
        double nominator = sigma_count_s1p1 / M_p1;
        double denominator = sigma_count_s1p1 / M_p1 + sigma_count_s1p2 / M_p2;
        if (denominator == 0) {
            System.err.println("Denominator is zero, cannot calculate T1!");
            return 0.0;
        }
        double proportion = nominator / denominator;
        double t1 = quantifier.getMembership(proportion, 1);
        return t1;
    }

    // Generate natural language summary
    public String generateSummary() {
        return String.format("%s | %s utworów %s w odniesieniu do %s jest [%s %s]",
                summaryType,
                quantifier.getName(),
                predicate1.toUpperCase(),
                predicate2.toUpperCase(),
                summarizer.getName(),
                summarizer.linguisiticVariable
        );
    }

    // Generate summary with T1 value
    public String generateSummaryWithMeasures(List<SongRecord> dataset) {
        double t1 = calculateT1(dataset);

        String summaryString = generateSummary();
        return String.format("%-113s" + " %.4f",
                summaryString, t1);
    }

    public String toString() {
        return generateSummary();
    }

    public void printLatexFuzzySummaryResults(List<SongRecord> dataset) {
        double t1 = calculateT1(dataset);

        String summaryString = generateSummary();
        System.out.printf("%s", summaryString);
        System.out.printf(" & %.4f", t1);
        System.out.printf(" \\\\ %n");
        System.out.println("\\midrule");
    }
}
