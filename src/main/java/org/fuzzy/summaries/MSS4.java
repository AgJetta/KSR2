package org.fuzzy.summaries;

import org.fuzzy.SongRecord;
import org.fuzzy.summarizer.Summarizer;

import java.util.List;

// MultiSubject Summary Form 1
public class MSS4 extends MSS1 {
    protected final String predicate1;
    protected final String predicate2;

    protected final Summarizer summarizer;
    protected String summaryType = "2S F4";

    public MSS4(String predicate1, String predicate2, Summarizer summarizer) {
        super(predicate1, predicate2, null, summarizer);
        this.predicate1 = predicate1;
        this.predicate2 = predicate2;
        this.summarizer = summarizer;
    }

    @Override
    public String getPredicate1() {
        return predicate1;
    }
    @Override
    public String getPredicate2() { return predicate2; }

    @Override
    public Summarizer getSummarizer() {
        return summarizer;
    }

    // Calculate T1 (degree of truth)
    @Override
    public double calculateT1(List<SongRecord> dataset) {
        // Filter in records which have value SongRecord.genreStringToDouble(predicate1) against key 'playlist_genre'
        List<SongRecord> songsPredicate1 = dataset.stream()
                .filter(song -> song.getAttribute("playlist_genre") == SongRecord.genreStringtoDouble(predicate1))
                .toList();
        List<SongRecord> songsPredicate2 = dataset.stream()
                .filter(song -> song.getAttribute("playlist_genre") == SongRecord.genreStringtoDouble(predicate2))
                .toList();
        int M_p1 = songsPredicate1.size();
        int M_p2 = songsPredicate2.size();
        int minSize = Math.min(M_p1, M_p2);

        double cardinalNumberOfImplicator = 0.0;
        for (int i = 0; i < minSize; i++) {
            double membershipValueSP1 = summarizer.getFuzzySet().getMembership(songsPredicate1.get(i).getAttribute(summarizer.getFieldName()));
            double membershipValueSP2 = summarizer.getFuzzySet().getMembership(songsPredicate2.get(i).getAttribute(summarizer.getFieldName()));
            double implicatorValue = implicatorReichenbach(membershipValueSP2, membershipValueSP1);
            cardinalNumberOfImplicator += implicatorValue;
        }
        return 1.0 - (cardinalNumberOfImplicator / minSize);
    }

    private double implicatorReichenbach(double a, double b) { return 1 - a + a*b; }

    // Generate natural language summary
    @Override
    public String generateSummary() {
        return String.format("%s | Więcej utworów %s niż %s jest [%s %s]",
                summaryType,
                predicate1.toUpperCase(),
                predicate2.toUpperCase(),
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

    @Override
    public void printLatexFuzzySummaryResults(List<SongRecord> dataset) {
        double t1 = calculateT1(dataset);

        String summaryString = generateSummary();
        System.out.printf("%s", summaryString);
        System.out.printf(" & %.4f", t1);
        System.out.printf(" \\\\ %n");
        System.out.println("\\midrule");
    }
}
