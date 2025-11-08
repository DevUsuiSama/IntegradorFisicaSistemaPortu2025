package com.simulador.ui;

import com.simulador.engine.*;
import com.simulador.model.*;
import com.simulador.utils.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Interfaz gráfica principal del simulador de circuitos RLC
 * Main GUI for RLC circuit simulator
 */
public class RLCSimulator extends JFrame implements SimulationObserver {
    private CircuitEngine engine;
    private java.util.List<CircuitComponent> components;
    private SimulationResult lastResult;
    private DecimalFormat df = new DecimalFormat("0.000");

    // Componentes de UI
    private JTextField voltageField, frequencyField, valueField;
    private JComboBox<String> componentTypeCombo, methodCombo, presetCombo, langCombo;
    private JList<String> componentsList;
    private DefaultListModel<String> componentsModel;
    private JTextArea resultsArea;
    private JLabel circuitDiagram;
    private JButton addButton, removeButton, simulateButton, graphButton, clearButton;
    private JProgressBar progressBar;

    public RLCSimulator() {
        this.engine = new CircuitEngine();
        this.components = new ArrayList<>();

        initializeEngine();
        initializeUI();
        setupEventHandlers();
    }

    private void initializeEngine() {
        engine.addObserver(this);
        engine.setStrategy(new AnalyticalStrategy());
    }

    private void initializeUI() {
        setTitle("Simulador de Circuitos RLC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1200, 800));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createControlPanel(), BorderLayout.NORTH);
        mainPanel.add(createCircuitPanel(), BorderLayout.CENTER);
        mainPanel.add(createResultsPanel(), BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);

        // Configurar cierre seguro
        setupSafeClose();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Controles de Entrada"));

        // Selector de idioma
        JPanel langPanel = createLanguagePanel();

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

        panel.add(langPanel);
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

    private JPanel createLanguagePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Idioma:"));

        langCombo = new JComboBox<>(new String[] { "Español", "Português", "English" });
        langCombo.setSelectedItem("Español");
        panel.add(langCombo);

        return panel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel.add(new JLabel("Voltaje (V):"));
        voltageField = new JTextField("10", 8);
        voltageField.setToolTipText("Voltaje de alimentación (0.1 - 1000 V)");
        panel.add(voltageField);

        panel.add(new JLabel("Frecuencia (Hz):"));
        frequencyField = new JTextField("60", 8);
        frequencyField.setToolTipText("Frecuencia de operación (0.1 - 10000 Hz)");
        panel.add(frequencyField);

        return panel;
    }

    private JPanel createMethodPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Método:"));

        methodCombo = new JComboBox<>();
        for (SimulationStrategy strategy : CircuitEngine.getAvailableStrategies()) {
            methodCombo.addItem(strategy.getName());
        }
        methodCombo.setToolTipText("Seleccione el método de simulación");
        panel.add(methodCombo);

        return panel;
    }

    private JPanel createPresetPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Circuito Predefinido:"));

        presetCombo = new JComboBox<>(new String[] {
                "Personalizado", "Subamortiguado", "Crítico", "Sobreamortiguado",
                "RLC Serie", "Filtro Pasa Altos", "Filtro Pasa Bajos"
        });
        presetCombo.setToolTipText("Seleccione un circuito predefinido");
        panel.add(presetCombo);

