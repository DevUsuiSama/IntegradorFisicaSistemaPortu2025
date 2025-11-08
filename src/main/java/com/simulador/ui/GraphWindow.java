package com.simulador.ui;

import com.simulador.model.CircuitComponent;
import com.simulador.model.SimulationResult;
import com.simulador.utils.LanguageManager;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Ventana principal para mostrar gráficos del simulador RLC
 * Main window for displaying RLC simulator graphs
 */
public class GraphWindow extends JDialog {
    private SimulationResult result;
    private List<CircuitComponent> components;
    private LanguageManager languageManager;
    
    private TimeGraph timeGraph;
    private FrequencyGraph frequencyGraph;
    private PhasorDiagram phasorDiagram;
    private WaveformGraph waveformGraph;
    
    // Componentes de UI para traducción
    private JTabbedPane tabbedPane;
    private JButton refreshButton, printButton, closeButton;
    
    public GraphWindow(JFrame parent, SimulationResult result, List<CircuitComponent> components) {
        super(parent, true); // Modal
        this.result = result;
        this.components = components;
        this.languageManager = LanguageManager.getInstance();
        
        initializeComponents();
        setupUI();
    }
    
    private void initializeComponents() {
        timeGraph = new TimeGraph(result);
        frequencyGraph = new FrequencyGraph(components);
        phasorDiagram = new PhasorDiagram(result, components);
        waveformGraph = new WaveformGraph(result);
    }
    
    private void setupUI() {
        setTitle(languageManager.getTranslation("graph_window_title"));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(1000, 700));
        setLocationRelativeTo(getOwner());
        
        // Crear pestañas
        tabbedPane = new JTabbedPane();
        
        // Pestaña de dominio de tiempo
        JPanel timePanel = createGraphPanel(timeGraph, "time_domain");
        tabbedPane.addTab(languageManager.getTranslation("time_tab"), timePanel);
        
        // Pestaña de frecuencia
        JPanel freqPanel = createGraphPanel(frequencyGraph, "frequency_response");
        tabbedPane.addTab(languageManager.getTranslation("frequency_tab"), freqPanel);
        
        // Pestaña fasorial
        JPanel phasorPanel = createGraphPanel(phasorDiagram, "phasor_diagram");
        tabbedPane.addTab(languageManager.getTranslation("phasor_tab"), phasorPanel);
        
        // Pestaña de formas de onda
        JPanel wavePanel = createGraphPanel(waveformGraph, "waveforms");
        tabbedPane.addTab(languageManager.getTranslation("waveforms_tab"), wavePanel);
        
        // Panel de controles
        JPanel controlPanel = createControlPanel();
        
        // Layout principal
        setLayout(new BorderLayout(10, 10));
        add(controlPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(getOwner());
        
        // Configurar cierre seguro
        setupSafeClose();
    }
    
    private JPanel createGraphPanel(JComponent graph, String titleKey) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Título
        JLabel titleLabel = new JLabel(languageManager.getTranslation(titleKey), JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        
        // Panel de gráfico con scroll
        JScrollPane scrollPane = new JScrollPane(graph);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.setBackground(new Color(240, 240, 240));
        
        // Botón de actualizar
        refreshButton = new JButton(languageManager.getTranslation("refresh_graphs"));
        refreshButton.addActionListener(e -> refreshGraphs());
        
        // Botón de imprimir
        printButton = new JButton(languageManager.getTranslation("save_as_image"));
        printButton.addActionListener(e -> saveAsImage());
        
        // Botón de cerrar
        closeButton = new JButton(languageManager.getTranslation("close"));
        closeButton.addActionListener(e -> disposeSafely());
        
        panel.add(refreshButton);
        panel.add(printButton);
        panel.add(closeButton);
        
        return panel;
    }
    
    private void refreshGraphs() {
        timeGraph.setResult(result);
        frequencyGraph.setComponents(components);
        phasorDiagram.setData(result, components);
        waveformGraph.setResult(result);
        
        repaint();
    }
    
    private void saveAsImage() {
        JOptionPane.showMessageDialog(this,
            languageManager.getTranslation("save_image_not_implemented"),
            languageManager.getTranslation("information"),
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void setupSafeClose() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                disposeSafely();
            }
        });
    }
    
    private void disposeSafely() {
        // Limpiar recursos de gráficos
        if (timeGraph != null) timeGraph = null;
        if (frequencyGraph != null) frequencyGraph = null;
        if (phasorDiagram != null) phasorDiagram = null;
        if (waveformGraph != null) waveformGraph = null;
        
        dispose();
    }
    
    public void updateData(SimulationResult newResult, List<CircuitComponent> newComponents) {
        this.result = newResult;
        this.components = newComponents;
        refreshGraphs();
    }
    
    /**
     * Actualiza todos los textos de la interfaz según el idioma actual
     */
    public void updateUITexts() {
        setTitle(languageManager.getTranslation("graph_window_title"));
        
        // Actualizar textos de los botones
        refreshButton.setText(languageManager.getTranslation("refresh_graphs"));
        printButton.setText(languageManager.getTranslation("save_as_image"));
        closeButton.setText(languageManager.getTranslation("close"));
        
        // Actualizar títulos de las pestañas
        updateTabTitles();
        
        // Actualizar títulos de los gráficos
        updateGraphTitles();
    }
    
    private void updateTabTitles() {
        if (tabbedPane.getTabCount() >= 4) {
            tabbedPane.setTitleAt(0, languageManager.getTranslation("time_tab"));
            tabbedPane.setTitleAt(1, languageManager.getTranslation("frequency_tab"));
            tabbedPane.setTitleAt(2, languageManager.getTranslation("phasor_tab"));
            tabbedPane.setTitleAt(3, languageManager.getTranslation("waveforms_tab"));
        }
    }
    
    private void updateGraphTitles() {
        // Actualizar títulos en los paneles de gráficos
        Component[] components = tabbedPane.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                updatePanelTitles((JPanel) comp);
            }
        }
    }
    
    private void updatePanelTitles(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                String text = label.getText();
                if (text != null) {
                    if (text.contains("Dominio del Tiempo") || text.contains("Time Domain") || text.contains("Domínio do Tempo")) {
                        label.setText(languageManager.getTranslation("time_domain"));
                    } else if (text.contains("Respuesta en Frecuencia") || text.contains("Frequency Response") || text.contains("Resposta em Frequência")) {
                        label.setText(languageManager.getTranslation("frequency_response"));
                    } else if (text.contains("Diagrama Fasorial") || text.contains("Phasor Diagram") || text.contains("Diagrama Fasorial")) {
                        label.setText(languageManager.getTranslation("phasor_diagram"));
                    } else if (text.contains("Formas de Onda") || text.contains("Waveforms") || text.contains("Formas de Onda")) {
                        label.setText(languageManager.getTranslation("waveforms"));
                    }
                }
            }
        }
    }
    
    @Override
    public void dispose() {
        // Limpieza adicional antes de cerrar
        components = null;
        result = null;
        super.dispose();
    }
}