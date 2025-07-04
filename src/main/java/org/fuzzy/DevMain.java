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
        for (Summarizer summarizer: summarizers){
            for (Quantifier quantifier: quantifiers) {
            LinguisticSummary summary = new LinguisticSummary(quantifier, "songs", summarizer);
            System.out.println(summary.generateSummaryWithMeasures(dataset));
            }
        }

        Quantifier one_third = quantifiers.stream()
                .filter(q -> q.getName().equals("JEDNA TRZECIA (1/3)"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Quantifier 'one third' not found"));
        System.out.println("\n=== Second Order Summaries ===");
        for (Summarizer summarizer: summarizers){
            for (Summarizer qualifier: summarizers) {
                if (qualifier.equals(summarizer)) continue; // Skip self-qualifying
                LinguisticSummary secondOrderSummary = new SecondOrderLinguisticSummary(
                        one_third, "songs", summarizer, qualifier);
                System.out.println(secondOrderSummary.generateSummaryWithMeasures(dataset));
            }
        }

    }
}

class FuzzySetExample {
    public static void main(String[] args) {
        // Create universe
        Universe universe = new Universe(0.0, 10.0, false, 0.5);

        // Create triangular fuzzy set
        FuzzySet triangular = new FuzzySet(universe,
                MembershipFunctions.triangular(2.0, 5.0, 8.0));

        // Create classic set
        FuzzySet classic = FuzzySet.classicSet(universe, 3.0, 7.0);

        // Operations
        FuzzySet union = triangular.union(classic);
        FuzzySet intersection = triangular.intersection(classic);
        FuzzySet complement = triangular.complement();

        // Properties
        System.out.println("Triangular set height: " + triangular.height());
        System.out.println("Is normal: " + triangular.isNormal());
        System.out.println("Is convex: " + triangular.isConvex());
        System.out.println("Cardinality: " + triangular.cardinality());
        System.out.println("Centroid: " + triangular.centroid());
    }
}

class LinguisticSummaryExample {

    public static void main(String[] args) {
        // Create sample dataset
        List<SongRecord> dataset = createSampleDataset();

        // Create summarizers
        Summarizer highEnergy = SummarizerFactory.highEnergy();
        Summarizer fastTempo = SummarizerFactory.fastTempo();
        Summarizer popular = SummarizerFactory.popular();

        // Create quantifiers
        Quantifier most = Quantifier.most();
        Quantifier few = Quantifier.few();
        Quantifier aboutFive = Quantifier.about(5);

        // Create linguistic summaries
        LinguisticSummary summary1 = new LinguisticSummary(most, "songs", highEnergy);
        LinguisticSummary summary2 = new LinguisticSummary(few, "songs", fastTempo);
        LinguisticSummary summary3 = new LinguisticSummary(aboutFive, "songs", popular);

        // Test summaries
        System.out.println("=== Linguistic Summaries with T1 ===");
        System.out.println(summary1.generateSummaryWithMeasures(dataset));
        System.out.println(summary2.generateSummaryWithMeasures(dataset));
        System.out.println(summary3.generateSummaryWithMeasures(dataset));

        // Detailed analysis
        System.out.println("\n=== Detailed Analysis ===");
        analyzeSummary(summary1, dataset);
        analyzeSummary(summary2, dataset);
        analyzeSummary(summary3, dataset);
    }

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

    // Create sample dataset for testing
    private static List<SongRecord> createSampleDataset() {
        List<SongRecord> dataset = new ArrayList<>();

        // Sample songs with various attributes
        dataset.add(createSong(0.8, 150.0, 75.0)); // High energy, medium tempo, medium popularity
        dataset.add(createSong(0.3, 80.0, 90.0));  // Low energy, slow tempo, high popularity
        dataset.add(createSong(0.9, 180.0, 60.0)); // High energy, fast tempo, medium popularity
        dataset.add(createSong(0.2, 70.0, 95.0));  // Low energy, slow tempo, high popularity
        dataset.add(createSong(0.7, 140.0, 80.0)); // Medium energy, medium tempo, high popularity
        dataset.add(createSong(0.85, 170.0, 45.0)); // High energy, fast tempo, low popularity
        dataset.add(createSong(0.4, 90.0, 70.0));  // Low energy, slow tempo, medium popularity
        dataset.add(createSong(0.75, 160.0, 85.0)); // High energy, fast tempo, high popularity
        dataset.add(createSong(0.1, 65.0, 30.0));  // Very low energy, very slow tempo, low popularity
        dataset.add(createSong(0.95, 190.0, 95.0)); // Very high energy, very fast tempo, very high popularity

        return dataset;
    }

    private static SongRecord createSong(double energy, double tempo, double popularity) {
        Map<String, Double> attributes = new HashMap<>();
        attributes.put("energy", energy);
        attributes.put("tempo", tempo);
        attributes.put("popularity", popularity);
        // TODO: Add other attributes like danceability, valence, etc.
        return new SongRecord(attributes);
    }
}

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