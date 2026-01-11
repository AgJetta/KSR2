package org.fuzzy;

import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summaries.LinguisticSummary;
import org.fuzzy.summaries.MSS1;
import org.fuzzy.summaries.MSS2;
import org.fuzzy.summaries.MSS3;
import org.fuzzy.summaries.MSS4;
import org.fuzzy.summarizer.Summarizer;

import java.util.*;

import static org.dataImport.ConfigImporter.*;
import static org.dataImport.CsvSongImporter.importSongs;

public class DevMain {
    public static void main(String[] args) {
        System.out.println("=== LOADING DATA ===\n");

        List<SongRecord> dataset = importSongs(10000);
        System.out.println("Loaded " + dataset.size() + " songs from CSV");

        List<Summarizer> summarizers = loadSummarizersFromConfig();
        System.out.println("Loaded " + summarizers.size() + " simple summarizers from config");

        List<Quantifier> quantifiers = loadQuantifiersFromConfig();
        System.out.println("Loaded " + quantifiers.size() + " quantifiers from config\n");

        System.out.println("=== CREATING COMPOUND SUMMARIZERS ===\n");

        List<Summarizer> compoundSummarizers = new ArrayList<>();

        Summarizer nisza = findSummarizer(summarizers, "Nisza");
        Summarizer hit = findSummarizer(summarizers, "Hit");
        Summarizer mainstream = findSummarizer(summarizers, "Mainstream");
        Summarizer ballada = findSummarizer(summarizers, "Ballada");
        Summarizer mocnaTaniec = findSummarizer(summarizers, "Mocna");
        Summarizer glosna = findSummarizer(summarizers, "Głośna");
        Summarizer szybkie = findSummarizer(summarizers, "Szybkie");
        Summarizer energiczna = findSummarizer(summarizers, "Bardzo Energiczna");

        if (nisza != null && ballada != null) {
            Summarizer compound1 = new Summarizer(
                    "Nisza AND Ballada",
                    Arrays.asList(nisza.getFieldName(0), ballada.getFieldName(0)),
                    Arrays.asList(nisza.getFuzzySet(0), ballada.getFuzzySet(0)),
                    Arrays.asList(LogicalConnective.AND),
                    Arrays.asList(nisza.getLinguisticVariable(0), ballada.getLinguisticVariable(0))
            );
            compoundSummarizers.add(compound1);
            System.out.println("Created: " + compound1.getName());
        }

        if (hit != null && mocnaTaniec != null) {
            Summarizer compound2 = new Summarizer(
                    "Hit AND Mocna taneczność",
                    Arrays.asList(hit.getFieldName(0), mocnaTaniec.getFieldName(0)),
                    Arrays.asList(hit.getFuzzySet(0), mocnaTaniec.getFuzzySet(0)),
                    Arrays.asList(LogicalConnective.AND),
                    Arrays.asList(hit.getLinguisticVariable(0), mocnaTaniec.getLinguisticVariable(0))
            );
            compoundSummarizers.add(compound2);
            System.out.println("Created: " + compound2.getName());
        }

        if (glosna != null && szybkie != null) {
            Summarizer compound3 = new Summarizer(
                    "Głośna OR Szybkie",
                    Arrays.asList(glosna.getFieldName(0), szybkie.getFieldName(0)),
                    Arrays.asList(glosna.getFuzzySet(0), szybkie.getFuzzySet(0)),
                    Arrays.asList(LogicalConnective.OR),
                    Arrays.asList(glosna.getLinguisticVariable(0), szybkie.getLinguisticVariable(0))
            );
            compoundSummarizers.add(compound3);
            System.out.println("Created: " + compound3.getName());
        }

        System.out.println("\nTotal compound summarizers: " + compoundSummarizers.size());

        System.out.println("\n=== GENERATING LINGUISTIC SUMMARIES (F1) ===\n");

        Quantifier aboutHalf = findQuantifier(quantifiers, "OKOŁO POŁOWY");
        Quantifier almostAll = findQuantifier(quantifiers, "PRAWIE WSZYSTKIE");
        Quantifier oneThird = findQuantifier(quantifiers, "JEDNA TRZECIA (1/3)");

        List<Quantifier> selectedQuantifiers = Arrays.asList(aboutHalf, almostAll, oneThird);

        List<Summarizer> selectedSimple = summarizers.subList(0, Math.min(5, summarizers.size()));

        System.out.println("Simple Summarizers:");
        printSummaryHeader();
        for (Quantifier q : selectedQuantifiers) {
            if (q == null) continue;
            for (Summarizer s : selectedSimple) {
                LinguisticSummary summary = new LinguisticSummary(q, "utworów", s);
                printSummaryResults(summary, dataset);
            }
        }

        System.out.println("\n\nCompound Summarizers:");
        printSummaryHeader();
        for (Quantifier q : selectedQuantifiers) {
            if (q == null) continue;
            for (Summarizer s : compoundSummarizers) {
                LinguisticSummary summary = new LinguisticSummary(q, "utworów", s);
                printSummaryResults(summary, dataset);
            }
        }

        System.out.println("\n\n=== GENERATING SECOND-ORDER SUMMARIES (F2) ===\n");

        System.out.println("F2 with Simple Qualifier and Simple Summarizer:");
        printSummaryHeader();

        if (hit != null && energiczna != null) {
            for (Quantifier q : selectedQuantifiers) {
                if (q == null) continue;
                LinguisticSummary f2Summary = new LinguisticSummary(q, "utworów", energiczna, hit);
                printSummaryResults(f2Summary, dataset);
            }
        }

        if (glosna != null && szybkie != null) {
            for (Quantifier q : selectedQuantifiers) {
                if (q == null) continue;
                LinguisticSummary f2Summary = new LinguisticSummary(q, "utworów", szybkie, glosna);
                printSummaryResults(f2Summary, dataset);
            }
        }

        if (mainstream != null && mocnaTaniec != null) {
            for (Quantifier q : selectedQuantifiers) {
                if (q == null) continue;
                LinguisticSummary f2Summary = new LinguisticSummary(q, "utworów", mocnaTaniec, mainstream);
                printSummaryResults(f2Summary, dataset);
            }
        }

        System.out.println("\n\nF2 with Compound Qualifier and Simple Summarizer:");
        printSummaryHeader();

        if (compoundSummarizers.size() > 0 && energiczna != null) {
            Summarizer compoundQualifier = compoundSummarizers.get(0);
            for (Quantifier q : selectedQuantifiers) {
                if (q == null) continue;
                LinguisticSummary f2Summary = new LinguisticSummary(q, "utworów", energiczna, compoundQualifier);
                printSummaryResults(f2Summary, dataset);
            }
        }

        System.out.println("\n\nF2 with Simple Qualifier and Compound Summarizer:");
        printSummaryHeader();

        if (hit != null && compoundSummarizers.size() > 1) {
            Summarizer compoundSum = compoundSummarizers.get(1);
            for (Quantifier q : selectedQuantifiers) {
                if (q == null) continue;
                LinguisticSummary f2Summary = new LinguisticSummary(q, "utworów", compoundSum, hit);
                printSummaryResults(f2Summary, dataset);
            }
        }

        System.out.println("\n\nF2 with Compound Qualifier and Compound Summarizer:");
        printSummaryHeader();

        if (compoundSummarizers.size() >= 2) {
            Summarizer compoundQualifier = compoundSummarizers.get(0);
            Summarizer compoundSum = compoundSummarizers.get(2);
            for (Quantifier q : selectedQuantifiers) {
                if (q == null) continue;
                LinguisticSummary f2Summary = new LinguisticSummary(q, "utworów", compoundSum, compoundQualifier);
                printSummaryResults(f2Summary, dataset);
            }
        }

        System.out.println("\n\n=== TESTING ALL QUALITY MEASURES (F1) ===\n");

        if (aboutHalf != null && nisza != null) {
            LinguisticSummary testSummary = new LinguisticSummary(aboutHalf, "utworów", nisza);

            System.out.println("Summary: " + testSummary.generateSummary());
            System.out.println("\nIndividual Quality Measures:");
            System.out.println("T1  (Degree of Truth):           " + String.format("%.4f", testSummary.calculateT1(dataset)));
            System.out.println("T2  (Degree of Imprecision):     " + String.format("%.4f", testSummary.calculateT2(dataset)));
            System.out.println("T3  (Degree of Covering):        " + String.format("%.4f", testSummary.calculateT3(dataset)));
            System.out.println("T4  (Degree of Appropriateness): " + String.format("%.4f", testSummary.calculateT4(dataset)));
            System.out.println("T5  (Length of Summary):         " + String.format("%.4f", testSummary.calculateT5(dataset)));
            System.out.println("T6  (Quantifier Imprecision):    " + String.format("%.4f", testSummary.calculateT6(dataset)));
            System.out.println("T7  (Quantifier Cardinality):    " + String.format("%.4f", testSummary.calculateT7(dataset)));
            System.out.println("T8  (Summarizer Cardinality):    " + String.format("%.4f", testSummary.calculateT8(dataset)));
            System.out.println("T9  (Qualifier Imprecision):     " + String.format("%.4f", testSummary.calculateT9(dataset)));
            System.out.println("T10 (Qualifier Cardinality):     " + String.format("%.4f", testSummary.calculateT10(dataset)));
            System.out.println("T11 (Length of Qualifier):       " + String.format("%.4f", testSummary.calculateT11(dataset)));
            System.out.println("\nOptimal (Weighted Average):      " + String.format("%.4f", testSummary.calculateOptimal(dataset)));
        }

        System.out.println("\n\n=== TESTING ALL QUALITY MEASURES (F2) ===\n");

        if (aboutHalf != null && hit != null && energiczna != null) {
            LinguisticSummary testSummaryF2 = new LinguisticSummary(aboutHalf, "utworów", energiczna, hit);

            System.out.println("Summary: " + testSummaryF2.generateSummary());
            System.out.println("\nIndividual Quality Measures:");
            System.out.println("T1  (Degree of Truth):           " + String.format("%.4f", testSummaryF2.calculateT1(dataset)));
            System.out.println("T2  (Degree of Imprecision):     " + String.format("%.4f", testSummaryF2.calculateT2(dataset)));
            System.out.println("T3  (Degree of Covering):        " + String.format("%.4f", testSummaryF2.calculateT3(dataset)));
            System.out.println("T4  (Degree of Appropriateness): " + String.format("%.4f", testSummaryF2.calculateT4(dataset)));
            System.out.println("T5  (Length of Summary):         " + String.format("%.4f", testSummaryF2.calculateT5(dataset)));
            System.out.println("T6  (Quantifier Imprecision):    " + String.format("%.4f", testSummaryF2.calculateT6(dataset)));
            System.out.println("T7  (Quantifier Cardinality):    " + String.format("%.4f", testSummaryF2.calculateT7(dataset)));
            System.out.println("T8  (Summarizer Cardinality):    " + String.format("%.4f", testSummaryF2.calculateT8(dataset)));
            System.out.println("T9  (Qualifier Imprecision):     " + String.format("%.4f", testSummaryF2.calculateT9(dataset)));
            System.out.println("T10 (Qualifier Cardinality):     " + String.format("%.4f", testSummaryF2.calculateT10(dataset)));
            System.out.println("T11 (Length of Qualifier):       " + String.format("%.4f", testSummaryF2.calculateT11(dataset)));
            System.out.println("\nOptimal (Weighted Average):      " + String.format("%.4f", testSummaryF2.calculateOptimal(dataset)));
        }

        System.out.println("\n\n=== MULTI-SUBJECT SUMMARIES (MSS) ===\n");

        if (aboutHalf != null && energiczna != null && glosna != null) {
            System.out.println("MSS examples comparing 'rap' vs 'pop' genres:\n");
            System.out.println(String.format("%-85s | %6s", "Summary", "T1"));
            System.out.println("-".repeat(95));

            double rapValue = SongRecord.genreStringtoDouble("rap");
            double popValue = SongRecord.genreStringtoDouble("pop");

            MSS1 mss1 = new MSS1("playlist_genre", "rap", "pop", rapValue, popValue,
                    aboutHalf, energiczna);
            System.out.println(mss1.generateSummaryWithMeasures(dataset));

            if (oneThird != null) {
                MSS1 mss1b = new MSS1("playlist_genre", "rap", "pop", rapValue, popValue,
                        oneThird, energiczna);
                System.out.println(mss1b.generateSummaryWithMeasures(dataset));
            }

            System.out.println();

            MSS2 mss2 = new MSS2("playlist_genre", "rap", "pop", rapValue, popValue,
                    aboutHalf, energiczna, glosna);
            System.out.println(mss2.generateSummaryWithMeasures(dataset));

            if (almostAll != null) {
                MSS2 mss2b = new MSS2("playlist_genre", "rap", "pop", rapValue, popValue,
                        almostAll, energiczna, glosna);
                System.out.println(mss2b.generateSummaryWithMeasures(dataset));
            }

            System.out.println();

            MSS3 mss3 = new MSS3("playlist_genre", "rap", "pop", rapValue, popValue,
                    aboutHalf, energiczna, glosna);
            System.out.println(mss3.generateSummaryWithMeasures(dataset));

            System.out.println();

            MSS4 mss4 = new MSS4("playlist_genre", "rap", "pop", rapValue, popValue, energiczna);
            System.out.println(mss4.generateSummaryWithMeasures(dataset));

            if (szybkie != null) {
                MSS4 mss4b = new MSS4("playlist_genre", "rap", "pop", rapValue, popValue, szybkie);
                System.out.println(mss4b.generateSummaryWithMeasures(dataset));
            }
        }

        System.out.println("\n=== DONE ===");
    }

