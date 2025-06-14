package org.gui;

import org.dataImport.ConfigImporter;
import org.dataImport.CsvSongImporter;
import org.fuzzy.SongRecord;
import org.fuzzy.quantifiers.Quantifier;
import org.fuzzy.summarizer.Summarizer;
import org.fuzzy.summaries.LinguisticSummary;
import org.fuzzy.summaries.SecondOrderLinguisticSummary;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LineSummaryGUI extends JFrame {
    // Common data
    private List<Quantifier> quantifiers;
    private List<Summarizer> summarizers;
    private List<SongRecord> dataset;

    // First tab components
    private JComboBox<String> quantifierCombo1;
    private JComboBox<String> summarizerCombo1;
    private JTextArea resultArea1;
    private DefaultTableModel tableModel1;
    private JTable summaryTable1;
    private List<LinguisticSummaryResult> generatedSummaries1 = new ArrayList<>();

    // Second tab components
    private JComboBox<String> quantifierCombo2;
    private JComboBox<String> summarizerCombo2;
    private JComboBox<String> qualifierCombo2;
    private JTextArea resultArea2;
    private DefaultTableModel tableModel2;
    private JTable summaryTable2;
    private List<LinguisticSummaryResult> generatedSummaries2 = new ArrayList<>();

    public LineSummaryGUI() {
        setTitle("Linguistic Summary Generator");
        setSize(2000, 1000);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Load configs and data
        quantifiers = ConfigImporter.loadQuantifiersFromConfig();
        summarizers = ConfigImporter.loadSummarizersFromConfig();
        dataset = CsvSongImporter.importSongs(30000);

        // Tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // --- First tab: First-order linguistic summary ---
        JPanel firstTabPanel = new JPanel(new BorderLayout());

        JPanel topPanel1 = new JPanel();
        quantifierCombo1 = new JComboBox<>();
        summarizerCombo1 = new JComboBox<>();

        for (Quantifier q : quantifiers) quantifierCombo1.addItem(q.getName());
        for (Summarizer s : summarizers) summarizerCombo1.addItem(s.getName());

        JButton loadBtn1 = new JButton("Load Summaries");
        JButton generateBtn1 = new JButton("Generate");
        JButton clearBtn1 = new JButton("Clear Summaries");
        JButton saveBtn1 = new JButton("Save Summaries");

        topPanel1.add(quantifierCombo1);
        topPanel1.add(new JLabel("songs are"));
        topPanel1.add(summarizerCombo1);
        topPanel1.add(generateBtn1);
        topPanel1.add(clearBtn1);
        topPanel1.add(saveBtn1);
        topPanel1.add(loadBtn1);

        firstTabPanel.add(topPanel1, BorderLayout.NORTH);

        resultArea1 = new JTextArea(6, 80);
        resultArea1.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea1.setEditable(false);
        firstTabPanel.add(new JScrollPane(resultArea1), BorderLayout.CENTER);

        String[] columns = {
                "Summary", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "Optimal"
        };
        tableModel1 = new DefaultTableModel(columns, 0);
        summaryTable1 = new JTable(tableModel1);
        summaryTable1.setAutoCreateRowSorter(true);

        // Make Summary column wider
        TableColumn summaryCol1 = summaryTable1.getColumnModel().getColumn(0);
        summaryCol1.setPreferredWidth(600);

        // Left-align all columns except Summary, which is wrapped or left aligned too
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        for (int i = 1; i < columns.length; i++) {
            summaryTable1.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
        }

        JScrollPane tableScroll1 = new JScrollPane(summaryTable1);
        tableScroll1.setPreferredSize(new Dimension(1050, 200));
        firstTabPanel.add(tableScroll1, BorderLayout.SOUTH);

        tabbedPane.addTab("First Order Summary Generator", firstTabPanel);

        // --- Second tab: Second-order linguistic summary ---
        JPanel secondTabPanel = new JPanel(new BorderLayout());

        JPanel topPanel2 = new JPanel();
        quantifierCombo2 = new JComboBox<>();
        summarizerCombo2 = new JComboBox<>();
        qualifierCombo2 = new JComboBox<>();

        for (Quantifier q : quantifiers) quantifierCombo2.addItem(q.getName());
        for (Summarizer s : summarizers) {
            summarizerCombo2.addItem(s.getName());
            qualifierCombo2.addItem(s.getName());
        }

        JButton loadBtn2 = new JButton("Load Summaries");
        JButton generateBtn2 = new JButton("Generate");
        JButton clearBtn2 = new JButton("Clear Summaries");
        JButton saveBtn2 = new JButton("Save Summaries");

        topPanel2.add(quantifierCombo2);
        topPanel2.add(new JLabel("songs that are"));
        topPanel2.add(qualifierCombo2);
        topPanel2.add(new JLabel("are"));
        topPanel2.add(summarizerCombo2);
        topPanel2.add(generateBtn2);
        topPanel2.add(clearBtn2);
        topPanel2.add(saveBtn2);
        topPanel2.add(loadBtn2);

        secondTabPanel.add(topPanel2, BorderLayout.NORTH);

        resultArea2 = new JTextArea(6, 80);
        resultArea2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea2.setEditable(false);
        secondTabPanel.add(new JScrollPane(resultArea2), BorderLayout.CENTER);

        String[] columns2 = {"Summary", "T1", "T3", "T9", "T10", "T11", "Optimal"};
        tableModel2 = new DefaultTableModel(columns2, 0);
        summaryTable2 = new JTable(tableModel2);
        summaryTable2.setAutoCreateRowSorter(true);

        // Make Summary column wider for second tab too
        TableColumn summaryCol2 = summaryTable2.getColumnModel().getColumn(0);
        summaryCol2.setPreferredWidth(600);

        // Left-align metrics columns for second tab
        for (int i = 1; i < columns2.length; i++) {
            summaryTable2.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
        }

        JScrollPane tableScroll2 = new JScrollPane(summaryTable2);
        tableScroll2.setPreferredSize(new Dimension(1050, 200));
        secondTabPanel.add(tableScroll2, BorderLayout.SOUTH);

        tabbedPane.addTab("Second Order Summary Generator", secondTabPanel);

        // Add tabbed pane to frame
        add(tabbedPane, BorderLayout.CENTER);



// Background colors
        Color pinkPastel = new Color(177, 59, 255);
        Color lavender = new Color(230, 230, 250);
        Color babyBlue = new Color(173, 216, 230);

// Set main background color
        getContentPane().setBackground(pinkPastel);

// Set tabbed pane background and font
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setBackgroundAt(i, lavender);
        }
        tabbedPane.setFont(new Font("Comic Sans MS", Font.BOLD, 16));

// Set font and background for combo boxes
        Font coolFont = new Font("Comic Sans MS", Font.ITALIC, 14);
        quantifierCombo1.setFont(coolFont);
        summarizerCombo1.setFont(coolFont);
        quantifierCombo2.setFont(coolFont);
        summarizerCombo2.setFont(coolFont);
        qualifierCombo2.setFont(coolFont);

        quantifierCombo1.setBackground(Color.WHITE);
        summarizerCombo1.setBackground(Color.WHITE);
        quantifierCombo2.setBackground(Color.WHITE);
        summarizerCombo2.setBackground(Color.WHITE);
        qualifierCombo2.setBackground(Color.WHITE);

// Style buttons with pastel colors and rounded corners
        for (Component c : topPanel1.getComponents()) {
            if (c instanceof JButton btn) {
                btn.setBackground(new Color(59, 103, 255)); // hot pink
                btn.setForeground(Color.WHITE);
                btn.setFont(coolFont);
                btn.setFocusPainted(false);
            }
        }
        for (Component c : topPanel2.getComponents()) {
            if (c instanceof JButton btn) {
                btn.setBackground(new Color(177, 59, 255));
                btn.setForeground(Color.WHITE);
                btn.setFont(coolFont);
                btn.setFocusPainted(false);
            }
        }

// TextAreas styling
        resultArea1.setBackground(babyBlue);
        resultArea1.setForeground(new Color(138, 43, 226));
        resultArea1.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));

        resultArea2.setBackground(lavender);
        resultArea2.setForeground(new Color(138, 43, 226));
        resultArea2.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));