        return panel;
    }

    private JPanel createComponentPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel.add(new JLabel("Tipo:"));
        componentTypeCombo = new JComboBox<>(new String[] {
                "Resistencia", "Inductor", "Capacitor"
        });
        panel.add(componentTypeCombo);

        panel.add(new JLabel("Valor:"));
        valueField = new JTextField("100", 8);
        valueField.setToolTipText("Valor del componente (debe ser positivo)");
        panel.add(valueField);

        addButton = new JButton("Agregar Componente");
        addButton.setToolTipText("Agregar componente al circuito");
        panel.add(addButton);

        return panel;
    }

    private JPanel createComponentListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        componentsModel = new DefaultListModel<>();
        componentsList = new JList<>(componentsModel);
        componentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        componentsList.setToolTipText("Componentes en el circuito");

        JScrollPane listScroll = new JScrollPane(componentsList);
        listScroll.setPreferredSize(new Dimension(400, 100));

        removeButton = new JButton("Eliminar Seleccionado");
        removeButton.setToolTipText("Eliminar componente seleccionado");

        panel.add(new JLabel("Lista de Componentes:"), BorderLayout.NORTH);
        panel.add(listScroll, BorderLayout.CENTER);
        panel.add(removeButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCircuitPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Diagrama del Circuito"));
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
        panel.setBorder(BorderFactory.createTitledBorder("Resultados y Gráficos"));

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

        simulateButton = new JButton("Simular Circuito");
        simulateButton.setToolTipText("Ejecutar simulación del circuito");

        graphButton = new JButton("Ver Gráficos");
        graphButton.setEnabled(false);
        graphButton.setToolTipText("Abrir ventana de gráficos");

        clearButton = new JButton("Limpiar Todo");
        clearButton.setToolTipText("Limpiar circuito y resultados");

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
        resultsArea.setText(
                "=== SIMULADOR DE CIRCUITOS RLC ===\n\n" +
                        "Instrucciones:\n" +
                        "1. Agregue componentes (R, L, C) al circuito\n" +
                        "2. Configure voltaje y frecuencia\n" +
                        "3. Seleccione método de simulación\n" +
                        "4. Haga clic en 'Simular Circuito'\n\n" +
                        "Características:\n" +
                        "• Análisis en dominio de tiempo y frecuencia\n" +
                        "• Diagramas fasoriales interactivos\n" +
                        "• Múltiples métodos de cálculo\n" +
                        "• Circuitos predefinidos\n" +
                        "• Soporte multiidioma\n");

        JScrollPane scroll = new JScrollPane(resultsArea);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void setupEventHandlers() {
        // Selector de idioma
        langCombo.addActionListener(e -> changeLanguage());

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

    private void setupSafeClose() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeApplication();
            }
        });
    }

    private void changeLanguage() {
        String selected = (String) langCombo.getSelectedItem();
        switch (selected) {
            case "Español":
                I18N.setLanguage("es");
                break;
            case "Português":
                I18N.setLanguage("pt");
                break;
            case "English":
                I18N.setLanguage("en");
                break;
        }
        updateUITexts();
    }

    private void updateUITexts() {
        setTitle(I18N.get("title"));
        // Actualizar otros textos de UI según sea necesario
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
        if (selected == null || "Personalizado".equals(selected))
            return;

        String presetType = "";
        switch (selected) {
            case "Subamortiguado":
                presetType = "underdamped";
                break;
            case "Crítico":
                presetType = "critical";
                break;
            case "Sobreamortiguado":
                presetType = "overdamped";
                break;
            case "RLC Serie":
                presetType = "series_rlc";
                break;
            case "Filtro Pasa Altos":
                presetType = "high_pass";
                break;
            case "Filtro Pasa Bajos":
                presetType = "low_pass";
                break;
        }

        components.clear();
        components.addAll(CircuitFactory.createPreset(presetType));

        updateComponentList();
        updateCircuitDiagram();

        showInfo("Circuito predefinido '" + selected + "' cargado");
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
                showError("El valor del componente debe ser positivo");
                return;
            }

            CircuitComponent comp = new CircuitComponent(type, value);
            components.add(comp);
            updateComponentList();
            updateCircuitDiagram();

            valueField.setText("");
            valueField.requestFocus();

        } catch (NumberFormatException ex) {
            showError("Por favor ingrese un valor numérico válido");
        }
    }

    private void removeComponent() {
        int index = componentsList.getSelectedIndex();
        if (index >= 0 && index < components.size()) {
            components.remove(index);
            updateComponentList();
            updateCircuitDiagram();
        } else {
            showError("Seleccione un componente para eliminar");
        }
    }

    private void simulateCircuit() {
        if (components.isEmpty()) {
            showError("Agregue al menos un componente al circuito");
            return;
        }

        if (!validateInputs())
            return;

        try {
            double voltage = Double.parseDouble(voltageField.getText());
            double frequency = Double.parseDouble(frequencyField.getText());

            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString("Simulando...");

            simulateButton.setEnabled(false);
            graphButton.setEnabled(false);

            engine.simulate(components, voltage, frequency);

        } catch (NumberFormatException ex) {
            showError("Error en los valores de voltaje o frecuencia");
        }
    }

    private boolean validateInputs() {
        try {
            double voltage = Double.parseDouble(voltageField.getText());
            double frequency = Double.parseDouble(frequencyField.getText());

            if (voltage <= 0 || voltage > 1000) {
                showError("El voltaje debe estar entre 0.1 y 1000 V");
                return false;
            }

            if (frequency <= 0 || frequency > 10000) {
                showError("La frecuencia debe estar entre 0.1 y 10000 Hz");
                return false;
            }

            return true;

        } catch (NumberFormatException e) {
            showError("Ingrese valores numéricos válidos para voltaje y frecuencia");
            return false;
        }
    }

    private void showGraphs() {
        if (lastResult != null) {
            GraphWindow graphWindow = new GraphWindow(this, lastResult, components);
            graphWindow.setVisible(true);
        }
    }

    private void clearAll() {
        components.clear();
        updateComponentList();
        updateCircuitDiagram();

        resultsArea.setText("Circuito limpiado. Listo para nueva simulación.");
        graphButton.setEnabled(false);
        lastResult = null;

        showInfo("Circuito y resultados limpiados");
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
            html.append("<p style='color:gray;font-size:14px;'>Circuito Vacío</p>");
            html.append("<p style='color:gray;font-size:12px;'>Agregue componentes para comenzar</p>");
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

    // En RLCSimulator.java - REEMPLAZAR el método onSimulationComplete existente:

    @Override
    public void onSimulationComplete(Object result) {
        SwingUtilities.invokeLater(() -> {
            if (result instanceof SimulationResult) {
                SimulationResult simResult = (SimulationResult) result;
                lastResult = simResult;
                graphButton.setEnabled(true);

                StringBuilder sb = new StringBuilder();
                sb.append("=== RESULTADOS DE SIMULACIÓN ===\n\n");
                sb.append("• Impedancia: ").append(df.format(simResult.getImpedance())).append(" Ω\n");
                sb.append("• Corriente: ").append(df.format(simResult.getCurrent())).append(" A\n");
                sb.append("• Ángulo de Fase: ").append(df.format(Math.toDegrees(simResult.getPhaseAngle())))
                        .append("°\n");
                sb.append("• Potencia Activa: ").append(df.format(simResult.getActivePower())).append(" W\n");
                sb.append("• Potencia Reactiva: ").append(df.format(simResult.getReactivePower())).append(" VAR\n");
                sb.append("• Potencia Aparente: ").append(df.format(simResult.getApparentPower())).append(" VA\n");
                sb.append("• Factor de Potencia: ").append(df.format(simResult.getPowerFactor())).append("\n\n");

                // Información adicional
                double phaseDeg = Math.toDegrees(simResult.getPhaseAngle());
                if (phaseDeg > 0) {
                    sb.append("→ Circuito INDUCTIVO (corriente atrasada)\n");
                } else if (phaseDeg < 0) {
                    sb.append("→ Circuito CAPACITIVO (corriente adelantada)\n");
                } else {
                    sb.append("→ Circuito RESISTIVO (corriente en fase)\n");
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
            showError("Error en simulación: " + error);

            progressBar.setVisible(false);
            simulateButton.setEnabled(true);
            graphButton.setEnabled(false);
        });
    }

    // AÑADIR este método faltante:
    @Override
    public void onSimulationStart() {
        SwingUtilities.invokeLater(() -> {
            resultsArea.setText("Simulación en progreso...\n\nPor favor espere.");
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString("Simulando...");
        });
    }

    // Métodos de utilidad
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void closeApplication() {
        if (engine != null) {
            engine.dispose();
        }
        dispose();
        System.exit(0);
    }

    public static void main(String[] args) {
        // Configurar look and feel de forma segura para Java 17
        setupLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Iniciando Simulador RLC...");
                RLCSimulator simulator = new RLCSimulator();
                simulator.setVisible(true);
                System.out.println("Simulador iniciado correctamente");
            } catch (Exception e) {
                handleStartupError(e);
            }
        });
    }

    private static void setupLookAndFeel() {
        try {
            // Método CORREGIDO para Java 17 - usar getSystemLookAndFeelClassName()
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("Look and feel del sistema configurado correctamente");
        } catch (Exception e1) {
            System.err.println("Error configurando look and feel del sistema: " + e1.getMessage());
            try {
                // Fallback a Nimbus (moderno)
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                System.out.println("Look and feel Nimbus configurado como fallback");
            } catch (Exception e2) {
                try {
                    // Fallback final a Metal (cross-platform)
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

        // Mostrar mensaje de error básico
        JOptionPane.showMessageDialog(null,
                "Error iniciando la aplicación:\n" + e.getMessage() +
                        "\n\nVer consola para más detalles.",
                "Error de Inicio",
                JOptionPane.ERROR_MESSAGE);
    }
}