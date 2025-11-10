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
 */
public class RLCSimulator extends JPanel implements SimulationObserver {
    private CircuitEngine engine;
    private ProcessScheduler scheduler;
    private java.util.List<CircuitComponent> components;
    private SimulationResult lastResult;
    private DecimalFormat df = new DecimalFormat("0.000");
    private LanguageManager languageManager;

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

    // Componentes para gráficos
    private BaseGraph currentGraph;
    private JPanel graphContainer;
    private JComboBox<String> graphTypeCombo;

    // Componentes para análisis
    private JTextArea analysisArea;

    public RLCSimulator() {
        this.engine = new CircuitEngine();
        this.scheduler = new ProcessScheduler();
        this.components = new ArrayList<>();
        this.languageManager = LanguageManager.getInstance();
        this.updateTimer = null;

        initializeEngines();
        initializeUI();
        setupEventHandlers();
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
                SwingUtilities.invokeLater(this::updateTasksTable);
            }
        });
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(LIGHT_SLATE);

        // Header mejorado
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Panel principal dividido en izquierda y derecha
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(400);
        mainSplitPane.setResizeWeight(0.4);
        mainSplitPane.setBorder(BorderFactory.createEmptyBorder());

        // Panel izquierdo - Controles
        JPanel leftPanel = createControlsPanel();
        mainSplitPane.setLeftComponent(leftPanel);

        // Panel derecho - Visualización
        JPanel rightPanel = createVisualizationPanel();
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
        
        JLabel titleLabel = new JLabel("Simulador Avanzado de Circuitos RLC", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(25, 0, 5, 0));
        
        JLabel subtitleLabel = new JLabel("Con Algoritmos de Planificación Integrados - Analisis en Tiempo Real", JLabel.CENTER);
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

        // Pestaña 1: Simulación de Circuitos
        JPanel circuitPanel = createCircuitControlsPanel();
        JScrollPane circuitScroll = new JScrollPane(circuitPanel);
        circuitScroll.setBorder(null);
        circuitScroll.getVerticalScrollBar().setUnitIncrement(16);
        circuitScroll.setBackground(LIGHT_SLATE);
        tabbedPane.addTab("Circuito RLC", circuitScroll);

        // Pestaña 2: Planificación de Procesos
        JPanel schedulingPanel = createSchedulingControlsPanel();
        JScrollPane schedulingScroll = new JScrollPane(schedulingPanel);
        schedulingScroll.setBorder(null);
        schedulingScroll.getVerticalScrollBar().setUnitIncrement(16);
        schedulingScroll.setBackground(LIGHT_SLATE);
        tabbedPane.addTab("Planificación", schedulingScroll);

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

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
        JPanel methodPanel = createModernCardPanel("Metodo de Simulacion", createMethodPanel());
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
        JPanel algorithmPanel = createModernCardPanel("Algoritmo de Planificacion", 
            createSimpleComboBoxPanel("Seleccione algoritmo:", 
                new String[] { "First-Come, First-Served (FCFS)", "Round Robin (RR)", "Shortest Job First (SJF)" }));
        algorithmCombo = findComboBoxInPanel(algorithmPanel);
        controlsPanel.add(algorithmPanel);
        controlsPanel.add(Box.createVerticalStrut(15));

        // Tipo de lote
        JPanel batchPanel = createModernCardPanel("Tipo de Lote", 
            createSimpleComboBoxPanel("Configuracion del lote:",
                new String[] { "Homogeneo - Simple", "Homogeneo - Medio", "Homogeneo - Complejo",
                        "Heterogeneo - Mixto" }));
        batchTypeCombo = findComboBoxInPanel(batchPanel);
        if (batchTypeCombo == null) {
            batchTypeCombo = new JComboBox<>(new String[] {
                    "Homogeneo - Simple", "Homogeneo - Medio", "Homogeneo - Complejo", "Heterogeneo - Mixto"
            });
            batchPanel.add(batchTypeCombo);
        }
        batchTypeCombo.addActionListener(e -> updateBatchControls());
        controlsPanel.add(batchPanel);
        controlsPanel.add(Box.createVerticalStrut(15));

        // Controles de batch
        JPanel batchControlsPanel = createModernCardPanel("Configuracion del Lote", createBatchControlsPanel());
        controlsPanel.add(batchControlsPanel);
        controlsPanel.add(Box.createVerticalStrut(15));

        // Botones de control
        JPanel buttonPanel = createModernCardPanel("Control de Ejecucion", createSchedulingButtonPanel());
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

        // Panel inferior - Log de Planificación y Tareas
        JPanel logPanel = createSchedulingLogPanelForLeft();

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

    private JPanel createSchedulingLogPanelForLeft() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        panel.setBackground(LIGHT_SLATE);
        panel.setPreferredSize(new Dimension(350, 250));

        // Crear pestañas para Log y Tareas
        JTabbedPane logTabs = new JTabbedPane(JTabbedPane.TOP);
        logTabs.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        setupModernTabbedPane(logTabs);

        // Pestaña de Tareas
        JPanel tasksPanel = new JPanel(new BorderLayout());
        tasksPanel.setBackground(CARD_BACKGROUND);
        tasksPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = { "ID", "Nombre", "Complejidad", "Duracion (ms)", "Estado", "Progreso" };
        tasksTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tasksTable = new JTable(tasksTableModel);
        tasksTable.setAutoCreateRowSorter(true);
        tasksTable.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        tasksTable.setBackground(Color.WHITE);
        tasksTable.setRowHeight(25);
        tasksTable.setShowGrid(false);
        tasksTable.setIntercellSpacing(new Dimension(0, 0));

        // Renderizado mejorado para la tabla
        tasksTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (isSelected) {
                    c.setBackground(PRIMARY_BLUE);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(248, 250, 252) : Color.WHITE);
                    c.setForeground(DARK_SLATE);
                }
                
                setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
                setFont(new Font("Segoe UI", Font.PLAIN, 10));
                
                return c;
            }
        });

        JScrollPane tableScroll = new JScrollPane(tasksTable);
        tableScroll.setPreferredSize(new Dimension(300, 120));
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        tableScroll.getViewport().setBackground(Color.WHITE);
        tasksPanel.add(tableScroll, BorderLayout.CENTER);

        // Pestaña de Log
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(CARD_BACKGROUND);
        logPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
        logPanel.add(logScroll, BorderLayout.CENTER);

        // Agregar pestañas
        logTabs.addTab("Tareas", tasksPanel);
        logTabs.addTab("Log", logPanel);

        panel.add(logTabs, BorderLayout.CENTER);
        return panel;
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

    private JPanel createVisualizationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(LIGHT_SLATE);

        // Panel superior: Diagrama del circuito
        JPanel diagramPanel = createCircuitPanel();
        panel.add(diagramPanel, BorderLayout.NORTH);

        // Panel central: Pestañas para gráficos y resultados
        JTabbedPane centerTabs = new JTabbedPane(JTabbedPane.TOP);
        centerTabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        setupModernTabbedPane(centerTabs);

        // Pestaña 1: Visualización (Gráficos)
        JPanel graphPanel = createGraphPanel();
        centerTabs.addTab("Visualizacion", graphPanel);

        // Pestaña 2: Resultados
        JPanel resultsPanel = createResultsPanel();
        JScrollPane resultsScroll = new JScrollPane(resultsPanel);
        resultsScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        centerTabs.addTab("Resultados", resultsScroll);

        // Pestaña 3: Analisis Detallado
        JPanel analysisPanel = createAnalysisPanel();
        centerTabs.addTab("Analisis", analysisPanel);

        panel.add(centerTabs, BorderLayout.CENTER);

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
                "=== ANALISIS DETALLADO DEL CIRCUITO ===\n\n" +
                "Esta seccion muestra analisis avanzados:\n\n" +
                "- Parametros del circuito en diferentes frecuencias\n" +
                "- Comportamiento transitorio vs permanente\n" +
                "- Analisis de estabilidad del sistema\n" +
                "- Respuesta a diferentes tipos de entrada\n" +
                "- Analisis de sensibilidad de componentes\n\n" +
                "Ejecute una simulacion para ver los analisis detallados.");

        JScrollPane scroll = new JScrollPane(analysisArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

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
        methodCombo.setToolTipText("Metodo de calculo para la simulacion");
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
        simulateButton.setToolTipText("Ejecutar simulacion del circuito actual");
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

        startSchedulerButton = createModernButton("Iniciar Planificacion", SUCCESS_EMERALD);
        startSchedulerButton.addActionListener(e -> startScheduling());

        stopSchedulerButton = createModernButton("Detener", ERROR_ROSE);
        stopSchedulerButton.addActionListener(e -> stopScheduling());
        stopSchedulerButton.setEnabled(false);

        panel.add(startSchedulerButton);
        panel.add(stopSchedulerButton);

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
        graphTypePanel.add(createModernLabel("Tipo de Grafico:"));

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

    // ========== COMPONENTES MODERNOS ==========

    private JLabel createModernLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(DARK_SLATE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        return label;
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

    // ========== MÉTODOS DE PLANIFICACIÓN ==========

    private void updateBatchControls() {
        String selected = (String) batchTypeCombo.getSelectedItem();
        boolean isHeterogeneous = selected != null && selected.contains("Heterogeneo");

        simpleSpinner.setEnabled(isHeterogeneous);
        mediumSpinner.setEnabled(isHeterogeneous);
        complexSpinner.setEnabled(isHeterogeneous);
    }

    private void generateBatch() {
        String batchType = (String) batchTypeCombo.getSelectedItem();
        if (batchType == null)
            return;

        scheduler.clearTasks();

        if (batchType.contains("Homogeneo")) {
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
        } else if (batchType.contains("Heterogeneo")) {
            int simpleCount = (Integer) simpleSpinner.getValue();
            int mediumCount = (Integer) mediumSpinner.getValue();
            int complexCount = (Integer) complexSpinner.getValue();

            List<CircuitSimulationTask> batch = scheduler.generateHeterogeneousBatch(
                    simpleCount, mediumCount, complexCount);
            scheduler.addTasks(batch);
        }

        updateTasksTable();
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

            logSchedulingMessage("Iniciando planificacion con " + algorithm);
            scheduler.startSimulation();
            startUpdateTimer();

        } catch (Exception ex) {
            logSchedulingMessage("ERROR: " + ex.getMessage());
            showError("Error al iniciar planificacion: " + ex.getMessage());
        }
    }

    private void stopScheduling() {
        scheduler.stopSimulation();
        stopUpdateTimer();
        logSchedulingMessage("Planificacion detenida");
    }

    private void startUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
        updateTimer = new Timer(500, e -> updateTasksTable());
        updateTimer.start();
    }

    private void stopUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.stop();
            updateTimer = null;
        }
    }

    private void updateTasksTable() {
        if (tasksTableModel == null)
            return;

        SwingUtilities.invokeLater(() -> {
            tasksTableModel.setRowCount(0);

            for (CircuitSimulationTask task : scheduler.getTasks()) {
                Object[] row = {
                        task.getId(),
                        task.getName(),
                        task.getComplexity().getDisplayName(),
                        task.getEstimatedDuration(),
                        task.getState().getDisplayName(),
                        String.format("%.1f%%", task.getProgress())
                };
                tasksTableModel.addRow(row);
            }

            // Actualizar barra de progreso
            if (scheduler.isSimulationRunning()) {
                long completed = scheduler.getTasks().stream()
                        .filter(t -> t.getState() == CircuitSimulationTask.TaskState.COMPLETED)
                        .count();
                long total = scheduler.getTasks().size();

                int progress = total > 0 ? (int) ((completed * 100) / total) : 0;
                schedulingProgressBar.setValue(progress);
                schedulingProgressBar.setString(String.format("%d/%d tareas (%d%%)", completed, total, progress));
            }
        });
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
        // Handlers para simulación de circuitos
        methodCombo.addActionListener(e -> updateStrategy());
        presetCombo.addActionListener(e -> loadPreset());
        addButton.addActionListener(e -> addComponent());
        removeButton.addActionListener(e -> removeComponent());
        simulateButton.addActionListener(e -> simulateCircuit());
        clearButton.addActionListener(e -> clearAll());
        valueField.addActionListener(e -> addComponent());
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
            showError("Ingrese valores numericos validos");
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
            progressBar.setString("Simulacion en progreso...");

            simulateButton.setEnabled(false);

            engine.simulate(components, voltage, frequency);

        } catch (NumberFormatException ex) {
            showError("Ingrese valores numericos validos");
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
            showError("Ingrese valores numericos validos para voltaje y frecuencia");
            return false;
        }
    }

    private void clearAll() {
        components.clear();
        updateComponentList();
        updateCircuitDiagram();

        resultsArea.setText("Circuito limpiado. Listo para nueva simulacion.");
        lastResult = null;

        updateGraphType();

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

    private void updateInitialResultsText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Simulador Avanzado de Circuitos RLC ===\n\n");
        sb.append("Instrucciones:\n");
        sb.append("   1. Agregue componentes (R, L, C) al circuito\n");
        sb.append("   2. Configure voltaje y frecuencia\n");
        sb.append("   3. Seleccione metodo de simulacion\n");
        sb.append("   4. Haga clic en 'Simular Circuito'\n\n");
        sb.append("Caracteristicas:\n");
        sb.append("   - Analisis en dominio de tiempo y frecuencia\n");
        sb.append("   - Diagramas fasoriales interactivos\n");
        sb.append("   - Multiples metodos de calculo\n");
        sb.append("   - Circuitos predefinidos\n");
        sb.append("   - Algoritmos de planificacion integrados\n");
        sb.append("   - Interfaz moderna e intuitiva\n\n");
        sb.append("¡Comience agregando componentes y ejecutando una simulacion!");

        resultsArea.setText(sb.toString());
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
                sb.append("=== RESULTADOS DE SIMULACION ===\n\n");
                sb.append("- Impedancia: ").append(df.format(simResult.getImpedance())).append(" Ω\n");
                sb.append("- Corriente: ").append(df.format(simResult.getCurrent())).append(" A\n");
                sb.append("- Angulo de Fase: ").append(df.format(Math.toDegrees(simResult.getPhaseAngle())))
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

                resultsArea.setText(sb.toString());

                progressBar.setVisible(false);
                simulateButton.setEnabled(true);

                showInfo("Simulacion completada exitosamente");

            } else {
                onSimulationError("Resultado de simulacion invalido");
            }
        });
    }

    @Override
    public void onSimulationError(String error) {
        SwingUtilities.invokeLater(() -> {
            String detailedError = "Error en la simulacion:\n\n" + error;

            showError(detailedError);

            progressBar.setVisible(false);
            simulateButton.setEnabled(true);

            resultsArea.setText("Error en la simulacion. Por favor, verifique los parametros e intente nuevamente.\n\n"
                    + "Detalles del error: " + error);

            updateGraphType();
        });
    }

    @Override
    public void onSimulationStart() {
        SwingUtilities.invokeLater(() -> {
            resultsArea.setText("Simulacion en progreso...\n\nPor favor espere...");
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString("Simulacion en progreso...");
        });
    }

    private void updateAnalysisPanel(SimulationResult result) {
        if (analysisArea == null) return;
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("=== ANALISIS DETALLADO DEL CIRCUITO ===\n\n");
        
        // Análisis básico del circuito
        double totalR = components.stream().mapToDouble(CircuitComponent::getResistance).sum();
        double totalL = components.stream().mapToDouble(CircuitComponent::getInductance).sum();
        double totalC = components.stream().mapToDouble(CircuitComponent::getCapacitance).sum();
        
        analysis.append("PARAMETROS DEL CIRCUITO:\n");
        analysis.append(String.format("- Resistencia total: %.2f Ω\n", totalR));
        analysis.append(String.format("- Inductancia total: %.4f H\n", totalL));
        analysis.append(String.format("- Capacitancia total: %.6f F\n", totalC));
        
        // Análisis de potencia
        analysis.append("\nANALISIS DE POTENCIA:\n");
        analysis.append(String.format("- Potencia activa: %.2f W\n", result.getActivePower()));
        analysis.append(String.format("- Potencia reactiva: %.2f VAR\n", result.getReactivePower()));
        analysis.append(String.format("- Potencia aparente: %.2f VA\n", result.getApparentPower()));
        analysis.append(String.format("- Factor de potencia: %.3f\n", result.getPowerFactor()));
        
        // Análisis de eficiencia
        double efficiency = (result.getActivePower() / result.getApparentPower()) * 100;
        analysis.append(String.format("- Eficiencia energetica: %.1f%%\n", efficiency));
        
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
            analysis.append("- Corriente y voltaje estan en fase\n");
        }
        
        // Recomendaciones
        analysis.append("\nRECOMENDACIONES:\n");
        if (Math.abs(result.getPowerFactor()) < 0.9) {
            analysis.append("- Considerar correccion del factor de potencia\n");
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
        JOptionPane.showMessageDialog(this, message, "Informacion", JOptionPane.INFORMATION_MESSAGE);
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