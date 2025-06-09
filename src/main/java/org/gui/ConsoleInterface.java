package org.gui;

import org.dataImport.ConfigImporter;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;

import java.util.List;
import java.util.Scanner;

public class ConsoleInterface {
    private final Scanner scanner = new Scanner(System.in);

    private List<Quantifier> quantifiers;
    private List<Summarizer> summarizers;

    private Quantifier selectedQuantifier;
    private List<Summarizer> selectedSummarizers;

    public ConsoleInterface() {
        // Ładujemy config przy starcie
        quantifiers = ConfigImporter.loadQuantifiersFromConfig();
        summarizers = ConfigImporter.loadSummarizersFromConfig();
    }

    public void start() {
        while (true) {
            System.out.println("\n=== MENU GŁÓWNE ===");
            System.out.println("1. Wybierz sumaryzator i kwalifikator");
            System.out.println("2. Generuj podsumowania lingwistyczne");
            System.out.println("3. Ustaw wagi miar jakości");
            System.out.println("4. Posortuj podsumowania");
            System.out.println("5. Zapisz podsumowania do pliku");
            System.out.println("6. Tryb zaawansowany");
            System.out.println("0. Wyjście");

            System.out.print("Twój wybór: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    selectLabels();
                    break;
                case "2":
                    generateSummaries();
                    break;
                case "3":
                    setWeights();
                    break;
                case "4":
                    sortSummaries();
                    break;
                case "5":
                    saveSummaries();
                    break;
                case "6":
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

    private void selectLabels() {
        System.out.println("\nWybierz kwalifikator (Quantifier):");
        for (int i = 0; i < quantifiers.size(); i++) {
            System.out.println((i + 1) + ". " + quantifiers.get(i).getName());
        }
        System.out.print("Podaj numer kwalifikatora: ");
        int quantIndex = Integer.parseInt(scanner.nextLine()) - 1;

        if (quantIndex >= 0 && quantIndex < quantifiers.size()) {
            selectedQuantifier = quantifiers.get(quantIndex);
            System.out.println("Wybrano kwalifikator: " + selectedQuantifier.getName());
        } else {
            System.out.println("Nieprawidłowy numer kwalifikatora.");
            return;
        }

        System.out.println("\nWybierz sumaryzatory (Summarizers) (oddziel numery przecinkami):");
        for (int i = 0; i < summarizers.size(); i++) {
            System.out.println((i + 1) + ". " + summarizers.get(i).getName() +
                    " (pole: " + summarizers.get(i).getFieldName() + ")");
        }
        System.out.print("Podaj numery sumaryzatorów: ");
        String[] summarizerIndices = scanner.nextLine().split(",");
        selectedSummarizers = new java.util.ArrayList<>();

        for (String idxStr : summarizerIndices) {
            try {
                int idx = Integer.parseInt(idxStr.trim()) - 1;
                if (idx >= 0 && idx < summarizers.size()) {
                    selectedSummarizers.add(summarizers.get(idx));
                } else {
                    System.out.println("Nieprawidłowy numer sumaryzatora: " + (idx + 1));
                }
            } catch (NumberFormatException e) {
                System.out.println("Błędny format numeru: " + idxStr);
            }
        }

        System.out.println("Wybrano " + selectedSummarizers.size() + " sumaryzatorów.");
    }

    // Placeholder dla innych metod:
    private void generateSummaries() {
        System.out.println("Generowanie podsumowań... (niezaimplementowane)");
    }
    private void setWeights() {
        System.out.println("Ustawianie wag... (niezaimplementowane)");
    }
    private void sortSummaries() {
        System.out.println("Sortowanie podsumowań... (niezaimplementowane)");
    }
    private void saveSummaries() {
        System.out.println("Zapis do pliku... (niezaimplementowane)");
    }
    private void advancedMode() {
        System.out.println("Tryb zaawansowany... (niezaimplementowane)");
    }
}