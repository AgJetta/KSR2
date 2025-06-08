package org.fuzzy;

import org.dataImport.ConfigImporter;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.fuzzy.summarizer.CompoundSummarizer;
import org.fuzzy.summarizer.Summarizer;
import org.fuzzy.summarizer.SummarizerFactory;

import java.util.*;

import static org.dataImport.ConfigImporter.loadQuantifiersFromConfig;
import static org.dataImport.ConfigImporter.loadSummarizersFromConfig;
import static org.dataImport.CsvSongImporter.importSongs;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        List<SongRecord> dataset = importSongs(30000);

        List<Summarizer> summarizers = loadSummarizersFromConfig();
        for (Summarizer summarizer : summarizers) { summarizer.calculateAllMemberships(dataset);}
        List<Quantifier> quantifiers = loadQuantifiersFromConfig();
        for (Quantifier quantifier : quantifiers) { quantifier.calculateAllMemberships(dataset);}

        System.out.println("Loaded " + summarizers.size() + " summarizers");

        System.out.println("=== Linguistic Summary with T1 ===");
        for (Summarizer summarizer: summarizers){
            for (Quantifier quantifier: quantifiers) {
            LinguisticSummary summary = new LinguisticSummary(quantifier, "songs", summarizer);
            System.out.println(summary.generateSummaryWithT1(dataset));
            }
        }
        for (Summarizer summarizer: summarizers){
            for (Quantifier quantifier: quantifiers) {
                LinguisticSummary summary = new LinguisticSummary(quantifier, "songs", summarizer);

                // Detailed analysis
                System.out.println("\n=== Detailed Analysis ===");
                analyzeSummary(summary, dataset);
            }
        }

        Quantifier one_third_quantifier = quantifiers.stream()
                .filter(q -> q.getName().equals("JEDNA TRZECIA (1/3)"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Quantifier 'one-third' not found"));

        System.out.println("=== Linguistic Summary with One-Third Quantifier ===");
        for (Summarizer summarizer: summarizers) {
            CompoundSummarizer not_summarizer = new CompoundSummarizer(summarizer);
            LinguisticSummary summary = new LinguisticSummary(one_third_quantifier, "songs", not_summarizer);
            System.out.println(summary.generateSummaryWithT1(dataset));

            // Detailed analysis
            analyzeSummary(summary, dataset);
        }
    }

    private static void analyzeSummary(LinguisticSummary summary, List<SongRecord> dataset) {
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
};

// TODO: Future enhancements
/*
 * TODO: Second-order linguistic summaries
 * TODO: Compound summarizers (AND, OR operations)
 * TODO: T2-T11 measures implementation
 * TODO: CSV file loading utility
 * TODO: More sophisticated quantifier definitions
 * TODO: Linguistic variable support
 * TODO: Summary quality ranking
 * TODO: Protoform validation
 * TODO: Natural language generation improvements
 */