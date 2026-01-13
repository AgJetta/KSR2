package org.gui;

import org.dataImport.ConfigImporter;
import org.dataImport.CsvSongImporter;
import org.fuzzy.*;
import org.fuzzy.membershipFunctions.MembershipFunction;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summaries.*;
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
import java.util.Map;
import java.util.stream.Collectors;

public class RefactoredSummaryGUI extends JFrame {
    // Data
    // UI Components - Summarizer Panel
    private JTable summarizerTable;
    private DefaultTableModel summarizerTableModel;
    private List<Quantifier> quantifiers;
    private List<Summarizer> summarizers;
    private List<SongRecord> dataset;

    // UI Components - Predicate Panel
    private JComboBox<String> predicateCombo1;
    private JComboBox<String> predicateCombo2;

    // UI Components - Summarizer Selection
    private JTable summarizerSelectionTable;
    private DefaultTableModel summarizerSelectionModel;

    // UI Components - Results
    private DefaultTableModel tableModel;
    private JTable resultsTable;
    private JLabel statusLabel;

    private final static String[] predicates = {
            "rock", "rap", "edm", "latin", "pop"
    };

    private List<Double> measureWeights = Arrays.asList(0.2, 0.05, 0.05, 0.2, 0.05, 0.05, 0.1, 0.1, 0.1, 0.1);

    private JTextField[] weightFields;

    private final static String NO_PREDICATE = "";

    private JCheckBox f1Checkbox;
    private JCheckBox f2Checkbox;
    private JCheckBox mss1Checkbox;
    private JCheckBox mss2Checkbox;
    private JCheckBox mss3Checkbox;
    private JCheckBox mss4Checkbox;
    private JCheckBox twoSummarizersCheckbox;
    private JSpinner maxCompoundSpinner;
    private JCheckBox andConnectiveCheckbox;
    private JCheckBox orConnectiveCheckbox;

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
        model.setRowCount(0);

