package org.fuzzy;

import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summaries.*;
import org.fuzzy.summarizer.CompoundSummarizer;
import org.fuzzy.summarizer.Summarizer;

import java.util.*;

import static org.dataImport.ConfigImporter.loadQuantifiersFromConfig;
import static org.dataImport.ConfigImporter.loadSummarizersFromConfig;
import static org.dataImport.CsvSongImporter.importSongs;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class DevMain {
    public static void main(String[] args) {
        List<Summarizer> summarizers = loadSummarizersFromConfig();
        summarizers = summarizers.subList(6, 25);
        List<Quantifier> quantifiers = loadQuantifiersFromConfig();

        System.out.println("Loaded " + summarizers.size() + " summarizers");
        List<SongRecord> dataset = importSongs(30000);

        for (Summarizer summarizer : summarizers) {
            summarizer.getFuzzySet().getUniverse().setCardinalNumber(dataset.size());
            summarizer.connectDataset(dataset);
        }

        for (Quantifier quantifier : quantifiers) {
            quantifier.connectDataset(dataset);
            int cardinalNumber =  quantifier.isRelative() ? 1 : dataset.size(); // For relative quantifiers, universe is [0,1], for absolute [0,30000]
            quantifier.getFuzzySet().getUniverse().setCardinalNumber(cardinalNumber);
        }

//        // MSS1
        String predicate1 = "rap";
        String predicate2 = "pop";
        Quantifier testQuantifier = quantifiers.stream()
                .filter(q -> q.getName().equals("JEDNA TRZECIA (1/3)"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Quantifier not found"));
//
//        for (Summarizer summarizer : summarizers) {
//            MSS1 mss1 = new MSS1(predicate1, predicate2, testQuantifier, summarizer);
////            System.out.println(mss1.generateSummaryWithMeasures(dataset));
//            mss1.printLatexFuzzySummaryResults(dataset);
//        }
//        // MSS2
//        for (Summarizer summarizer1 : summarizers) {
//            for (Summarizer summarizer2 : summarizers) {
//                if (summarizer1.equals(summarizer2)) continue; // Skip self-comparison
//                MSS2 mss2 = new MSS2(predicate1, predicate2, testQuantifier, summarizer1, summarizer2);
////                System.out.println(mss2.generateSummaryWithMeasures(dataset));
//                mss2.printLatexFuzzySummaryResults(dataset);
//            }
//
//        }
//        // MSS3
//        for (Summarizer summarizer1 : summarizers) {
//            for (Summarizer summarizer2 : summarizers) {
//                if (summarizer1.equals(summarizer2)) continue; // Skip self-comparison
//                MSS3 mss3 = new MSS3(predicate1, predicate2, testQuantifier, summarizer1, summarizer2);
////                System.out.println(mss3.generateSummaryWithMeasures(dataset));
//                mss3.printLatexFuzzySummaryResults(dataset);
//            }
//        }
//        // MSS4
//        for (Summarizer summarizer : summarizers) {
//                MSS4 mss4 = new MSS4(predicate1, predicate2, summarizer);
////                System.out.println(mss4.generateSummaryWithMeasures(dataset));
//                mss4.printLatexFuzzySummaryResults(dataset);
//        }
        // Compound Summarizer

        for (Summarizer summarizer1 : summarizers) {
            for (Summarizer summarizer2 : summarizers) {
                if (summarizer1.equals(summarizer2)) continue; // Skip self-comparison
                List<Summarizer> temp = new ArrayList<>();
                temp.add(summarizer1); temp.add(summarizer2);
                CompoundSummarizer compoundSummarizer = new CompoundSummarizer(temp);
                LinguisticSummaryCompound summary = new LinguisticSummaryCompound(
                        testQuantifier, "utworów", compoundSummarizer);
//                System.out.println(summary.generateSummaryWithMeasures(dataset));
                summary.printLatexFuzzySummaryResults(dataset);
            }

        }
        System.exit(0);

//        System.out.println("=== Linguistic Summaries===");
//        String header = String.format("Podsumowanie %98s", " ") + "   | T1   | T2     | T3     | T4     | T5     | T6     | T7     | T8     | T9     | T10    | T11    | Optimal";
//        System.out.println(header);
//        for (Summarizer summarizer: summarizers){
//            for (Quantifier quantifier: quantifiers) {
//            LinguisticSummary summary = new LinguisticSummary(quantifier, "utworów", summarizer);
////            System.out.println(summary.generateSummaryWithMeasures(dataset));
//                summary.printLatexFuzzySummaryResults(dataset);
//            }
//        }
//
//        Quantifier one_third = quantifiers.stream()
//                .filter(q -> q.getName().equals("OKOŁO POŁOWY"))
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("Quantifier 'more than 1000' not found"));
//        System.out.println("\n=== Second Order Summaries ===");
//        System.out.println(header);
//        for (Summarizer summarizer: summarizers){
//            for (Summarizer qualifier: summarizers) {
//                if (qualifier.equals(summarizer)) continue; // Skip self-qualifying
//                if (qualifier.linguisiticVariable.equals(summarizer.linguisiticVariable)) continue; // Skip same linguistic variable
//                LinguisticSummary secondOrderSummary = new SecondOrderLinguisticSummary(
//                        one_third, "utworów", summarizer, qualifier);
////                System.out.println(secondOrderSummary.generateSummaryWithMeasures(dataset));
//                secondOrderSummary.printLatexFuzzySummaryResults(dataset);
//            }
//        }

    }
}

class LinguisticSummaryExample {

    public static void main(String[] args) {}

    public static void analyzeSummary(LinguisticSummary summary, List<SongRecord> dataset) {
        System.out.println("\nSummary: " + summary.generateSummary());

        double r = summary.getSummarizer().calculateR(dataset);
        int m = dataset.size();
        double t1 = summary.calculateT1(dataset);

        System.out.println("  Records matching summarizer (r): " + String.format("%.7f", r));
        System.out.println("  Total records (m): " + m);
        if (summary.getQuantifier().isRelative()) {
            System.out.println("  Proportion (r/m): " + String.format("%.7f", r/m));
        }
        System.out.println("  T1 (degree of truth): " + String.format("%.7f", t1));
    }
}