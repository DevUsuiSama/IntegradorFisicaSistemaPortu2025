package com.simulador.ui;

import com.simulador.scheduler.*;
import com.simulador.model.CircuitSimulationTask;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Panel principal para la pestaña Navigator con planificación de procesos
 * Main panel for Navigator tab with process scheduling
 */
public class NavigatorPanel extends JPanel implements PropertyChangeListener {
    private ProcessScheduler scheduler;
    private JComboBox<String> algorithmCombo;
    private JComboBox<String> batchTypeCombo;
    private JSpinner simpleSpinner, mediumSpinner, complexSpinner;
    private JButton startButton, stopButton, clearButton;
    private JTable tasksTable;
    private DefaultTableModel tableModel;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private Timer updateTimer;
    
    // Constantes locales para mayor claridad
    private static final String PROPERTY_MESSAGE = "message";
    private static final String PROPERTY_SIMULATION_STATE = "simulationState";
    private static final String PROPERTY_TASKS_UPDATED = "tasksUpdated";
    
    public NavigatorPanel() {
        this.scheduler = new ProcessScheduler();
        // Registrar como listener de PropertyChange events
        this.scheduler.addPropertyChangeListener(this);
        
        initializeUI();
        setupEventHandlers();
        startUpdateTimer();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(createControlPanel(), BorderLayout.NORTH);
        add(createTasksPanel(), BorderLayout.CENTER);
        add(createLogPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Configuración de Planificación"));
        
        // Panel de algoritmo
        JPanel algorithmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        algorithmPanel.add(new JLabel("Algoritmo:"));
        
        algorithmCombo = new JComboBox<>(new String[]{
            "First-Come, First-Served (FCFS)",
            "Round Robin (RR)", 
            "Shortest Job First (SJF)"
        });
        algorithmCombo.setToolTipText("Seleccione el algoritmo de planificación");
        algorithmPanel.add(algorithmCombo);
        
        // Panel de tipo de lote
        JPanel batchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        batchPanel.add(new JLabel("Tipo de Lote:"));
        
        batchTypeCombo = new JComboBox<>(new String[]{
            "Homogéneo - Simple",
            "Homogéneo - Medio", 
            "Homogéneo - Complejo",
            "Heterogéneo - Mixto"
        });
        batchTypeCombo.addActionListener(e -> updateBatchControls());
        batchPanel.add(batchTypeCombo);
        
        // Panel de controles de batch
        JPanel batchControlsPanel = createBatchControlsPanel();
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startButton = new JButton("Iniciar Simulación");
        stopButton = new JButton("Detener");
        clearButton = new JButton("Limpiar Todo");
        
        stopButton.setEnabled(false);
        
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(clearButton);
        
        // Barra de progreso
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        panel.add(algorithmPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(batchPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(batchControlsPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(buttonPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(progressBar);
        
        return panel;
    }
    
    private JPanel createBatchControlsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        panel.add(new JLabel("Simples:"));
        simpleSpinner = new JSpinner(new SpinnerNumberModel(3, 0, 20, 1));
        panel.add(simpleSpinner);
        
        panel.add(new JLabel("Medios:"));
        mediumSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 15, 1));
        panel.add(mediumSpinner);
        
        panel.add(new JLabel("Complejos:"));
        complexSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));
        panel.add(complexSpinner);
        
        JButton generateButton = new JButton("Generar Lote");
        generateButton.addActionListener(e -> generateBatch());
        panel.add(generateButton);
        
        return panel;
    }
    
    private JPanel createTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Tareas de Simulación"));
        
        // Modelo de tabla
        String[] columns = {"ID", "Nombre", "Complejidad", "Duración (ms)", "Estado", "Progreso"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tasksTable = new JTable(tableModel);
        tasksTable.setAutoCreateRowSorter(true);
        
        JScrollPane scrollPane = new JScrollPane(tasksTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Log de Ejecución"));
        panel.setPreferredSize(new Dimension(800, 150));
        
        logArea = new JTextArea(8, 70);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        startButton.addActionListener(e -> startSimulation());
        stopButton.addActionListener(e -> stopSimulation());
        clearButton.addActionListener(e -> clearAll());
    }
    
    private void startUpdateTimer() {
        updateTimer = new Timer(500, e -> updateTasksTable());
        updateTimer.start();
    }
    
    private void updateBatchControls() {
        String selected = (String) batchTypeCombo.getSelectedItem();
        boolean isHeterogeneous = selected != null && selected.contains("Heterogéneo");
        
        simpleSpinner.setEnabled(isHeterogeneous);
        mediumSpinner.setEnabled(isHeterogeneous);
        complexSpinner.setEnabled(isHeterogeneous);
    }
    
    private void generateBatch() {
        String batchType = (String) batchTypeCombo.getSelectedItem();
        
        if (batchType == null) return;
        
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
    }
    
    private void startSimulation() {
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
            
            scheduler.startSimulation();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            progressBar.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void stopSimulation() {
        scheduler.stopSimulation();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        progressBar.setVisible(false);
    }
    
    private void clearAll() {
        if (!scheduler.isSimulationRunning()) {
            scheduler.clearTasks();
            logArea.setText("");
            updateTasksTable();
        } else {
            JOptionPane.showMessageDialog(this, 
                "No se puede limpiar durante la simulación", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void updateTasksTable() {
        tableModel.setRowCount(0);
        
        for (CircuitSimulationTask task : scheduler.getTasks()) {
            Object[] row = {
                task.getId(),
                task.getName(),
                task.getComplexity().getDisplayName(),
                task.getEstimatedDuration(),
                task.getState().getDisplayName(),
                String.format("%.1f%%", task.getProgress())
            };
            tableModel.addRow(row);
        }
        
        // Actualizar barra de progreso general
        if (scheduler.isSimulationRunning()) {
            long completed = scheduler.getTasks().stream()
                .filter(t -> t.getState() == CircuitSimulationTask.TaskState.COMPLETED)
                .count();
            long total = scheduler.getTasks().size();
            
            int progress = total > 0 ? (int)((completed * 100) / total) : 0;
            progressBar.setValue(progress);
            progressBar.setString(String.format("%d/%d tareas completadas (%d%%)", 
                completed, total, progress));
        }
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + 
                message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        Object newValue = evt.getNewValue();
        
        switch (propertyName) {
            case PROPERTY_MESSAGE:
                if (newValue instanceof String) {
                    log((String) newValue);
                }
                break;
                
            case PROPERTY_SIMULATION_STATE:
                // Actualizar estado de los botones cuando cambia el estado de simulación
                SwingUtilities.invokeLater(() -> {
                    boolean isRunning = Boolean.TRUE.equals(newValue);
                    startButton.setEnabled(!isRunning);
                    stopButton.setEnabled(isRunning);
                    progressBar.setVisible(isRunning);
                    
                    if (!isRunning) {
                        // Cuando termina la simulación, actualizar la tabla una última vez
                        updateTasksTable();
                    }
                });
                break;
                
            case PROPERTY_TASKS_UPDATED:
                // Actualizar tabla cuando cambian las tareas
                SwingUtilities.invokeLater(this::updateTasksTable);
                break;
        }
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (updateTimer != null) {
            updateTimer.stop();
        }
        if (scheduler != null && scheduler.isSimulationRunning()) {
            scheduler.stopSimulation();
        }
        
        // Remover el listener para evitar memory leaks
        if (scheduler != null) {
            scheduler.removePropertyChangeListener(this);
        }
    }
}