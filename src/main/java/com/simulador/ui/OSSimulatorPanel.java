package com.simulador.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Panel del simulador de sistema operativo que reemplaza el placeholder
 * Operating system simulator panel that replaces the placeholder
 */
public class OSSimulatorPanel extends JPanel {
    private JTabbedPane tabbedPane;
    private NavigatorPanel navigatorPanel;
    
    public OSSimulatorPanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Crear pestañas para diferentes funcionalidades del SO
        tabbedPane = new JTabbedPane();
        
        // Pestaña Navigator (planificación de procesos)
        navigatorPanel = new NavigatorPanel();
        tabbedPane.addTab("Navigator - Planificación", navigatorPanel);
        
        // Pestaña de métricas (placeholder para futuras expansiones)
        JPanel metricsPanel = createMetricsPanel();
        tabbedPane.addTab("Métricas del Sistema", metricsPanel);
        
        // Pestaña de monitorización (placeholder)
        JPanel monitorPanel = createMonitorPanel();
        tabbedPane.addTab("Monitorización", monitorPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel("Panel de Métricas del Sistema - En Desarrollo", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(new Color(100, 100, 100));
        
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createMonitorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel("Monitorización en Tiempo Real - En Desarrollo", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(new Color(100, 100, 100));
        
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Libera recursos cuando el panel se cierra
     */
    public void dispose() {
        if (navigatorPanel != null) {
            navigatorPanel.removeNotify();
        }
    }
}