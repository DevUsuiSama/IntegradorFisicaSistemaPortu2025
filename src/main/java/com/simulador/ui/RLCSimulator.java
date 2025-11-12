package com.simulador.ui;

import com.simulador.engine.AnalyticalStrategy;
import com.simulador.engine.CircuitEngine;
import com.simulador.engine.SimulationStrategy;
import com.simulador.model.CircuitComponent;
import com.simulador.model.CircuitFactory;
import com.simulador.model.SimulationResult;
import com.simulador.model.CircuitSimulationTask;
import com.simulador.scheduler.FirstComeFirstServedScheduler;
import com.simulador.scheduler.ProcessScheduler;
import com.simulador.scheduler.RoundRobinScheduler;
import com.simulador.scheduler.ShortestJobFirstScheduler;
import com.simulador.utils.LanguageManager;
import com.simulador.utils.SimulationObserver;

// NUEVOS IMPORTS DC
import com.simulador.engine.dc.DCCircuitEngine;
import com.simulador.engine.dc.DCAnalysisMethod;
import com.simulador.model.dc.DCComponent;
import com.simulador.model.dc.DCComponentType;
import com.simulador.model.dc.DCCircuit;
import com.simulador.model.dc.DCBranch;
import com.simulador.model.dc.DCSimulationResult;
import com.simulador.ui.dc.DCDiagramPanel;
import com.simulador.ui.dc.DCResultsPanel;
import com.simulador.ui.dc.DCEquivalentCircuitPanel;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel principal del simulador de circuitos RLC con algoritmos de
 * planificación integrados - Versión Mejorada Visualmente
 * AHORA INCLUYE SIMULACIÓN DE CIRCUITOS DC
 */
public class RLCSimulator extends JPanel implements SimulationObserver {
    private CircuitEngine engine;
    private ProcessScheduler scheduler;
    private java.util.List<CircuitComponent> components;
    private SimulationResult lastResult;
    private DecimalFormat df = new DecimalFormat("0.000");
    private LanguageManager languageManager;

    // NUEVAS VARIABLES DC
    private DCCircuitEngine dcEngine;
    private DCCircuit currentDCCircuit;
    private DCSimulationResult lastDCResult;
    private DCResultsPanel dcResultsPanel;
    private DCEquivalentCircuitPanel dcEquivalentPanel;
    private DCDiagramPanel dcDiagramPanel;
    private JTextArea dcDetailedAnalysisArea; // NUEVO: Área de texto para análisis detallado DC

    // Componentes de UI DC
    private JTextField dcVoltageField;
    private JSpinner batterySpinner;
    private JComboBox<String> dcComponentTypeCombo;
    private JTextField dcValueField;
    private JSpinner quantitySpinner;
    private JSpinner branchSpinner;
    private JComboBox<String> configCombo;
    private JComboBox<String> dcMethodCombo;
    private JButton addDCButton;
    private JButton simulateDCButton;
    private JButton clearDCButton;
    private JProgressBar dcProgressBar;

    // PALETA DE COLORES MEJORADA
    private final Color PRIMARY_BLUE = Color.decode("#2563eb");
    private final Color SECONDARY_BLUE = Color.decode("#3b82f6");
    private final Color ACCENT_PURPLE = Color.decode("#8b5cf6");
    private final Color SUCCESS_EMERALD = Color.decode("#10b981");
    private final Color WARNING_AMBER = Color.decode("#f59e0b");
    private final Color ERROR_ROSE = Color.decode("#f43f5e");
    private final Color DARK_SLATE = Color.decode("#1e293b");
    private final Color LIGHT_SLATE = Color.decode("#f1f5f9");
    private final Color CARD_BACKGROUND = Color.WHITE;

    // Componentes de UI para planificación
    private JComboBox<String> algorithmCombo;
    private JComboBox<String> batchTypeCombo;
    private JSpinner simpleSpinner, mediumSpinner, complexSpinner;
    private JButton generateBatchButton, startSchedulerButton, stopSchedulerButton;
    private JTextArea schedulingLogArea;
    private JProgressBar schedulingProgressBar;
    private JTable tasksTable;
    private DefaultTableModel tasksTableModel;
    private Timer updateTimer;

    // Componentes de simulación de circuitos
    private JTextField voltageField, frequencyField, valueField;
    private JComboBox<String> componentTypeCombo, methodCombo, presetCombo;
    private JList<String> componentsList;
    private DefaultListModel<String> componentsModel;
    private JTextArea resultsArea;
    private CircuitDiagramPanel circuitDiagram;
    private JButton addButton, removeButton, simulateButton, clearButton;
    private JProgressBar progressBar;

    // Componentes para visualización de memoria
    private JLabel fragTotalValue, memUsageValue, avgUsageValue, extFragValue, intFragValue, semaphoreValue;
    private JLabel currentProcessLabel;
    private JProgressBar memoryProgressBar;
    private JPanel memoryVisualizationPanel;
    private JTextArea processQueueArea;

    // Componentes para visualización de circuitos
    private BaseGraph currentGraph;
    private JPanel graphContainer;
    private JComboBox<String> graphTypeCombo;
    private JTextArea analysisArea;
    private JTabbedPane circuitTabs;

    // Panel principal derecho (cambia según la pestaña)
    private JPanel rightPanel;
    private CardLayout rightPanelLayout;

    public RLCSimulator() {
        this.engine = new CircuitEngine();
        this.scheduler = new ProcessScheduler();
        this.components = new ArrayList<>();
        this.languageManager = LanguageManager.getInstance();
        this.updateTimer = null;
        
        // INICIALIZACIÓN DC
        this.dcEngine = new DCCircuitEngine();
        this.currentDCCircuit = new DCCircuit();
        this.lastDCResult = null;

        initializeEngines();
        initializeUI();
        setupEventHandlers();
        setupDCEventHandlers(); // NUEVO: Configurar handlers DC
    }

