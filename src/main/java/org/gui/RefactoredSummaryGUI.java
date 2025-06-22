package org.gui;

import org.dataImport.ConfigImporter;
import org.dataImport.CsvSongImporter;
import org.fuzzy.SongRecord;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;
import org.fuzzy.summaries.LinguisticSummary;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RefactoredSummaryGUI extends JFrame {
    // Data
    private List<Quantifier> quantifiers;
    private List<Summarizer> summarizers;
    private List<SongRecord> dataset;

    // UI Components - Predicate Panel
    private JComboBox<String> predicateCombo1;
    private JComboBox<String> predicateCombo2;

    // UI Components - Summarizer Selection
    private JList<String> summarizerSelectionList;
    private DefaultListModel<String> summarizerListModel;

    // UI Components - Results
    private DefaultTableModel tableModel;
    private JTable resultsTable;
    private JLabel statusLabel;

    private final static String[] predicates = {
            "ROCK", "RAP", "EDM", "LATIN"
    };

    private final static String NO_PREDICATE = "";
    // Generated results storage
    private List<SummaryResult> allResults = new ArrayList<>();

    public RefactoredSummaryGUI() {
        setTitle("Linguistic Summary Generator - Refactored");
        setSize(1800, 1000);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Load data
        loadData();

        // Create UI
        createPredicatePanel();
        createSummarizerSelectionPanel();
        createControlPanel();
        createResultsPanel();

        // Apply styling
        applyTheme();

        setVisible(true);
    }

    private void loadData() {
        quantifiers = ConfigImporter.loadQuantifiersFromConfig();
        summarizers = ConfigImporter.loadSummarizersFromConfig();
        dataset = CsvSongImporter.importSongs(30000);

        // Configure summarizers and quantifiers as in original
        for (Summarizer summarizer : summarizers) {
            summarizer.getFuzzySet().getUniverse().setCardinalNumber(dataset.size());
            summarizer.connectDataset(dataset);
        }

        for (Quantifier quantifier : quantifiers) {
            quantifier.connectDataset(dataset);
            int cardinalNumber = quantifier.isRelative() ? 1 : dataset.size();
            quantifier.getFuzzySet().getUniverse().setCardinalNumber(cardinalNumber);
        }
    }

    private void createPredicatePanel() {
        JPanel predicatePanel = new JPanel();
        predicatePanel.setBorder(BorderFactory.createTitledBorder("Predicate Configuration"));
        predicatePanel.setLayout(new BoxLayout(predicatePanel, BoxLayout.Y_AXIS));

        // First predicate (always visible)
        JPanel firstPredicatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        firstPredicatePanel.setBorder(BorderFactory.createTitledBorder("First Predicate"));

        predicateCombo1 = new JComboBox<>();
        predicateCombo2 = new JComboBox<>();

        firstPredicatePanel.add(new JLabel("Gatunek 1"));
        firstPredicatePanel.add(predicateCombo1);
        firstPredicatePanel.add(new JLabel("Gatunek 2"));
        firstPredicatePanel.add(predicateCombo2);

        for (String predicate : predicates) {
            predicateCombo1.addItem(predicate);
            predicateCombo2.addItem(predicate);
        }
        predicateCombo2.addItem(NO_PREDICATE);

        predicatePanel.add(firstPredicatePanel);

        add(predicatePanel, BorderLayout.NORTH);
    }

    private void createSummarizerSelectionPanel() {
        JPanel summarizerPanel = new JPanel(new BorderLayout());
        summarizerPanel.setBorder(BorderFactory.createTitledBorder("Select Summarizers for Combination"));

        // Create list model and populate
        summarizerListModel = new DefaultListModel<>();
        for (Summarizer s : summarizers) {
            summarizerListModel.addElement(s.getName());
        }

        // Create list with multi-selection
        summarizerSelectionList = new JList<>(summarizerListModel);
        summarizerSelectionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        summarizerSelectionList.setLayoutOrientation(JList.VERTICAL);
        summarizerSelectionList.setVisibleRowCount(6);

        JScrollPane scrollPane = new JScrollPane(summarizerSelectionList);
        scrollPane.setPreferredSize(new Dimension(400, 150));

        // Selection control buttons
        JPanel selectionButtonPanel = new JPanel(new FlowLayout());
        JButton selectAllBtn = new JButton("Select All");
        JButton clearAllBtn = new JButton("Clear All");

        selectAllBtn.addActionListener(e ->
                summarizerSelectionList.setSelectionInterval(0, summarizerListModel.getSize() - 1));
        clearAllBtn.addActionListener(e ->
                summarizerSelectionList.clearSelection());

        selectionButtonPanel.add(selectAllBtn);
        selectionButtonPanel.add(clearAllBtn);

        summarizerPanel.add(new JLabel("Select summarizers to combine with the chosen predicate:"), BorderLayout.NORTH);
        summarizerPanel.add(scrollPane, BorderLayout.CENTER);
        summarizerPanel.add(selectionButtonPanel, BorderLayout.SOUTH);

        add(summarizerPanel, BorderLayout.WEST);
    }

    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton generateBtn = new JButton("Generate All Combinations");
        JButton clearBtn = new JButton("Clear Results");
        JButton saveBtn = new JButton("Save Results");
        JButton loadBtn = new JButton("Load Results");

        // Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.BLUE);

        generateBtn.addActionListener(this::generateAllCombinations);
        clearBtn.addActionListener(this::clearResults);
        saveBtn.addActionListener(this::saveResults);
        loadBtn.addActionListener(this::loadResults);

        controlPanel.add(generateBtn);
        controlPanel.add(clearBtn);
        controlPanel.add(saveBtn);
        controlPanel.add(loadBtn);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(statusLabel);

        add(controlPanel, BorderLayout.CENTER);
    }

    private void createResultsPanel() {
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Generated Summaries"));

        String[] columns = {
                "Summary", "T1", "T2", "T3", "T4", "T5", "T6",
                "T7", "T8", "T9", "T10", "T11", "Optimal"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setAutoCreateRowSorter(true);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configure column widths
        TableColumn summaryColumn = resultsTable.getColumnModel().getColumn(0);
        summaryColumn.setPreferredWidth(500);

        // Right-align numeric columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 1; i < columns.length; i++) {
            resultsTable.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
            resultsTable.getColumnModel().getColumn(i).setPreferredWidth(60);
        }

        JScrollPane tableScrollPane = new JScrollPane(resultsTable);
        tableScrollPane.setPreferredSize(new Dimension(1200, 400));

        resultsPanel.add(tableScrollPane, BorderLayout.CENTER);

        add(resultsPanel, BorderLayout.SOUTH);
    }

    private void generateAllCombinations(ActionEvent e) {
        // Clear previous results
        allResults.clear();
        tableModel.setRowCount(0);
        statusLabel.setText("Generating combinations...");
        statusLabel.setForeground(Color.ORANGE);

        // Get selected predicate configuration
        String predicate1 = (String) predicateCombo1.getSelectedItem();
        String predicate2 = (String) predicateCombo2.getSelectedItem();
        if (predicate1 == null) {
            statusLabel.setText("Please select at least the first predicate");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // Get selected summarizers for combination
        List<Integer> selectedIndices = Arrays.stream(summarizerSelectionList.getSelectedIndices())
                .boxed().collect(Collectors.toList());

        if (selectedIndices.isEmpty()) {
            statusLabel.setText("Please select at least one summarizer for combination");
            statusLabel.setForeground(Color.RED);
            return;
        }

        int totalCombinations = 0;
        int filteredCombinations = 0;

        // Generate combinations
        for (int summarizerIndex : selectedIndices) {
            Summarizer combinationSummarizer = summarizers.get(summarizerIndex);
            for (Quantifier quantifier : quantifiers) {
//                if (quantifier.isRelative() && predicate2.equals(NO_PREDICATE)) {
//                    continue; // Skip if relative quantifier and no second predicate
//                }
                // Create linguistic summary
                LinguisticSummary summary = new LinguisticSummary(
                        quantifier,
                        predicate1,
                        combinationSummarizer
                );

                // Calculate all T values
                double[] tValues = calculateAllTValues(summary);
                double t1 = tValues[0]; // Optimal is at index 11

                totalCombinations++;

                // Filter out zero/low values (you can adjust this threshold)
                if (t1 > 0.001) { // Using small threshold instead of exactly 0
                    String summaryText = summary.generateSummary();
                    SummaryResult result = new SummaryResult(summaryText, tValues);
                    allResults.add(result);
                    addResultToTable(result);
                    filteredCombinations++;
                }
            }

        }

        statusLabel.setText(String.format("Generated %d combinations, %d passed filter",
                totalCombinations, filteredCombinations));
        statusLabel.setForeground(Color.GREEN);
    }

    private double[] calculateAllTValues(LinguisticSummary summary) {
        double[] values = new double[12];
        values[0] = summary.calculateT1(dataset);
        values[1] = summary.calculateT2(dataset);
        values[2] = summary.calculateT3(dataset);
        values[3] = summary.calculateT4(dataset);
        values[4] = summary.calculateT5(dataset);
        values[5] = summary.calculateT6(dataset);
        values[6] = summary.calculateT7(dataset);
        values[7] = summary.calculateT8(dataset);
        values[8] = summary.calculateT9(dataset);
        values[9] = summary.calculateT10(dataset);
        values[10] = summary.calculateT11(dataset);
        values[11] = summary.calculateOptimal(dataset);
        return values;
    }

    private void addResultToTable(SummaryResult result) {
        Object[] row = new Object[13];
        row[0] = result.getSummary();
        double[] tValues = result.getTValues();
        for (int i = 0; i < tValues.length; i++) {
            row[i + 1] = String.format("%.4f", tValues[i]);
        }
        tableModel.addRow(row);
    }

    private void clearResults(ActionEvent e) {
        allResults.clear();
        tableModel.setRowCount(0);
        statusLabel.setText("Results cleared");
        statusLabel.setForeground(Color.BLUE);
    }

    private void saveResults(ActionEvent e) {
        if (allResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results to save.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Results");
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                for (SummaryResult res : allResults) {
                    writer.println(res.getSummary());
                    for (double val : res.getTValues()) {
                        writer.print(val + " ");
                    }
                    writer.println();
                }
                statusLabel.setText("Results saved successfully");
                statusLabel.setForeground(Color.GREEN);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving results: " + ex.getMessage());
                statusLabel.setText("Error saving results");
                statusLabel.setForeground(Color.RED);
            }
        }
    }

    private void loadResults(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Results");
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            allResults.clear();
            tableModel.setRowCount(0);

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String summaryText = line;
                    String metricsLine = reader.readLine();
                    if (metricsLine == null) break;

                    String[] parts = metricsLine.trim().split("\\s+");
                    double[] metrics = new double[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        metrics[i] = Double.parseDouble(parts[i]);
                    }

                    SummaryResult res = new SummaryResult(summaryText, metrics);
                    allResults.add(res);
                    addResultToTable(res);
                }
                statusLabel.setText("Results loaded successfully");
                statusLabel.setForeground(Color.GREEN);
            } catch (IOException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error loading results: " + ex.getMessage());
                statusLabel.setText("Error loading results");
                statusLabel.setForeground(Color.RED);
            }
        }
    }

    private void applyTheme() {
        // Apply the colorful theme from the original
        Color pinkPastel = new Color(177, 59, 255);
        Color lavender = new Color(230, 230, 250);
        Color babyBlue = new Color(173, 216, 230);

        getContentPane().setBackground(pinkPastel);

        Font coolFont = new Font("Comic Sans MS", Font.ITALIC, 14);
        Font boldFont = new Font("Comic Sans MS", Font.BOLD, 14);

        // Style combo boxes
        predicateCombo1.setFont(coolFont);
        predicateCombo2.setFont(coolFont);
        predicateCombo1.setBackground(Color.WHITE);
        predicateCombo2.setBackground(Color.WHITE);

        // Style list
        summarizerSelectionList.setFont(coolFont);
        summarizerSelectionList.setBackground(babyBlue);

        // Style table
        resultsTable.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
        resultsTable.getTableHeader().setFont(boldFont);
        resultsTable.setBackground(new Color(236, 203, 252));
        resultsTable.setForeground(new Color(199, 21, 133));

        // Style buttons
        for (Component comp : getContentPane().getComponents()) {
            styleComponentRecursively(comp, coolFont);
        }
    }

    private void styleComponentRecursively(Component comp, Font font) {
        if (comp instanceof JButton btn) {
            btn.setBackground(new Color(59, 103, 255));
            btn.setForeground(Color.WHITE);
            btn.setFont(font);
            btn.setFocusPainted(false);
        } else if (comp instanceof Container container) {
            for (Component child : container.getComponents()) {
                styleComponentRecursively(child, font);
            }
        }
    }

    private static class SummaryResult {
        private final String summary;
        private final double[] tValues;

        SummaryResult(String summary, double[] tValues) {
            this.summary = summary;
            this.tValues = tValues.clone();
        }

        String getSummary() { return summary; }
        double[] getTValues() { return tValues; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new RefactoredSummaryGUI();
        });
    }
}