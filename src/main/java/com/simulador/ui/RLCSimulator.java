package com.simulador.ui;

import com.simulador.engine.*;
import com.simulador.model.*;
import com.simulador.utils.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Panel principal del simulador de circuitos RLC
 * Main panel for RLC circuit simulator
 */
public class RLCSimulator extends JPanel implements SimulationObserver {
    private CircuitEngine engine;
    private java.util.List<CircuitComponent> components;
    private SimulationResult lastResult;
    private DecimalFormat df = new DecimalFormat("0.000");
    private LanguageManager languageManager;

    // Componentes de UI
    private JTextField voltageField, frequencyField, valueField;
    private JComboBox<String> componentTypeCombo, methodCombo, presetCombo;
    private JList<String> componentsList;
    private DefaultListModel<String> componentsModel;
    private JTextArea resultsArea;
    private JLabel circuitDiagram;
    private JButton addButton, removeButton, simulateButton, graphButton, clearButton;
    private JProgressBar progressBar;

    public RLCSimulator() {
        this.engine = new CircuitEngine();
        this.components = new ArrayList<>();
        this.languageManager = LanguageManager.getInstance();

        initializeEngine();
        initializeUI();
        setupEventHandlers();
    }

    private void initializeEngine() {
        engine.addObserver(this);
        engine.setStrategy(new AnalyticalStrategy());
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createControlPanel(), BorderLayout.NORTH);
        add(createCircuitPanel(), BorderLayout.CENTER);
        add(createResultsPanel(), BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(languageManager.getTranslation("controls")));

        // ELIMINADO: Panel de idioma (se movió al menú superior)

        // Panel de entrada principal
        JPanel inputPanel = createInputPanel();

        // Selector de método
        JPanel methodPanel = createMethodPanel();

        // Presets de circuito
        JPanel presetPanel = createPresetPanel();

        // Panel de componentes
        JPanel componentPanel = createComponentPanel();

        // Lista de componentes
        JPanel listPanel = createComponentListPanel();

        // ELIMINADO: panel.add(langPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(inputPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(methodPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(presetPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(componentPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(listPanel);

        return panel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel.add(new JLabel(languageManager.getTranslation("voltage")));
        voltageField = new JTextField("10", 8);
        voltageField.setToolTipText(languageManager.getTranslation("voltage_range"));
        panel.add(voltageField);

        panel.add(new JLabel(languageManager.getTranslation("frequency")));
        frequencyField = new JTextField("60", 8);
        frequencyField.setToolTipText(languageManager.getTranslation("frequency_range"));
        panel.add(frequencyField);