        for (Quantifier q : quantifiers) {
            Universe u = q.getFuzzySet().getUniverse();

            model.addRow(new Object[]{
                    q.getName(),
                    q.isRelative() ? "Relative" : "Absolute",
                    "[" + u.getStart() + ", " + u.getEnd() + "]",
                    q.getFunctionType(),
                    Arrays.toString(q.getParameters())
            });
        }
    }


    private JPanel createSummarizerPanel(JDialog parentDialog) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = new JPanel(new BorderLayout());

        List<String> fields = summarizers.stream()
                .map(s -> s.getFieldName(0))   // get field name
                .distinct()                     // remove duplicates
                .toList();                      // Java 16+; otherwise use collect(Collectors.toList())

        JComboBox<String> fieldBox = new JComboBox<>(fields.toArray(new String[0]));

        JTextField universeField = new JTextField(15);
        universeField.setEditable(false);

        Map<String, Universe> fieldUniverses = summarizers.stream()
                .collect(Collectors.toMap(
                        s -> s.getFieldName(0),
                        s -> s.getFuzzySet(0).getUniverse(),
                        (u1, u2) -> u1  // in case of duplicates, just pick the first
                ));

        fieldBox.addActionListener(e -> {
            String selectedField = (String) fieldBox.getSelectedItem();
            if (selectedField != null) {
                Universe u = fieldUniverses.get(selectedField);
                if (u != null) {
                    universeField.setText(u.getStart() + ", " + u.getEnd());
                }
                updateSummarizerTableForField(selectedField);
            }
        });

        fieldBox.setSelectedIndex(0);

        JTextField nameField = new JTextField(15);
        JComboBox<String> functionTypeBox = new JComboBox<>(new String[]{"triangular", "trapezoidal", "gaussian", "rampUp", "rampDown", "crisp"}
        );
        JTextField paramField = new JTextField(15);

        JPanel formPanel = new JPanel(new GridLayout(5, 2));
        formPanel.add(new JLabel("Field:"));
        formPanel.add(fieldBox);
        formPanel.add(new JLabel("Universe:"));
        formPanel.add(universeField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Function Type:"));
        formPanel.add(functionTypeBox);
        formPanel.add(new JLabel("Parameters (comma-separated):"));
        formPanel.add(paramField);

        summarizerTableModel = new DefaultTableModel(
                new Object[]{"Name", "Field", "Function", "Universe", "Parameters"}, 0
        );
        summarizerTable = new JTable(summarizerTableModel);
        JScrollPane scrollPane = new JScrollPane(summarizerTable);

        JButton addButton = new JButton("Add Summarizer");
        addButton.addActionListener(e -> {
            try {
                String field = (String) fieldBox.getSelectedItem();
                String name = nameField.getText().trim();
                String funcType = (String) functionTypeBox.getSelectedItem();

                if (name.isEmpty()) throw new IllegalArgumentException("Name required");

                double[] params = Arrays.stream(paramField.getText().split(","))
                        .map(String::trim)
                        .mapToDouble(Double::parseDouble)
                        .toArray();

                MembershipFunction mf;
                switch (funcType) {
                    case "triangular" -> {
                        if (params.length != 3) throw new IllegalArgumentException("Triangular requires 3 parameters");
                        mf = MembershipFunctions.triangular(params[0], params[1], params[2]);
                    }
                    case "trapezoidal" -> {
                        if (params.length != 4) throw new IllegalArgumentException("Trapezoidal requires 4 parameters");
                        mf = MembershipFunctions.trapezoidal(params[0], params[1], params[2], params[3]);
                    }
                    case "gaussian" -> {
                        if (params.length != 2) throw new IllegalArgumentException("Gaussian requires 2 parameters");
                        mf = MembershipFunctions.gaussian(params[0], params[1]);
                    }
                    case "rampUp" -> {
                        if (params.length != 2) throw new IllegalArgumentException("RampUp requires 2 parameters");
                        mf = MembershipFunctions.rampUp(params[0], params[1]);
                    }
                    case "rampDown" -> {
                        if (params.length != 2) throw new IllegalArgumentException("RampDown requires 2 parameters");
                        mf = MembershipFunctions.rampDown(params[0], params[1]);
                    }
                    case "crisp" -> {
                        if (params.length != 2) throw new IllegalArgumentException("Crisp requires 2 parameters");
                        mf = MembershipFunctions.crisp(params[0], params[1]);
                    }
                    default -> throw new IllegalArgumentException("Unknown function type: " + funcType);
                }


                Universe universe = fieldUniverses.get(field);
                FuzzySet fs = new FuzzySet(universe, mf);

                Summarizer s = new Summarizer(name, field, fs, funcType, params, universe);
                summarizers.add(s);
                updateSummarizerTableForField(field);
                updateSummarizerSelectionTable();
                nameField.setText("");
                paramField.setText("");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        leftPanel,
                        "Invalid parameters or missing data",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        leftPanel.add(addButton, BorderLayout.SOUTH);

        JTextArea exampleTextArea = new JTextArea();
        exampleTextArea.setEditable(false);
        exampleTextArea.setText(
                "" +
                        "EXAMPLE CONFIGURATIONS:\n\n" +

                        "{\n" +
                        "  \"name\": \"LOW ENERGY\",\n" +
                        "  \"field\": \"energy\",\n" +
                        "  \"functionType\": \"triangular\",\n" +
                        "  \"parameters\": [0.0, 0.2, 0.4],\n" +
//  "  \"universe\": [0, 1]\n" +
                        "},\n\n" +

                        "{\n" +
                        "  \"name\": \"HIGH ENERGY\",\n" +
                        "  \"field\": \"energy\",\n" +
                        "  \"functionType\": \"triangular\",\n" +
                        "  \"parameters\": [0.6, 0.8, 1.0],\n" +
//  "  \"universe\": [0, 1]\n" +
                        "},\n\n" +

                        "{\n" +
                        "  \"name\": \"SHORT DURATION\",\n" +
                        "  \"field\": \"duration_ms\",\n" +
                        "  \"functionType\": \"trapezoidal\",\n" +
                        "  \"parameters\": [0, 0, 120000, 180000],\n" +
//  "  \"universe\": [0, 600000]\n" +
                        "},\n\n" +

                        "MEMBERSHIP FUNCTIONS EXAMPLES:\n\n" +
                        "Function     # of Params     Example Params\n" +
                        "triangular   3               0.0, 0.2, 0.4\n" +
                        "trapezoidal  4               0.0, 0.2, 0.4, 0.6\n" +
                        "gaussian     2               0.5, 0.1\n" +
                        "crisp        2               0.3, 0.7\n" +
                        "rampUp       2               0.0, 0.5\n" +
                        "rampDown     2               0.5, 1.0\n"

        );
        JScrollPane exampleScrollPane = new JScrollPane(exampleTextArea);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, exampleScrollPane);
        splitPane.setDividerLocation(1400);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private void updateSummarizerTableForField(String field) {
        if (summarizerTableModel == null) return;

        summarizerTableModel.setRowCount(0);

        for (Summarizer s : summarizers) {
            if (s.getFieldName(0).equals(field)) {
                summarizerTableModel.addRow(new Object[]{
                        s.getName(),
                        s.getFieldName(0),
                        s.getFunctionType(),
                        "[" + s.getUniverse().getStart() + ", " + s.getUniverse().getEnd() + "]",
                        Arrays.toString(s.getParameters())
                });
            }
        }
    }


    private void openAdvancedSettingsDialog() {
        JDialog dialog = new JDialog(this, "Zaawansowane ustawienia", true);
        dialog.setSize(1800, 1000);
        dialog.setLocationRelativeTo(this);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Quantifiers", createQuantifierPanel(dialog));
        tabbedPane.addTab("Qualifiers/Summarizers", createSummarizerPanel(dialog));


        dialog.add(tabbedPane);
        dialog.setVisible(true);
    }

    private JPanel createQuantifierPanel(JDialog parentDialog) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Left side: your existing panel with form + table + button
        JPanel leftPanel = new JPanel(new BorderLayout());

        JTextField nameField = new JTextField(15);
        JComboBox<String> functionTypeBox = new JComboBox<>(new String[]{"triangular", "trapezoidal", "gaussian", "rampUp", "rampDown", "crisp"});
        JCheckBox relativeBox = new JCheckBox("Relative");
        JTextField paramField = new JTextField(15);
//        JTextField universeField = new JTextField(15);

        JPanel formPanel = new JPanel(new GridLayout(5, 2));
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Function Type:"));
        formPanel.add(functionTypeBox);

        formPanel.add(new JLabel("Parameters (comma-separated):"));
        formPanel.add(paramField);
        formPanel.add(new JLabel("Relative:"));
        formPanel.add(relativeBox);

        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[]{
                        "Name",
                        "Type",
                        "Universe",
                        "Function",
                        "Parameters"
                }, 0
        );

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        updateTableFromQuantifiers(tableModel);

        JButton addButton = new JButton("Dodaj kwantyfikator");
        addButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String funcType = (String) functionTypeBox.getSelectedItem();
                boolean isRelative = relativeBox.isSelected();

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(leftPanel, "Nazwa nie może być pusta");
                    return;
                }

                double[] parameters = Arrays.stream(paramField.getText().split(","))
                        .map(String::trim)
                        .mapToDouble(Double::parseDouble)
                        .toArray();

                if ("triangular".equals(funcType) && parameters.length != 3 ||
                        "trapezoidal".equals(funcType) && parameters.length != 4) {
                    JOptionPane.showMessageDialog(leftPanel, "Nieprawidłowa liczba parametrów dla " + funcType);
                    return;
                }

                MembershipFunction mf;
                switch (funcType) {
                    case "triangular" -> {
                        if (parameters.length != 3) throw new IllegalArgumentException("Triangular requires 3 params");
                        mf = MembershipFunctions.triangular(parameters[0], parameters[1], parameters[2]);
                    }
                    case "trapezoidal" -> {
                        if (parameters.length != 4) throw new IllegalArgumentException("Trapezoidal requires 4 params");
                        mf = MembershipFunctions.trapezoidal(parameters[0], parameters[1], parameters[2], parameters[3]);
                    }
                    case "gaussian" -> {
                        if (parameters.length != 2) throw new IllegalArgumentException("Gaussian requires 2 params");
                        mf = MembershipFunctions.gaussian(parameters[0], parameters[1]);
                    }
                    case "rampUp" -> {
                        if (parameters.length != 2) throw new IllegalArgumentException("RampUp requires 2 params");
                        mf = MembershipFunctions.rampUp(parameters[0], parameters[1]);
                    }
                    case "rampDown" -> {
                        if (parameters.length != 2) throw new IllegalArgumentException("RampDown requires 2 params");
                        mf = MembershipFunctions.rampDown(parameters[0], parameters[1]);
                    }
                    case "crisp" -> {
                        if (parameters.length != 2) throw new IllegalArgumentException("Crisp requires 2 params");
                        mf = MembershipFunctions.crisp(parameters[0], parameters[1]);
                    }
                    default -> throw new IllegalArgumentException("Unknown function type");
                }

                Universe universe = isRelative
                        ? new Universe(0.0, 1.0, true)
                        : new Universe(0.0, 300000, false);

                FuzzySet fuzzySet = new FuzzySet(universe, mf);

                Quantifier quantifier = new Quantifier(name, fuzzySet, isRelative, funcType, parameters);

                quantifiers.add(quantifier);
                updateTableFromQuantifiers(tableModel);

                nameField.setText("");
                paramField.setText("");
                relativeBox.setSelected(false);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(leftPanel,
                        "Nieprawidłowy format parametrów (użyj liczb zmiennoprzecinkowych)");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(leftPanel,
                        "Błąd podczas dodawania: " + ex.getMessage());
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

                        "\n\n{" +
                        "  \"name\": \"JEDNA TRZECIA (1/3)\",\n" +
                        "  \"relative\": true,\n" +
                        "  \"functionType\": \"triangular\",\n" +
                        "  \"parameters\": [0.0, 0.3333333, 0.6666667],\n" +
//                        "  \"universe\": [0, 1]\n" +
                        "},\n" +

                        "{\n" +
                        "  \"name\": \"MNIEJ NIŻ 100\",\n" +
                        "  \"relative\": false,\n" +
                        "  \"functionType\": \"trapezoidal\",\n" +
                        "  \"parameters\": [0, 0, 95, 100],\n" +
//                        "  \"universe\": [0, 30000]\n" +
                        "}\n\n"+


                        "MEMBERSHIP FUNCTIONS EXAMPLES:\n\n" +
                        "Function     # of Params     Example Params\n" +
                        "triangular   3               0.0, 0.2, 0.4\n" +
                        "trapezoidal  4               0.0, 0.2, 0.4, 0.6\n" +
                        "gaussian     2               0.5, 0.1\n" +
                        "crisp        2               0.3, 0.7\n" +
                        "rampUp       2               0.0, 0.5\n" +
                        "rampDown     2               0.5, 1.0\n"


        );
        JScrollPane exampleScrollPane = new JScrollPane(exampleTextArea);

        // Use JSplitPane to split left and right panels nicely
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, exampleScrollPane);
        splitPane.setDividerLocation(1400);  // Adjust width of left panel

        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }


    private void loadData() {
        quantifiers = ConfigImporter.loadQuantifiersFromConfig();
        summarizers = ConfigImporter.loadSummarizersFromConfig();
        dataset = CsvSongImporter.importSongs(30000);

        for (Summarizer summarizer : summarizers) {
            summarizer.getFuzzySet(0).getUniverse().setCardinalNumber(dataset.size());
        }

        for (Quantifier quantifier : quantifiers) {
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

    private void updateMeasureWeights() {
        for (int i = 0; i < 10; i++) {
            try {
                double value = Double.parseDouble(weightFields[i].getText());
                measureWeights.set(i, value);
            } catch (NumberFormatException e) {
            }
        }
    }

    public List<Double> getMeasureWeights() {
        updateMeasureWeights();
        return new ArrayList<>(measureWeights);
    }

    private void createSummarizerSelectionPanel() {
        JPanel summarizerPanel = new JPanel(new BorderLayout());
        summarizerPanel.setBorder(BorderFactory.createTitledBorder("Select Summarizers for Combination"));

        summarizerSelectionModel = new DefaultTableModel(
                new Object[]{"Select", "Negate", "Summarizer"}, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column < 2 ? Boolean.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column < 2;
            }
        };

        summarizerSelectionTable = new JTable(summarizerSelectionModel);
        summarizerSelectionTable.getColumnModel().getColumn(0).setMaxWidth(50);
        summarizerSelectionTable.getColumnModel().getColumn(1).setMaxWidth(60);

        updateSummarizerSelectionTable();

        JScrollPane scrollPane = new JScrollPane(summarizerSelectionTable);
        scrollPane.setPreferredSize(new Dimension(400, 150));

        JPanel selectionButtonPanel = new JPanel(new FlowLayout());
        JButton selectAllBtn = new JButton("Select All");
        JButton clearAllBtn = new JButton("Clear All");

        selectAllBtn.addActionListener(e -> {
            for (int i = 0; i < summarizerSelectionModel.getRowCount(); i++) {
                summarizerSelectionModel.setValueAt(true, i, 0);
            }
        });

        clearAllBtn.addActionListener(e -> {
            for (int i = 0; i < summarizerSelectionModel.getRowCount(); i++) {
                summarizerSelectionModel.setValueAt(false, i, 0);
                summarizerSelectionModel.setValueAt(false, i, 1);
            }
        });

        selectionButtonPanel.add(selectAllBtn);
        selectionButtonPanel.add(clearAllBtn);

        summarizerPanel.add(new JLabel("Select summarizers and optionally negate them:"), BorderLayout.NORTH);
        summarizerPanel.add(scrollPane, BorderLayout.CENTER);
        summarizerPanel.add(selectionButtonPanel, BorderLayout.SOUTH);

        add(summarizerPanel, BorderLayout.WEST);
    }

    private void updateSummarizerSelectionTable() {
        if (summarizerSelectionModel == null) return;

        summarizerSelectionModel.setRowCount(0);
        for (Summarizer s : summarizers) {
            String fieldName = s.getFieldName(0);
            String displayName = fieldName + ": " + s.getName();
            summarizerSelectionModel.addRow(new Object[]{false, false, displayName});
        }
    }


    private void createControlPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkboxPanel.setBorder(BorderFactory.createTitledBorder("Summary Types"));

        f1Checkbox = new JCheckBox("F1", true);
        f2Checkbox = new JCheckBox("F2", true);
        mss1Checkbox = new JCheckBox("MSS1", true);
        mss2Checkbox = new JCheckBox("MSS2", true);
        mss3Checkbox = new JCheckBox("MSS3", true);
        mss4Checkbox = new JCheckBox("MSS4", true);

        twoSummarizersCheckbox = new JCheckBox("Compound Summarizers", false);
        maxCompoundSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));

        andConnectiveCheckbox = new JCheckBox("AND", true);
        orConnectiveCheckbox = new JCheckBox("OR", false);

        checkboxPanel.add(f1Checkbox);
        checkboxPanel.add(f2Checkbox);
        checkboxPanel.add(mss1Checkbox);
        checkboxPanel.add(mss2Checkbox);
        checkboxPanel.add(mss3Checkbox);
        checkboxPanel.add(mss4Checkbox);
        checkboxPanel.add(Box.createHorizontalStrut(20));
        checkboxPanel.add(twoSummarizersCheckbox);
        checkboxPanel.add(new JLabel("Max components:"));
        checkboxPanel.add(maxCompoundSpinner);
        checkboxPanel.add(Box.createHorizontalStrut(10));
        checkboxPanel.add(new JLabel("Connectives:"));
        checkboxPanel.add(andConnectiveCheckbox);
        checkboxPanel.add(orConnectiveCheckbox);

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton generateBtn = new JButton("Generate All Combinations");
        JButton clearBtn = new JButton("Clear Results");
        JButton saveBtn = new JButton("Save Results");
        JButton loadBtn = new JButton("Load Results");

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

        mainPanel.add(checkboxPanel, BorderLayout.NORTH);
        mainPanel.add(controlPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
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

        List<Integer> selectedSummarizerIndices = new ArrayList<>();
        List<Boolean> negationFlags = new ArrayList<>();

        for (int i = 0; i < summarizerSelectionModel.getRowCount(); i++) {
            Boolean selected = (Boolean) summarizerSelectionModel.getValueAt(i, 0);
            if (Boolean.TRUE.equals(selected)) {
                selectedSummarizerIndices.add(i);
                Boolean negate = (Boolean) summarizerSelectionModel.getValueAt(i, 1);
                negationFlags.add(Boolean.TRUE.equals(negate));
            }
        }

        if (selectedSummarizerIndices.isEmpty()) {
            statusLabel.setText("Please select at least one summarizer for combination");
            statusLabel.setForeground(Color.RED);
            return;
        }

        int totalCombinations = 0;
        int filteredCombinations = 0;

        List<Summarizer> summarizersToUse = new ArrayList<>();

        for (int i = 0; i < selectedSummarizerIndices.size(); i++) {
            int idx = selectedSummarizerIndices.get(i);
            boolean negate = negationFlags.get(i);

            Summarizer baseSummarizer = summarizers.get(idx);

            if (negate) {
                FuzzySet complementedSet = baseSummarizer.getFuzzySet(0).complement();
                Summarizer negatedSummarizer = new Summarizer(
                        "NOT " + baseSummarizer.getName(),
                        baseSummarizer.getFieldName(0),
                        complementedSet,
                        baseSummarizer.getFunctionType(),
                        baseSummarizer.getParameters(),
                        baseSummarizer.getUniverse()
                );
                summarizersToUse.add(negatedSummarizer);
            } else {
                summarizersToUse.add(baseSummarizer);
            }
        }

        if (twoSummarizersCheckbox.isSelected()) {
            int maxComponents = (int) maxCompoundSpinner.getValue();
            if (maxComponents >= 2) {
                List<Summarizer> compoundSummarizers = new ArrayList<>();

                boolean useAnd = andConnectiveCheckbox.isSelected();
                boolean useOr = orConnectiveCheckbox.isSelected();

                if (!useAnd && !useOr) {
                    statusLabel.setText("Please select at least one connective (AND or OR)");
                    statusLabel.setForeground(Color.RED);
                    return;
                }

                for (int i = 0; i < summarizersToUse.size(); i++) {
                    for (int j = i + 1; j < summarizersToUse.size(); j++) {
                        Summarizer s1 = summarizersToUse.get(i);
                        Summarizer s2 = summarizersToUse.get(j);

                        if (useAnd) {
                            Summarizer compound = new Summarizer(
                                    s1.getName() + " AND " + s2.getName(),
                                    List.of(s1.getFieldName(0), s2.getFieldName(0)),
                                    List.of(s1.getFuzzySet(0), s2.getFuzzySet(0)),
                                    List.of(LogicalConnective.AND),
                                    List.of(s1.getLinguisticVariable(0), s2.getLinguisticVariable(0))
                            );
                            compoundSummarizers.add(compound);
                        }

                        if (useOr) {
                            Summarizer compound = new Summarizer(
                                    s1.getName() + " OR " + s2.getName(),
                                    List.of(s1.getFieldName(0), s2.getFieldName(0)),
                                    List.of(s1.getFuzzySet(0), s2.getFuzzySet(0)),
                                    List.of(LogicalConnective.OR),
                                    List.of(s1.getLinguisticVariable(0), s2.getLinguisticVariable(0))
                            );
                            compoundSummarizers.add(compound);
                        }
                    }
                }
                summarizersToUse = compoundSummarizers;
            }
        }

        if (f1Checkbox.isSelected()) {
            for (Summarizer summarizer : summarizersToUse) {
                for (Quantifier quantifier : quantifiers) {
                    LinguisticSummary summary = new LinguisticSummary(
                            quantifier,
                            "utworów",
                            summarizer
                    );
                    LinguisticSummary.setMeasureWeights(getMeasureWeights());

                    double[] tValues = calculateAllTValues(summary);
                    double t1 = tValues[0];

                    totalCombinations++;

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

        if (f2Checkbox.isSelected()) {
            for (int i = 0; i < summarizersToUse.size(); i++) {
                for (int j = 0; j < summarizersToUse.size(); j++) {
                    if (i == j) continue;
                    Summarizer summarizer1 = summarizersToUse.get(i);
                    Summarizer summarizer2 = summarizersToUse.get(j);

                    for (Quantifier quantifier : quantifiers) {
                        if (!quantifier.isRelative()) {
                            continue;
                        }
                        LinguisticSummary summary = new LinguisticSummary(
                                quantifier,
                                "utworów",
                                summarizer1,
                                summarizer2
                        );
                        LinguisticSummary.setMeasureWeights(getMeasureWeights());

                        double[] tValues = calculateAllTValues(summary);
                        double t1 = tValues[0];

                        totalCombinations++;

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
        }

        if (predicate2.equals(NO_PREDICATE)) {
            statusLabel.setText(String.format("Generated %d first-order combinations, %d passed filter",
                    totalCombinations, filteredCombinations));
            statusLabel.setForeground(Color.GREEN);
            return;
        }

        if (mss1Checkbox.isSelected() && !predicate2.equals(NO_PREDICATE)) {
            for (Summarizer summarizer1 : summarizersToUse) {
                for (Quantifier quantifier : quantifiers) {
                    if (!quantifier.isRelative()) {
                        continue;
                    }
                    MSS1 summary = new MSS1(
                            "playlist_genre",
                            predicate1,
                            predicate2,
                            SongRecord.genreStringtoDouble(predicate1),
                            SongRecord.genreStringtoDouble(predicate2),
                            quantifier,
                            summarizer1
                    );

                    int[] counts = addMSSResults(summary);
                    totalCombinations += counts[0];
                    filteredCombinations += counts[1];

                    MSS1 summaryReversed = new MSS1(
                            "playlist_genre",
                            predicate2,
                            predicate1,
                            SongRecord.genreStringtoDouble(predicate2),
                            SongRecord.genreStringtoDouble(predicate1),
                            quantifier,
                            summarizer1
                    );

                    int[] countsReversed = addMSSResults(summaryReversed);
                    totalCombinations += countsReversed[0];
                    filteredCombinations += countsReversed[1];
                }
            }
        }

        if ((mss2Checkbox.isSelected() || mss3Checkbox.isSelected()) && !predicate2.equals(NO_PREDICATE)) {
            for (int i = 0; i < summarizersToUse.size(); i++) {
                for (int j = 0; j < summarizersToUse.size(); j++) {
                    if (i == j) continue;
                    Summarizer summarizer1 = summarizersToUse.get(i);
                    Summarizer summarizer2 = summarizersToUse.get(j);

                    for (Quantifier quantifier : quantifiers) {
                        if (!quantifier.isRelative()) {
                            continue;
                        }

                        if (mss2Checkbox.isSelected()) {
                            MSS2 summary = new MSS2(
                                    "playlist_genre",
                                    predicate1,
                                    predicate2,
                                    SongRecord.genreStringtoDouble(predicate1),
                                    SongRecord.genreStringtoDouble(predicate2),
                                    quantifier,
                                    summarizer1,
                                    summarizer2
                            );

                            int[] counts = addMSSResults(summary);
                            totalCombinations += counts[0];
                            filteredCombinations += counts[1];

                            MSS2 summaryReversed = new MSS2(
                                    "playlist_genre",
                                    predicate2,
                                    predicate1,
                                    SongRecord.genreStringtoDouble(predicate2),
                                    SongRecord.genreStringtoDouble(predicate1),
                                    quantifier,
                                    summarizer1,
                                    summarizer2
                            );

                            int[] countsReversed = addMSSResults(summaryReversed);
                            totalCombinations += countsReversed[0];
                            filteredCombinations += countsReversed[1];
                        }

                        if (mss3Checkbox.isSelected()) {
                            MSS3 summary3 = new MSS3(
                                    "playlist_genre",
                                    predicate1,
                                    predicate2,
                                    SongRecord.genreStringtoDouble(predicate1),
                                    SongRecord.genreStringtoDouble(predicate2),
                                    quantifier,
                                    summarizer1,
                                    summarizer2
                            );

                            int[] counts3 = addMSSResults(summary3);
                            totalCombinations += counts3[0];
                            filteredCombinations += counts3[1];

                            MSS3 summary3Reversed = new MSS3(
                                    "playlist_genre",
                                    predicate2,
                                    predicate1,
                                    SongRecord.genreStringtoDouble(predicate2),
                                    SongRecord.genreStringtoDouble(predicate1),
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
            }
        }

        if (mss4Checkbox.isSelected() && !predicate2.equals(NO_PREDICATE)) {
            for (Summarizer summarizer : summarizersToUse) {
                MSS4 summary = new MSS4(
                        "playlist_genre",
                        predicate1,
                        predicate2,
                        SongRecord.genreStringtoDouble(predicate1),
                        SongRecord.genreStringtoDouble(predicate2),
                        summarizer
                );

                int[] counts = addMSSResults(summary);
                totalCombinations += counts[0];
                filteredCombinations += counts[1];

                MSS4 summaryReversed = new MSS4(
                        "playlist_genre",
                        predicate2,
                        predicate1,
                        SongRecord.genreStringtoDouble(predicate2),
                        SongRecord.genreStringtoDouble(predicate1),
                        summarizer
                );

                int[] countsReversed = addMSSResults(summaryReversed);
                totalCombinations += countsReversed[0];
                filteredCombinations += countsReversed[1];
            }
        }


        statusLabel.setText(String.format("Generated %d combinations, %d passed filter",
                totalCombinations, filteredCombinations));
        statusLabel.setForeground(Color.GREEN);
    }

    private int[] addMSSResults(MSS1 summary) {
        int totalCombinations = 0;
        int filteredCombinations = 0;
        double[] tValues = new double[12];
        tValues[0] = summary.calculateT1(dataset);
        for (int j = 1; j < 12; j++) {
            tValues[j] = 0.0;
        }
        totalCombinations++;
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
        boolean isMSS = result.getSummary().startsWith("MSS");

        for (int i = 0; i < tValues.length; i++) {
            if (isMSS && Math.abs(tValues[i]) < 0.00001) {
                row[i + 1] = "";
            } else {
                row[i + 1] = String.format("%.4f", tValues[i]);
            }
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

        // Style selection table
        summarizerSelectionTable.setFont(coolFont);
        summarizerSelectionTable.setBackground(babyBlue);
        summarizerSelectionTable.getTableHeader().setFont(boldFont);

        // Style results table
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