package org.gui;

import org.dataImport.ConfigImporter;
import org.dataImport.CsvSongImporter;
import org.fuzzy.FuzzySet;
import org.fuzzy.SongRecord;
import org.fuzzy.Universe;
import org.fuzzy.membershipFunctions.MembershipFunction;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summaries.*;
import org.fuzzy.summarizer.CompoundSummarizer;
import org.fuzzy.summarizer.Summarizer;

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
            "rock", "rap", "edm", "latin"
    };

    private List<Double> measureWeights = Arrays.asList(0.2, 0.05, 0.05, 0.2, 0.05, 0.05, 0.1, 0.1, 0.1, 0.1);

    private JTextField[] weightFields;

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

    private void updateTableFromQuantifiers(DefaultTableModel model) {
        model.setRowCount(0); // Clear existing rows
        for (Quantifier q : quantifiers) {
            model.addRow(new Object[] {
                    q.getName(),
                    q.isRelative() ? "Relative" : "Absolute",  // string instead of boolean
                    "[" + q.getFuzzySet().getUniverse().getStart() + ", " + q.getFuzzySet().getUniverse().getEnd() + "]"
            });
        }
    }


    private void openAdvancedSettingsDialog() {
        // Create a modal dialog
        JDialog dialog = new JDialog(this, "Zaawansowane ustawienia", true);
        dialog.setSize(1200, 1000);
        dialog.setLocationRelativeTo(this);

        // Create tabbed pane with quantifiers tab
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Quantifiers", createQuantifierPanel(dialog));

        dialog.add(tabbedPane);
        dialog.setVisible(true);
    }

    private JPanel createQuantifierPanel(JDialog parentDialog) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Left side: your existing panel with form + table + button
        JPanel leftPanel = new JPanel(new BorderLayout());

        JTextField nameField = new JTextField(15);
        JComboBox<String> functionTypeBox = new JComboBox<>(new String[]{"triangular", "trapezoidal"});
        JCheckBox relativeBox = new JCheckBox("Relative");
        JTextField paramField = new JTextField(15);
        JTextField universeField = new JTextField(15);

        JPanel formPanel = new JPanel(new GridLayout(5, 2));
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Function Type:"));
        formPanel.add(functionTypeBox);
        formPanel.add(new JLabel("Universe Range (min,max):"));
        formPanel.add(universeField);
        formPanel.add(new JLabel("Parameters (comma-separated):"));
        formPanel.add(paramField);
        formPanel.add(new JLabel("Relative:"));
        formPanel.add(relativeBox);

        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[]{"Nazwa", "Typ", "Dziedzina"}, 0
        );
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        updateTableFromQuantifiers(tableModel);

        JButton addButton = new JButton("Dodaj kwantyfikator");
        addButton.addActionListener(e -> {
            // existing add logic here (same as you have)
            try {
                String name = nameField.getText().trim();
                String funcType = (String) functionTypeBox.getSelectedItem();
                boolean isRelative = relativeBox.isSelected();

                double[] parameters = Arrays.stream(paramField.getText().split(","))
                        .map(String::trim)
                        .mapToDouble(Double::parseDouble)
                        .toArray();

                double[] universe = Arrays.stream(universeField.getText().split(","))
                        .map(String::trim)
                        .mapToDouble(Double::parseDouble)
                        .toArray();

                if ((funcType.equals("triangular") && parameters.length != 3) ||
                        (funcType.equals("trapezoidal") && parameters.length != 4)) {
                    JOptionPane.showMessageDialog(leftPanel, "Nieprawidłowa liczba parametrów dla " + funcType);
                    return;
                }

                MembershipFunction mf;
                if (funcType.equals("triangular")) {
                    mf = MembershipFunctions.triangular(parameters[0], parameters[1], parameters[2]);
                } else {
                    mf = MembershipFunctions.trapezoidal(parameters[0], parameters[1], parameters[2], parameters[3]);
                }

                Universe universeObj = new Universe(universe[0], universe[1], true);

                FuzzySet fuzzySet = new FuzzySet(universeObj, mf);
                Quantifier newQuantifier = new Quantifier(name, fuzzySet, isRelative, universe[0], universe[1]);

                quantifiers.add(newQuantifier);
                updateTableFromQuantifiers(tableModel);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(leftPanel, "Błąd podczas dodawania: " + ex.getMessage());
            }
        });

        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        leftPanel.add(addButton, BorderLayout.SOUTH);

        // Right side: example quantifiers text area
        JTextArea exampleTextArea = new JTextArea();
        exampleTextArea.setEditable(false);
        exampleTextArea.setText(

                "" +
                        "EXAMPLE CONFIGURATIONS:" +
                        "\n{" +
                        "  \"name\": \"JEDNA TRZECIA (1/3)\",\n" +
                        "  \"relative\": true,\n" +
                        "  \"functionType\": \"triangular\",\n" +
                        "  \"parameters\": [0.0, 0.3333333, 0.6666667],\n" +
                        "  \"universe\": [0, 1]\n" +
                        "},\n" +
                        "{\n" +
                        "  \"name\": \"MNIEJ NIŻ 100\",\n" +
                        "  \"relative\": false,\n" +
                        "  \"functionType\": \"trapezoidal\",\n" +
                        "  \"parameters\": [0, 0, 95, 100],\n" +
                        "  \"universe\": [0, 30000]\n" +
                        "}"
        );
        JScrollPane exampleScrollPane = new JScrollPane(exampleTextArea);

        // Use JSplitPane to split left and right panels nicely
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, exampleScrollPane);
        splitPane.setDividerLocation(800);  // Adjust width of left panel

        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
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

        // Measure Weights Panel
        JPanel weightsPanel = new JPanel();
        weightsPanel.setBorder(BorderFactory.createTitledBorder("Measure Weights"));
        weightsPanel.setLayout(new GridLayout(5, 4, 2, 2)); // 5 rows, 4 columns (2 labels + 2 fields per row)

        // Initialize weight fields array
        weightFields = new JTextField[10];
        String[] weightLabels = {
                "Weight 1:", "Weight 2:", "Weight 3:", "Weight 4:", "Weight 5:",
                "Weight 6:", "Weight 7:", "Weight 8:", "Weight 9:", "Weight 10:"
        };

        // Create weight input fields with default values
        for (int i = 0; i < 10; i++) {
            JLabel label = new JLabel(weightLabels[i]);
            weightFields[i] = new JTextField(8);
            weightFields[i].setText(String.valueOf(measureWeights.get(i)));

            weightsPanel.add(label);
            weightsPanel.add(weightFields[i]);
        }

        predicatePanel.add(weightsPanel);
        add(predicatePanel, BorderLayout.NORTH);
    }

    // Method to update the measureWeights list from the text fields
    private void updateMeasureWeights() {
        for (int i = 0; i < 10; i++) {
            try {
                double value = Double.parseDouble(weightFields[i].getText());
                measureWeights.set(i, value);
            } catch (NumberFormatException e) {
            }
        }
    }

    // Method to get current weights (useful for other parts of your application)
    public List<Double> getMeasureWeights() {
        updateMeasureWeights();
        return new ArrayList<>(measureWeights);
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


        JButton advancedSettingsBtn = new JButton("Advanced Settings");
        advancedSettingsBtn.addActionListener(e -> openAdvancedSettingsDialog());
        controlPanel.add(advancedSettingsBtn);


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
        summaryColumn.setPreferredWidth(750);

        // Right-align numeric columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 1; i < columns.length; i++) {
            resultsTable.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
            resultsTable.getColumnModel().getColumn(i).setPreferredWidth(60);
        }

        JScrollPane tableScrollPane = new JScrollPane(resultsTable);
        tableScrollPane.setPreferredSize(new Dimension(1200, 500));

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
        List<String> selectedPredicates = new ArrayList<>();
        selectedPredicates.add(predicate1);
        assert predicate2 != null;
        if (!predicate2.equals(NO_PREDICATE)) {
            selectedPredicates.add(predicate2);
        }
        if (predicate1.equals(predicate2)) {
            statusLabel.setText("Both predicates cannot be the same");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // Get selected summarizers for combination
        List<Integer> selectedSummarizerIndices = Arrays.stream(summarizerSelectionList.getSelectedIndices())
                .boxed().collect(Collectors.toList());

        if (selectedSummarizerIndices.isEmpty()) {
            statusLabel.setText("Please select at least one summarizer for combination");
            statusLabel.setForeground(Color.RED);
            return;
        }

        int totalCombinations = 0;
        int filteredCombinations = 0;

        // Generate all first-order summaries
        for (int summarizerIndex : selectedSummarizerIndices) {
            Summarizer summarizer = summarizers.get(summarizerIndex);
            for (Quantifier quantifier : quantifiers) {
                for (String predicate : selectedPredicates) {
                    LinguisticSummary summary = new LinguisticSummary(
                            quantifier,
                            "utworów",
                            summarizer
                    );
                    summary.setMeasureWeights(getMeasureWeights());

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
        }

        // Generate all second-order summaries
        for (int i = 0; i < selectedSummarizerIndices.size(); i++) {
            for (int j = 0; j < selectedSummarizerIndices.size(); j++) {
                if (i == j) continue; // Skip same summarizer combination
                Summarizer summarizer1 = summarizers.get(selectedSummarizerIndices.get(i));
                Summarizer summarizer2 = summarizers.get(selectedSummarizerIndices.get(j));

                for (Quantifier quantifier : quantifiers) {
                    if (!quantifier.isRelative()) {
                        continue;
                    }
                    LinguisticSummary summary = new SecondOrderLinguisticSummary(
                            quantifier,
                            "utworów",
                            summarizer1,
                            summarizer2
                    );
                    summary.setMeasureWeights(getMeasureWeights());

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
        }

        // Compound Summarizer
        // Generate all second-order summaries
        for (int i = 0; i < selectedSummarizerIndices.size(); i++) {
            for (int j = 0; j < selectedSummarizerIndices.size(); j++) {
                if (i == j) continue; // Skip same summarizer combination
                Summarizer summarizer1 = summarizers.get(selectedSummarizerIndices.get(i));
                Summarizer summarizer2 = summarizers.get(selectedSummarizerIndices.get(j));

                for (Quantifier quantifier : quantifiers) {
                    if (!quantifier.isRelative()) {
                        continue;
                    }
                    List<Summarizer> summarizers = new ArrayList<>();
                    summarizers.add(summarizer1);
                    summarizers.add(summarizer2);
                    CompoundSummarizer compoundSummarizer = new CompoundSummarizer(
                            summarizers
                    );
                    LinguisticSummaryCompound summary = new LinguisticSummaryCompound(
                            quantifier,
                            predicate1,
                            compoundSummarizer
                    );

                    // Calculate all T values
                    double[] tValues = calculateAllTValues(summary);
                    double t1 = tValues[0];

                    totalCombinations++;

                    // Filter out zero/low values
                    if (t1 > 0.001) {
                        String summaryText = summary.generateSummary();
                        SummaryResult result = new SummaryResult(summaryText, tValues);
                        allResults.add(result);
                        addResultToTable(result);
                        filteredCombinations++;
                    }
                }
            }
        }

        if (predicate2.equals(NO_PREDICATE)) {
            statusLabel.setText(String.format("Generated %d first-order combinations, %d passed filter",
                    totalCombinations, filteredCombinations));
            statusLabel.setForeground(Color.GREEN);
            return;
        }

        // MSS1
        for (int i = 0; i < selectedSummarizerIndices.size(); i++) {
            Summarizer summarizer1 = summarizers.get(selectedSummarizerIndices.get(i));

            for (Quantifier quantifier : quantifiers) {
                if (!quantifier.isRelative()) {
                    continue;
                }
                MSS1 summary = new MSS1(
                        predicate1,
                        predicate2,
                        quantifier,
                        summarizer1
                );

                int[] counts = addMSSResults(summary);
                totalCombinations += counts[0];
                filteredCombinations += counts[1];

                // MSS1 Reversed predicates
                MSS1 summaryReversed = new MSS1(
                        predicate2,
                        predicate1,
                        quantifier,
                        summarizer1
                );

                int[] countsReversed = addMSSResults(summaryReversed);
                totalCombinations += countsReversed[0];
                filteredCombinations += countsReversed[1];
            }
        }

        // MSS2, MSS3
        for (int i = 0; i < selectedSummarizerIndices.size(); i++) {
            for (int j = 0; j < selectedSummarizerIndices.size(); j++) {
                if (i == j) continue; // Skip same summarizer combination
                Summarizer summarizer1 = summarizers.get(selectedSummarizerIndices.get(i));
                Summarizer summarizer2 = summarizers.get(selectedSummarizerIndices.get(j));

                for (Quantifier quantifier : quantifiers) {
                    if (!quantifier.isRelative()) {
                        continue;
                    }
                    MSS2 summary = new MSS2(
                            predicate1,
                            predicate2,
                            quantifier,
                            summarizer1,
                            summarizer2
                    );

                    int[] counts = addMSSResults(summary);
                    totalCombinations += counts[0];
                    filteredCombinations += counts[1];

                    // MSS2 Reversed predicates
                    MSS2 summaryReversed = new MSS2(
                            predicate2,
                            predicate1,
                            quantifier,
                            summarizer1,
                            summarizer2
                    );

                    int[] countsReversed = addMSSResults(summaryReversed);
                    totalCombinations += countsReversed[0];
                    filteredCombinations += countsReversed[1];

                    // MSS3
                    MSS2 summary3 = new MSS3(
                            predicate1,
                            predicate2,
                            quantifier,
                            summarizer1,
                            summarizer2
                    );

                    int[] counts3 = addMSSResults(summary3);
                    totalCombinations += counts3[0];
                    filteredCombinations += counts3[1];

                    // MSS3 Reversed predicates
                    MSS2 summary3Reversed = new MSS3(
                            predicate2,
                            predicate1,
                            quantifier,
                            summarizer1,
                            summarizer2
                    );

                    int[] counts3Reversed = addMSSResults(summary3Reversed);
                    totalCombinations += counts3Reversed[0];
                    filteredCombinations += counts3Reversed[1];
                }
            }
        }
        // MSS4
        for (Integer selectedSummarizerIndex : selectedSummarizerIndices) {
            Summarizer summarizer = summarizers.get(selectedSummarizerIndex);

            MSS4 summary = new MSS4(
                    predicate1,
                    predicate2,
                    summarizer
            );

            int[] counts = addMSSResults(summary);
            totalCombinations += counts[0];
            filteredCombinations += counts[1];

            // MSS4 Reversed predicates
            MSS4 summaryReversed = new MSS4(
                    predicate2,
                    predicate1,
                    summarizer
            );

            int[] countsReversed = addMSSResults(summaryReversed);
            totalCombinations += countsReversed[0];
            filteredCombinations += countsReversed[1];
        }


        statusLabel.setText(String.format("Generated %d combinations, %d passed filter",
                totalCombinations, filteredCombinations));
        statusLabel.setForeground(Color.GREEN);
    }

    private int[] addMSSResults(MSS1 summary) {
        int totalCombinations = 0;
        int filteredCombinations = 0;
        // Calculate all T values
        double[] tValues = new double[12];
        tValues[0] = summary.calculateT1(dataset);
        for (int j = 1; j < 12; j++) {
            tValues[j] = 0.0;
        }
        totalCombinations++;
        // Filter out zero/low values (you can adjust this threshold)
        if (tValues[0] > 0.001) { // Using small threshold instead of exactly 0
            String summaryText = summary.generateSummary();
            SummaryResult result = new SummaryResult(summaryText, tValues);
            allResults.add(result);
            addResultToTable(result);
            filteredCombinations++;
        }
        return new int[]{totalCombinations, filteredCombinations};
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
        resultsTable.setBackground(new Color(160, 160, 160));
        resultsTable.setForeground(new Color(255, 255, 255));

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

        String getSummary() {
            return summary;
        }

        double[] getTValues() {
            return tValues;
        }
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