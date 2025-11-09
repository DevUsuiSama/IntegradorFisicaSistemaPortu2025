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
 * Panel principal del simulador de circuitos RLC con diseño de dos columnas
 * Main panel for RLC circuit simulator with two-column layout
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
    private CircuitDiagramPanel circuitDiagram;
    private JButton addButton, removeButton, simulateButton, clearButton;
    private JProgressBar progressBar;

    // Nuevos componentes para el diseño de dos columnas
    private JPanel leftPanel, rightPanel;
    private JSplitPane splitPane;
    private BaseGraph currentGraph;
    private JPanel graphContainer;
    private JComboBox<String> graphTypeCombo;

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
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Crear el diseño de dos columnas
        createTwoColumnLayout();
    }

    private void createTwoColumnLayout() {
        // Crear paneles izquierdo y derecho
        leftPanel = createLeftPanel();
        rightPanel = createRightPanel();

        // Crear split pane con proporción 35%-65%
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(0.35); // 35% izquierda, 65% derecha
        splitPane.setResizeWeight(0.35); // Mantener la proporción al redimensionar
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setPreferredSize(new Dimension(350, 700));

        // Panel de controles (scrollable para muchos controles)
        JPanel controlsPanel = createControlsPanel();
        JScrollPane controlsScroll = new JScrollPane(controlsPanel);
        controlsScroll.setBorder(BorderFactory.createTitledBorder(languageManager.getTranslation("configuration")));
        controlsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        controlsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(controlsScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Panel superior: Diagrama del circuito con scroll
        JPanel diagramPanel = createCircuitPanel();

        // Panel central: Gráfico con scroll
        JPanel graphPanel = createGraphPanel();

        // Panel inferior: Resultados con scroll
        JPanel resultsPanel = createResultsPanel();

        // Usar un split pane vertical para los tres paneles
        JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, diagramPanel, graphPanel);
        topSplit.setDividerLocation(0.3); // 30% diagrama, 70% gráfico
        topSplit.setResizeWeight(0.3);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, resultsPanel);
        mainSplit.setDividerLocation(0.7); // 70% superior, 30% resultados
        mainSplit.setResizeWeight(0.7);

        panel.add(mainSplit, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de entrada principal
        JPanel inputPanel = createInputPanel();
        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Selector de método
        JPanel methodPanel = createMethodPanel();
        methodPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Presets de circuito
        JPanel presetPanel = createPresetPanel();
        presetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel de componentes
        JPanel componentPanel = createComponentPanel();
        componentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Lista de componentes
        JPanel listPanel = createComponentListPanel();
        listPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel de botones de acción
        JPanel actionPanel = createActionPanel();
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(inputPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(methodPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(presetPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(componentPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(listPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(actionPanel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(languageManager.getTranslation("power_supply")));

        JPanel voltagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        voltagePanel.add(new JLabel(languageManager.getTranslation("voltage")));
        voltageField = new JTextField("10", 10);
        voltageField.setToolTipText(languageManager.getTranslation("voltage_range"));
        voltagePanel.add(voltageField);
        voltagePanel.add(new JLabel("V"));

        JPanel frequencyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        frequencyPanel.add(new JLabel(languageManager.getTranslation("frequency")));
        frequencyField = new JTextField("60", 10);
        frequencyField.setToolTipText(languageManager.getTranslation("frequency_range"));
        frequencyPanel.add(frequencyField);
        frequencyPanel.add(new JLabel("Hz"));

        panel.add(voltagePanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(frequencyPanel);

        return panel;
    }

    private JPanel createMethodPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(languageManager.getTranslation("simulation_method")));

        methodCombo = new JComboBox<>();
        for (SimulationStrategy strategy : CircuitEngine.getAvailableStrategies()) {
            String methodKey = strategy.getName().toLowerCase().replace("-", "");
            methodCombo.addItem(languageManager.getTranslation(methodKey));
        }
        methodCombo.setToolTipText(languageManager.getTranslation("method"));
        methodCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        methodCombo.setMaximumSize(new Dimension(300, 25));

        panel.add(methodCombo);
        return panel;
    }

    private JPanel createPresetPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(languageManager.getTranslation("circuit_presets")));

        String[] presetKeys = {
                "custom", "underdamped", "critical", "overdamped",
                "series_rlc", "high_pass", "low_pass"
        };

        presetCombo = new JComboBox<>();
        for (String key : presetKeys) {
            presetCombo.addItem(languageManager.getTranslation(key));
        }
        presetCombo.setToolTipText(languageManager.getTranslation("preset"));
        presetCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        presetCombo.setMaximumSize(new Dimension(300, 25));

        panel.add(presetCombo);
        return panel;
    }

    private JPanel createComponentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(languageManager.getTranslation("components")));

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(new JLabel(languageManager.getTranslation("component_type")));

        String[] componentTypes = { "resistance", "inductor", "capacitor" };
        componentTypeCombo = new JComboBox<>();
        for (String type : componentTypes) {
            componentTypeCombo.addItem(languageManager.getTranslation(type));
        }
        componentTypeCombo.setMaximumSize(new Dimension(150, 25));
        typePanel.add(componentTypeCombo);

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        valuePanel.add(new JLabel(languageManager.getTranslation("value")));
        valueField = new JTextField("100", 10);
        valueField.setToolTipText(languageManager.getTranslation("component_value_positive"));
        valuePanel.add(valueField);

        addButton = new JButton(languageManager.getTranslation("add_component"));
        addButton.setToolTipText(languageManager.getTranslation("add_component"));

        panel.add(typePanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(valuePanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(addButton);

        return panel;
    }

    private JPanel createComponentListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(languageManager.getTranslation("component_list")));
        panel.setMaximumSize(new Dimension(350, 200));

        componentsModel = new DefaultListModel<>();
        componentsList = new JList<>(componentsModel);
        componentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        componentsList.setToolTipText(languageManager.getTranslation("component_list"));

        JScrollPane listScroll = new JScrollPane(componentsList);
        listScroll.setPreferredSize(new Dimension(300, 120));

        removeButton = new JButton(languageManager.getTranslation("remove_selected"));
        removeButton.setToolTipText(languageManager.getTranslation("remove_selected"));

        panel.add(listScroll, BorderLayout.CENTER);
        panel.add(removeButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(languageManager.getTranslation("actions")));

        simulateButton = new JButton(languageManager.getTranslation("simulate"));
        simulateButton.setToolTipText(languageManager.getTranslation("simulate"));
        simulateButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        clearButton = new JButton(languageManager.getTranslation("clear_all"));
        clearButton.setToolTipText(languageManager.getTranslation("clear_all"));
        clearButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Barra de progreso
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(300, 20));

        panel.add(simulateButton);
        panel.add(Box.createVerticalStrut(8));
        panel.add(clearButton);
        panel.add(Box.createVerticalStrut(8));
        panel.add(progressBar);

        return panel;
    }

    private JPanel createCircuitPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(languageManager.getTranslation("circuit_diagram")));
        panel.setPreferredSize(new Dimension(600, 200));

        // Usar el nuevo CircuitDiagramPanel
        circuitDiagram = new CircuitDiagramPanel();

        JScrollPane diagramScroll = new JScrollPane(circuitDiagram);
        diagramScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        diagramScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(diagramScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createGraphPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(languageManager.getTranslation("simulation_graph")));

        // Gráfico por defecto (se actualizará después de la simulación)
        currentGraph = new TimeGraph(null);
        graphContainer = new JPanel(new BorderLayout());

        JScrollPane graphScroll = new JScrollPane(currentGraph);
        graphScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        graphScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        graphContainer.add(graphScroll, BorderLayout.CENTER);

        // Panel de selección de tipo de gráfico
        JPanel graphTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        graphTypePanel.add(new JLabel("Tipo de Gráfico:"));

        graphTypeCombo = new JComboBox<>(new String[] {
                "Dominio de Tiempo", "Respuesta en Frecuencia", "Diagrama Fasorial", "Formas de Onda"
        });
        graphTypeCombo.addActionListener(e -> updateGraphType(graphTypeCombo.getSelectedIndex()));
        graphTypePanel.add(graphTypeCombo);

        panel.add(graphTypePanel, BorderLayout.NORTH);
        panel.add(graphContainer, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(languageManager.getTranslation("results")));
        panel.setPreferredSize(new Dimension(600, 200));

        resultsArea = new JTextArea(8, 50);
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);

        // Texto inicial en el idioma actual
        updateInitialResultsText();

        JScrollPane scroll = new JScrollPane(resultsArea);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void updateGraphType(int graphType) {
        if (lastResult == null || components == null) {
            // Crear gráfico vacío con mensaje informativo
            switch (graphType) {
                case 0:
                    currentGraph = new TimeGraph(null);
                    break;
                case 1:
                    currentGraph = new FrequencyGraph(components != null ? components : new ArrayList<>());
                    break;
                case 2:
                    currentGraph = new PhasorDiagram(null, components != null ? components : new ArrayList<>());
                    break;
                case 3:
                    currentGraph = new WaveformGraph(null);
                    break;
            }
        } else {
            // Crear gráfico con datos de simulación
            switch (graphType) {
                case 0:
                    currentGraph = new TimeGraph(lastResult);
                    break;
                case 1:
                    currentGraph = new FrequencyGraph(components);
                    break;
                case 2:
                    currentGraph = new PhasorDiagram(lastResult, components);
                    break;
                case 3:
                    currentGraph = new WaveformGraph(lastResult);
                    break;
            }
        }

        // Actualizar el contenedor del gráfico
        graphContainer.removeAll();
        JScrollPane graphScroll = new JScrollPane(currentGraph);
        graphScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        graphScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        graphContainer.add(graphScroll, BorderLayout.CENTER);
        graphContainer.revalidate();
        graphContainer.repaint();

        // Actualizar tooltip del combo box
        String[] tooltips = {
                "Muestra la corriente en función del tiempo",
                "Muestra la impedancia en función de la frecuencia",
                "Muestra el diagrama fasorial de voltajes y corriente",
                "Muestra las formas de onda de voltaje y corriente"
        };

        if (graphType >= 0 && graphType < tooltips.length) {
            graphTypeCombo.setToolTipText(tooltips[graphType]);
        }
    }

    private void setupEventHandlers() {
        // Selector de método
        methodCombo.addActionListener(e -> updateStrategy());

        // Selector de preset
        presetCombo.addActionListener(e -> loadPreset());

        // Botones de componentes
        addButton.addActionListener(e -> addComponent());
        removeButton.addActionListener(e -> removeComponent());

        // Botones principales
        simulateButton.addActionListener(e -> simulateCircuit());
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
                if (title.contains("Configuración") || title.contains("Configuration")) {
                    titledBorder.setTitle(languageManager.getTranslation("configuration"));
                } else if (title.contains("Fuente") || title.contains("Power")) {
                    titledBorder.setTitle(languageManager.getTranslation("power_supply"));
                } else if (title.contains("Método") || title.contains("Method")) {
                    titledBorder.setTitle(languageManager.getTranslation("simulation_method"));
                } else if (title.contains("Circuitos Predefinidos") || title.contains("Circuit Presets")) {
                    titledBorder.setTitle(languageManager.getTranslation("circuit_presets"));
                } else if (title.contains("Componentes") || title.contains("Components")) {
                    titledBorder.setTitle(languageManager.getTranslation("components"));
                } else if (title.contains("Lista de Componentes") || title.contains("Component List")) {
                    titledBorder.setTitle(languageManager.getTranslation("component_list"));
                } else if (title.contains("Acciones") || title.contains("Actions")) {
                    titledBorder.setTitle(languageManager.getTranslation("actions"));
                } else if (title.contains("Diagrama") || title.contains("Diagram")) {
                    titledBorder.setTitle(languageManager.getTranslation("circuit_diagram"));
                } else if (title.contains("Gráfico") || title.contains("Graph")) {
                    titledBorder.setTitle(languageManager.getTranslation("simulation_graph"));
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
                    } else if (text.contains("Tipo de Gráfico") || text.contains("Graph Type")) {
                        label.setText("Tipo de Gráfico:");
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

    private void clearAll() {
        components.clear();
        updateComponentList();
        updateCircuitDiagram();

        resultsArea.setText(languageManager.getTranslation("circuit_cleared"));
        lastResult = null;

        // Resetear gráfico
        updateGraphType(0);

        showInfo(languageManager.getTranslation("circuit_results_cleared"));
    }

    private void updateComponentList() {
        componentsModel.clear();
        for (CircuitComponent comp : components) {
            componentsModel.addElement(comp.toString());
        }
    }

    private void updateCircuitDiagram() {
        if (circuitDiagram != null) {
            circuitDiagram.setComponents(components);
            circuitDiagram.repaint();
        }
    }

    @Override
    public void onSimulationComplete(Object result) {
        SwingUtilities.invokeLater(() -> {
            if (result instanceof SimulationResult) {
                SimulationResult simResult = (SimulationResult) result;
                lastResult = simResult;

                // Actualizar el gráfico actual con los nuevos resultados
                updateGraphType(graphTypeCombo.getSelectedIndex());

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

                // Información adicional sobre el tipo de circuito
                double phaseDeg = Math.toDegrees(simResult.getPhaseAngle());
                String circuitType;
                String behaviorDescription;

                if (phaseDeg > 0) {
                    circuitType = languageManager.getTranslation("inductive_circuit");
                    behaviorDescription = "• La corriente está ATRASADA respecto al voltaje\n";
                    behaviorDescription += "• La potencia reactiva es POSITIVA (Q > 0)\n";
                    behaviorDescription += "• El inductor domina el comportamiento del circuito";
                } else if (phaseDeg < 0) {
                    circuitType = languageManager.getTranslation("capacitive_circuit");
                    behaviorDescription = "• La corriente está ADELANTADA respecto al voltaje\n";
                    behaviorDescription += "• La potencia reactiva es NEGATIVA (Q < 0)\n";
                    behaviorDescription += "• El capacitor domina el comportamiento del circuito";
                } else {
                    circuitType = languageManager.getTranslation("resistive_circuit");
                    behaviorDescription = "• La corriente está EN FASE con el voltaje\n";
                    behaviorDescription += "• La potencia reactiva es CERO (Q = 0)\n";
                    behaviorDescription += "• Resonancia o circuito puramente resistivo";
                }

                sb.append(circuitType).append("\n\n");
                sb.append("Comportamiento del Circuito:\n");
                sb.append(behaviorDescription).append("\n\n");

                // Análisis de resonancia si aplica
                if (components != null && !components.isEmpty()) {
                    double totalL = 0, totalC = 0;
                    for (CircuitComponent comp : components) {
                        totalL += comp.getInductance();
                        totalC += comp.getCapacitance();
                    }

                    if (totalL > 0 && totalC > 0 && totalC < 1e10) {
                        double resonantFreq = 1.0 / (2 * Math.PI * Math.sqrt(totalL * totalC));
                        double currentFreq = Double.parseDouble(frequencyField.getText());

                        sb.append("Análisis de Resonancia:\n");
                        sb.append(String.format("• Frecuencia de resonancia: %.2f Hz\n", resonantFreq));
                        sb.append(String.format("• Frecuencia actual: %.2f Hz\n", currentFreq));

                        double ratio = currentFreq / resonantFreq;
                        if (Math.abs(ratio - 1.0) < 0.1) {
                            sb.append("• ⚡ El circuito está CERCA de la resonancia\n");
                            sb.append("• La impedancia es MÍNIMA y la corriente MÁXIMA\n");
                        } else if (currentFreq < resonantFreq) {
                            sb.append("• Frecuencia por DEBAJO de la resonancia\n");
                            sb.append("• Comportamiento CAPACITIVO dominante\n");
                        } else {
                            sb.append("• Frecuencia por ENCIMA de la resonancia\n");
                            sb.append("• Comportamiento INDUCTIVO dominante\n");
                        }
                        sb.append("\n");
                    }
                }

                // Información de calidad del circuito
                if (simResult.getImpedance() > 0) {
                    double qualityFactor = Math.abs(simResult.getReactivePower()) / simResult.getActivePower();
                    sb.append("Factor de Calidad (Q):\n");
                    sb.append(String.format("• Q = |Q| / P = %.3f\n", qualityFactor));

                    if (qualityFactor > 10) {
                        sb.append("• Circuito de ALTA calidad (subamortiguado)\n");
                    } else if (qualityFactor > 1) {
                        sb.append("• Circuito de calidad MODERADA\n");
                    } else {
                        sb.append("• Circuito de BAJA calidad (sobreamortiguado)\n");
                    }
                    sb.append("\n");
                }

                // Resumen de potencia
                sb.append("Resumen de Potencia:\n");
                sb.append(String.format("• Potencia Activa (P): %.3f W → Energía útil\n", simResult.getActivePower()));
                sb.append(String.format("• Potencia Reactiva (Q): %.3f VAR → Energía oscilante\n",
                        simResult.getReactivePower()));
                sb.append(String.format("• Potencia Aparente (S): %.3f VA → Potencia total\n",
                        simResult.getApparentPower()));
                sb.append(String.format("• Factor de Potencia: %.3f → Eficiencia energética\n",
                        simResult.getPowerFactor()));

                // Evaluación del factor de potencia
                if (simResult.getPowerFactor() >= 0.9) {
                    sb.append("• ✅ Excelente factor de potencia (eficiente)\n");
                } else if (simResult.getPowerFactor() >= 0.8) {
                    sb.append("• ⚠️  Factor de potencia aceptable\n");
                } else {
                    sb.append("• ❌ Factor de potencia pobre (ineficiente)\n");
                }

                resultsArea.setText(sb.toString());

                progressBar.setVisible(false);
                simulateButton.setEnabled(true);

                // Mostrar mensaje de éxito
                showInfo("Simulación completada exitosamente. Los resultados están listos.");

            } else {
                onSimulationError("Resultado de simulación inválido: tipo de objeto incorrecto");
            }
        });
    }

    @Override
    public void onSimulationError(String error) {
        SwingUtilities.invokeLater(() -> {
            // Mensaje de error más descriptivo
            String detailedError = "Error en la simulación:\n\n" + error;

            // Intentar dar más información según el tipo de error
            if (error.contains("impedance") || error.contains("impedancia")) {
                detailedError += "\n\nPosible causa: Valores de componentes que resultan en impedancia cero o negativa.";
            } else if (error.contains("frequency") || error.contains("frecuencia")) {
                detailedError += "\n\nPosible causa: Frecuencia fuera del rango permitido (0.1 - 10000 Hz).";
            } else if (error.contains("voltage") || error.contains("voltaje")) {
                detailedError += "\n\nPosible causa: Voltaje fuera del rango permitido (0.1 - 1000 V).";
            } else if (error.contains("component") || error.contains("componente")) {
                detailedError += "\n\nPosible causa: Configuración inválida de componentes del circuito.";
            }

            showError(detailedError);

            progressBar.setVisible(false);
            simulateButton.setEnabled(true);

            // Limpiar resultados en caso de error
            resultsArea.setText("Error en la simulación. Por favor, verifique los parámetros e intente nuevamente.\n\n"
                    + "Detalles del error: " + error);

            // Limpiar gráficos
            updateGraphType(0); // Resetear al gráfico de tiempo vacío
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