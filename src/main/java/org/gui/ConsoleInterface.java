package org.gui;

import org.dataImport.ConfigImporter;
import org.fuzzy.LinguisticSummary;
import org.fuzzy.SongRecord;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleInterface {
    private final Scanner scanner = new Scanner(System.in);

    private List<String> lastGeneratedSummaries = new ArrayList<>();

    private List<SongRecord> dataset;

    private List<Quantifier> quantifiers;
    private List<Summarizer> summarizers;

    private Quantifier selectedQuantifier;
    private List<Summarizer> selectedSummarizers;

    public ConsoleInterface() {
        // Ładujemy config przy starcie
        quantifiers = ConfigImporter.loadQuantifiersFromConfig();
        summarizers = ConfigImporter.loadSummarizersFromConfig();
        dataset = new ArrayList<>();  // pusta lista lub załaduj dane w inny sposób

    }

    public void start() {
        while (true) {
            System.out.println("\n=== MENU GŁÓWNE ===");
            System.out.println("1. Wybierz kwalifikator");
            System.out.println("2. Wybierz zmienne lingwistyczne (sumaryzatory)");
            System.out.println("3. Generuj podsumowania lingwistyczne");
            System.out.println("4. Ustaw wagi miar jakości");
            System.out.println("5. Posortuj podsumowania");
            System.out.println("6. Zapisz podsumowania do pliku");
            System.out.println("7. Tryb zaawansowany");
            System.out.println("0. Wyjście");

            System.out.print("Twój wybór: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    selectQuantifier();
                    break;
                case "2":
                    selectSummarizers();
                    break;
                case "3":
                    generateSummaries();
                    break;
                case "4":
                    setWeights();
                    break;
                case "5":
                    sortSummaries();
                    break;
                case "6":
                    saveSummaries();
                    break;
                case "7":
                    advancedMode();
                    break;
                case "0":
                    System.out.println("Zamykanie...");
                    return;
                default:
                    System.out.println("Nieprawidłowy wybór. Spróbuj ponownie.");
            }
        }
    }



    private void selectQuantifier() {
        System.out.println("\nWybierz kwalifikator (Quantifier):");
        for (int i = 0; i < quantifiers.size(); i++) {
            System.out.println((i + 1) + ". " + quantifiers.get(i).getName());
        }
        System.out.print("Podaj numer kwalifikatora: ");
        try {
            int quantIndex = Integer.parseInt(scanner.nextLine()) - 1;
            if (quantIndex >= 0 && quantIndex < quantifiers.size()) {
                selectedQuantifier = quantifiers.get(quantIndex);
                System.out.println("Wybrano kwalifikator: " + selectedQuantifier.getName());
            } else {
                System.out.println("Nieprawidłowy numer kwalifikatora.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Błędny format numeru kwalifikatora.");
        }
    }

    private void selectSummarizers() {
        System.out.println("\nWybierz zmienne lingwistyczne (Summarizers) (oddziel numery przecinkami):");
        for (int i = 0; i < summarizers.size(); i++) {
            System.out.println((i + 1) + ". " + summarizers.get(i).getName() +
                    " (pole: " + summarizers.get(i).getFieldName() + ")");
        }
        System.out.print("Podaj numery zmiennych: ");
        String[] summarizerIndices = scanner.nextLine().split(",");
        selectedSummarizers = new java.util.ArrayList<>();

        for (String idxStr : summarizerIndices) {
            try {
                int idx = Integer.parseInt(idxStr.trim()) - 1;
                if (idx >= 0 && idx < summarizers.size()) {
                    selectedSummarizers.add(summarizers.get(idx));
                } else {
                    System.out.println("Nieprawidłowy numer: " + (idx + 1));
                }
            } catch (NumberFormatException e) {
                System.out.println("Błędny format numeru: " + idxStr);
            }
        }

        System.out.println("Wybrano " + selectedSummarizers.size() + " zmiennych lingwistyczne.");
    }

    private void generateSummaries() {
        lastGeneratedSummaries.clear();

        if (selectedQuantifier == null) {
            System.out.println("Najpierw wybierz kwalifikator (opcja 1).");
            return;
        }
        if (selectedSummarizers == null || selectedSummarizers.isEmpty()) {
            System.out.println("Najpierw wybierz zmienne lingwistyczne (opcja 2).");
            return;
        }
        if (dataset.isEmpty()) {
            System.out.println("Brak danych do analizy.");
            return;
        }

        System.out.println("\nGenerowanie podsumowań lingwistycznych:");

        for (Summarizer summ : selectedSummarizers) {
            LinguisticSummary ls = new LinguisticSummary(selectedQuantifier, "songs", summ);
            double truthDegree = ls.calculateT1(dataset);
            String summaryText = ls.generateSummary();

            String line = String.format("%s\nStopień prawdziwości: %.3f", summaryText, truthDegree);
            System.out.println("- " + line);
            lastGeneratedSummaries.add(line);
        }
    }


    private void setWeights() {
        System.out.println("Ustawianie wag... (niezaimplementowane)");
    }

    private void sortSummaries() {
        System.out.println("Sortowanie podsumowań... (niezaimplementowane)");
    }

    private void saveSummaries() {
        if (lastGeneratedSummaries.isEmpty()) {
            System.out.println("Brak podsumowań do zapisania. Wygeneruj podsumowania najpierw (opcja 3).");
            return;
        }

        System.out.print("Podaj nazwę pliku do zapisu (np. podsumowania.txt): ");
        String filename = scanner.nextLine();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String summary : lastGeneratedSummaries) {
                writer.write(summary);
                writer.newLine();
                writer.newLine();
            }
            System.out.println("Podsumowania zostały zapisane do pliku: " + filename);
        } catch (IOException e) {
            System.out.println("Błąd podczas zapisu do pliku: " + e.getMessage());
        }
    }

    private void advancedMode() {
        System.out.println("Tryb zaawansowany... (niezaimplementowane)");
    }
}