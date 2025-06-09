package org.fuzzy;

import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.fuzzy.summaries.LinguisticSummary;
import org.fuzzy.summaries.SecondOrderLinguisticSummary;
import org.fuzzy.summarizer.Summarizer;
import org.fuzzy.summarizer.SummarizerFactory;

import java.util.*;

import static org.dataImport.ConfigImporter.loadQuantifiersFromConfig;
import static org.dataImport.ConfigImporter.loadSummarizersFromConfig;
import static org.dataImport.CsvSongImporter.importSongs;
import static org.fuzzy.LinguisticSummaryExample.analyzeSummary;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class DevMain {
    public static void main(String[] args) {
        List<Summarizer> summarizers = loadSummarizersFromConfig();
        summarizers = summarizers.subList(0, 7);
        List<Quantifier> quantifiers = loadQuantifiersFromConfig();

        System.out.println("Loaded " + summarizers.size() + " summarizers");
        List<SongRecord> dataset = importSongs(30000);

        System.out.println("=== Linguistic Summaries===");
        String header = String.format("Podsumowanie %98s", " ") + "| T1   | T2     | T3     | T4     | T5     | T6     | T7     | T8     | T9     | T10  | T11  | Optimal";
        System.out.println(header);
        for (Summarizer summarizer: summarizers){
            for (Quantifier quantifier: quantifiers) {
            LinguisticSummary summary = new LinguisticSummary(quantifier, "utworów", summarizer);
//            System.out.println(summary.generateSummaryWithMeasures(dataset));
                summary.printLatexFuzzySummaryResults(dataset);
            }
        }

        Quantifier one_third = quantifiers.stream()
                .filter(q -> q.getName().equals("JEDNA TRZECIA (1/3)"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Quantifier 'one third' not found"));
        System.out.println("\n=== Second Order Summaries ===");
        System.out.println(header);
        for (Summarizer summarizer: summarizers){
            for (Summarizer qualifier: summarizers) {
                if (qualifier.equals(summarizer)) continue; // Skip self-qualifying
                LinguisticSummary secondOrderSummary = new SecondOrderLinguisticSummary(
                        one_third, "utworów", summarizer, qualifier);
//                System.out.println(secondOrderSummary.generateSummaryWithMeasures(dataset));
                secondOrderSummary.printLatexFuzzySummaryResults(dataset);
            }
        }

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