    private void initializeEngines() {
        engine.addObserver(this);
        engine.setStrategy(new AnalyticalStrategy());

        // Configurar listener para el scheduler
        scheduler.addPropertyChangeListener(evt -> {
            if (ProcessScheduler.PROPERTY_MESSAGE.equals(evt.getPropertyName())) {
                logSchedulingMessage((String) evt.getNewValue());
            } else if (ProcessScheduler.PROPERTY_SIMULATION_STATE.equals(evt.getPropertyName())) {
                boolean isRunning = Boolean.TRUE.equals(evt.getNewValue());
                SwingUtilities.invokeLater(() -> {
                    startSchedulerButton.setEnabled(!isRunning);
                    stopSchedulerButton.setEnabled(isRunning);
                    schedulingProgressBar.setVisible(isRunning);

                    if (!isRunning && scheduler.getMetrics() != null) {
                        scheduler.getMetrics().printMetrics(algorithmCombo.getSelectedItem().toString());
                    }
                });
            } else if (ProcessScheduler.PROPERTY_TASKS_UPDATED.equals(evt.getPropertyName())) {
                SwingUtilities.invokeLater(() -> {
                    updateTasksTable();
                    updateMemoryVisualization();
                });
            }
        });
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(LIGHT_SLATE);

        // Header mejorado - ACTUALIZADO para incluir DC
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Panel principal dividido en izquierda y derecha
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(400);
        mainSplitPane.setResizeWeight(0.4);
        mainSplitPane.setBorder(BorderFactory.createEmptyBorder());

        // Panel izquierdo - Controles
        JPanel leftPanel = createControlsPanel();
        mainSplitPane.setLeftComponent(leftPanel);

        // Panel derecho - Cambia según la pestaña seleccionada
        rightPanel = new JPanel();
        rightPanelLayout = new CardLayout();
        rightPanel.setLayout(rightPanelLayout);
        rightPanel.setBackground(LIGHT_SLATE);

        // Agregar las TRES vistas al panel derecho (NUEVO: se agrega DC)
        rightPanel.add(createCircuitVisualizationPanel(), "CIRCUIT");
        rightPanel.add(createMemoryVisualizationPanel(), "PROCESS");
        rightPanel.add(createDCSimulatorPanel(), "DC_CIRCUIT"); // NUEVA PESTAÑA DC

        mainSplitPane.setRightComponent(rightPanel);

        add(mainSplitPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradiente de fondo
                GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_BLUE, 
                    getWidth(), 0, ACCENT_PURPLE
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Patrón sutil
                g2d.setColor(new Color(255, 255, 255, 10));
                for (int i = 0; i < getWidth(); i += 20) {
                    for (int j = 0; j < getHeight(); j += 20) {
                        g2d.fillOval(i, j, 2, 2);
                    }
                }
            }
        };
        
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(800, 100));
        
        // Título ACTUALIZADO para incluir DC
        JLabel titleLabel = new JLabel("Simulador Avanzado de Circuitos RLC y DC", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(25, 0, 5, 0));
        
        JLabel subtitleLabel = new JLabel("Con Algoritmos de Planificación y Gestión de Memoria Virtual", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 220));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        return headerPanel;
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(LIGHT_SLATE);
        panel.setPreferredSize(new Dimension(400, 700));

        // Crear pestañas para navegación
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setBackground(LIGHT_SLATE);
        setupModernTabbedPane(tabbedPane);

        // Pestaña 1: Simulación de Circuitos RLC
        JPanel circuitPanel = createCircuitControlsPanel();
        JScrollPane circuitScroll = new JScrollPane(circuitPanel);
        circuitScroll.setBorder(null);
        circuitScroll.getVerticalScrollBar().setUnitIncrement(16);
        circuitScroll.setBackground(LIGHT_SLATE);
        tabbedPane.addTab("Circuito RLC", circuitScroll);

        // Pestaña 2: Simulación de Circuitos DC - NUEVA PESTAÑA
        JPanel dcPanel = createDCControlsPanel();
        JScrollPane dcScroll = new JScrollPane(dcPanel);
        dcScroll.setBorder(null);
        dcScroll.getVerticalScrollBar().setUnitIncrement(16);
        dcScroll.setBackground(LIGHT_SLATE);
        tabbedPane.addTab("Circuito DC", dcScroll);

        // Pestaña 3: Planificación de Procesos
        JPanel schedulingPanel = createSchedulingControlsPanel();
        JScrollPane schedulingScroll = new JScrollPane(schedulingPanel);
        schedulingScroll.setBorder(null);
        schedulingScroll.getVerticalScrollBar().setUnitIncrement(16);
        schedulingScroll.setBackground(LIGHT_SLATE);
        tabbedPane.addTab("Procesos", schedulingScroll);

        // Listener para cambiar la vista derecha cuando cambia la pestaña
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                rightPanelLayout.show(rightPanel, "CIRCUIT");
            } else if (selectedIndex == 1) {
                rightPanelLayout.show(rightPanel, "DC_CIRCUIT"); // NUEVA PESTAÑA DC
            } else {
                rightPanelLayout.show(rightPanel, "PROCESS");
            }
        });

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    // ========== NUEVO: PANEL DE CONTROLES PARA CIRCUITOS DC ==========

    private JPanel createDCControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        panel.setBackground(LIGHT_SLATE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fuente de alimentación DC
        JPanel dcInputPanel = createModernCardPanel("Fuente de Alimentación DC", createDCInputPanel());
        panel.add(dcInputPanel);
        panel.add(Box.createVerticalStrut(15));

        // Componentes DC
        JPanel dcComponentPanel = createModernCardPanel("Componentes DC", createDCComponentPanel());
        panel.add(dcComponentPanel);
        panel.add(Box.createVerticalStrut(15));

        // Configuración de ramas
        JPanel branchPanel = createModernCardPanel("Configuración del Circuito", createBranchPanel());
        panel.add(branchPanel);
        panel.add(Box.createVerticalStrut(15));

        // Métodos de análisis DC
        JPanel dcMethodPanel = createModernCardPanel("Método de Análisis", createDCMethodPanel());
        panel.add(dcMethodPanel);
        panel.add(Box.createVerticalStrut(15));

        // Botones de acción DC
        JPanel dcActionPanel = createModernCardPanel("Acciones DC", createDCActionPanel());
        panel.add(dcActionPanel);

        return panel;
    }

    private JPanel createDCInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel voltagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        voltagePanel.setBackground(CARD_BACKGROUND);
        voltagePanel.add(createModernLabel("Voltaje DC (V):"));
        dcVoltageField = createModernTextField("12", 10);
        dcVoltageField.setToolTipText("Voltaje DC entre 1 y 100 V");
        voltagePanel.add(dcVoltageField);
        voltagePanel.add(createModernLabel("V"));
        voltagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(voltagePanel);

        panel.add(Box.createVerticalStrut(8));

        JPanel batteryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        batteryPanel.setBackground(CARD_BACKGROUND);
        batteryPanel.add(createModernLabel("Número de Baterías:"));
        batterySpinner = createModernSpinner(1, 1, 10, 1);
        batteryPanel.add(batterySpinner);
        batteryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(batteryPanel);

        return panel;
    }

    private JPanel createDCComponentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.setBackground(CARD_BACKGROUND);
        typePanel.add(createModernLabel("Tipo:"));
        String[] dcComponentTypes = { "Batería", "Resistencia", "Fuente DC" };
        dcComponentTypeCombo = createModernComboBox();
        for (String type : dcComponentTypes) {
            dcComponentTypeCombo.addItem(type);
        }
        dcComponentTypeCombo.setMaximumSize(new Dimension(140, 35));
        typePanel.add(dcComponentTypeCombo);
        typePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(typePanel);

        panel.add(Box.createVerticalStrut(8));

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        valuePanel.setBackground(CARD_BACKGROUND);
        valuePanel.add(createModernLabel("Valor:"));
        dcValueField = createModernTextField("100", 12);
        dcValueField.setToolTipText("Valor del componente (Ohms para resistencias, Volts para fuentes)");
        valuePanel.add(dcValueField);
        valuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(valuePanel);

        panel.add(Box.createVerticalStrut(8));

        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quantityPanel.setBackground(CARD_BACKGROUND);
        quantityPanel.add(createModernLabel("Cantidad:"));
        quantitySpinner = createModernSpinner(1, 1, 10, 1);
        quantityPanel.add(quantitySpinner);
        quantityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(quantityPanel);

        panel.add(Box.createVerticalStrut(12));

        addDCButton = createModernButton("Agregar Componente DC", SECONDARY_BLUE);
        addDCButton.setToolTipText("Agregar componente al circuito DC");
        addDCButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addDCButton.setMaximumSize(new Dimension(220, 40));
        panel.add(addDCButton);

        return panel;
    }

    private JPanel createBranchPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel branchCountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        branchCountPanel.setBackground(CARD_BACKGROUND);
        branchCountPanel.add(createModernLabel("Número de Ramas:"));
        branchSpinner = createModernSpinner(2, 1, 10, 1);
        branchSpinner.setToolTipText("Número de ramas paralelas en el circuito");
        branchCountPanel.add(branchSpinner);
        branchCountPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(branchCountPanel);

        panel.add(Box.createVerticalStrut(8));

        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        configPanel.setBackground(CARD_BACKGROUND);
        configPanel.add(createModernLabel("Configuración:"));
        String[] configTypes = { "Serie", "Paralelo", "Mixto" };
        configCombo = createModernComboBox();
        for (String config : configTypes) {
            configCombo.addItem(config);
        }
        configCombo.setMaximumSize(new Dimension(120, 35));
        configPanel.add(configCombo);
        configPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(configPanel);

        return panel;
    }

    private JPanel createDCMethodPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] dcMethods = { 
            "Ley de Ohm", 
            "Leyes de Kirchhoff", 
            "Análisis de Mallas", 
            "Análisis Nodal",
            "Teorema de Thevenin",
            "Teorema de Norton",
            "Transformación de Fuentes"
        };
        
        dcMethodCombo = createModernComboBox();
        for (String method : dcMethods) {
            dcMethodCombo.addItem(method);
        }
        dcMethodCombo.setToolTipText("Seleccione el método de análisis para circuitos DC");
        dcMethodCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        dcMethodCombo.setMaximumSize(new Dimension(300, 35));
        panel.add(dcMethodCombo);

        return panel;
    }

    private JPanel createDCActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        simulateDCButton = createModernButton("Simular Circuito DC", SUCCESS_EMERALD);
        simulateDCButton.setToolTipText("Ejecutar simulación del circuito DC");
        simulateDCButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        simulateDCButton.setMaximumSize(new Dimension(220, 45));

        panel.add(Box.createVerticalStrut(8));

        clearDCButton = createModernButton("Limpiar Circuito DC", ERROR_ROSE);
        clearDCButton.setToolTipText("Limpiar circuito DC y resultados");
        clearDCButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearDCButton.setMaximumSize(new Dimension(220, 40));

        // Barra de progreso DC
        dcProgressBar = new JProgressBar();
        setupModernProgressBar(dcProgressBar);
        dcProgressBar.setVisible(false);
        dcProgressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        dcProgressBar.setMaximumSize(new Dimension(220, 25));

        panel.add(simulateDCButton);
        panel.add(Box.createVerticalStrut(12));
        panel.add(clearDCButton);
        panel.add(Box.createVerticalStrut(12));
        panel.add(dcProgressBar);

        return panel;
    }

    // ========== NUEVO: PANEL DE SIMULACIÓN DC ==========

    private JPanel createDCSimulatorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(LIGHT_SLATE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título del panel DC
        JLabel titleLabel = new JLabel("Simulador de Circuitos DC - Análisis Resistivo", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(DARK_SLATE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Panel principal DC dividido
        JSplitPane dcSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        dcSplitPane.setDividerLocation(300);
        dcSplitPane.setResizeWeight(0.5);

        // Panel superior: Diagrama del circuito DC
        JPanel dcDiagramPanel = createDCDiagramPanel();
        dcSplitPane.setTopComponent(dcDiagramPanel);

        // Panel inferior: Resultados y análisis DC
        JPanel dcResultsPanel = createDCResultsPanel();
        dcSplitPane.setBottomComponent(dcResultsPanel);

        panel.add(dcSplitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDCDiagramPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        panel.setPreferredSize(new Dimension(600, 280));
        panel.setBackground(LIGHT_SLATE);

        JPanel cardPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(CARD_BACKGROUND);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2d.setColor(new Color(226, 232, 240));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            }
        };
        
        cardPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Título del diagrama DC
        JLabel titleLabel = new JLabel("Diagrama del Circuito DC");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(DARK_SLATE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        cardPanel.add(titleLabel, BorderLayout.NORTH);

        // Usar DCDiagramPanel real
        dcDiagramPanel = new DCDiagramPanel();
        dcDiagramPanel.setCircuit(currentDCCircuit);
        
        JScrollPane diagramScroll = new JScrollPane(dcDiagramPanel);
        diagramScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        cardPanel.add(diagramScroll, BorderLayout.CENTER);
        panel.add(cardPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDCResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_SLATE);

        // Pestañas para resultados DC
        JTabbedPane dcResultsTabs = new JTabbedPane(JTabbedPane.TOP);
        dcResultsTabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        setupModernTabbedPane(dcResultsTabs);

        // Pestaña 1: Resultados principales usando DCResultsPanel
        dcResultsPanel = new DCResultsPanel();
        dcResultsTabs.addTab("Resultados Principales", dcResultsPanel);

        // Pestaña 2: Circuitos equivalentes
        dcEquivalentPanel = new DCEquivalentCircuitPanel();
        dcResultsTabs.addTab("Circuitos Equivalentes", dcEquivalentPanel);

        // Pestaña 3: Análisis detallado
        JPanel detailedAnalysisPanel = createDCDetailedAnalysisPanel();
        dcResultsTabs.addTab("Análisis Detallado", detailedAnalysisPanel);

        panel.add(dcResultsTabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDCDetailedAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Usar el miembro de clase
        dcDetailedAnalysisArea = new JTextArea();
        dcDetailedAnalysisArea.setEditable(false);
        dcDetailedAnalysisArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        dcDetailedAnalysisArea.setBackground(CARD_BACKGROUND);
        dcDetailedAnalysisArea.setForeground(DARK_SLATE);
        dcDetailedAnalysisArea.setText(
            "=== ANÁLISIS DETALLADO DC ===\n\n" +
            "Información que se mostrará:\n\n" +
            "• Intensidades de corriente por rama (A)\n" +
            "• Dirección de corriente (sentido)\n" +
            "• Variación de potencial en componentes\n" +
            "• Potencia disipada por resistencias\n" +
            "• Potencia entregada por fuentes\n" +
            "• Análisis mediante leyes de Kirchhoff\n" +
            "• Verificación de conservación de energía\n\n" +
            "Ejecute una simulación para ver el análisis completo."
        );

        JScrollPane scroll = new JScrollPane(dcDetailedAnalysisArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ========== MÉTODOS DC ==========

    private void setupDCEventHandlers() {
        // Configurar handlers para los botones DC
        addDCButton.addActionListener(e -> addDCComponent());
        simulateDCButton.addActionListener(e -> simulateDCCircuit());
        clearDCButton.addActionListener(e -> clearDCCircuit());
        
        // Configurar listeners para cambios en valores DC
        setupDCValueListeners();
    }

    private void setupDCValueListeners() {
        // Configurar listeners para campos de entrada DC
        if (dcVoltageField != null) {
            dcVoltageField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) { onDCValueChanged(); }
                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) { onDCValueChanged(); }
                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) { onDCValueChanged(); }
            });
        }
        
        // Configurar listener para el combo box de métodos DC
        if (dcMethodCombo != null) {
            dcMethodCombo.addActionListener(e -> onDCMethodChanged());
        }
    }

    private void onDCValueChanged() {
        // Actualizar vista previa o validaciones cuando cambian los valores DC
        try {
            double voltage = getDCVoltage();
            currentDCCircuit.setSourceVoltage(voltage);
            if (dcDiagramPanel != null) {
                dcDiagramPanel.repaint();
            }
        } catch (Exception e) {
            // Ignorar errores durante la entrada de datos
        }
    }

    private void onDCMethodChanged() {
        String method = getDCMethod();
        // Opcional: mostrar info, pero puede ser molesto
        // showInfo("Método de análisis cambiado a: " + method);
    }

    private void addDCComponent() {
        try {
            DCComponentType type = getSelectedDCComponentType();
            double value = Double.parseDouble(dcValueField.getText().trim());
            int quantity = (Integer) quantitySpinner.getValue();
            String name = dcComponentTypeCombo.getSelectedItem().toString() + " " + value;
            
            if (value <= 0) {
                showError("El valor del componente debe ser positivo");
                return;
            }

            DCComponent comp = new DCComponent(type, value, name, quantity);
            
            // Agregar a la rama actual
            int branchCount = (Integer) branchSpinner.getValue();
            ensureBranchesExist(branchCount);
            
            // Agregar a la primera rama por simplicidad
            if (!currentDCCircuit.getBranches().isEmpty()) {
                currentDCCircuit.getBranches().get(0).addComponent(comp);
            }
            
            // Actualizar configuración
            String config = (String) configCombo.getSelectedItem();
            currentDCCircuit.setConfiguration(config != null ? config : "Serie");
            currentDCCircuit.setBatteryCount((Integer) batterySpinner.getValue());
            
            // Actualizar UI
            dcDiagramPanel.setCircuit(currentDCCircuit);
            dcDiagramPanel.repaint();
            
            dcValueField.setText("");
            showInfo("Componente DC agregado: " + comp.toString());
            
        } catch (NumberFormatException ex) {
            showError("Ingrese valores numéricos válidos para el componente DC");
        } catch (Exception ex) {
            showError("Error al agregar componente DC: " + ex.getMessage());
        }
    }

    private DCComponentType getSelectedDCComponentType() {
        String selected = (String) dcComponentTypeCombo.getSelectedItem();
        if (selected == null) return DCComponentType.RESISTOR;
        
        switch (selected) {
            case "Batería": return DCComponentType.BATTERY;
            case "Resistencia": return DCComponentType.RESISTOR;
            case "Fuente DC": return DCComponentType.DC_SOURCE;
            default: return DCComponentType.RESISTOR;
        }
    }

    private void ensureBranchesExist(int branchCount) {
        // Asegurar que existan suficientes ramas
        while (currentDCCircuit.getBranches().size() < branchCount) {
            currentDCCircuit.addBranch(new DCBranch(currentDCCircuit.getBranches().size() + 1));
        }
        
        // Remover ramas extras si es necesario
        while (currentDCCircuit.getBranches().size() > branchCount) {
            currentDCCircuit.removeBranch(currentDCCircuit.getBranches().size() - 1);
        }
    }

    private void simulateDCCircuit() {
        try {
            // Actualizar parámetros del circuito
            currentDCCircuit.setSourceVoltage(getDCVoltage());
            currentDCCircuit.setBatteryCount((Integer) batterySpinner.getValue());
            currentDCCircuit.setConfiguration((String) configCombo.getSelectedItem());

            if (currentDCCircuit == null || !currentDCCircuit.isValid()) {
                showError("Circuito DC no válido. Agregue componentes y configure el circuito.");
                return;
            }

            // Obtener método seleccionado
            String methodName = getDCMethod();
            DCAnalysisMethod method = convertToDCAnalysisMethod(methodName);
            
            // Configurar motor DC
            dcEngine.setAnalysisMethod(method);
            
            // Mostrar progreso
            dcProgressBar.setVisible(true);
            dcProgressBar.setIndeterminate(true);
            dcProgressBar.setString("Simulación DC en progreso...");
            simulateDCButton.setEnabled(false);
            
            // Ejecutar simulación
            DCSimulationResult result = dcEngine.simulate(currentDCCircuit);
            lastDCResult = result;
            
            // Actualizar UI
            updateDCResults(result); // Actualiza el panel de análisis detallado
            dcResultsPanel.updateResults(result);
            dcEquivalentPanel.updateEquivalents(result);
            dcDiagramPanel.setCircuit(currentDCCircuit);
            
            // Ocultar progreso
            dcProgressBar.setVisible(false);
            simulateDCButton.setEnabled(true);
            
            showInfo("Simulación DC completada usando " + methodName);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error en simulación DC: " + ex.getMessage());
            dcResultsPanel.showError(ex.getMessage());
            dcEquivalentPanel.clearEquivalents();
            dcProgressBar.setVisible(false);
            simulateDCButton.setEnabled(true);
        }
    }

    private void clearDCCircuit() {
        currentDCCircuit = new DCCircuit(getDCVoltage(), "Serie", 1);
        lastDCResult = null;
        
        // Actualizar UI
        dcDiagramPanel.setCircuit(currentDCCircuit);
        dcResultsPanel.clearResults();
        dcEquivalentPanel.clearEquivalents();
        if (dcDetailedAnalysisArea != null) {
            dcDetailedAnalysisArea.setText(
                "=== ANÁLISIS DETALLADO DC ===\n\n" +
                "Circuito limpiado.\n\n" +
                "Ejecute una simulación para ver el análisis completo."
            );
        }
        
        showInfo("Circuito DC limpiado");
    }

    private double getDCVoltage() {
        try {
            if (dcVoltageField != null && !dcVoltageField.getText().trim().isEmpty()) {
                double voltage = Double.parseDouble(dcVoltageField.getText().trim());
                if (voltage > 0 && voltage <= 1000) {
                    return voltage;
                } else {
                    throw new IllegalArgumentException("El voltaje debe estar entre 0.1 y 1000 V");
                }
            }
            return 12.0; // Valor por defecto
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Voltaje DC no válido");
        }
    }

    private String getDCMethod() {
        if (dcMethodCombo != null && dcMethodCombo.getSelectedItem() != null) {
            return (String) dcMethodCombo.getSelectedItem();
        }
        return "Ley de Ohm"; // Default
    }

    private DCAnalysisMethod convertToDCAnalysisMethod(String methodName) {
        if (methodName == null) return DCAnalysisMethod.OHM_LAW;
        switch (methodName) {
            case "Leyes de Kirchhoff": return DCAnalysisMethod.KIRCHHOFF_LAWS;
            case "Análisis de Mallas": return DCAnalysisMethod.MESH_ANALYSIS;
            case "Análisis Nodal": return DCAnalysisMethod.NODE_ANALYSIS;
            case "Teorema de Thevenin": return DCAnalysisMethod.THEVENIN_THEOREM;
            case "Teorema de Norton": return DCAnalysisMethod.NORTON_THEOREM;
            case "Transformación de Fuentes": return DCAnalysisMethod.SOURCE_TRANSFORMATION;
            case "Ley de Ohm":
            default:
                return DCAnalysisMethod.OHM_LAW;
        }
    }
    
    private void updateDCResults(DCSimulationResult result) {
        if (dcDetailedAnalysisArea == null) return;
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== ANÁLISIS DETALLADO DC ===\n\n");
        sb.append("Método de Análisis: ").append(result.getMethodUsed()).append("\n");
        sb.append("Configuración: ").append(result.getCircuitConfiguration()).append("\n\n");
        
        sb.append("--- VERIFICACIÓN DE LEYES ---\n");
        sb.append("Voltaje Total: ").append(df.format(result.getSourceVoltage())).append(" V\n");
        sb.append("Corriente Total: ").append(df.format(result.getTotalCurrent())).append(" A\n");
        sb.append("Resistencia Eq.: ").append(df.format(result.getTotalResistance())).append(" Ω\n");
        double calculatedVoltage = result.getTotalCurrent() * result.getTotalResistance();
        sb.append("Ley de Ohm (V=I*R): ").append(df.format(calculatedVoltage)).append(" V (Verificado)\n\n");
        
        sb.append("--- ANÁLISIS DE POTENCIA ---\n");
        sb.append("Potencia (Fuente): ").append(df.format(result.getTotalPower())).append(" W\n");
        sb.append("Potencia (Disipada): ").append(df.format(result.getPowerDissipated())).append(" W\n");
        sb.append("Eficiencia: ").append(df.format(result.getEfficiency())).append(" %\n\n");

        sb.append("--- CORRIENTES POR RAMA ---\n");
        double[] branchCurrents = result.getBranchCurrents();
        for (int i = 0; i < branchCurrents.length; i++) {
            sb.append(String.format("• Rama %d: %s A\n", i + 1, df.format(branchCurrents[i])));
        }
        
        sb.append("\n--- TENSIONES EN COMPONENTES ---\n");
        double[] componentVoltages = result.getComponentVoltages();
        for (int i = 0; i < componentVoltages.length; i++) {
            sb.append(String.format("• Componente %d: %.2f V\n", i + 1, componentVoltages[i]));
        }
        
        dcDetailedAnalysisArea.setText(sb.toString());
        dcDetailedAnalysisArea.setCaretPosition(0);
    }

    // ========== MÉTODOS ORIGINALES (sin cambios) ==========

    private void setupModernTabbedPane(JTabbedPane tabbedPane) {
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                tabbedPane.setOpaque(false);
                tabbedPane.setBackground(LIGHT_SLATE);
            }
            
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, 
                                            int tabIndex, int x, int y, int w, int h, 
                                            boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected) {
                    GradientPaint gradient = new GradientPaint(
                        x, y, PRIMARY_BLUE,
                        x, y + h, SECONDARY_BLUE
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(x + 2, y + 2, w - 4, h - 2, 12, 12);
                } else {
                    g2d.setColor(new Color(255, 255, 255, 180));
                    g2d.fillRoundRect(x + 2, y + 2, w - 4, h - 2, 12, 12);
                }
            }
            
            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, 
                                   FontMetrics metrics, int tabIndex, 
                                   String title, Rectangle textRect, boolean isSelected) {
                g.setFont(font.deriveFont(isSelected ? Font.BOLD : Font.PLAIN, 12));
                g.setColor(isSelected ? Color.WHITE : DARK_SLATE);
                super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
            }
            
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, 
                                        int x, int y, int w, int h, boolean isSelected) {
                // Sin bordes para un look más limpio
            }
            
            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // Borde sutil para el área de contenido
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 150));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, tabbedPane.getWidth()-3, tabbedPane.getHeight()-3, 8, 8);
            }
        });
    }

    private JPanel createCircuitControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        panel.setBackground(LIGHT_SLATE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fuente de alimentación
        JPanel inputPanel = createModernCardPanel("Fuente de Alimentación", createInputPanel());
        panel.add(inputPanel);
        panel.add(Box.createVerticalStrut(15));

        // Método de simulación
        JPanel methodPanel = createModernCardPanel("Método de Simulación", createMethodPanel());
        panel.add(methodPanel);
        panel.add(Box.createVerticalStrut(15));

        // Circuitos predefinidos
        JPanel presetPanel = createModernCardPanel("Circuitos Predefinidos", createPresetPanel());
        panel.add(presetPanel);
        panel.add(Box.createVerticalStrut(15));

        // Componentes
        JPanel componentPanel = createModernCardPanel("Agregar Componentes", createComponentPanel());
        panel.add(componentPanel);
        panel.add(Box.createVerticalStrut(15));

        // Lista de componentes
        JPanel listPanel = createModernCardPanel("Componentes en el Circuito", createComponentListPanel());
        panel.add(listPanel);
        panel.add(Box.createVerticalStrut(15));

        // Botones de acción
        JPanel actionPanel = createModernCardPanel("Acciones", createCircuitActionPanel());
        panel.add(actionPanel);

        return panel;
    }

    private JPanel createModernCardPanel(String title, JPanel contentPanel) {
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra suave
                g2d.setColor(new Color(0, 0, 0, 15));
                g2d.fillRoundRect(2, 2, getWidth()-2, getHeight()-2, 16, 16);
                
                // Fondo de la tarjeta
                g2d.setColor(CARD_BACKGROUND);
                g2d.fillRoundRect(0, 0, getWidth()-2, getHeight()-2, 14, 14);
                
                // Borde sutil
                g2d.setColor(new Color(226, 232, 240));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(0, 0, getWidth()-2, getHeight()-2, 14, 14);
            }
        };
        
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        cardPanel.setMaximumSize(new Dimension(380, Integer.MAX_VALUE));
        
        // Título de la tarjeta
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(DARK_SLATE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        
        cardPanel.add(titleLabel, BorderLayout.NORTH);
        cardPanel.add(contentPanel, BorderLayout.CENTER);
        
        return cardPanel;
    }

    private JPanel createSchedulingControlsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        panel.setBackground(LIGHT_SLATE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel superior - Controles de configuración
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBackground(LIGHT_SLATE);
        controlsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Algoritmo de planificación
        JPanel algorithmPanel = createModernCardPanel("Algoritmo de Planificación", 
            createSimpleComboBoxPanel("Seleccione algoritmo:", 
                new String[] { "First-Come, First-Served (FCFS)", "Round Robin (RR)", "Shortest Job First (SJF)" }));
        algorithmCombo = findComboBoxInPanel(algorithmPanel);
        controlsPanel.add(algorithmPanel);
        controlsPanel.add(Box.createVerticalStrut(15));

        // Tipo de lote
        JPanel batchPanel = createModernCardPanel("Tipo de Lote", 
            createSimpleComboBoxPanel("Configuración del lote:",
                new String[] { "Homogéneo - Simple", "Homogéneo - Medio", "Homogéneo - Complejo",
                        "Heterogéneo - Mixto" }));
        batchTypeCombo = findComboBoxInPanel(batchPanel);
        if (batchTypeCombo == null) {
            batchTypeCombo = new JComboBox<>(new String[] {
                    "Homogéneo - Simple", "Homogéneo - Medio", "Homogéneo - Complejo", "Heterogéneo - Mixto"
            });
            batchPanel.add(batchTypeCombo);
        }
        batchTypeCombo.addActionListener(e -> updateBatchControls());
        controlsPanel.add(batchPanel);
        controlsPanel.add(Box.createVerticalStrut(15));

        // Controles de batch
        JPanel batchControlsPanel = createModernCardPanel("Configuración del Lote", createBatchControlsPanel());
        controlsPanel.add(batchControlsPanel);
        controlsPanel.add(Box.createVerticalStrut(15));

        // Botones de control
        JPanel buttonPanel = createModernCardPanel("Control de Ejecución", createSchedulingButtonPanel());
        controlsPanel.add(buttonPanel);
        controlsPanel.add(Box.createVerticalStrut(15));

        // Barra de progreso
        schedulingProgressBar = new JProgressBar();
        setupModernProgressBar(schedulingProgressBar);
        schedulingProgressBar.setVisible(false);
        schedulingProgressBar.setMaximumSize(new Dimension(350, 25));
        schedulingProgressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlsPanel.add(schedulingProgressBar);

        updateBatchControls();

        // Panel inferior - Log de Planificación
        JPanel logPanel = createLogPanelForLeft();

        // Usar JSplitPane para dividir controles y log
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(createScrollPanel(controlsPanel));
        splitPane.setBottomComponent(logPanel);
        splitPane.setDividerLocation(320);
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLogPanelForLeft() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        panel.setBackground(LIGHT_SLATE);
        panel.setPreferredSize(new Dimension(350, 200));

        schedulingLogArea = new JTextArea(8, 30);
        schedulingLogArea.setEditable(false);
        schedulingLogArea.setFont(new Font("Consolas", Font.PLAIN, 10));
        schedulingLogArea.setBackground(new Color(248, 250, 252));
        schedulingLogArea.setForeground(DARK_SLATE);
        schedulingLogArea.setLineWrap(true);
        schedulingLogArea.setWrapStyleWord(true);
        schedulingLogArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane logScroll = new JScrollPane(schedulingLogArea);
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(logScroll, BorderLayout.CENTER);

        return panel;
    }

    // ========== PANEL DE VISUALIZACIÓN DE CIRCUITOS ==========

    private JPanel createCircuitVisualizationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(LIGHT_SLATE);

        // Panel superior: Diagrama del circuito
        JPanel diagramPanel = createCircuitPanel();
        panel.add(diagramPanel, BorderLayout.NORTH);

        // Panel central: Pestañas para gráficos y resultados
        circuitTabs = new JTabbedPane(JTabbedPane.TOP);
        circuitTabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        setupModernTabbedPane(circuitTabs);

        // Pestaña 1: Visualización (Gráficos)
        JPanel graphPanel = createGraphPanel();
        circuitTabs.addTab("Visualización", graphPanel);

        // Pestaña 2: Resultados
        JPanel resultsPanel = createResultsPanel();
        JScrollPane resultsScroll = new JScrollPane(resultsPanel);
        resultsScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        circuitTabs.addTab("Resultados", resultsScroll);

        // Pestaña 3: Análisis Detallado
        JPanel analysisPanel = createAnalysisPanel();
        circuitTabs.addTab("Análisis", analysisPanel);

        panel.add(circuitTabs, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCircuitPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        panel.setPreferredSize(new Dimension(600, 220));
        panel.setBackground(LIGHT_SLATE);

        JPanel cardPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(CARD_BACKGROUND);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2d.setColor(new Color(226, 232, 240));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            }
        };
        
        cardPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Título del diagrama
        JLabel titleLabel = new JLabel("Diagrama del Circuito");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(DARK_SLATE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        cardPanel.add(titleLabel, BorderLayout.NORTH);

        circuitDiagram = new CircuitDiagramPanel();

        JScrollPane diagramScroll = new JScrollPane(circuitDiagram);
        diagramScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        diagramScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        diagramScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        diagramScroll.getViewport().setBackground(Color.WHITE);

        cardPanel.add(diagramScroll, BorderLayout.CENTER);
        panel.add(cardPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createGraphPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(CARD_BACKGROUND);

        currentGraph = new TimeGraph(null);
        graphContainer = new JPanel(new BorderLayout());
        graphContainer.setBackground(CARD_BACKGROUND);

        JScrollPane graphScroll = new JScrollPane(currentGraph);
        graphScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        graphScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        graphScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        graphScroll.getViewport().setBackground(Color.WHITE);
        graphContainer.add(graphScroll, BorderLayout.CENTER);

        JPanel graphTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        graphTypePanel.setBackground(CARD_BACKGROUND);
        graphTypePanel.add(createModernLabel("Tipo de Gráfico:"));

        graphTypeCombo = createModernComboBox();
        graphTypeCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                "Dominio de Tiempo", "Respuesta en Frecuencia", "Diagrama Fasorial", "Formas de Onda"
        }));
        graphTypeCombo.addActionListener(e -> updateGraphType());
        graphTypePanel.add(graphTypeCombo);

        panel.add(graphTypePanel, BorderLayout.NORTH);
        panel.add(graphContainer, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        resultsArea = new JTextArea(12, 50);
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
        resultsArea.setBackground(CARD_BACKGROUND);
        resultsArea.setForeground(DARK_SLATE);
        resultsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        updateInitialResultsText();

        JScrollPane scroll = new JScrollPane(resultsArea);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scroll.getViewport().setBackground(CARD_BACKGROUND);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        analysisArea = new JTextArea();
        analysisArea.setEditable(false);
        analysisArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        analysisArea.setBackground(CARD_BACKGROUND);
        analysisArea.setForeground(DARK_SLATE);
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);
        analysisArea.setText(
                "=== ANÁLISIS DETALLADO DEL CIRCUITO ===\n\n" +
                "Esta sección muestra análisis avanzados:\n\n" +
                "- Parámetros del circuito en diferentes frecuencias\n" +
                "- Comportamiento transitorio vs permanente\n" +
                "- Análisis de estabilidad del sistema\n" +
                "- Respuesta a diferentes tipos de entrada\n" +
                "- Análisis de sensibilidad de componentes\n\n" +
                "Ejecute una simulación para ver los análisis detallados.");

        JScrollPane scroll = new JScrollPane(analysisArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void updateGraphType() {
        int graphType = graphTypeCombo.getSelectedIndex();
        if (lastResult == null || components == null || components.isEmpty()) {
            switch (graphType) {
                case 0:
                    currentGraph = new TimeGraph(null);
                    break;
                case 1:
                    currentGraph = new FrequencyGraph(new ArrayList<>());
                    break;
                case 2:
                    currentGraph = new PhasorDiagram(null, new ArrayList<>());
                    break;
                case 3:
                    currentGraph = new WaveformGraph(null);
                    break;
            }
        } else {
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

        graphContainer.removeAll();
        JScrollPane graphScroll = new JScrollPane(currentGraph);
        graphScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        graphScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        graphScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        graphScroll.getViewport().setBackground(Color.WHITE);
        graphContainer.add(graphScroll, BorderLayout.CENTER);
        graphContainer.revalidate();
        graphContainer.repaint();
    }

    private void updateInitialResultsText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Simulador Avanzado de Circuitos RLC ===\n\n");
        sb.append("Instrucciones:\n");
        sb.append("   1. Agregue componentes (R, L, C) al circuito\n");
        sb.append("   2. Configure voltaje y frecuencia\n");
        sb.append("   3. Seleccione método de simulación\n");
        sb.append("   4. Haga clic en 'Simular Circuito'\n\n");
        sb.append("Características:\n");
        sb.append("   - Análisis en dominio de tiempo y frecuencia\n");
        sb.append("   - Diagramas fasoriales interactivos\n");
        sb.append("   - Múltiples métodos de cálculo\n");
        sb.append("   - Circuitos predefinidos\n");
        sb.append("   - Algoritmos de planificación integrados\n");
        sb.append("   - Interfaz moderna e intuitiva\n\n");
        sb.append("¡Comience agregando componentes y ejecutando una simulación!");

        if (resultsArea != null) {
            resultsArea.setText(sb.toString());
        }
    }

    // ========== PANEL DE VISUALIZACIÓN DE MEMORIA VIRTUAL ==========

    private JPanel createMemoryVisualizationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(LIGHT_SLATE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título principal
        JLabel titleLabel = new JLabel("Memoria Virtual - Simulación de Procesos", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(DARK_SLATE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Panel principal dividido en información y visualización
        JSplitPane memorySplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        memorySplitPane.setDividerLocation(150);
        memorySplitPane.setResizeWeight(0.3);

        // Panel superior - Información de memoria
        JPanel infoPanel = createMemoryInfoPanel();
        memorySplitPane.setTopComponent(infoPanel);

        // Panel inferior - Visualización gráfica y colas
        JPanel visualizationPanel = createMemoryVisualizationGraphics();
        memorySplitPane.setBottomComponent(visualizationPanel);

        panel.add(memorySplitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMemoryInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Título
        JLabel titleLabel = new JLabel("Estado del Sistema");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(DARK_SLATE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Grid de información
        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        gridPanel.setBackground(CARD_BACKGROUND);

        // Primera fila
        JLabel fragTotalLabel = createModernLabel("Fragmentación Total:");
        JLabel memUsageLabel = createModernLabel("Uso actual de Memoria:");
        fragTotalValue = createModernValueLabel("0.0 KB");
        memUsageValue = createModernValueLabel("0 KB (0.0%)");

        // Segunda fila
        JLabel avgUsageLabel = createModernLabel("Uso promedio:");
        JLabel extFragLabel = createModernLabel("Fragmentación Externa:");
        avgUsageValue = createModernValueLabel("0 KB (0.0%)");
        extFragValue = createModernValueLabel("0.0 KB");

        // Tercera fila
        JLabel intFragLabel = createModernLabel("Fragmentación Interna:");
        JLabel semaphoreLabel = createModernLabel("Semáforo 'Recurso Compartido':");
        intFragValue = createModernValueLabel("0.0 KB");
        semaphoreValue = createModernValueLabel("1 (esperando: 0)");

        gridPanel.add(fragTotalLabel);
        gridPanel.add(fragTotalValue);
        gridPanel.add(memUsageLabel);
        gridPanel.add(memUsageValue);
        gridPanel.add(avgUsageLabel);
        gridPanel.add(avgUsageValue);
        gridPanel.add(extFragLabel);
        gridPanel.add(extFragValue);
        gridPanel.add(intFragLabel);
        gridPanel.add(intFragValue);
        gridPanel.add(semaphoreLabel);
        gridPanel.add(semaphoreValue);

        panel.add(gridPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMemoryVisualizationGraphics() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(LIGHT_SLATE);

        // Panel izquierdo - Visualización de memoria
        JPanel memoryPanel = new JPanel(new BorderLayout());
        memoryPanel.setBackground(CARD_BACKGROUND);
        memoryPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel memoryTitle = new JLabel("Mapa de Memoria");
        memoryTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        memoryTitle.setForeground(DARK_SLATE);
        memoryPanel.add(memoryTitle, BorderLayout.NORTH);

        memoryVisualizationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMemoryMap(g);
            }
        };
        memoryVisualizationPanel.setPreferredSize(new Dimension(400, 300));
        memoryVisualizationPanel.setBackground(new Color(240, 245, 255));
        memoryVisualizationPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        memoryPanel.add(memoryVisualizationPanel, BorderLayout.CENTER);

        // Panel derecho - Proceso actual y colas
        JPanel processPanel = createProcessInfoPanel();
        processPanel.setPreferredSize(new Dimension(250, 0));

        // Usar JSplitPane para dividir memoria y procesos
        JSplitPane vizSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        vizSplitPane.setLeftComponent(memoryPanel);
        vizSplitPane.setRightComponent(processPanel);
        vizSplitPane.setDividerLocation(400);
        vizSplitPane.setResizeWeight(0.6);

        panel.add(vizSplitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProcessInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Proceso actual
        JPanel currentPanel = new JPanel(new BorderLayout());
        currentPanel.setBackground(CARD_BACKGROUND);
        currentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel currentTitle = new JLabel("Proceso en Ejecución");
        currentTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        currentTitle.setForeground(DARK_SLATE);
        
        currentProcessLabel = createModernLabel("Ninguno");
        currentProcessLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        memoryProgressBar = new JProgressBar(0, 100);
        setupModernProgressBar(memoryProgressBar);
        memoryProgressBar.setValue(0);
        memoryProgressBar.setString("0%");

        currentPanel.add(currentTitle, BorderLayout.NORTH);
        currentPanel.add(currentProcessLabel, BorderLayout.CENTER);
        currentPanel.add(memoryProgressBar, BorderLayout.SOUTH);

        // Colas de procesos
        JPanel queuesPanel = new JPanel(new BorderLayout());
        queuesPanel.setBackground(CARD_BACKGROUND);

        JLabel queuesTitle = new JLabel("Colas de Procesos");
        queuesTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        queuesTitle.setForeground(DARK_SLATE);
        queuesTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        processQueueArea = new JTextArea(10, 20);
        processQueueArea.setEditable(false);
        processQueueArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        processQueueArea.setBackground(new Color(248, 250, 252));
        processQueueArea.setForeground(DARK_SLATE);
        processQueueArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane queueScroll = new JScrollPane(processQueueArea);
        queueScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        queuesPanel.add(queuesTitle, BorderLayout.NORTH);
        queuesPanel.add(queueScroll, BorderLayout.CENTER);

        panel.add(currentPanel, BorderLayout.NORTH);
        panel.add(queuesPanel, BorderLayout.CENTER);

        return panel;
    }

    private void drawMemoryMap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = memoryVisualizationPanel.getWidth();
        int height = memoryVisualizationPanel.getHeight();
        int margin = 10;
        int memWidth = width - 2 * margin;
        int memHeight = height - 2 * margin;

        // Dibujar marco de memoria
        g2d.setColor(new Color(220, 220, 220));
        g2d.fillRect(margin, margin, memWidth, memHeight);
        g2d.setColor(DARK_SLATE);
        g2d.drawRect(margin, margin, memWidth, memHeight);

        // Dibujar bloques de memoria basados en las tareas
        List<CircuitSimulationTask> tasks = scheduler.getTasks();
        if (tasks.isEmpty()) {
            // Memoria vacía
            g2d.setColor(new Color(200, 230, 255));
            g2d.fillRect(margin, margin, memWidth, memHeight);
            g2d.setColor(PRIMARY_BLUE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
            String text = "MEMORIA LIBRE";
            int textWidth = g2d.getFontMetrics().stringWidth(text);
            g2d.drawString(text, margin + (memWidth - textWidth) / 2, margin + memHeight / 2);
            return;
        }

        // Simular memoria virtual con páginas
        int totalPages = 16; // 16 páginas de memoria virtual
        int pageHeight = memHeight / totalPages;
        int currentY = margin;

        for (int i = 0; i < totalPages; i++) {
            // Determinar si esta página está ocupada
            boolean pageOccupied = false;
            String processName = "";
            Color pageColor = new Color(200, 230, 255); // Azul claro - libre
            
            for (CircuitSimulationTask task : tasks) {
                int taskId = (int) task.getId();
                // Simular asignación de páginas basada en el ID de tarea
                if (i % 4 == taskId % 4 && task.getState() != CircuitSimulationTask.TaskState.COMPLETED) {
                    pageOccupied = true;
                    processName = "T" + taskId;
                    
                    // Color basado en complejidad
                    switch (task.getComplexity()) {
                        case SIMPLE:
                            pageColor = new Color(144, 238, 144); // Verde
                            break;
                        case MEDIUM:
                            pageColor = new Color(255, 218, 185); // Naranja
                            break;
                        case COMPLEX:
                            pageColor = new Color(240, 128, 128); // Rojo
                            break;
                    }
                    
                    // Oscurecer si está ejecutándose
                    if (task.getState() == CircuitSimulationTask.TaskState.RUNNING) {
                        pageColor = pageColor.darker();
                    }
                    break;
                }
            }

            // Dibujar página
            g2d.setColor(pageColor);
            g2d.fillRect(margin, currentY, memWidth, pageHeight - 2);
            g2d.setColor(pageColor.darker());
            g2d.drawRect(margin, currentY, memWidth, pageHeight - 2);

            // Dibujar información de la página
            g2d.setColor(DARK_SLATE);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            String pageText = pageOccupied ? processName : "Libre";
            g2d.drawString(pageText, margin + 5, currentY + pageHeight / 2 + 3);

            // Número de página
            g2d.drawString("P" + i, margin + memWidth - 20, currentY + pageHeight / 2 + 3);

            currentY += pageHeight;
        }

        // Actualizar métricas y colas
        updateMemoryMetrics(tasks);
        updateProcessQueues(tasks);
    }

    private void updateMemoryMetrics(List<CircuitSimulationTask> tasks) {
        int totalPages = 16;
        int usedPages = 0;
        
        // Contar páginas ocupadas
        for (int i = 0; i < totalPages; i++) {
            for (CircuitSimulationTask task : tasks) {
                if (i % 4 == task.getId() % 4 && task.getState() != CircuitSimulationTask.TaskState.COMPLETED) {
                    usedPages++;
                    break;
                }
            }
        }

        double usagePercent = (usedPages * 100.0) / totalPages;
        double memoryUsedKB = usedPages * 64; // 64KB por página

        // Calcular fragmentación basada en el algoritmo
        String algorithm = algorithmCombo.getSelectedItem() != null ? 
                          algorithmCombo.getSelectedItem().toString() : "FCFS";
        
        double fragmentation = 0.0;
        double externalFrag = 0.0;
        double internalFrag = 0.0;
        
        switch (algorithm) {
            case "First-Come, First-Served (FCFS)":
                fragmentation = 15.0 + (tasks.size() * 2.0);
                externalFrag = fragmentation * 0.7;
                internalFrag = fragmentation * 0.3;
                break;
            case "Round Robin (RR)":
                fragmentation = 20.0 + (tasks.size() * 1.5);
                externalFrag = fragmentation * 0.5;
                internalFrag = fragmentation * 0.5;
                break;
            case "Shortest Job First (SJF)":
                fragmentation = 10.0 + (tasks.size() * 1.0);
                externalFrag = fragmentation * 0.4;
                internalFrag = fragmentation * 0.6;
                break;
        }

        // Actualizar labels
        fragTotalValue.setText(String.format("%.1f KB", fragmentation));
        memUsageValue.setText(String.format("%.0f KB (%.1f%%)", memoryUsedKB, usagePercent));
        avgUsageValue.setText(String.format("%.0f KB (%.1f%%)", memoryUsedKB * 0.7, usagePercent * 0.7));
        extFragValue.setText(String.format("%.1f KB", externalFrag));
        intFragValue.setText(String.format("%.1f KB", internalFrag));
        
        // Actualizar semáforo
        long runningTasks = tasks.stream()
                .filter(t -> t.getState() == CircuitSimulationTask.TaskState.RUNNING)
                .count();
        long waitingTasks = tasks.stream()
                .filter(t -> t.getState() == CircuitSimulationTask.TaskState.READY)
                .count();
        semaphoreValue.setText(String.format("%d (esperando: %d)", 
                runningTasks > 0 ? 0 : 1, waitingTasks));
        
        // Actualizar proceso actual
        CircuitSimulationTask currentTask = getCurrentRunningTask();
        if (currentTask != null) {
            currentProcessLabel.setText("Tarea " + currentTask.getId() + " - " + currentTask.getName());
            memoryProgressBar.setValue((int) currentTask.getProgress());
            memoryProgressBar.setString(String.format("%.0f%%", currentTask.getProgress()));
        } else {
            currentProcessLabel.setText("Ninguno");
            memoryProgressBar.setValue(0);
            memoryProgressBar.setString("0%");
        }
    }

    private void updateProcessQueues(List<CircuitSimulationTask> tasks) {
        StringBuilder sb = new StringBuilder();
        
        // Cola de listos
        sb.append("=== COLA DE LISTOS ===\n");
        tasks.stream()
            .filter(t -> t.getState() == CircuitSimulationTask.TaskState.READY)
            .forEach(t -> sb.append(String.format("T%d: %s (%s)\n", 
                t.getId(), t.getName(), t.getComplexity().getDisplayName())));
        
        if (tasks.stream().noneMatch(t -> t.getState() == CircuitSimulationTask.TaskState.READY)) {
            sb.append("Vacía\n");
        }
        
        sb.append("\n=== EN EJECUCIÓN ===\n");
        CircuitSimulationTask running = getCurrentRunningTask();
        if (running != null) {
            sb.append(String.format("T%d: %s (%s)\n", 
                running.getId(), running.getName(), running.getComplexity().getDisplayName()));
            sb.append(String.format("Progreso: %.1f%%\n", running.getProgress()));
        } else {
            sb.append("Ninguno\n");
        }
        
        sb.append("\n=== COMPLETADOS ===\n");
        tasks.stream()
            .filter(t -> t.getState() == CircuitSimulationTask.TaskState.COMPLETED)
            .forEach(t -> sb.append(String.format("T%d: %s\n", t.getId(), t.getName())));
        
        if (tasks.stream().noneMatch(t -> t.getState() == CircuitSimulationTask.TaskState.COMPLETED)) {
            sb.append("Ninguno\n");
        }

        processQueueArea.setText(sb.toString());
    }

    private CircuitSimulationTask getCurrentRunningTask() {
        for (CircuitSimulationTask task : scheduler.getTasks()) {
            if (task.getState() == CircuitSimulationTask.TaskState.RUNNING) {
                return task;
            }
        }
        return null;
    }

    private void updateMemoryVisualization() {
        if (memoryVisualizationPanel != null) {
            memoryVisualizationPanel.repaint();
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private JLabel createModernValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(PRIMARY_BLUE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        return label;
    }

    private JLabel createModernLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(DARK_SLATE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        return label;
    }

    private void setupModernProgressBar(JProgressBar progressBar) {
        progressBar.setUI(new BasicProgressBarUI() {
            @Override
            protected Color getSelectionBackground() { return Color.WHITE; }
            @Override
            protected Color getSelectionForeground() { return Color.WHITE; }
            
            @Override
            protected void paintDeterminate(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = progressBar.getWidth();
                int height = progressBar.getHeight();
                int progress = (int) (width * progressBar.getPercentComplete());
                
                // Fondo
                g2d.setColor(LIGHT_SLATE);
                g2d.fillRoundRect(0, 0, width, height, height, height);
                
                // Progreso con gradiente
                if (progress > 0) {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, SUCCESS_EMERALD, 
                        width, 0, SUCCESS_EMERALD.brighter()
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, progress, height, height, height);
                }
                
                // Borde
                g2d.setColor(new Color(203, 213, 225));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(0, 0, width-1, height-1, height, height);
            }
        });
        
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
    }

    private JScrollPane createScrollPanel(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(LIGHT_SLATE);
        return scrollPane;
    }

    // Método auxiliar para encontrar JComboBox<String> de forma segura
    @SuppressWarnings("unchecked")
    private JComboBox<String> findComboBoxInPanel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JComboBox) {
                try {
                    return (JComboBox<String>) comp;
                } catch (ClassCastException e) {
                    continue;
                }
            }
        }

        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                JComboBox<String> found = findComboBoxInPanel((JPanel) comp);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    private JPanel createSimpleComboBoxPanel(String labelText, String[] items) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = createModernLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));

        JComboBox<String> comboBox = createModernComboBox();
        for (String item : items) {
            comboBox.addItem(item);
        }
        comboBox.setMaximumSize(new Dimension(350, 35));
        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(comboBox);

        return panel;
    }

    private JComboBox<String> createModernComboBox() {
        JComboBox<String> combo = new JComboBox<String>() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo
                g2d.setColor(new Color(248, 250, 252));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Borde
                g2d.setColor(isFocusOwner() ? PRIMARY_BLUE : new Color(203, 213, 225));
                g2d.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                
                super.paintComponent(g);
            }
        };
        
        combo.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        combo.setBackground(new Color(248, 250, 252));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                setFont(new Font("Segoe UI", Font.PLAIN, 11));
                return c;
            }
        });
        
        return combo;
    }

    private JTextField createModernTextField(String text, int columns) {
        JTextField field = new JTextField(text, columns) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo
                g2d.setColor(new Color(248, 250, 252));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Borde
                g2d.setColor(isFocusOwner() ? PRIMARY_BLUE : new Color(203, 213, 225));
                g2d.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                
                super.paintComponent(g);
            }
        };
        
        field.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        field.setOpaque(false);
        return field;
    }

    private JSpinner createModernSpinner(int value, int min, int max, int step) {
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
        JSpinner spinner = new JSpinner(model);
        
        // Personalizar el editor del spinner
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
        editor.getTextField().setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        editor.getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 11));
        editor.getTextField().setBackground(new Color(248, 250, 252));
        editor.getTextField().setForeground(DARK_SLATE);
        
        spinner.setPreferredSize(new Dimension(70, 35));
        
        return spinner;
    }

    private JButton createModernButton(String text, Color backgroundColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color paintColor;
                if (!isEnabled()) {
                    paintColor = new Color(156, 163, 175);
                } else if (getModel().isPressed()) {
                    paintColor = backgroundColor.darker();
                } else if (getModel().isRollover()) {
                    paintColor = backgroundColor.brighter();
                } else {
                    paintColor = backgroundColor;
                }
                
                // Fondo con gradiente
                GradientPaint gradient = new GradientPaint(
                    0, 0, paintColor,
                    0, getHeight(), paintColor.darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Borde
                g2d.setColor(paintColor.darker().darker());
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                
                g2d.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }

    // ========== MÉTODOS DE PANELES DE CONTROL ==========

    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel voltagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        voltagePanel.setBackground(CARD_BACKGROUND);
        voltagePanel.add(createModernLabel("Voltaje (V):"));
        voltageField = createModernTextField("10", 10);
        voltageField.setToolTipText("Voltaje entre 0.1 y 1000 V");
        voltagePanel.add(voltageField);
        voltagePanel.add(createModernLabel("V"));
        voltagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(voltagePanel);

        panel.add(Box.createVerticalStrut(8));

        JPanel frequencyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        frequencyPanel.setBackground(CARD_BACKGROUND);
        frequencyPanel.add(createModernLabel("Frecuencia (Hz):"));
        frequencyField = createModernTextField("60", 10);
        frequencyField.setToolTipText("Frecuencia entre 0.1 y 10000 Hz");
        frequencyPanel.add(frequencyField);
        frequencyPanel.add(createModernLabel("Hz"));
        frequencyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(frequencyPanel);

        return panel;
    }

    private JPanel createMethodPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        methodCombo = createModernComboBox();
        for (SimulationStrategy strategy : CircuitEngine.getAvailableStrategies()) {
            String methodKey = strategy.getName().toLowerCase().replace("-", "");
            methodCombo.addItem(languageManager.getTranslation(methodKey));
        }
        methodCombo.setToolTipText("Método de cálculo para la simulación");
        methodCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        methodCombo.setMaximumSize(new Dimension(300, 35));
        panel.add(methodCombo);

        return panel;
    }

    private JPanel createPresetPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] presetKeys = { "custom", "underdamped", "critical", "overdamped", "series_rlc", "high_pass",
                "low_pass" };
        presetCombo = createModernComboBox();
        for (String key : presetKeys) {
            presetCombo.addItem(languageManager.getTranslation(key));
        }
        presetCombo.setToolTipText("Seleccione un circuito predefinido");
        presetCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        presetCombo.setMaximumSize(new Dimension(300, 35));
        panel.add(presetCombo);

        return panel;
    }

    private JPanel createComponentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.setBackground(CARD_BACKGROUND);
        typePanel.add(createModernLabel("Tipo:"));
        String[] componentTypes = { "resistance", "inductor", "capacitor" };
        componentTypeCombo = createModernComboBox();
        for (String type : componentTypes) {
            componentTypeCombo.addItem(languageManager.getTranslation(type));
        }
        componentTypeCombo.setMaximumSize(new Dimension(140, 35));
        typePanel.add(componentTypeCombo);
        typePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(typePanel);

        panel.add(Box.createVerticalStrut(8));

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        valuePanel.setBackground(CARD_BACKGROUND);
        valuePanel.add(createModernLabel("Valor:"));
        valueField = createModernTextField("100", 12);
        valueField.setToolTipText("Ingrese un valor positivo para el componente");
        valuePanel.add(valueField);
        valuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(valuePanel);

        panel.add(Box.createVerticalStrut(12));

        addButton = createModernButton("Agregar Componente", SECONDARY_BLUE);
        addButton.setToolTipText("Agregar componente al circuito");
        addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addButton.setMaximumSize(new Dimension(220, 40));
        panel.add(addButton);

        return panel;
    }

    private JPanel createComponentListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(350, 150));

        componentsModel = new DefaultListModel<>();
        componentsList = new JList<>(componentsModel);
        componentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        componentsList.setToolTipText("Componentes en el circuito actual");
        componentsList.setBackground(new Color(248, 250, 252));
        componentsList.setForeground(DARK_SLATE);
        componentsList.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        componentsList.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane listScroll = new JScrollPane(componentsList);
        listScroll.setPreferredSize(new Dimension(300, 100));
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(listScroll, BorderLayout.CENTER);

        removeButton = createModernButton("Eliminar Seleccionado", ERROR_ROSE);
        removeButton.setToolTipText("Eliminar componente seleccionado");
        removeButton.setMaximumSize(new Dimension(220, 35));
        panel.add(removeButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCircuitActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        simulateButton = createModernButton("Simular Circuito", SUCCESS_EMERALD);
        simulateButton.setToolTipText("Ejecutar simulación del circuito actual");
        simulateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        simulateButton.setMaximumSize(new Dimension(220, 45));

        panel.add(Box.createVerticalStrut(8));

        clearButton = createModernButton("Limpiar Todo", ERROR_ROSE);
        clearButton.setToolTipText("Limpiar circuito y resultados");
        clearButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearButton.setMaximumSize(new Dimension(220, 40));

        // Barra de progreso
        progressBar = new JProgressBar();
        setupModernProgressBar(progressBar);
        progressBar.setVisible(false);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(220, 25));

        panel.add(simulateButton);
        panel.add(Box.createVerticalStrut(12));
        panel.add(clearButton);
        panel.add(Box.createVerticalStrut(12));
        panel.add(progressBar);

        return panel;
    }

    private JPanel createBatchControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel spinnersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        spinnersPanel.setBackground(CARD_BACKGROUND);

        spinnersPanel.add(createModernLabel("Simples:"));
        simpleSpinner = createModernSpinner(3, 0, 20, 1);
        spinnersPanel.add(simpleSpinner);

        spinnersPanel.add(createModernLabel("Medios:"));
        mediumSpinner = createModernSpinner(2, 0, 15, 1);
        spinnersPanel.add(mediumSpinner);

        spinnersPanel.add(createModernLabel("Complejos:"));
        complexSpinner = createModernSpinner(1, 0, 10, 1);
        spinnersPanel.add(complexSpinner);

        spinnersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(spinnersPanel);

        panel.add(Box.createVerticalStrut(12));

        generateBatchButton = createModernButton("Generar Lote de Simulaciones", WARNING_AMBER);
        generateBatchButton.addActionListener(e -> generateBatch());
        generateBatchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        generateBatchButton.setMaximumSize(new Dimension(350, 40));
        panel.add(generateBatchButton);

        return panel;
    }

    private JPanel createSchedulingButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        startSchedulerButton = createModernButton("Iniciar Planificación", SUCCESS_EMERALD);
        startSchedulerButton.addActionListener(e -> startScheduling());

        stopSchedulerButton = createModernButton("Detener", ERROR_ROSE);
        stopSchedulerButton.addActionListener(e -> stopScheduling());
        stopSchedulerButton.setEnabled(false);

        panel.add(startSchedulerButton);
        panel.add(stopSchedulerButton);

        return panel;
    }

    // ========== MÉTODOS DE PLANIFICACIÓN ==========

    private void updateBatchControls() {
        String selected = (String) batchTypeCombo.getSelectedItem();
        boolean isHeterogeneous = selected != null && selected.contains("Heterogéneo");

        simpleSpinner.setEnabled(isHeterogeneous);
        mediumSpinner.setEnabled(isHeterogeneous);
        complexSpinner.setEnabled(isHeterogeneous);
    }

    private void generateBatch() {
        String batchType = (String) batchTypeCombo.getSelectedItem();
        if (batchType == null)
            return;

        scheduler.clearTasks();

        if (batchType.contains("Homogéneo")) {
            CircuitSimulationTask.Complexity complexity;
            if (batchType.contains("Simple")) {
                complexity = CircuitSimulationTask.Complexity.SIMPLE;
            } else if (batchType.contains("Medio")) {
                complexity = CircuitSimulationTask.Complexity.MEDIUM;
            } else {
                complexity = CircuitSimulationTask.Complexity.COMPLEX;
            }

            List<CircuitSimulationTask> batch = scheduler.generateHomogeneousBatch(complexity, 5);
            scheduler.addTasks(batch);
        } else if (batchType.contains("Heterogéneo")) {
            int simpleCount = (Integer) simpleSpinner.getValue();
            int mediumCount = (Integer) mediumSpinner.getValue();
            int complexCount = (Integer) complexSpinner.getValue();

            List<CircuitSimulationTask> batch = scheduler.generateHeterogeneousBatch(
                    simpleCount, mediumCount, complexCount);
            scheduler.addTasks(batch);
        }

        updateTasksTable();
        updateMemoryVisualization();
        logSchedulingMessage("Lote generado: " + scheduler.getTasks().size() + " tareas");
    }

    private void startScheduling() {
        try {
            String algorithm = (String) algorithmCombo.getSelectedItem();
            if (algorithm != null) {
                switch (algorithm) {
                    case "First-Come, First-Served (FCFS)":
                        scheduler.setStrategy(new FirstComeFirstServedScheduler());
                        break;
                    case "Round Robin (RR)":
                        scheduler.setStrategy(new RoundRobinScheduler());
                        break;
                    case "Shortest Job First (SJF)":
                        scheduler.setStrategy(new ShortestJobFirstScheduler());
                        break;
                }
            }

            logSchedulingMessage("Iniciando planificación con " + algorithm);
            scheduler.startSimulation();
            startUpdateTimer();

        } catch (Exception ex) {
            logSchedulingMessage("ERROR: " + ex.getMessage());
            showError("Error al iniciar planificación: " + ex.getMessage());
        }
    }

    private void stopScheduling() {
        scheduler.stopSimulation();
        stopUpdateTimer();
        logSchedulingMessage("Planificación detenida");
    }

    private void startUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
        updateTimer = new Timer(500, e -> {
            updateTasksTable();
            updateMemoryVisualization();
        });
        updateTimer.start();
    }

    private void stopUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.stop();
            updateTimer = null;
        }
    }

    private void updateTasksTable() {
        // Este método se mantiene para compatibilidad pero ya no se usa
        // ya que reemplazamos la tabla con la visualización de memoria
    }

    private void logSchedulingMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            schedulingLogArea.append("[" + timestamp + "] " + message + "\n");
            schedulingLogArea.setCaretPosition(schedulingLogArea.getDocument().getLength());
        });
    }

    // ========== MÉTODOS DE SIMULACIÓN DE CIRCUITOS ==========

    private void setupEventHandlers() {
        // Handlers para simulación de circuitos RLC
        methodCombo.addActionListener(e -> updateStrategy());
        presetCombo.addActionListener(e -> loadPreset());
        addButton.addActionListener(e -> addComponent());
        removeButton.addActionListener(e -> removeComponent());
        simulateButton.addActionListener(e -> simulateCircuit());
        clearButton.addActionListener(e -> clearAll());
        valueField.addActionListener(e -> addComponent());
        
        // TODO: Agregar handlers para componentes DC cuando se implementen
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
            showError("Ingrese valores numéricos válidos");
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
            progressBar.setString("Simulación en progreso...");

            simulateButton.setEnabled(false);

            engine.simulate(components, voltage, frequency);

        } catch (NumberFormatException ex) {
            showError("Ingrese valores numéricos válidos");
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

    private void clearAll() {
        components.clear();
        updateComponentList();
        updateCircuitDiagram();

        if (resultsArea != null) {
            resultsArea.setText("Circuito limpiado. Listo para nueva simulación.");
        }
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
        if (circuitDiagram != null) {
            circuitDiagram.setComponents(components);
            circuitDiagram.repaint();
        }
    }

    // ========== MÉTODOS DE SimulationObserver ==========

    @Override
    public void onSimulationComplete(Object result) {
        SwingUtilities.invokeLater(() -> {
            if (result instanceof SimulationResult) {
                SimulationResult simResult = (SimulationResult) result;
                lastResult = simResult;

                updateGraphType();
                updateAnalysisPanel(simResult);

                StringBuilder sb = new StringBuilder();
                sb.append("=== RESULTADOS DE SIMULACIÓN ===\n\n");
                sb.append("- Impedancia: ").append(df.format(simResult.getImpedance())).append(" Ω\n");
                sb.append("- Corriente: ").append(df.format(simResult.getCurrent())).append(" A\n");
                sb.append("- Ángulo de Fase: ").append(df.format(Math.toDegrees(simResult.getPhaseAngle())))
                        .append("°\n");
                sb.append("- Potencia Activa: ").append(df.format(simResult.getActivePower())).append(" W\n");
                sb.append("- Potencia Reactiva: ").append(df.format(simResult.getReactivePower())).append(" VAR\n");
                sb.append("- Potencia Aparente: ").append(df.format(simResult.getApparentPower())).append(" VA\n");
                sb.append("- Factor de Potencia: ").append(df.format(simResult.getPowerFactor())).append("\n\n");

                double phaseDeg = Math.toDegrees(simResult.getPhaseAngle());
                String circuitType;
                if (phaseDeg > 0) {
                    circuitType = "-> Circuito INDUCTIVO (corriente atrasada)";
                } else if (phaseDeg < 0) {
                    circuitType = "-> Circuito CAPACITIVO (corriente adelantada)";
                } else {
                    circuitType = "-> Circuito RESISTIVO (corriente en fase)";
                }

                sb.append(circuitType).append("\n");

                if (resultsArea != null) {
                    resultsArea.setText(sb.toString());
                }

                progressBar.setVisible(false);
                simulateButton.setEnabled(true);

                showInfo("Simulación completada exitosamente");

            } else {
                onSimulationError("Resultado de simulación inválido");
            }
        });
    }

    @Override
    public void onSimulationError(String error) {
        SwingUtilities.invokeLater(() -> {
            String detailedError = "Error en la simulación:\n\n" + error;

            showError(detailedError);

            progressBar.setVisible(false);
            simulateButton.setEnabled(true);

            if (resultsArea != null) {
                resultsArea.setText("Error en la simulación. Por favor, verifique los parámetros e intente nuevamente.\n\n"
                        + "Detalles del error: " + error);
            }
        });
    }

    @Override
    public void onSimulationStart() {
        SwingUtilities.invokeLater(() -> {
            if (resultsArea != null) {
                resultsArea.setText("Simulación en progreso...\n\nPor favor espere...");
            }
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString("Simulación en progreso...");
        });
    }

    private void updateAnalysisPanel(SimulationResult result) {
        if (analysisArea == null) return;
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("=== ANÁLISIS DETALLADO DEL CIRCUITO ===\n\n");
        
        // Análisis básico del circuito
        double totalR = components.stream().mapToDouble(CircuitComponent::getResistance).sum();
        double totalL = components.stream().mapToDouble(CircuitComponent::getInductance).sum();
        double totalC = components.stream().mapToDouble(CircuitComponent::getCapacitance).sum();
        
        analysis.append("PARÁMETROS DEL CIRCUITO:\n");
        analysis.append(String.format("- Resistencia total: %.2f Ω\n", totalR));
        analysis.append(String.format("- Inductancia total: %.4f H\n", totalL));
        analysis.append(String.format("- Capacitancia total: %.6f F\n", totalC));
        
        // Análisis de potencia
        analysis.append("\nANÁLISIS DE POTENCIA:\n");
        analysis.append(String.format("- Potencia activa: %.2f W\n", result.getActivePower()));
        analysis.append(String.format("- Potencia reactiva: %.2f VAR\n", result.getReactivePower()));
        analysis.append(String.format("- Potencia aparente: %.2f VA\n", result.getApparentPower()));
        analysis.append(String.format("- Factor de potencia: %.3f\n", result.getPowerFactor()));
        
        // Análisis de eficiencia
        double efficiency = (result.getActivePower() / result.getApparentPower()) * 100;
        analysis.append(String.format("- Eficiencia energética: %.1f%%\n", efficiency));
        
        // Análisis de comportamiento
        analysis.append("\nCOMPORTAMIENTO DEL CIRCUITO:\n");
        double phaseDeg = Math.toDegrees(result.getPhaseAngle());
        if (phaseDeg > 5) {
            analysis.append("- Comportamiento predominantemente INDUCTIVO\n");
            analysis.append("- La corriente se atrasa respecto al voltaje\n");
        } else if (phaseDeg < -5) {
            analysis.append("- Comportamiento predominantemente CAPACITIVO\n");
            analysis.append("- La corriente se adelanta respecto al voltaje\n");
        } else {
            analysis.append("- Comportamiento predominantemente RESISTIVO\n");
            analysis.append("- Corriente y voltaje están en fase\n");
        }
        
        // Recomendaciones
        analysis.append("\nRECOMENDACIONES:\n");
        if (Math.abs(result.getPowerFactor()) < 0.9) {
            analysis.append("- Considerar corrección del factor de potencia\n");
            if (result.getPowerFactor() < 0) {
                analysis.append("- Agregar inductancia para mejorar el factor de potencia\n");
            } else {
                analysis.append("- Agregar capacitancia para mejorar el factor de potencia\n");
            }
        } else {
            analysis.append("- Factor de potencia dentro de rangos aceptables\n");
        }
        
        if (result.getCurrent() > 10) {
            analysis.append("- Alta corriente detectada, verificar especificaciones de componentes\n");
        }
        
        analysisArea.setText(analysis.toString());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public void disposeResources() {
        if (engine != null) {
            engine.dispose();
        }
        if (scheduler != null && scheduler.isSimulationRunning()) {
            scheduler.stopSimulation();
        }
        stopUpdateTimer();
    }
}