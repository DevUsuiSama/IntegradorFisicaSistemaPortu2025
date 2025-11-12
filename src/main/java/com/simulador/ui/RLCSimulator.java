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
import com.simulador.scheduler.SchedulerMetrics; // Asegúrate de que este import esté
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

// --- INICIO DE MODIFICACIÓN (Exportar Archivos) ---
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
// --- FIN DE MODIFICACIÓN ---

/**
 * Panel principal del simulador de circuitos RLC con algoritmos de
 * planificación integrados - Versión Mejorada Visualmente
 * AHORA INCLUYE SIMULACIÓN DE CIRCUITOS DC
 * * MODIFICADO: Se añadió un selector de POLARIDAD para componentes DC.
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
    private JComboBox<String> dcComponentTypeCombo;
    private JTextField dcValueField;
    private JSpinner branchSpinner;
    private JSpinner targetBranchSpinner;
    private JComboBox<String> configCombo;
    private JComboBox<String> dcMethodCombo;
    private JButton addDCButton;
    private JButton simulateDCButton;
    private JButton clearDCButton;
    private JProgressBar dcProgressBar;

    private JComboBox<String> dcPolarityCombo;
    private JPanel dcPolarityPanel;


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

    // Componentes para el historial de simulaciones
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private final List<com.simulador.scheduler.SchedulerMetrics> simulationHistory = new ArrayList<>();
    private JButton historyExportButton;
    private JButton historyClearButton;

    // Componentes para visualización de circuitos
    private BaseGraph currentGraph;
    private JPanel graphContainer;
    private JComboBox<String> graphTypeCombo;
    private JTextArea analysisArea;
    private JTabbedPane circuitTabs;

    // Panel principal derecho (cambia según la pestaña)
    private JPanel rightPanel;
    private CardLayout rightPanelLayout;

    // --- INICIO DE MODIFICACIÓN (I18N) ---
    // Barra de Menú
    private JMenuBar menuBar;
    private JMenu optionsMenu, languageMenu;
    private JRadioButtonMenuItem spanishItem, portugueseItem;

    // Pestañas
    private JTabbedPane mainTabbedPane;
    private JTabbedPane dcResultsTabs, processTabs;
    
    // Etiquetas y Títulos de Tarjetas (Paneles Izquierdos)
    private JLabel headerTitleLabel, headerSubtitleLabel;
    private JPanel rlcInputCard, rlcMethodCard, rlcPresetCard, rlcComponentCard, rlcListCard, rlcActionCard;
    private JPanel dcConfigCard, dcComponentCard, dcMethodCard, dcActionCard;
    private JPanel procAlgorithmCard, procBatchTypeCard, procBatchConfigCard, procExecCard;
    
    // Labels RLC
    private JLabel rlcVoltageLabel, rlcFrequencyLabel, rlcTypeLabel, rlcValueLabel;
    
    // Labels DC
    private JLabel dcNumBranchesLabel, dcConfigLabel, dcTypeLabel, dcValueLabel, dcPolarityLabel, dcInBranchLabel;

    // Labels Procesos
    private JLabel procAlgorithmLabel, procBatchConfigLabel, procSimpleLabel, procMediumLabel, procComplexLabel;

    // Labels Paneles Derechos
    private JLabel rlcDiagramTitleLabel, rlcGraphTypeLabel;
    private JLabel dcPanelTitleLabel, dcDiagramTitleLabel;
    private JLabel memPanelTitleLabel, memSystemStatusLabel, memTotalFragLabel, memCurrentUsageLabel, memAvgUsageLabel, memExtFragLabel, memIntFragLabel, memSemaphoreLabel;
    private JLabel memMapTitleLabel, memCurrentProcessLabel, memQueuesTitleLabel;
    // --- FIN DE MODIFICACIÓN (I18N) ---

    public RLCSimulator() {
        this.engine = new CircuitEngine();
        this.scheduler = new ProcessScheduler();
        this.components = new ArrayList<>();
        this.languageManager = LanguageManager.getInstance();
        this.updateTimer = null;
        
        // INICIALIZACIÓN DC
        this.dcEngine = new DCCircuitEngine();
        this.currentDCCircuit = new DCCircuit(); // Llama al constructor por defecto
        this.lastDCResult = null;

        createMenuBar(); // Crear la barra de menú primero
        initializeEngines();
        initializeUI();
        setupEventHandlers();
        setupDCEventHandlers(); // NUEVO: Configurar handlers DC
        updateLanguage(); // Aplicar idioma inicial
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
                        com.simulador.scheduler.SchedulerMetrics metrics = scheduler.getMetrics(); // Obtener métricas
                        metrics.printMetrics(algorithmCombo.getSelectedItem().toString());
                        
                        // Guardar y mostrar en la tabla de historial
                        this.simulationHistory.add(metrics); 
                        addMetricsToHistoryTable(metrics);
                    }
                });
            } else if (ProcessScheduler.PROPERTY_TASKS_UPDATED.equals(evt.getPropertyName())) {
                SwingUtilities.invokeLater(() -> {
                    // updateTasksTable(); // Este método ya no es necesario
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
    
    // --- INICIO DE MODIFICACIÓN (I18N) ---
    /**
     * Crea la barra de menú principal de la aplicación.
     */
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // --- Menú Opciones ---
        optionsMenu = new JMenu(); // Texto se setea en updateLanguage()
        menuBar.add(optionsMenu);

        // --- Submenú Idioma ---
        languageMenu = new JMenu(); // Texto se setea en updateLanguage()
        optionsMenu.add(languageMenu);

        ButtonGroup langGroup = new ButtonGroup();
        
        spanishItem = new JRadioButtonMenuItem(); // Texto se setea en updateLanguage()
        spanishItem.setSelected(languageManager.getCurrentLanguage().equals("es"));
        spanishItem.addActionListener(e -> {
            languageManager.setLanguage("es");
            updateLanguage();
        });
        langGroup.add(spanishItem);
        languageMenu.add(spanishItem);

        portugueseItem = new JRadioButtonMenuItem(); // Texto se setea en updateLanguage()
        portugueseItem.setSelected(languageManager.getCurrentLanguage().equals("pt"));
        portugueseItem.addActionListener(e -> {
            languageManager.setLanguage("pt");
            updateLanguage();
        });
        langGroup.add(portugueseItem);
        languageMenu.add(portugueseItem);
    }

    /**
     * Devuelve la barra de menú para que MainSimulatorFrame pueda usarla.
     */
    public JMenuBar getAppMenuBar() {
        return menuBar;
    }
    // --- FIN DE MODIFICACIÓN ---

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
        headerTitleLabel = new JLabel("", JLabel.CENTER); // Texto desde I18N
        headerTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerTitleLabel.setForeground(Color.WHITE);
        headerTitleLabel.setBorder(BorderFactory.createEmptyBorder(25, 0, 5, 0));
        
        headerSubtitleLabel = new JLabel("", JLabel.CENTER); // Texto desde I18N
        headerSubtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        headerSubtitleLabel.setForeground(new Color(255, 255, 255, 220));
        headerSubtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        
        headerPanel.add(headerTitleLabel, BorderLayout.CENTER);
        headerPanel.add(headerSubtitleLabel, BorderLayout.SOUTH);
        
        return headerPanel;
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(LIGHT_SLATE);
        panel.setPreferredSize(new Dimension(400, 700));

        // Crear pestañas para navegación
        mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        mainTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        mainTabbedPane.setBackground(LIGHT_SLATE);
        setupModernTabbedPane(mainTabbedPane);

        // Pestaña 1: Simulación de Circuitos RLC
        JPanel circuitPanel = createCircuitControlsPanel();
        JScrollPane circuitScroll = new JScrollPane(circuitPanel);
        circuitScroll.setBorder(null);
        circuitScroll.getVerticalScrollBar().setUnitIncrement(16);
        circuitScroll.setBackground(LIGHT_SLATE);
        mainTabbedPane.addTab("", circuitScroll); // Texto desde I18N

        // Pestaña 2: Simulación de Circuitos DC - NUEVA PESTAÑA
        JPanel dcPanel = createDCControlsPanel();
        JScrollPane dcScroll = new JScrollPane(dcPanel);
        dcScroll.setBorder(null);
        dcScroll.getVerticalScrollBar().setUnitIncrement(16);
        dcScroll.setBackground(LIGHT_SLATE);
        mainTabbedPane.addTab("", dcScroll); // Texto desde I18N

        // Pestaña 3: Planificación de Procesos
        JPanel schedulingPanel = createSchedulingControlsPanel();
        JScrollPane schedulingScroll = new JScrollPane(schedulingPanel);
        schedulingScroll.setBorder(null);
        schedulingScroll.getVerticalScrollBar().setUnitIncrement(16);
        schedulingScroll.setBackground(LIGHT_SLATE);
        mainTabbedPane.addTab("", schedulingScroll); // Texto desde I18N

        // Listener para cambiar la vista derecha cuando cambia la pestaña
        mainTabbedPane.addChangeListener(e -> {
            int selectedIndex = mainTabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                rightPanelLayout.show(rightPanel, "CIRCUIT");
            } else if (selectedIndex == 1) {
                rightPanelLayout.show(rightPanel, "DC_CIRCUIT"); // NUEVA PESTAÑA DC
            } else {
                rightPanelLayout.show(rightPanel, "PROCESS");
            }
        });

        panel.add(mainTabbedPane, BorderLayout.CENTER);
        return panel;
    }

    // ========== NUEVO: PANEL DE CONTROLES PARA CIRCUITOS DC ==========

    private JPanel createDCControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        panel.setBackground(LIGHT_SLATE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Configuración de ramas
        dcConfigCard = createModernCardPanel("", createBranchPanel());
        panel.add(dcConfigCard);
        panel.add(Box.createVerticalStrut(15));

        // Componentes DC
        dcComponentCard = createModernCardPanel("", createDCComponentPanel());
        panel.add(dcComponentCard);
        panel.add(Box.createVerticalStrut(15));

        // Métodos de análisis DC
        dcMethodCard = createModernCardPanel("", createDCMethodPanel());
        panel.add(dcMethodCard);
        panel.add(Box.createVerticalStrut(15));

        // Botones de acción DC
        dcActionCard = createModernCardPanel("", createDCActionPanel());
        panel.add(dcActionCard);

        return panel;
    }

    private JPanel createDCComponentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.setBackground(CARD_BACKGROUND);
        dcTypeLabel = createModernLabel("");
        typePanel.add(dcTypeLabel);
        dcComponentTypeCombo = createModernComboBox();
        dcComponentTypeCombo.addActionListener(e -> updateDCPolarityPanel());
        dcComponentTypeCombo.setMaximumSize(new Dimension(140, 35));
        typePanel.add(dcComponentTypeCombo);
        typePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(typePanel);

        panel.add(Box.createVerticalStrut(8));

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        valuePanel.setBackground(CARD_BACKGROUND);
        dcValueLabel = createModernLabel("");
        valuePanel.add(dcValueLabel);
        dcValueField = createModernTextField("100", 12);
        valuePanel.add(dcValueField);
        valuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(valuePanel);

        panel.add(Box.createVerticalStrut(8));

        // Panel para seleccionar la polaridad (se muestra/oculta)
        dcPolarityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dcPolarityPanel.setBackground(CARD_BACKGROUND);
        dcPolarityLabel = createModernLabel("");
        dcPolarityPanel.add(dcPolarityLabel);
        dcPolarityCombo = createModernComboBox();
        dcPolarityPanel.add(dcPolarityCombo);
        dcPolarityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(dcPolarityPanel);
        
        panel.add(Box.createVerticalStrut(8));

        // Panel para seleccionar la rama de destino
        JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetPanel.setBackground(CARD_BACKGROUND);
        dcInBranchLabel = createModernLabel("");
        targetPanel.add(dcInBranchLabel);
        targetBranchSpinner = createModernSpinner(1, 1, 10, 1); // Inicia con max 10
        targetPanel.add(targetBranchSpinner);
        
        targetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(targetPanel);

        panel.add(Box.createVerticalStrut(12));

        addDCButton = createModernButton("", SECONDARY_BLUE);
        addDCButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addDCButton.setMaximumSize(new Dimension(220, 40));
        panel.add(addDCButton);
        
        // Actualizar la visibilidad inicial del panel de polaridad
        updateDCPolarityPanel();

        return panel;
    }

    private JPanel createBranchPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel branchCountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        branchCountPanel.setBackground(CARD_BACKGROUND);
        dcNumBranchesLabel = createModernLabel("");
        branchCountPanel.add(dcNumBranchesLabel);
        branchSpinner = createModernSpinner(2, 1, 10, 1);
        branchSpinner.addChangeListener(e -> updateTargetBranchSpinnerMax());
        branchCountPanel.add(branchSpinner);
        branchCountPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(branchCountPanel);

        panel.add(Box.createVerticalStrut(8));

        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        configPanel.setBackground(CARD_BACKGROUND);
        dcConfigLabel = createModernLabel("");
        configPanel.add(dcConfigLabel);
        configCombo = createModernComboBox();
        configCombo.setMaximumSize(new Dimension(120, 35));
        configPanel.add(configCombo);
        configPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(configPanel);
        
        // Inicializar el spinner de rama destino
        updateTargetBranchSpinnerMax();

        return panel;
    }

    private JPanel createDCMethodPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        dcMethodCombo = createModernComboBox();
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

        simulateDCButton = createModernButton("", SUCCESS_EMERALD);
        simulateDCButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        simulateDCButton.setMaximumSize(new Dimension(220, 45));

        panel.add(Box.createVerticalStrut(8));

        clearDCButton = createModernButton("", ERROR_ROSE);
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
        dcPanelTitleLabel = new JLabel("", JLabel.CENTER);
        dcPanelTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dcPanelTitleLabel.setForeground(DARK_SLATE);
        dcPanelTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(dcPanelTitleLabel, BorderLayout.NORTH);

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
        dcDiagramTitleLabel = new JLabel("");
        dcDiagramTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dcDiagramTitleLabel.setForeground(DARK_SLATE);
        dcDiagramTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        cardPanel.add(dcDiagramTitleLabel, BorderLayout.NORTH);

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
        dcResultsTabs = new JTabbedPane(JTabbedPane.TOP);
        dcResultsTabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        setupModernTabbedPane(dcResultsTabs);

        // Pestaña 1: Resultados principales usando DCResultsPanel
        dcResultsPanel = new DCResultsPanel(); // Este ya tiene sus propios labels internos
        dcResultsTabs.addTab("", dcResultsPanel); // Texto desde I18N

        // Pestaña 2: Circuitos equivalentes
        dcEquivalentPanel = new DCEquivalentCircuitPanel(); // Este maneja sus propias pestañas
        dcResultsTabs.addTab("", dcEquivalentPanel); // Texto desde I18N

        // Pestaña 3: Análisis detallado
        JPanel detailedAnalysisPanel = createDCDetailedAnalysisPanel();
        dcResultsTabs.addTab("", detailedAnalysisPanel); // Texto desde I18N

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
        // Texto inicial se setea en updateLanguage()

        JScrollPane scroll = new JScrollPane(dcDetailedAnalysisArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ========== MÉTODOS DC ==========

    /**
     * Sincroniza el valor máximo del spinner de rama destino
     * con el número total de ramas seleccionadas.
     */
    private void updateTargetBranchSpinnerMax() {
        if (branchSpinner == null || targetBranchSpinner == null) {
            return; // Ocurre durante la inicialización
        }
        
        int numBranches = (Integer) branchSpinner.getValue();
        int currentTarget = (Integer) targetBranchSpinner.getValue();
        
        // Crear un nuevo modelo para el spinner de rama destino
        SpinnerNumberModel model = new SpinnerNumberModel(
            Math.min(currentTarget, numBranches), // Valor actual (limitado por el max)
            1,     // Mínimo
            numBranches, // Máximo
            1      // Paso
        );
        targetBranchSpinner.setModel(model);
    }
    
    /**
     * Muestra u oculta el panel de polaridad basado en el tipo de componente.
     */
    private void updateDCPolarityPanel() {
        if (dcPolarityPanel == null || dcComponentTypeCombo == null) {
            return; // Ocurre durante la inicialización
        }
        
        String selectedType = (String) dcComponentTypeCombo.getSelectedItem();
        
        // Comparar usando las claves de idioma
        if (languageManager.getTranslation("dc_type_resistor").equals(selectedType)) {
            dcPolarityPanel.setVisible(false);
        } else {
            // "Batería" o "Fuente DC"
            dcPolarityPanel.setVisible(true);
        }
    }
    
    private void setupDCEventHandlers() {
        // Configurar handlers para los botones DC
        addDCButton.addActionListener(e -> addDCComponent());
        simulateDCButton.addActionListener(e -> simulateDCCircuit());
        clearDCButton.addActionListener(e -> clearDCCircuit());
        
        // Configurar listeners para cambios en valores DC
        setupDCValueListeners();
    }

    private void setupDCValueListeners() {
        // Configurar listener para el combo box de métodos DC
        if (dcMethodCombo != null) {
            dcMethodCombo.addActionListener(e -> onDCMethodChanged());
        }
    }

    private void onDCMethodChanged() {
        // String method = getDCMethod(); // Variable no usada, se elimina para quitar warning
        // Opcional: mostrar info, pero puede ser molesto
        // showInfo("Método de análisis cambiado a: " + method);
    }

    private void addDCComponent() {
        try {
            DCComponentType type = getSelectedDCComponentType();
            double value = Double.parseDouble(dcValueField.getText().trim());
            
            // Aplicar polaridad si es una fuente
            if (type == DCComponentType.BATTERY || type == DCComponentType.DC_SOURCE) {
                // Comparar usando la clave de idioma
                if (languageManager.getTranslation("dc_polarity_down").equals(dcPolarityCombo.getSelectedItem())) { 
                    value = -value; // Aplicar valor negativo
                }
            }

            String name = dcComponentTypeCombo.getSelectedItem().toString() + " " + value;
            
            if (value <= 0 && type == DCComponentType.RESISTOR) {
                showError(languageManager.getTranslation("dc_value_positive"));
                return;
            }

            // Se asume quantity = 1
            DCComponent comp = new DCComponent(type, value, name, 1);
            
            // 1. Asegurarse de que el número de ramas en el modelo coincida con la UI
            int totalBranchesInUI = (Integer) branchSpinner.getValue();
            ensureBranchesExist(totalBranchesInUI);

            // 2. Obtener la rama de destino seleccionada por el usuario
            int targetBranchIndex = (Integer) targetBranchSpinner.getValue() - 1; // -1 porque los spinners son 1-based

            // 3. Validar que la rama de destino exista
            if (targetBranchIndex < 0 || targetBranchIndex >= currentDCCircuit.getBranches().size()) {
                showError(languageManager.getTranslation("dc_branch_error"));
                return;
            }

            // 4. Agregar el componente a la rama correcta
            currentDCCircuit.getBranches().get(targetBranchIndex).addComponent(comp);
            
            // Actualizar configuración
            String config = (String) configCombo.getSelectedItem();
            currentDCCircuit.setConfiguration(config != null ? config : "Serie");
            
            // Actualizar UI
            dcDiagramPanel.setCircuit(currentDCCircuit);
            dcDiagramPanel.repaint();
            
            dcValueField.setText("");
            showInfo(languageManager.getFormattedTranslation("dc_add_success", targetBranchIndex + 1));
            
        } catch (NumberFormatException ex) {
            showError(languageManager.getTranslation("dc_numeric_error"));
        } catch (Exception ex) {
            showError(languageManager.getFormattedTranslation("dc_add_error", ex.getMessage()));
        }
    }

    private DCComponentType getSelectedDCComponentType() {
        String selected = (String) dcComponentTypeCombo.getSelectedItem();
        if (selected == null) return DCComponentType.RESISTOR;
        
        if (selected.equals(languageManager.getTranslation("dc_type_battery"))) {
            return DCComponentType.BATTERY;
        } else if (selected.equals(languageManager.getTranslation("dc_type_resistor"))) {
            return DCComponentType.RESISTOR;
        } else if (selected.equals(languageManager.getTranslation("dc_type_source"))) {
            return DCComponentType.DC_SOURCE;
        }
        return DCComponentType.RESISTOR;
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
            // 1. Asegurarse de que las ramas y config estén sincronizadas
            int totalBranchesInUI = (Integer) branchSpinner.getValue();
            ensureBranchesExist(totalBranchesInUI);
            currentDCCircuit.setConfiguration((String) configCombo.getSelectedItem());

            if (currentDCCircuit == null || !currentDCCircuit.isValid()) {
                showError(languageManager.getTranslation("dc_invalid_circuit"));
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
            dcProgressBar.setString(languageManager.getTranslation("sim_in_progress"));
            simulateDCButton.setEnabled(false);
            
            // Ejecutar simulación
            DCSimulationResult result = dcEngine.simulate(currentDCCircuit);
            lastDCResult = result;
            
            // Actualizar UI
            updateDCResults(result); // Actualiza el panel de análisis detallado
            dcResultsPanel.updateResults(result);
            dcEquivalentPanel.updateEquivalents(result); // Pasar LanguageManager
            dcDiagramPanel.setCircuit(currentDCCircuit);
            
            // Ocultar progreso
            dcProgressBar.setVisible(false);
            simulateDCButton.setEnabled(true);
            
            showInfo(languageManager.getFormattedTranslation("dc_sim_success", methodName));
            
        } catch (Exception ex) {
            ex.printStackTrace();
            String errorMsg = languageManager.getFormattedTranslation("dc_sim_error", ex.getMessage());
            showError(errorMsg);
            dcResultsPanel.showError(ex.getMessage());
            dcEquivalentPanel.clearEquivalents(); // Pasar LanguageManager
            dcProgressBar.setVisible(false);
            simulateDCButton.setEnabled(true);
        }
    }

    private void clearDCCircuit() {
        // 1. Crear circuito vacío
        currentDCCircuit = new DCCircuit(); // Llama al constructor por defecto
        lastDCResult = null;
        
        // 2. Resetear los spinners de la UI
        branchSpinner.setValue(2); // Valor por defecto
        updateTargetBranchSpinnerMax(); // Sincronizar
        targetBranchSpinner.setValue(1); // Valor por defecto
        configCombo.setSelectedItem(languageManager.getTranslation("dc_config_series"));
        
        // Actualizar UI
        dcDiagramPanel.setCircuit(currentDCCircuit);
        dcResultsPanel.clearResults(); // Pasar LanguageManager
        dcEquivalentPanel.clearEquivalents(); // Pasar LanguageManager
        if (dcDetailedAnalysisArea != null) {
            dcDetailedAnalysisArea.setText(
                languageManager.getTranslation("dc_analysis_placeholder_title") +
                "\n\n" + languageManager.getTranslation("dc_cleared")
            );
        }
        
        showInfo(languageManager.getTranslation("dc_cleared"));
    }

    private double getDCVoltage() {
        // Este método ya no es llamado por la lógica de simulación,
        // pero lo dejamos en 0.0 por seguridad.
        return 0.0;
    }

    private String getDCMethod() {
        if (dcMethodCombo != null && dcMethodCombo.getSelectedItem() != null) {
            return (String) dcMethodCombo.getSelectedItem();
        }
        return languageManager.getTranslation("dc_method_ohm"); // Default
    }

    private DCAnalysisMethod convertToDCAnalysisMethod(String methodName) {
        if (methodName == null) return DCAnalysisMethod.OHM_LAW;
        
        if (methodName.equals(languageManager.getTranslation("dc_method_kirchhoff"))) {
            return DCAnalysisMethod.KIRCHHOFF_LAWS;
        } else if (methodName.equals(languageManager.getTranslation("dc_method_mesh"))) {
            return DCAnalysisMethod.MESH_ANALYSIS;
        } else if (methodName.equals(languageManager.getTranslation("dc_method_nodal"))) {
            return DCAnalysisMethod.NODE_ANALYSIS;
        } else if (methodName.equals(languageManager.getTranslation("dc_method_thevenin"))) {
            return DCAnalysisMethod.THEVENIN_THEOREM;
        } else if (methodName.equals(languageManager.getTranslation("dc_method_norton"))) {
            return DCAnalysisMethod.NORTON_THEOREM;
        } else if (methodName.equals(languageManager.getTranslation("dc_method_source"))) {
            return DCAnalysisMethod.SOURCE_TRANSFORMATION;
        }
        
        return DCAnalysisMethod.OHM_LAW;
    }
    
    private void updateDCResults(DCSimulationResult result) {
        if (dcDetailedAnalysisArea == null) return;
        
        StringBuilder sb = new StringBuilder();
        sb.append(languageManager.getTranslation("dc_analysis_placeholder_title")).append("\n\n");
        sb.append(languageManager.getTranslation("dc_analysis_method")).append(" ").append(result.getMethodUsed()).append("\n");
        sb.append(languageManager.getTranslation("dc_config")).append(" ").append(result.getCircuitConfiguration()).append("\n\n");
        
        sb.append("--- VERIFICACIÓN DE LEYES ---\n");
        // USA EL VOLTAJE CALCULADO (EJ. NODAL)
        sb.append(languageManager.getTranslation("dc_results_voltage_card")).append(": ").append(df.format(result.getCalculatedVoltage())).append(" V\n"); 
        sb.append(languageManager.getTranslation("dc_results_current_card")).append(": ").append(df.format(result.getTotalCurrent())).append(" A\n");
        sb.append(languageManager.getTranslation("dc_results_resistance_card")).append(": ").append(df.format(result.getTotalResistance())).append(" Ω\n");
        
        // Verificación V = I * R
        double calculatedVoltageCheck = result.getTotalCurrent() * result.getTotalResistance();
        sb.append("Ley de Ohm (Va = I_eq * R_eq): ").append(df.format(calculatedVoltageCheck)).append(" V (Verificado)\n\n");
        
        sb.append("--- ANÁLISIS DE POTENCIA ---\n");
        sb.append("Potencia (Fuente): ").append(df.format(result.getTotalPower())).append(" W\n");
        sb.append("Potencia (Disipada): ").append(df.format(result.getPowerDissipated())).append(" W\n");
        sb.append("Eficiencia: ").append(df.format(result.getEfficiency())).append(" %\n\n");

        sb.append("--- CORRIENTES POR RAMA ---\n");
        double[] branchCurrents = result.getBranchCurrents();
        double kclCheck = 0.0;
        for (int i = 0; i < branchCurrents.length; i++) {
            kclCheck += branchCurrents[i];
            String sentido = (branchCurrents[i] >= 0) ? "↓ (Hacia abajo)" : "↑ (Hacia arriba)";
            sb.append(String.format("• Rama %d: %s A  [%s]\n", 
                i + 1, 
                df.format(branchCurrents[i]),
                sentido));
        }
        sb.append(String.format("Verificación KCL (ΣI en Nodo Va): %.6f A (debería ser 0)\n", kclCheck));
        
        sb.append("\n--- TENSIONES EN COMPONENTES ---\n");
        double[] componentVoltages = result.getComponentVoltages();
        for (int i = 0; i < componentVoltages.length; i++) {
            sb.append(String.format("• Componente %d (Valor): %.2f V\n", i + 1, componentVoltages[i]));
        }
        
        dcDetailedAnalysisArea.setText(sb.toString());
        dcDetailedAnalysisArea.setCaretPosition(0);
    }

    // ========== MÉTODOS RLC ORIGINALES (Ahora usan I18N) ==========

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
        rlcInputCard = createModernCardPanel("", createInputPanel());
        panel.add(rlcInputCard);
        panel.add(Box.createVerticalStrut(15));

        // Método de simulación
        rlcMethodCard = createModernCardPanel("", createMethodPanel());
        panel.add(rlcMethodCard);
        panel.add(Box.createVerticalStrut(15));

        // Circuitos predefinidos
        rlcPresetCard = createModernCardPanel("", createPresetPanel());
        panel.add(rlcPresetCard);
        panel.add(Box.createVerticalStrut(15));

        // Componentes
        rlcComponentCard = createModernCardPanel("", createComponentPanel());
        panel.add(rlcComponentCard);
        panel.add(Box.createVerticalStrut(15));

        // Lista de componentes
        rlcListCard = createModernCardPanel("", createComponentListPanel());
        panel.add(rlcListCard);
        panel.add(Box.createVerticalStrut(15));

        // Botones de acción
        rlcActionCard = createModernCardPanel("", createCircuitActionPanel());
        panel.add(rlcActionCard);

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
        
        // Almacenar el label del título para I18N (si el panel es una tarjeta con título)
        // Esto es un truco, asumimos que el primer componente es el JLabel del título
        if (contentPanel.getParent() == cardPanel) {
             cardPanel.putClientProperty("titleLabel", titleLabel);
        }

        return cardPanel;
    }
    
    private void updateCardTitle(JPanel cardPanel, String key) {
        if (cardPanel != null) {
            JLabel titleLabel = (JLabel) cardPanel.getClientProperty("titleLabel");
            if (titleLabel != null) {
                titleLabel.setText(languageManager.getTranslation(key));
            }
        }
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
        procAlgorithmCard = createModernCardPanel("", 
            createSimpleComboBoxPanel("proc_select_algorithm", 
                new String[] { "proc_algo_fcfs", "proc_algo_rr", "proc_algo_sjf" }));
        algorithmCombo = findComboBoxInPanel(procAlgorithmCard);
        controlsPanel.add(procAlgorithmCard);
        controlsPanel.add(Box.createVerticalStrut(15));

        // Tipo de lote
        procBatchTypeCard = createModernCardPanel("", 
            createSimpleComboBoxPanel("proc_batch_config_label",
                new String[] { "proc_batch_simple", "proc_batch_medium", "proc_batch_complex",
                                "proc_batch_mixed" }));
        batchTypeCombo = findComboBoxInPanel(procBatchTypeCard);
        batchTypeCombo.addActionListener(e -> updateBatchControls());
        controlsPanel.add(procBatchTypeCard);
        controlsPanel.add(Box.createVerticalStrut(15));

        // Controles de batch
        procBatchConfigCard = createModernCardPanel("", createBatchControlsPanel());
        controlsPanel.add(procBatchConfigCard);
        controlsPanel.add(Box.createVerticalStrut(15));

        // Botones de control
        procExecCard = createModernCardPanel("", createSchedulingButtonPanel());
        controlsPanel.add(procExecCard);
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
        circuitTabs.addTab("", graphPanel); // Texto desde I18N

        // Pestaña 2: Resultados
        JPanel resultsPanel = createResultsPanel();
        JScrollPane resultsScroll = new JScrollPane(resultsPanel);
        resultsScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        circuitTabs.addTab("", resultsScroll); // Texto desde I18N

        // Pestaña 3: Análisis Detallado
        JPanel analysisPanel = createAnalysisPanel();
        circuitTabs.addTab("", analysisPanel); // Texto desde I18N

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
        rlcDiagramTitleLabel = new JLabel(""); // Texto desde I18N
        rlcDiagramTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rlcDiagramTitleLabel.setForeground(DARK_SLATE);
        rlcDiagramTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        cardPanel.add(rlcDiagramTitleLabel, BorderLayout.NORTH);

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
        rlcGraphTypeLabel = createModernLabel(""); // Texto desde I18N
        graphTypePanel.add(rlcGraphTypeLabel);

        graphTypeCombo = createModernComboBox();
        // Items se añaden en updateLanguage()
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

        updateInitialResultsText(); // Se actualizará con I18N

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
        // Texto inicial se setea en updateLanguage()

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
        sb.append(languageManager.getTranslation("rlc_results_placeholder_title")).append("\n\n");
        sb.append(languageManager.getTranslation("rlc_results_placeholder_inst")).append("\n");
        sb.append("    1. ").append(languageManager.getTranslation("rlc_add_components")).append("\n");
        sb.append("    2. ").append(languageManager.getTranslation("rlc_voltage")).append(" y ").append(languageManager.getTranslation("rlc_frequency")).append("\n");
        sb.append("    3. ").append(languageManager.getTranslation("rlc_simulation_method")).append("\n");
        sb.append("    4. ").append(languageManager.getTranslation("rlc_simulate_button")).append("\n");

        if (resultsArea != null) {
            resultsArea.setText(sb.toString());
        }
        
        if (analysisArea != null) {
            analysisArea.setText(languageManager.getTranslation("rlc_analysis_placeholder_title") + "\n\n...");
        }
    }

    // ========== PANEL DE VISUALIZACIÓN DE MEMORIA VIRTUAL ==========

    private JPanel createMemoryVisualizationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(LIGHT_SLATE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título principal
        memPanelTitleLabel = new JLabel("", JLabel.CENTER);
        memPanelTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        memPanelTitleLabel.setForeground(DARK_SLATE);
        memPanelTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(memPanelTitleLabel, BorderLayout.NORTH);

        // Crear un TabbedPane para Memoria e Historial
        processTabs = new JTabbedPane();
        setupModernTabbedPane(processTabs);

        // -- Pestaña 1: Memoria Virtual (Código existente) --
        JSplitPane memorySplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        memorySplitPane.setDividerLocation(150);
        memorySplitPane.setResizeWeight(0.3);
        memorySplitPane.setBorder(null);

        // Panel superior - Información de memoria
        JPanel infoPanel = createMemoryInfoPanel();
        memorySplitPane.setTopComponent(infoPanel);

        // Panel inferior - Visualización gráfica y colas
        JPanel visualizationPanel = createMemoryVisualizationGraphics();
        memorySplitPane.setBottomComponent(visualizationPanel);
        
        processTabs.addTab("", memorySplitPane); // Texto desde I18N

        // -- Pestaña 2: Historial de Simulaciones (Nuevo Panel) --
        JPanel historyPanel = createHistoryPanel(); // Nuevo método
        processTabs.addTab("", historyPanel); // Texto desde I18N

        panel.add(processTabs, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMemoryInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Título
        memSystemStatusLabel = new JLabel("");
        memSystemStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        memSystemStatusLabel.setForeground(DARK_SLATE);
        memSystemStatusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(memSystemStatusLabel, BorderLayout.NORTH);

        // Grid de información
        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        gridPanel.setBackground(CARD_BACKGROUND);

        // Primera fila
        memTotalFragLabel = createModernLabel("");
        memCurrentUsageLabel = createModernLabel("");
        fragTotalValue = createModernValueLabel("0.0 KB");
        memUsageValue = createModernValueLabel("0 KB (0.0%)");

        // Segunda fila
        memAvgUsageLabel = createModernLabel("");
        memExtFragLabel = createModernLabel("");
        avgUsageValue = createModernValueLabel("0 KB (0.0%)");
        extFragValue = createModernValueLabel("0.0 KB");

        // Tercera fila
        memIntFragLabel = createModernLabel("");
        memSemaphoreLabel = createModernLabel("");
        intFragValue = createModernValueLabel("0.0 KB");
        semaphoreValue = createModernValueLabel("1 (esperando: 0)");

        gridPanel.add(memTotalFragLabel);
        gridPanel.add(fragTotalValue);
        gridPanel.add(memCurrentUsageLabel);
        gridPanel.add(memUsageValue);
        gridPanel.add(memAvgUsageLabel);
        gridPanel.add(avgUsageValue);
        gridPanel.add(memExtFragLabel);
        gridPanel.add(extFragValue);
        gridPanel.add(memIntFragLabel);
        gridPanel.add(intFragValue);
        gridPanel.add(memSemaphoreLabel);
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

        memMapTitleLabel = new JLabel("");
        memMapTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        memMapTitleLabel.setForeground(DARK_SLATE);
        memoryPanel.add(memMapTitleLabel, BorderLayout.NORTH);

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
        vizSplitPane.setBorder(null);

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

        memCurrentProcessLabel = new JLabel("");
        memCurrentProcessLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        memCurrentProcessLabel.setForeground(DARK_SLATE);
        
        currentProcessLabel = createModernLabel(""); // Texto desde I18N
        currentProcessLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        memoryProgressBar = new JProgressBar(0, 100);
        setupModernProgressBar(memoryProgressBar);
        memoryProgressBar.setValue(0);
        memoryProgressBar.setString("0%");

        currentPanel.add(memCurrentProcessLabel, BorderLayout.NORTH);
        currentPanel.add(currentProcessLabel, BorderLayout.CENTER);
        currentPanel.add(memoryProgressBar, BorderLayout.SOUTH);

        // Colas de procesos
        JPanel queuesPanel = new JPanel(new BorderLayout());
        queuesPanel.setBackground(CARD_BACKGROUND);

        memQueuesTitleLabel = new JLabel("");
        memQueuesTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        memQueuesTitleLabel.setForeground(DARK_SLATE);
        memQueuesTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        processQueueArea = new JTextArea(10, 20);
        processQueueArea.setEditable(false);
        processQueueArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        processQueueArea.setBackground(new Color(248, 250, 252));
        processQueueArea.setForeground(DARK_SLATE);
        processQueueArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane queueScroll = new JScrollPane(processQueueArea);
        queueScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        queuesPanel.add(memQueuesTitleLabel, BorderLayout.NORTH);
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
            String text = languageManager.getTranslation("mem_free").toUpperCase();
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
            String pageText = pageOccupied ? processName : languageManager.getTranslation("mem_free");
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
        
        if (algorithm.equals(languageManager.getTranslation("proc_algo_fcfs"))) {
            fragmentation = 15.0 + (tasks.size() * 2.0);
            externalFrag = fragmentation * 0.7;
            internalFrag = fragmentation * 0.3;
        } else if (algorithm.equals(languageManager.getTranslation("proc_algo_rr"))) {
            fragmentation = 20.0 + (tasks.size() * 1.5);
            externalFrag = fragmentation * 0.5;
            internalFrag = fragmentation * 0.5;
        } else if (algorithm.equals(languageManager.getTranslation("proc_algo_sjf"))) {
            fragmentation = 10.0 + (tasks.size() * 1.0);
            externalFrag = fragmentation * 0.4;
            internalFrag = fragmentation * 0.6;
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
            currentProcessLabel.setText(languageManager.getTranslation("mem_none"));
            memoryProgressBar.setValue(0);
            memoryProgressBar.setString("0%");
        }
    }

    private void updateProcessQueues(List<CircuitSimulationTask> tasks) {
        StringBuilder sb = new StringBuilder();
        
        // Cola de listos
        sb.append(languageManager.getTranslation("mem_queue_ready")).append("\n");
        long readyCount = tasks.stream()
            .filter(t -> t.getState() == CircuitSimulationTask.TaskState.READY)
            .peek(t -> sb.append(String.format("T%d: %s (%s)\n", 
                t.getId(), t.getName(), t.getComplexity().getDisplayName())))
            .count();
        
        if (readyCount == 0) {
            sb.append(languageManager.getTranslation("mem_empty")).append("\n");
        }
        
        sb.append("\n").append(languageManager.getTranslation("mem_queue_running")).append("\n");
        CircuitSimulationTask running = getCurrentRunningTask();
        if (running != null) {
            sb.append(String.format("T%d: %s (%s)\n", 
                running.getId(), running.getName(), running.getComplexity().getDisplayName()));
            sb.append(String.format("Progreso: %.1f%%\n", running.getProgress()));
        } else {
            sb.append(languageManager.getTranslation("mem_none")).append("\n");
        }
        
        sb.append("\n").append(languageManager.getTranslation("mem_queue_completed")).append("\n");
        long completedCount = tasks.stream()
            .filter(t -> t.getState() == CircuitSimulationTask.TaskState.COMPLETED)
            .peek(t -> sb.append(String.format("T%d: %s\n", t.getId(), t.getName())))
            .count();
        
        if (completedCount == 0) {
            sb.append(languageManager.getTranslation("mem_none")).append("\n");
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
        // Busca en el primer nivel
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JComboBox) {
                try {
                    return (JComboBox<String>) comp;
                } catch (ClassCastException e) {
                    continue; // Ignorar si no es JComboBox<String>
                }
            }
        }
        // Busca recursivamente en sub-paneles
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

    private JPanel createSimpleComboBoxPanel(String labelKey, String[] itemKeys) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- MODIFICADO PARA I18N ---
        JLabel label;
        if ("proc_select_algorithm".equals(labelKey)) {
            procAlgorithmLabel = createModernLabel("");
            label = procAlgorithmLabel;
        } else if ("proc_batch_config_label".equals(labelKey)) {
            procBatchConfigLabel = createModernLabel("");
            label = procBatchConfigLabel;
        } else {
            label = createModernLabel(languageManager.getTranslation(labelKey));
        }
        // --- FIN MODIFICACIÓN ---

        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));

        JComboBox<String> comboBox = createModernComboBox();
        for (String item : itemKeys) {
            comboBox.addItem(languageManager.getTranslation(item)); // Traducir items
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

    // ========== MÉTODOS DE PANELES DE CONTROL (MODIFICADOS PARA I18N) ==========

    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel voltagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        voltagePanel.setBackground(CARD_BACKGROUND);
        rlcVoltageLabel = createModernLabel("");
        voltagePanel.add(rlcVoltageLabel);
        voltageField = createModernTextField("10", 10);
        voltagePanel.add(voltageField);
        voltagePanel.add(createModernLabel("V"));
        voltagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(voltagePanel);

        panel.add(Box.createVerticalStrut(8));

        JPanel frequencyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        frequencyPanel.setBackground(CARD_BACKGROUND);
        rlcFrequencyLabel = createModernLabel("");
        frequencyPanel.add(rlcFrequencyLabel);
        frequencyField = createModernTextField("60", 10);
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
        // Items se añaden en updateLanguage()
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

        presetCombo = createModernComboBox();
        // Items se añaden en updateLanguage()
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
        rlcTypeLabel = createModernLabel("");
        typePanel.add(rlcTypeLabel);
        componentTypeCombo = createModernComboBox();
        // Items se añaden en updateLanguage()
        componentTypeCombo.setMaximumSize(new Dimension(140, 35));
        typePanel.add(componentTypeCombo);
        typePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(typePanel);

        panel.add(Box.createVerticalStrut(8));

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        valuePanel.setBackground(CARD_BACKGROUND);
        rlcValueLabel = createModernLabel("");
        valuePanel.add(rlcValueLabel);
        valueField = createModernTextField("100", 12);
        valuePanel.add(valueField);
        valuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(valuePanel);

        panel.add(Box.createVerticalStrut(12));

        addButton = createModernButton("", SECONDARY_BLUE);
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
        componentsList.setBackground(new Color(248, 250, 252));
        componentsList.setForeground(DARK_SLATE);
        componentsList.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        componentsList.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane listScroll = new JScrollPane(componentsList);
        listScroll.setPreferredSize(new Dimension(300, 100));
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(listScroll, BorderLayout.CENTER);

        removeButton = createModernButton("", ERROR_ROSE);
        removeButton.setMaximumSize(new Dimension(220, 35));
        panel.add(removeButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCircuitActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        simulateButton = createModernButton("", SUCCESS_EMERALD);
        simulateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        simulateButton.setMaximumSize(new Dimension(220, 45));

        panel.add(Box.createVerticalStrut(8));

        clearButton = createModernButton("", ERROR_ROSE);
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

        procSimpleLabel = createModernLabel("");
        spinnersPanel.add(procSimpleLabel);
        simpleSpinner = createModernSpinner(3, 0, 20, 1);
        spinnersPanel.add(simpleSpinner);

        procMediumLabel = createModernLabel("");
        spinnersPanel.add(procMediumLabel);
        mediumSpinner = createModernSpinner(2, 0, 15, 1);
        spinnersPanel.add(mediumSpinner);

        procComplexLabel = createModernLabel("");
        spinnersPanel.add(procComplexLabel);
        complexSpinner = createModernSpinner(1, 0, 10, 1);
        spinnersPanel.add(complexSpinner);

        spinnersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(spinnersPanel);

        panel.add(Box.createVerticalStrut(12));

        generateBatchButton = createModernButton("", WARNING_AMBER);
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

        startSchedulerButton = createModernButton("", SUCCESS_EMERALD);
        startSchedulerButton.addActionListener(e -> startScheduling());

        stopSchedulerButton = createModernButton("", ERROR_ROSE);
        stopSchedulerButton.addActionListener(e -> stopScheduling());
        stopSchedulerButton.setEnabled(false);

        panel.add(startSchedulerButton);
        panel.add(stopSchedulerButton);

        return panel;
    }

    // ========== MÉTODOS DE PLANIFICACIÓN ==========

    private void updateBatchControls() {
        String selected = (String) batchTypeCombo.getSelectedItem();
        boolean isHeterogeneous = selected != null && selected.equals(languageManager.getTranslation("proc_batch_mixed"));

        simpleSpinner.setEnabled(isHeterogeneous);
        mediumSpinner.setEnabled(isHeterogeneous);
        complexSpinner.setEnabled(isHeterogeneous);
    }

    private void generateBatch() {
        String batchType = (String) batchTypeCombo.getSelectedItem();
        if (batchType == null)
            return;

        scheduler.clearTasks();

        if (batchType.contains(languageManager.getTranslation("proc_batch_simple"))) {
            List<CircuitSimulationTask> batch = scheduler.generateHomogeneousBatch(CircuitSimulationTask.Complexity.SIMPLE, 5);
            scheduler.addTasks(batch);
        } else if (batchType.contains(languageManager.getTranslation("proc_batch_medium"))) {
            List<CircuitSimulationTask> batch = scheduler.generateHomogeneousBatch(CircuitSimulationTask.Complexity.MEDIUM, 5);
            scheduler.addTasks(batch);
        } else if (batchType.contains(languageManager.getTranslation("proc_batch_complex"))) {
            List<CircuitSimulationTask> batch = scheduler.generateHomogeneousBatch(CircuitSimulationTask.Complexity.COMPLEX, 5);
            scheduler.addTasks(batch);
        } else if (batchType.contains(languageManager.getTranslation("proc_batch_mixed"))) {
            int simpleCount = (Integer) simpleSpinner.getValue();
            int mediumCount = (Integer) mediumSpinner.getValue();
            int complexCount = (Integer) complexSpinner.getValue();

            List<CircuitSimulationTask> batch = scheduler.generateHeterogeneousBatch(
                    simpleCount, mediumCount, complexCount);
            scheduler.addTasks(batch);
        }

        updateTasksTable();
        updateMemoryVisualization();
        logSchedulingMessage(languageManager.getFormattedTranslation("proc_batch_generated", scheduler.getTasks().size()));
    }

    private void startScheduling() {
        try {
            String algorithm = (String) algorithmCombo.getSelectedItem();
            if (algorithm != null) {
                if (algorithm.equals(languageManager.getTranslation("proc_algo_fcfs"))) {
                    scheduler.setStrategy(new FirstComeFirstServedScheduler());
                } else if (algorithm.equals(languageManager.getTranslation("proc_algo_rr"))) {
                    scheduler.setStrategy(new RoundRobinScheduler());
                } else if (algorithm.equals(languageManager.getTranslation("proc_algo_sjf"))) {
                    scheduler.setStrategy(new ShortestJobFirstScheduler());
                }
            }

            logSchedulingMessage(languageManager.getFormattedTranslation("proc_start_log", algorithm));
            scheduler.startSimulation();
            startUpdateTimer();

        } catch (Exception ex) {
            String errorMsg = languageManager.getFormattedTranslation("proc_start_error", ex.getMessage());
            logSchedulingMessage("ERROR: " + ex.getMessage());
            showError(errorMsg);
        }
    }

    private void stopScheduling() {
        scheduler.stopSimulation();
        stopUpdateTimer();
        logSchedulingMessage(languageManager.getTranslation("proc_stop_log"));
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
            if (schedulingLogArea != null) {
                schedulingLogArea.append("[" + timestamp + "] " + message + "\n");
                schedulingLogArea.setCaretPosition(schedulingLogArea.getDocument().getLength());
            }
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

        showInfo(languageManager.getFormattedTranslation("rlc_preset_loaded", selected));
    }

    private void addComponent() {
        try {
            String type = "";
            String selected = (String) componentTypeCombo.getSelectedItem();
            if (languageManager.getTranslation("resistance").equals(selected)) {
                type = "Resistance";
            } else if (languageManager.getTranslation("inductor").equals(selected)) {
                type = "Inductor";
            } else if (languageManager.getTranslation("capacitor").equals(selected)) {
                type = "Capacitor";
            }

            double value = Double.parseDouble(valueField.getText());
            if (value <= 0) {
                showError(languageManager.getTranslation("rlc_value_positive"));
                return;
            }

            CircuitComponent comp = new CircuitComponent(type, value);
            components.add(comp);
            updateComponentList();
            updateCircuitDiagram();

            valueField.setText("");
            valueField.requestFocus();

        } catch (NumberFormatException ex) {
            showError(languageManager.getTranslation("rlc_numeric_error"));
        }
    }

    private void removeComponent() {
        int index = componentsList.getSelectedIndex();
        if (index >= 0 && index < components.size()) {
            components.remove(index);
            updateComponentList();
            updateCircuitDiagram();
        } else {
            showError(languageManager.getTranslation("rlc_select_to_remove"));
        }
    }

    private void simulateCircuit() {
        if (components.isEmpty()) {
            showError(languageManager.getTranslation("rlc_add_one_component"));
            return;
        }

        if (!validateInputs())
            return;

        try {
            double voltage = Double.parseDouble(voltageField.getText());
            double frequency = Double.parseDouble(frequencyField.getText());

            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString(languageManager.getTranslation("sim_in_progress"));

            simulateButton.setEnabled(false);

            engine.simulate(components, voltage, frequency);

        } catch (NumberFormatException ex) {
            showError(languageManager.getTranslation("rlc_numeric_error"));
        }
    }

    private boolean validateInputs() {
        try {
            double voltage = Double.parseDouble(voltageField.getText());
            double frequency = Double.parseDouble(frequencyField.getText());

            if (voltage <= 0 || voltage > 1000) {
                showError(languageManager.getTranslation("rlc_voltage_range"));
                return false;
            }

            if (frequency <= 0 || frequency > 10000) {
                showError(languageManager.getTranslation("rlc_frequency_range"));
                return false;
            }

            return true;

        } catch (NumberFormatException e) {
            showError(languageManager.getTranslation("rlc_numeric_error"));
            return false;
        }
    }

    private void clearAll() {
        components.clear();
        updateComponentList();
        updateCircuitDiagram();

        if (resultsArea != null) {
            resultsArea.setText(languageManager.getTranslation("rlc_cleared"));
        }
        lastResult = null;

        showInfo(languageManager.getTranslation("rlc_cleared_all"));
    }

    private void updateComponentList() {
        componentsModel.clear();
        for (CircuitComponent comp : components) {
            componentsModel.addElement(comp.toString()); // toString() usa LanguageManager
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
                sb.append(languageManager.getTranslation("simulation_results")).append("\n\n");
                sb.append(languageManager.getTranslation("impedance")).append(" ").append(df.format(simResult.getImpedance())).append(" Ω\n");
                sb.append(languageManager.getTranslation("current")).append(" ").append(df.format(simResult.getCurrent())).append(" A\n");
                sb.append(languageManager.getTranslation("phase_angle")).append(" ").append(df.format(Math.toDegrees(simResult.getPhaseAngle())))
                        .append("°\n");
                sb.append(languageManager.getTranslation("active_power")).append(" ").append(df.format(simResult.getActivePower())).append(" W\n");
                sb.append(languageManager.getTranslation("reactive_power")).append(" ").append(df.format(simResult.getReactivePower())).append(" VAR\n");
                sb.append(languageManager.getTranslation("apparent_power")).append(" ").append(df.format(simResult.getApparentPower())).append(" VA\n");
                sb.append(languageManager.getTranslation("power_factor")).append(" ").append(df.format(simResult.getPowerFactor())).append("\n\n");

                double phaseDeg = Math.toDegrees(simResult.getPhaseAngle());
                String circuitType;
                if (phaseDeg > 0) {
                    circuitType = languageManager.getTranslation("inductive_circuit");
                } else if (phaseDeg < 0) {
                    circuitType = languageManager.getTranslation("capacitive_circuit");
                } else {
                    circuitType = languageManager.getTranslation("resistive_circuit");
                }

                sb.append(circuitType).append("\n");

                if (resultsArea != null) {
                    resultsArea.setText(sb.toString());
                }

                progressBar.setVisible(false);
                simulateButton.setEnabled(true);

                showInfo(languageManager.getTranslation("sim_complete"));

            } else {
                onSimulationError("Resultado de simulación inválido");
            }
        });
    }

    @Override
    public void onSimulationError(String error) {
        SwingUtilities.invokeLater(() -> {
            String detailedError = languageManager.getFormattedTranslation("sim_error_generic", error);

            showError(detailedError);

            progressBar.setVisible(false);
            simulateButton.setEnabled(true);

            if (resultsArea != null) {
                resultsArea.setText(languageManager.getFormattedTranslation("sim_error_details", error));
            }
        });
    }

    @Override
    public void onSimulationStart() {
        SwingUtilities.invokeLater(() -> {
            if (resultsArea != null) {
                resultsArea.setText(languageManager.getTranslation("sim_in_progress") + "\n\n" + languageManager.getTranslation("please_wait"));
            }
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString(languageManager.getTranslation("sim_in_progress"));
        });
    }

    private void updateAnalysisPanel(SimulationResult result) {
        // (Este método se mantiene igual que antes, pero ahora su texto inicial se setea en updateLanguage)
        if (analysisArea == null) return;
        
        StringBuilder analysis = new StringBuilder();
        analysis.append(languageManager.getTranslation("rlc_analysis_placeholder_title")).append("\n\n");
        
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
        JOptionPane.showMessageDialog(this, message, languageManager.getTranslation("error"), JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, languageManager.getTranslation("info"), JOptionPane.INFORMATION_MESSAGE);
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

    // --- INICIO DE MODIFICACIÓN (HISTORIAL DE PROCESOS) ---

    /**
     * Crea el panel que contiene la tabla del historial de simulaciones.
     */
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Columnas para la tabla de historial (basado en el output de printMetrics)
        String[] columnNames = {
            "ID", "Algoritmo", "T. Total (ms)", "Tareas", 
            "Avg. Turnaround (ms)", "Avg. Wait (ms)", "CPU Util (%)",
            "T. Simple (ms)", "T. Medio (ms)", "T. Complejo (ms)"
        };

        historyTableModel = new DefaultTableModel(columnNames, 0);
        historyTable = new JTable(historyTableModel);
        
        // Hacer la tabla no editable
        historyTable.setDefaultEditor(Object.class, null);
        historyTable.getTableHeader().setReorderingAllowed(false);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        historyTable.setRowHeight(20);

        // Renderizador para centrar texto
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        // Aplicar renderizador a todas las columnas
        for (int i = 0; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BACKGROUND);
        
        historyExportButton = createModernButton("", SECONDARY_BLUE); // Texto desde I18N
        historyExportButton.addActionListener(e -> exportHistory()); 
        buttonPanel.add(historyExportButton);
        
        historyClearButton = createModernButton("", ERROR_ROSE); // Texto desde I18N
        historyClearButton.addActionListener(e -> clearSimulationHistory());
        buttonPanel.add(historyClearButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Limpia el historial de simulaciones y la tabla.
     */
    private void clearSimulationHistory() {
        this.simulationHistory.clear();
        if (historyTableModel != null) {
            historyTableModel.setRowCount(0);
        }
        logSchedulingMessage(languageManager.getTranslation("history_cleared_log"));
    }

    /**
     * Añade una nueva fila a la tabla de historial con las métricas de la simulación.
     */
    private void addMetricsToHistoryTable(com.simulador.scheduler.SchedulerMetrics metrics) {
        if (historyTableModel == null) return;

        String algorithm = scheduler.getCurrentStrategy() != null ? scheduler.getCurrentStrategy().getName() : "N/A";
        long totalTime = metrics.getTotalExecutionTime();
        int taskCount = metrics.getTasks().size(); // Total de tareas en el lote
        double avgTurnaround = metrics.getAverageTurnaroundTime();
        double avgWait = metrics.getAverageWaitingTime();
        double cpuUtil = metrics.getCPUUtilization();

        // Obtener métricas por complejidad (asumiendo que SchedulerMetrics fue modificado)
        double simpleTurnaround = metrics.getAverageTurnaroundTime(CircuitSimulationTask.Complexity.SIMPLE);
        double mediumTurnaround = metrics.getAverageTurnaroundTime(CircuitSimulationTask.Complexity.MEDIUM);
        double complexTurnaround = metrics.getAverageTurnaroundTime(CircuitSimulationTask.Complexity.COMPLEX);

        // Formatear para mostrar "N/A" si no hay tareas de ese tipo
        String sSimple = metrics.getTaskCount(CircuitSimulationTask.Complexity.SIMPLE) > 0 ? df.format(simpleTurnaround) : "N/A";
        String sMedium = metrics.getTaskCount(CircuitSimulationTask.Complexity.MEDIUM) > 0 ? df.format(mediumTurnaround) : "N/A";
        String sComplex = metrics.getTaskCount(CircuitSimulationTask.Complexity.COMPLEX) > 0 ? df.format(complexTurnaround) : "N/A";

        // Añadir la fila a la tabla
        historyTableModel.addRow(new Object[]{
            historyTableModel.getRowCount() + 1, // ID (simple conteo)
            algorithm,
            totalTime,
            taskCount,
            df.format(avgTurnaround),
            df.format(avgWait),
            df.format(cpuUtil), // Añadida CPU Util
            sSimple,
            sMedium,
            sComplex
        });
    }

    /**
     * Abre un JFileChooser para permitir al usuario guardar el historial como CSV o TXT.
     */
    private void exportHistory() {
        if (historyTableModel.getRowCount() == 0) {
            showError(languageManager.getTranslation("history_export_error_empty"));
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(languageManager.getTranslation("history_export_title"));

        // Filtro para TXT (Pretty Print)
        javax.swing.filechooser.FileFilter txtFilter = new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
            }
            @Override
            public String getDescription() {
                return languageManager.getTranslation("file_filter_txt");
            }
        };
        
        // Filtro para CSV (Excel, Google Sheets)
        javax.swing.filechooser.FileFilter csvFilter = new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            @Override
            public String getDescription() {
                return languageManager.getTranslation("file_filter_csv");
            }
        };

        fileChooser.addChoosableFileFilter(txtFilter);
        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.setFileFilter(txtFilter); // TXT por defecto
        fileChooser.setSelectedFile(new File("historial_simulacion.txt"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String selectedExtension = "";
            
            // Determinar qué filtro se seleccionó
            if (fileChooser.getFileFilter() == txtFilter) {
                selectedExtension = ".txt";
            } else if (fileChooser.getFileFilter() == csvFilter) {
                selectedExtension = ".csv";
            } else {
                if (fileToSave.getName().toLowerCase().endsWith(".csv")) {
                    selectedExtension = ".csv";
                } else {
                    selectedExtension = ".txt"; // Default
                }
            }

            // Asegurar la extensión correcta
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(selectedExtension)) {
                fileToSave = new File(filePath + selectedExtension);
            }

            // Llamar al método de escritura apropiado
            try {
                if (selectedExtension.equals(".txt")) {
                    writeHistoryAsTXT(fileToSave);
                } else {
                    writeHistoryAsCSV(fileToSave);
                }
                showInfo(languageManager.getFormattedTranslation("history_export_success", fileToSave.getAbsolutePath()));
            } catch (IOException e) {
                showError(languageManager.getFormattedTranslation("history_export_error_io", e.getMessage()));
                e.printStackTrace();
            }
        }
    }

    /**
     * Escribe el historial de la tabla a un archivo en formato CSV.
     */
    private void writeHistoryAsCSV(File fileToSave) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave))) {
            // Escribir cabeceras
            for (int i = 0; i < historyTableModel.getColumnCount(); i++) {
                bw.write(escapeCSV(historyTableModel.getColumnName(i)));
                if (i < historyTableModel.getColumnCount() - 1) {
                    bw.write(",");
                }
            }
            bw.newLine();

            // Escribir datos
            for (int row = 0; row < historyTableModel.getRowCount(); row++) {
                for (int col = 0; col < historyTableModel.getColumnCount(); col++) {
                    Object value = historyTableModel.getValueAt(row, col);
                    bw.write(escapeCSV(value != null ? value.toString() : ""));
                    if (col < historyTableModel.getColumnCount() - 1) {
                        bw.write(",");
                    }
                }
                bw.newLine();
            }
        }
    }

    /**
     * Escribe el historial de la tabla a un archivo en formato de texto
     * alineado (pretty-print).
     */
    private void writeHistoryAsTXT(File fileToSave) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave))) {
            // Definir formatos de alineación
            String headerFormat = "%-5s | %-30s | %-12s | %-8s | %-20s | %-18s | %-12s | %-15s | %-15s | %-15s%n";
            String rowFormat      = "%-5s | %-30s | %-12s | %-8s | %-20s | %-18s | %-12s | %-15s | %-15s | %-15s%n";

            // Escribir cabeceras (usando los nombres de columna de la tabla)
            bw.write(String.format(headerFormat, 
                historyTableModel.getColumnName(0), historyTableModel.getColumnName(1), historyTableModel.getColumnName(2),
                historyTableModel.getColumnName(3), historyTableModel.getColumnName(4), historyTableModel.getColumnName(5),
                historyTableModel.getColumnName(6), historyTableModel.getColumnName(7), historyTableModel.getColumnName(8),
                historyTableModel.getColumnName(9)
            ));
            
            // Escribir línea separadora
            for(int i = 0; i < 165; i++) bw.write("-");
            bw.newLine();

            // Escribir datos
            for (int row = 0; row < historyTableModel.getRowCount(); row++) {
                bw.write(String.format(rowFormat,
                    historyTableModel.getValueAt(row, 0).toString(),
                    historyTableModel.getValueAt(row, 1).toString(),
                    historyTableModel.getValueAt(row, 2).toString(),
                    historyTableModel.getValueAt(row, 3).toString(),
                    historyTableModel.getValueAt(row, 4).toString(),
                    historyTableModel.getValueAt(row, 5).toString(),
                    historyTableModel.getValueAt(row, 6).toString(),
                    historyTableModel.getValueAt(row, 7).toString(),
                    historyTableModel.getValueAt(row, 8).toString(),
                    historyTableModel.getValueAt(row, 9).toString()
                ));
            }
        }
    }

    /**
     * Helper para formatear texto para CSV (maneja comas y comillas).
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
    // --- FIN DE MODIFICACIÓN ---
    
    // --- INICIO DE MODIFICACIÓN (I18N) ---
    /**
     * Actualiza todos los componentes de la UI con el idioma actual del LanguageManager.
     */
    private void updateLanguage() {
        // Menú
        languageManager.updateComponentText(optionsMenu, "menu_options");
        languageManager.updateComponentText(languageMenu, "menu_language");
        languageManager.updateComponentText(spanishItem, "lang_es");
        languageManager.updateComponentText(portugueseItem, "lang_pt");

        // Header
        languageManager.updateComponentText(headerTitleLabel, "header_title");
        languageManager.updateComponentText(headerSubtitleLabel, "header_subtitle");

        // Pestañas Izquierdas
        mainTabbedPane.setTitleAt(0, languageManager.getTranslation("tab_rlc"));
        mainTabbedPane.setTitleAt(1, languageManager.getTranslation("tab_dc"));
        mainTabbedPane.setTitleAt(2, languageManager.getTranslation("tab_process"));

        // --- Pestaña RLC ---
        updateCardTitle(rlcInputCard, "rlc_power_supply");
        languageManager.updateComponentText(rlcVoltageLabel, "rlc_voltage");
        languageManager.updateComponentText(rlcFrequencyLabel, "rlc_frequency");
        languageManager.updateToolTipText(voltageField, "rlc_voltage_tooltip");
        languageManager.updateToolTipText(frequencyField, "rlc_frequency_tooltip");
        
        updateCardTitle(rlcMethodCard, "rlc_simulation_method");
        languageManager.updateComboBox(methodCombo, new String[]{"analytical", "euler", "runge_kutta"});
        languageManager.updateToolTipText(methodCombo, "rlc_method_tooltip");
        
        updateCardTitle(rlcPresetCard, "rlc_presets");
        languageManager.updateComboBox(presetCombo, new String[]{"custom", "underdamped", "critical", "overdamped", "series_rlc", "high_pass", "low_pass"});
        languageManager.updateToolTipText(presetCombo, "rlc_preset_tooltip");

        updateCardTitle(rlcComponentCard, "rlc_add_components");
        languageManager.updateComponentText(rlcTypeLabel, "rlc_type");
        languageManager.updateComponentText(rlcValueLabel, "rlc_value");
        languageManager.updateComboBox(componentTypeCombo, new String[]{"resistance", "inductor", "capacitor"});
        languageManager.updateToolTipText(valueField, "rlc_value_tooltip");
        languageManager.updateComponentText(addButton, "rlc_add_button");
        languageManager.updateToolTipText(addButton, "rlc_add_tooltip");
        
        updateCardTitle(rlcListCard, "rlc_component_list");
        languageManager.updateToolTipText(componentsList, "rlc_list_tooltip");
        languageManager.updateComponentText(removeButton, "rlc_remove_button");
        languageManager.updateToolTipText(removeButton, "rlc_remove_tooltip");
        updateComponentList(); // Re-traducir items en la lista

        updateCardTitle(rlcActionCard, "rlc_actions");
        languageManager.updateComponentText(simulateButton, "rlc_simulate_button");
        languageManager.updateToolTipText(simulateButton, "rlc_simulate_tooltip");
        languageManager.updateComponentText(clearButton, "rlc_clear_button");
        languageManager.updateToolTipText(clearButton, "rlc_clear_tooltip");

        // --- Pestaña DC ---
        updateCardTitle(dcConfigCard, "dc_config_circuit");
        languageManager.updateComponentText(dcNumBranchesLabel, "dc_num_branches");
        languageManager.updateToolTipText(branchSpinner, "dc_branches_tooltip");
        languageManager.updateComponentText(dcConfigLabel, "dc_config");
        languageManager.updateComboBox(configCombo, new String[]{"dc_config_series", "dc_config_parallel", "dc_config_mixed"});
        
        updateCardTitle(dcComponentCard, "dc_add_components");
        languageManager.updateComponentText(dcTypeLabel, "dc_type");
        languageManager.updateComboBox(dcComponentTypeCombo, new String[]{"dc_type_battery", "dc_type_resistor", "dc_type_source"});
        languageManager.updateComponentText(dcValueLabel, "dc_value");
        languageManager.updateToolTipText(dcValueField, "dc_value_tooltip");
        languageManager.updateComponentText(dcPolarityLabel, "dc_polarity");
        languageManager.updateComboBox(dcPolarityCombo, new String[]{"dc_polarity_up", "dc_polarity_down"});
        languageManager.updateToolTipText(dcPolarityCombo, "dc_polarity_tooltip");
        languageManager.updateComponentText(dcInBranchLabel, "dc_in_branch");
        languageManager.updateToolTipText(targetBranchSpinner, "dc_in_branch_tooltip");
        languageManager.updateComponentText(addDCButton, "dc_add_button");
        languageManager.updateToolTipText(addDCButton, "dc_add_tooltip");
        
        updateCardTitle(dcMethodCard, "dc_analysis_method");
        languageManager.updateComboBox(dcMethodCombo, new String[]{"dc_method_ohm", "dc_method_kirchhoff", "dc_method_mesh", "dc_method_nodal", "dc_method_thevenin", "dc_method_norton", "dc_method_source"});
        languageManager.updateToolTipText(dcMethodCombo, "dc_method_tooltip");

        updateCardTitle(dcActionCard, "dc_actions");
        languageManager.updateComponentText(simulateDCButton, "dc_simulate_button");
        languageManager.updateToolTipText(simulateDCButton, "dc_simulate_tooltip");
        languageManager.updateComponentText(clearDCButton, "dc_clear_button");
        languageManager.updateToolTipText(clearDCButton, "dc_clear_tooltip");

        // --- Pestaña Procesos ---
        updateCardTitle(procAlgorithmCard, "proc_algorithm");
        languageManager.updateComponentText(procAlgorithmLabel, "proc_select_algorithm");
        languageManager.updateComboBox(algorithmCombo, new String[]{"proc_algo_fcfs", "proc_algo_rr", "proc_algo_sjf"});
        
        updateCardTitle(procBatchTypeCard, "proc_batch_type");
        languageManager.updateComponentText(procBatchConfigLabel, "proc_batch_config_label");
        languageManager.updateComboBox(batchTypeCombo, new String[]{"proc_batch_simple", "proc_batch_medium", "proc_batch_complex", "proc_batch_mixed"});
        
        updateCardTitle(procBatchConfigCard, "proc_batch_config");
        languageManager.updateComponentText(procSimpleLabel, "proc_simple");
        languageManager.updateComponentText(procMediumLabel, "proc_medium");
        languageManager.updateComponentText(procComplexLabel, "proc_complex");
        languageManager.updateComponentText(generateBatchButton, "proc_generate_batch");

        updateCardTitle(procExecCard, "proc_execution_control");
        languageManager.updateComponentText(startSchedulerButton, "proc_start_button");
        languageManager.updateComponentText(stopSchedulerButton, "proc_stop_button");

        // --- Panel RLC (Derecha) ---
        languageManager.updateComponentText(rlcDiagramTitleLabel, "rlc_diagram_title");
        circuitTabs.setTitleAt(0, languageManager.getTranslation("rlc_tab_visualization"));
        circuitTabs.setTitleAt(1, languageManager.getTranslation("rlc_tab_results"));
        circuitTabs.setTitleAt(2, languageManager.getTranslation("rlc_tab_analysis"));
        languageManager.updateComponentText(rlcGraphTypeLabel, "rlc_graph_type");
        languageManager.updateComboBox(graphTypeCombo, new String[]{"rlc_graph_time", "rlc_graph_frequency", "rlc_graph_phasor", "rlc_graph_waveforms"});
        updateInitialResultsText(); // Actualiza placeholders

        // --- Panel DC (Derecha) ---
        languageManager.updateComponentText(dcPanelTitleLabel, "dc_panel_title");
        languageManager.updateComponentText(dcDiagramTitleLabel, "dc_diagram_title");
        dcResultsTabs.setTitleAt(0, languageManager.getTranslation("dc_tab_main_results"));
        dcResultsTabs.setTitleAt(1, languageManager.getTranslation("mem_tab_history"));
        
        languageManager.updateComponentText(memSystemStatusLabel, "mem_system_status");
        languageManager.updateComponentText(memTotalFragLabel, "mem_total_frag");
        languageManager.updateComponentText(memCurrentUsageLabel, "mem_current_usage");
        languageManager.updateComponentText(memAvgUsageLabel, "mem_avg_usage");
        languageManager.updateComponentText(memExtFragLabel, "mem_ext_frag");
        languageManager.updateComponentText(memIntFragLabel, "mem_int_frag");
        languageManager.updateComponentText(memSemaphoreLabel, "mem_semaphore");
        
        languageManager.updateComponentText(memMapTitleLabel, "mem_map_title");
        languageManager.updateComponentText(memCurrentProcessLabel, "mem_current_process");
        languageManager.updateComponentText(memQueuesTitleLabel, "mem_queues_title");
        
        languageManager.updateComponentText(historyExportButton, "history_export_button");
        languageManager.updateToolTipText(historyExportButton, "history_export_tooltip");
        languageManager.updateComponentText(historyClearButton, "history_clear_button");
        languageManager.updateToolTipText(historyClearButton, "history_clear_tooltip");

        // Actualizar datos que dependen del idioma
        updateMemoryVisualization(); 
    }
    // --- FIN DE MODIFICACIÓN (I18N) ---
}