    private static void printSummaryHeader() {
        System.out.println(String.format("%-80s | %6s | %6s | %6s | %6s | %6s | %6s | %6s | %6s | %6s | %6s | %6s | %7s",
                "Summary", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "Optimal"));
        System.out.println("-".repeat(200));
    }

    private static void printSummaryResults(LinguisticSummary summary, List<SongRecord> dataset) {
        String summaryText = summary.generateSummary();
        if (summaryText.length() > 80) {
            summaryText = summaryText.substring(0, 77) + "...";
        }

        double t1 = summary.calculateT1(dataset);
        double t2 = summary.calculateT2(dataset);
        double t3 = summary.calculateT3(dataset);
        double t4 = summary.calculateT4(dataset);
        double t5 = summary.calculateT5(dataset);
        double t6 = summary.calculateT6(dataset);
        double t7 = summary.calculateT7(dataset);
        double t8 = summary.calculateT8(dataset);
        double t9 = summary.calculateT9(dataset);
        double t10 = summary.calculateT10(dataset);
        double t11 = summary.calculateT11(dataset);
        double optimal = summary.calculateOptimal(dataset);

        System.out.println(String.format("%-80s | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.4f | %.5f",
                summaryText, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, optimal));
    }

    private static Summarizer findSummarizer(List<Summarizer> summarizers, String name) {
        return summarizers.stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private static Quantifier findQuantifier(List<Quantifier> quantifiers, String name) {
        return quantifiers.stream()
                .filter(q -> q.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}