// Table headers and rows styling
        summaryTable1.getTableHeader().setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        summaryTable1.setFont(new Font("Comic Sans MS", Font.PLAIN, 13));
        summaryTable1.setBackground(new Color(199, 237, 255)); // misty rose
        summaryTable1.setForeground(new Color(199, 21, 133)); // medium violet red

        summaryTable2.getTableHeader().setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        summaryTable2.setFont(new Font("Comic Sans MS", Font.PLAIN, 13));
        summaryTable2.setBackground(new Color(236, 203, 252));
        summaryTable2.setForeground(new Color(199, 21, 133));


        // Event handlers for first tab
        generateBtn1.addActionListener(this::generateFirstOrderSummary);
        clearBtn1.addActionListener(e -> clearSummaries(1));
        saveBtn1.addActionListener(e -> saveSummaries(1));
        loadBtn1.addActionListener(e -> loadSummaries(1));

        // Event handlers for second tab
        generateBtn2.addActionListener(this::generateSecondOrderSummary);
        clearBtn2.addActionListener(e -> clearSummaries(2));
        saveBtn2.addActionListener(e -> saveSummaries(2));
        loadBtn2.addActionListener(e -> loadSummaries(2));
    }

    // ========== First order summary generation ==========
    private void generateFirstOrderSummary(ActionEvent e) {
        int quantIndex = quantifierCombo1.getSelectedIndex();
        int sumIndex = summarizerCombo1.getSelectedIndex();

        if (quantIndex == -1 || sumIndex == -1) {
            resultArea1.setText("Select both a quantifier and summarizer.");
            return;
        }

        Quantifier quantifier = quantifiers.get(quantIndex);
        Summarizer summarizer = summarizers.get(sumIndex);

        LinguisticSummary summary = new LinguisticSummary(quantifier, "songs", summarizer);
        double[] t = new double[12];
        t[0] = summary.calculateT1(dataset);
        t[1] = summary.calculateT2(dataset);
        t[2] = summary.calculateT3(dataset);
        t[3] = summary.calculateT4(dataset);
        t[4] = summary.calculateT5(dataset);
        t[5] = summary.calculateT6(dataset);
        t[6] = summary.calculateT7(dataset);
        t[7] = summary.calculateT8(dataset);
        t[8] = summary.calculateT9(dataset);
        t[9] = summary.calculateT10(dataset);
        t[10] = summary.calculateT11(dataset);
        t[11] = summary.calculateOptimal(dataset);

        String summaryText = summary.generateSummary();
        resultArea1.setText("→ " + summaryText + "\n\n");
        for (int i = 0; i < 11; i++) {
            resultArea1.append(String.format("T%d = %.3f%n", i + 1, t[i]));
        }
        resultArea1.append(String.format("Optimal = %.3f%n", t[11]));

        generatedSummaries1.add(new LinguisticSummaryResult(summaryText, t));

        Object[] row = new Object[13];
        row[0] = summaryText;
        for (int i = 0; i < t.length; i++) {
            row[i + 1] = String.format("%.3f", t[i]);
        }
        tableModel1.addRow(row);
    }

    // ========== Second order summary generation ==========
    private void generateSecondOrderSummary(ActionEvent e) {
        int quantIndex = quantifierCombo2.getSelectedIndex();
        int sumIndex = summarizerCombo2.getSelectedIndex();
        int qualIndex = qualifierCombo2.getSelectedIndex();

        if (quantIndex == -1 || sumIndex == -1 || qualIndex == -1) {
            resultArea2.setText("Select a quantifier, summarizer, and qualifier.");
            return;
        }

        Quantifier quantifier = quantifiers.get(quantIndex);
        Summarizer summarizer = summarizers.get(sumIndex);
        Summarizer qualifier = summarizers.get(qualIndex);

        SecondOrderLinguisticSummary summary = new SecondOrderLinguisticSummary(
                quantifier, "songs", summarizer, qualifier);

        double[] t = new double[6]; // Only these Ts
        t[0] = summary.calculateT1(dataset);
        t[1] = summary.calculateT3(dataset);
        t[2] = summary.calculateT9(dataset);
        t[3] = summary.calculateT10(dataset);
        t[4] = summary.calculateT11(dataset);
        t[5] = summary.calculateOptimal(dataset);

        String summaryText = summary.generateSummary();
        resultArea2.setText("→ " + summaryText + "\n\n");
        resultArea2.append(String.format("T1 = %.3f%n", t[0]));
        resultArea2.append(String.format("T3 = %.3f%n", t[1]));
        resultArea2.append(String.format("T9 = %.3f%n", t[2]));
        resultArea2.append(String.format("T10 = %.3f%n", t[3]));
        resultArea2.append(String.format("T11 = %.3f%n", t[4]));
        resultArea2.append(String.format("Optimal = %.3f%n", t[5]));

        generatedSummaries2.add(new LinguisticSummaryResult(summaryText, t));

        Object[] row = new Object[7];
        row[0] = summaryText;
        for (int i = 0; i < t.length; i++) {
            row[i + 1] = String.format("%.3f", t[i]);
        }
        tableModel2.addRow(row);
    }

    private void clearSummaries(int tab) {
        if (tab == 1) {
            generatedSummaries1.clear();
            tableModel1.setRowCount(0);
            resultArea1.setText("");
        } else {
            generatedSummaries2.clear();
            tableModel2.setRowCount(0);
            resultArea2.setText("");
        }
    }

    private void saveSummaries(int tab) {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            List<LinguisticSummaryResult> summaries = (tab == 1) ? generatedSummaries1 : generatedSummaries2;

            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                for (LinguisticSummaryResult summary : summaries) {
                    pw.println(summary.getSummary());
                    double[] t = summary.getTValues();
                    for (double val : t) {
                        pw.print(val + " ");
                    }
                    pw.println();
                }
                JOptionPane.showMessageDialog(this, "Saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
            }
        }
    }

    private void loadSummaries(int tab) {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                List<LinguisticSummaryResult> summaries = (tab == 1) ? generatedSummaries1 : generatedSummaries2;
                DefaultTableModel targetModel = (tab == 1) ? tableModel1 : tableModel2;
                targetModel.setRowCount(0);
                summaries.clear();

                String line;
                while ((line = br.readLine()) != null) {
                    String summaryText = line;
                    String valuesLine = br.readLine();
                    if (valuesLine == null) break;

                    String[] parts = valuesLine.trim().split("\\s+");
                    double[] metrics = new double[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        metrics[i] = Double.parseDouble(parts[i]);
                    }
                    summaries.add(new LinguisticSummaryResult(summaryText, metrics));

                    int columns = (tab == 1) ? 13 : 7;
                    Object[] row = new Object[columns];
                    row[0] = summaryText;
                    // Fill only columns available in the table, preventing index errors
                    for (int i = 0; i < metrics.length && i + 1 < columns; i++) {
                        row[i + 1] = String.format("%.3f", metrics[i]);
                    }
                    targetModel.addRow(row);
                }
                JOptionPane.showMessageDialog(this, "Loaded successfully!");
            } catch (IOException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + ex.getMessage());
            }
        }
    }

    // Helper class to store summary and its metric values
    private static class LinguisticSummaryResult {
        private final String summary;
        private final double[] tValues;

        public LinguisticSummaryResult(String summary, double[] tValues) {
            this.summary = summary;
            this.tValues = tValues;
        }

        public String getSummary() {
            return summary;
        }

        public double[] getTValues() {
            return tValues;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LineSummaryGUI gui = new LineSummaryGUI();
            gui.setVisible(true);
        });
    }
}