        return panel;
    }

    private JPanel createMethodPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(languageManager.getTranslation("method")));

        methodCombo = new JComboBox<>();
        for (SimulationStrategy strategy : CircuitEngine.getAvailableStrategies()) {
            // Usar las claves de traducción para los métodos
            String methodKey = strategy.getName().toLowerCase().replace("-", "");
            methodCombo.addItem(languageManager.getTranslation(methodKey));
        }
        methodCombo.setToolTipText(languageManager.getTranslation("method"));
        panel.add(methodCombo);

        return panel;
    }

    private JPanel createPresetPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(languageManager.getTranslation("preset")));

        String[] presetKeys = {
                "custom", "underdamped", "critical", "overdamped",
                "series_rlc", "high_pass", "low_pass"
        };

        presetCombo = new JComboBox<>();
        for (String key : presetKeys) {
            presetCombo.addItem(languageManager.getTranslation(key));
        }
        presetCombo.setToolTipText(languageManager.getTranslation("preset"));
        panel.add(presetCombo);

        return panel;
    }

    private JPanel createComponentPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel.add(new JLabel(languageManager.getTranslation("component_type")));

        String[] componentTypes = { "resistance", "inductor", "capacitor" };
        componentTypeCombo = new JComboBox<>();
        for (String type : componentTypes) {
            componentTypeCombo.addItem(languageManager.getTranslation(type));
        }
        panel.add(componentTypeCombo);

        panel.add(new JLabel(languageManager.getTranslation("value")));
        valueField = new JTextField("100", 8);
        valueField.setToolTipText(languageManager.getTranslation("component_value_positive"));
        panel.add(valueField);

        addButton = new JButton(languageManager.getTranslation("add_component"));
        addButton.setToolTipText(languageManager.getTranslation("add_component"));
        panel.add(addButton);

        return panel;
    }

    private JPanel createComponentListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        componentsModel = new DefaultListModel<>();
        componentsList = new JList<>(componentsModel);
        componentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        componentsList.setToolTipText(languageManager.getTranslation("component_list"));

        JScrollPane listScroll = new JScrollPane(componentsList);
        listScroll.setPreferredSize(new Dimension(400, 100));

        removeButton = new JButton(languageManager.getTranslation("remove_selected"));
        removeButton.setToolTipText(languageManager.getTranslation("remove_selected"));

        panel.add(new JLabel(languageManager.getTranslation("component_list")), BorderLayout.NORTH);
        panel.add(listScroll, BorderLayout.CENTER);
        panel.add(removeButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCircuitPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(languageManager.getTranslation("circuit_diagram")));
        panel.setPreferredSize(new Dimension(600, 150));

        circuitDiagram = new JLabel("", JLabel.CENTER);
        circuitDiagram.setFont(new Font("Arial", Font.PLAIN, 16));
        circuitDiagram.setVerticalTextPosition(JLabel.CENTER);
        circuitDiagram.setHorizontalTextPosition(JLabel.CENTER);

        updateCircuitDiagram();

        panel.add(circuitDiagram, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(languageManager.getTranslation("results")));

        // Panel de botones
        JPanel buttonPanel = createButtonPanel();

        // Área de resultados
        JPanel resultsPanel = createResultsAreaPanel();

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(resultsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        simulateButton = new JButton(languageManager.getTranslation("simulate"));
        simulateButton.setToolTipText(languageManager.getTranslation("simulate"));

        graphButton = new JButton(languageManager.getTranslation("view_graphs"));
        graphButton.setEnabled(false);
        graphButton.setToolTipText(languageManager.getTranslation("view_graphs"));

        clearButton = new JButton(languageManager.getTranslation("clear_all"));
        clearButton.setToolTipText(languageManager.getTranslation("clear_all"));

        // Barra de progreso
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);

        panel.add(simulateButton);
        panel.add(graphButton);
        panel.add(clearButton);
        panel.add(progressBar);

        return panel;
    }

    private JPanel createResultsAreaPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        resultsArea = new JTextArea(12, 70);
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Texto inicial en el idioma actual
        updateInitialResultsText();

        JScrollPane scroll = new JScrollPane(resultsArea);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void setupEventHandlers() {
        // ELIMINADO: Selector de idioma (ahora está en el menú superior)
        
        // Selector de método
        methodCombo.addActionListener(e -> updateStrategy());

        // Selector de preset
        presetCombo.addActionListener(e -> loadPreset());

        // Botones de componentes
        addButton.addActionListener(e -> addComponent());
        removeButton.addActionListener(e -> removeComponent());

        // Botones principales
        simulateButton.addActionListener(e -> simulateCircuit());
        graphButton.addActionListener(e -> showGraphs());
        clearButton.addActionListener(e -> clearAll());

        // Enter en campos de texto
        valueField.addActionListener(e -> addComponent());
        voltageField.addActionListener(e -> simulateCircuit());
        frequencyField.addActionListener(e -> simulateCircuit());
    }

    // NUEVO: Método público para cambiar idioma desde el menú superior
    public void changeLanguage(String languageCode) {
        languageManager.setLanguage(languageCode);
        updateUITexts();
    }

    private void updateUITexts() {
        // Actualizar textos de bordes con título
        updateTitledBorders();

        // Actualizar todos los componentes de UI
        updateAllUITexts();

        // Actualizar área de resultados inicial
        updateInitialResultsText();

        // Actualizar lista de componentes
        updateComponentList();

        // Actualizar diagrama del circuito
        updateCircuitDiagram();
    }

    private void updateTitledBorders(JPanel panel) {
        Border border = panel.getBorder();
        if (border instanceof TitledBorder) {
            TitledBorder titledBorder = (TitledBorder) border;
            String title = titledBorder.getTitle();

            // Mapear títulos a claves de traducción
            if (title != null) {
                if (title.contains("Controles") || title.contains("Controls")
                        || title.contains("Controles de Entrada")) {
                    titledBorder.setTitle(languageManager.getTranslation("controls"));
                } else if (title.contains("Diagrama") || title.contains("Diagram")) {
                    titledBorder.setTitle(languageManager.getTranslation("circuit_diagram"));
                } else if (title.contains("Resultados") || title.contains("Results")) {
                    titledBorder.setTitle(languageManager.getTranslation("results"));
                }
            }
        }

        // Actualizar recursivamente
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                updateTitledBorders((JPanel) comp);
            }
        }
    }

    private void updateTitledBorders() {
        updateTitledBorders((JPanel) this);
    }

    private void updateAllUITexts() {
        // Actualizar labels
        updateAllComponents(this);
    }

    private void updateAllComponents(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                String text = label.getText();
                if (text != null) {
                    if (text.contains("Voltaje") || text.contains("Voltage") || text.contains("Tensão")) {
                        label.setText(languageManager.getTranslation("voltage"));
                    } else if (text.contains("Frecuencia") || text.contains("Frequency")
                            || text.contains("Frequência")) {
                        label.setText(languageManager.getTranslation("frequency"));
                    } else if (text.contains("Método:") || text.contains("Method:") || text.contains("Método:")) {
                        label.setText(languageManager.getTranslation("method"));
                    } else if (text.contains("Circuito Predefinido") || text.contains("Preset Circuit")
                            || text.contains("Circuito Predefinido")) {
                        label.setText(languageManager.getTranslation("preset"));
                    } else if (text.contains("Tipo:") || text.contains("Type:") || text.contains("Tipo:")) {
                        label.setText(languageManager.getTranslation("component_type"));
                    } else if (text.contains("Valor:") || text.contains("Value:") || text.contains("Valor:")) {
                        label.setText(languageManager.getTranslation("value"));
                    } else if (text.contains("Lista de Componentes") || text.contains("Component List")
                            || text.contains("Lista de Componentes")) {
                        label.setText(languageManager.getTranslation("component_list"));
                    }
                }
            } else if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                String text = button.getText();
                if (text != null) {
                    if (text.contains("Agregar Componente") || text.contains("Add Component")
                            || text.contains("Adicionar Componente")) {
                        button.setText(languageManager.getTranslation("add_component"));
                    } else if (text.contains("Eliminar Seleccionado") || text.contains("Remove Selected")
                            || text.contains("Remover Selecionado")) {
                        button.setText(languageManager.getTranslation("remove_selected"));
                    } else if (text.contains("Simular Circuito") || text.contains("Simulate Circuit")
                            || text.contains("Simular Circuito")) {
                        button.setText(languageManager.getTranslation("simulate"));
                    } else if (text.contains("Ver Gráficos") || text.contains("View Graphs")
                            || text.contains("Ver Gráficos")) {
                        button.setText(languageManager.getTranslation("view_graphs"));
                    } else if (text.contains("Limpiar Todo") || text.contains("Clear All")
                            || text.contains("Limpar Tudo")) {
                        button.setText(languageManager.getTranslation("clear_all"));
                    }
                }
            } else if (comp instanceof Container) {
                updateAllComponents((Container) comp);
            }
        }
    }

    private void updateInitialResultsText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(languageManager.getTranslation("title")).append(" ===\n\n");
        sb.append(languageManager.getTranslation("instructions")).append("\n");
        sb.append(languageManager.getTranslation("instruction1")).append("\n");
        sb.append(languageManager.getTranslation("instruction2")).append("\n");
        sb.append(languageManager.getTranslation("instruction3")).append("\n");
        sb.append(languageManager.getTranslation("instruction4")).append("\n\n");
        sb.append(languageManager.getTranslation("features")).append("\n");
        sb.append(languageManager.getTranslation("feature1")).append("\n");
        sb.append(languageManager.getTranslation("feature2")).append("\n");
        sb.append(languageManager.getTranslation("feature3")).append("\n");
        sb.append(languageManager.getTranslation("feature4")).append("\n");
        sb.append(languageManager.getTranslation("feature5")).append("\n");

        resultsArea.setText(sb.toString());
    }

    private void updateStrategy() {
        int index = methodCombo.getSelectedIndex();
        SimulationStrategy[] strategies = CircuitEngine.getAvailableStrategies();
        if (index >= 0 && index < strategies.length) {
            engine.setStrategy(strategies[index]);
        }
    }

    private void loadPreset() {
        String selected = (String) presetCombo.getSelectedItem();
        if (selected == null || languageManager.getTranslation("custom").equals(selected))
            return;

        String presetType = "";
        if (languageManager.getTranslation("underdamped").equals(selected)) {
            presetType = "underdamped";
        } else if (languageManager.getTranslation("critical").equals(selected)) {
            presetType = "critical";
        } else if (languageManager.getTranslation("overdamped").equals(selected)) {
            presetType = "overdamped";
        } else if (languageManager.getTranslation("series_rlc").equals(selected)) {
            presetType = "series_rlc";
        } else if (languageManager.getTranslation("high_pass").equals(selected)) {
            presetType = "high_pass";
        } else if (languageManager.getTranslation("low_pass").equals(selected)) {
            presetType = "low_pass";
        }

        components.clear();
        components.addAll(CircuitFactory.createPreset(presetType));

        updateComponentList();
        updateCircuitDiagram();

        showInfo(languageManager.getFormattedTranslation("preset_loaded", selected));
    }

    private void addComponent() {
        try {
            String type = "";
            int index = componentTypeCombo.getSelectedIndex();
            switch (index) {
                case 0:
                    type = "Resistance";
                    break;
                case 1:
                    type = "Inductor";
                    break;
                case 2:
                    type = "Capacitor";
                    break;
            }

            double value = Double.parseDouble(valueField.getText());
            if (value <= 0) {
                showError(languageManager.getTranslation("component_value_positive"));
                return;
            }

            CircuitComponent comp = new CircuitComponent(type, value);
            components.add(comp);
            updateComponentList();
            updateCircuitDiagram();

            valueField.setText("");
            valueField.requestFocus();

        } catch (NumberFormatException ex) {
            showError(languageManager.getTranslation("enter_numeric_values"));
        }
    }

    private void removeComponent() {
        int index = componentsList.getSelectedIndex();
        if (index >= 0 && index < components.size()) {
            components.remove(index);
            updateComponentList();
            updateCircuitDiagram();
        } else {
            showError(languageManager.getTranslation("select_component_remove"));
        }
    }

    private void simulateCircuit() {
        if (components.isEmpty()) {
            showError(languageManager.getTranslation("add_least_one_component"));
            return;
        }

        if (!validateInputs())
            return;

        try {
            double voltage = Double.parseDouble(voltageField.getText());
            double frequency = Double.parseDouble(frequencyField.getText());

            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString(languageManager.getTranslation("simulation_in_progress"));

            simulateButton.setEnabled(false);
            graphButton.setEnabled(false);

            engine.simulate(components, voltage, frequency);

        } catch (NumberFormatException ex) {
            showError(languageManager.getTranslation("enter_numeric_values"));
        }
    }

    private boolean validateInputs() {
        try {
            double voltage = Double.parseDouble(voltageField.getText());
            double frequency = Double.parseDouble(frequencyField.getText());

            if (voltage <= 0 || voltage > 1000) {
                showError(languageManager.getTranslation("voltage_range"));
                return false;
            }

            if (frequency <= 0 || frequency > 10000) {
                showError(languageManager.getTranslation("frequency_range"));
                return false;
            }

            return true;

        } catch (NumberFormatException e) {
            showError(languageManager.getTranslation("enter_numeric_values"));
            return false;
        }
    }

    private void showGraphs() {
        if (lastResult != null) {
            // Necesitamos obtener el JFrame padre para crear el GraphWindow
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            if (parentWindow instanceof JFrame) {
                GraphWindow graphWindow = new GraphWindow((JFrame) parentWindow, lastResult, components);
                graphWindow.setVisible(true);
            } else {
                showError("No se puede mostrar la ventana de gráficos: ventana padre no disponible");
            }
        }
    }

    private void clearAll() {
        components.clear();
        updateComponentList();
        updateCircuitDiagram();

        resultsArea.setText(languageManager.getTranslation("circuit_cleared"));
        graphButton.setEnabled(false);
        lastResult = null;

        showInfo(languageManager.getTranslation("circuit_results_cleared"));
    }

    private void updateComponentList() {
        componentsModel.clear();
        for (CircuitComponent comp : components) {
            componentsModel.addElement(comp.toString());
        }
    }

    private void updateCircuitDiagram() {
        StringBuilder html = new StringBuilder("<html><div style='text-align:center;'>");

        if (components.isEmpty()) {
            html.append("<p style='color:gray;font-size:14px;'>")
                    .append(languageManager.getTranslation("empty_circuit"))
                    .append("</p>");
            html.append("<p style='color:gray;font-size:12px;'>")
                    .append(languageManager.getTranslation("add_components_start"))
                    .append("</p>");
        } else {
            html.append("<p style='font-size:16px;margin-bottom:10px;'>⚡ ");

            for (int i = 0; i < components.size(); i++) {
                if (i > 0)
                    html.append(" — ");
                String type = components.get(i).getType();
                if (type.equals("Resistance"))
                    html.append("R");
                else if (type.equals("Inductor"))
                    html.append("L");
                else
                    html.append("C");

                html.append(" (").append(components.get(i).getValue()).append(")");
            }

            html.append(" ⚡</p>");

            // Información resumida
            double totalR = 0, totalL = 0, totalC = 0;
            for (CircuitComponent comp : components) {
                totalR += comp.getResistance();
                totalL += comp.getInductance();
                totalC += comp.getCapacitance();
            }

            html.append("<p style='font-size:12px;color:darkblue;'>");
            html.append("R: ").append(String.format("%.2f Ω", totalR)).append(" | ");
            html.append("L: ").append(String.format("%.4f H", totalL)).append(" | ");
            html.append("C: ").append(String.format("%.6f F", totalC));
            html.append("</p>");
        }

        html.append("</div></html>");
        circuitDiagram.setText(html.toString());
    }

    @Override
    public void onSimulationComplete(Object result) {
        SwingUtilities.invokeLater(() -> {
            if (result instanceof SimulationResult) {
                SimulationResult simResult = (SimulationResult) result;
                lastResult = simResult;
                graphButton.setEnabled(true);

                StringBuilder sb = new StringBuilder();
                sb.append(languageManager.getTranslation("simulation_results")).append("\n\n");
                sb.append(languageManager.getTranslation("impedance")).append(" ")
                        .append(df.format(simResult.getImpedance())).append(" Ω\n");
                sb.append(languageManager.getTranslation("current")).append(" ")
                        .append(df.format(simResult.getCurrent())).append(" A\n");
                sb.append(languageManager.getTranslation("phase_angle")).append(" ")
                        .append(df.format(Math.toDegrees(simResult.getPhaseAngle()))).append("°\n");
                sb.append(languageManager.getTranslation("active_power")).append(" ")
                        .append(df.format(simResult.getActivePower())).append(" W\n");
                sb.append(languageManager.getTranslation("reactive_power")).append(" ")
                        .append(df.format(simResult.getReactivePower())).append(" VAR\n");
                sb.append(languageManager.getTranslation("apparent_power")).append(" ")
                        .append(df.format(simResult.getApparentPower())).append(" VA\n");
                sb.append(languageManager.getTranslation("power_factor")).append(" ")
                        .append(df.format(simResult.getPowerFactor())).append("\n\n");

                // Información adicional
                double phaseDeg = Math.toDegrees(simResult.getPhaseAngle());
                if (phaseDeg > 0) {
                    sb.append(languageManager.getTranslation("inductive_circuit")).append("\n");
                } else if (phaseDeg < 0) {
                    sb.append(languageManager.getTranslation("capacitive_circuit")).append("\n");
                } else {
                    sb.append(languageManager.getTranslation("resistive_circuit")).append("\n");
                }

                resultsArea.setText(sb.toString());

                progressBar.setVisible(false);
                simulateButton.setEnabled(true);
            } else {
                onSimulationError("Resultado de simulación inválido");
            }
        });
    }

    @Override
    public void onSimulationError(String error) {
        SwingUtilities.invokeLater(() -> {
            showError(languageManager.getFormattedTranslation("simulation_error", error));

            progressBar.setVisible(false);
            simulateButton.setEnabled(true);
            graphButton.setEnabled(false);
        });
    }

    @Override
    public void onSimulationStart() {
        SwingUtilities.invokeLater(() -> {
            resultsArea.setText(languageManager.getTranslation("simulation_in_progress") +
                    "\n\n" + languageManager.getTranslation("please_wait"));
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString(languageManager.getTranslation("simulation_in_progress"));
        });
    }

    // Métodos de utilidad
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
                languageManager.getTranslation("error"), JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message,
                languageManager.getTranslation("information"), JOptionPane.INFORMATION_MESSAGE);
    }

    // ========== MÉTODOS PARA LA INTEGRACIÓN ==========

    /**
     * Actualiza el tamaño de fuente en el simulador
     */
    public void updateFontSize(float newSize) {
        // Actualizar fuentes de los componentes principales
        updateComponentFonts(this, newSize);
        revalidate();
        repaint();
    }

    private void updateComponentFonts(Container container, float newSize) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JComponent) {
                JComponent jcomp = (JComponent) comp;
                
                if (jcomp instanceof JLabel) {
                    JLabel label = (JLabel) jcomp;
                    Font currentFont = label.getFont();
                    label.setFont(currentFont.deriveFont(newSize));
                } else if (jcomp instanceof JButton) {
                    JButton button = (JButton) jcomp;
                    Font currentFont = button.getFont();
                    button.setFont(currentFont.deriveFont(newSize));
                } else if (jcomp instanceof JTextField) {
                    JTextField textField = (JTextField) jcomp;
                    Font currentFont = textField.getFont();
                    textField.setFont(currentFont.deriveFont(newSize));
                } else if (jcomp instanceof JComboBox) {
                    JComboBox<?> combo = (JComboBox<?>) jcomp;
                    Font currentFont = combo.getFont();
                    combo.setFont(currentFont.deriveFont(newSize));
                } else if (jcomp instanceof JTextArea) {
                    JTextArea textArea = (JTextArea) jcomp;
                    Font currentFont = textArea.getFont();
                    textArea.setFont(currentFont.deriveFont(newSize));
                } else if (jcomp instanceof JTabbedPane) {
                    JTabbedPane tabs = (JTabbedPane) jcomp;
                    Font currentFont = tabs.getFont();
                    tabs.setFont(currentFont.deriveFont(newSize));
                } else if (jcomp instanceof JList) {
                    JList<?> list = (JList<?>) jcomp;
                    Font currentFont = list.getFont();
                    list.setFont(currentFont.deriveFont(newSize));
                }
            }
            
            if (comp instanceof Container) {
                updateComponentFonts((Container) comp, newSize);
            }
        }
    }

    /**
     * Libera recursos del simulador
     */
    public void disposeResources() {
        if (engine != null) {
            engine.dispose();
            engine = null;
        }
        if (components != null) {
            components.clear();
        }
        lastResult = null;
    }

    /**
     * Método para ejecución independiente (crea su propio JFrame)
     */
    public void showInFrame() {
        JFrame frame = new JFrame("Simulador de Circuitos RLC");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Método main para ejecución independiente
     */
    public static void main(String[] args) {
        setupLookAndFeel();
        
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Iniciando Simulador RLC en modo independiente...");
                RLCSimulator simulator = new RLCSimulator();
                simulator.showInFrame();
                System.out.println("Simulador RLC iniciado correctamente");
            } catch (Exception e) {
                handleStartupError(e);
            }
        });
    }

    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("Look and feel del sistema configurado correctamente");
        } catch (Exception e1) {
            System.err.println("Error configurando look and feel del sistema: " + e1.getMessage());
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                System.out.println("Look and feel Nimbus configurado como fallback");
            } catch (Exception e2) {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    System.out.println("Look and feel cross-platform configurado como fallback");
                } catch (Exception e3) {
                    System.err.println("No se pudo configurar ningún look and feel: " + e3.getMessage());
                }
            }
        }
    }

    private static void handleStartupError(Exception e) {
        System.err.println("Error crítico iniciando la aplicación: " + e.getMessage());
        e.printStackTrace();
        
        JOptionPane.showMessageDialog(null,
            "Error iniciando la aplicación:\n" + e.getMessage() +
            "\n\nVer consola para más detalles.",
            "Error de Inicio",
            JOptionPane.ERROR_MESSAGE);
    }
}