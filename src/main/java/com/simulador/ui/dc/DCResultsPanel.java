package com.simulador.ui.dc;

import com.simulador.model.dc.DCSimulationResult;
import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * Panel para mostrar resultados de simulación DC
 * MODIFICADO: Se cambió "Voltaje Total" por "Voltaje Nodal (Va)"
 * y se usa getCalculatedVoltage().
 */
public class DCResultsPanel extends JPanel {
    private JTextArea resultsArea;
    private JLabel voltageLabel, currentLabel, powerLabel, resistanceLabel;
    private DecimalFormat df = new DecimalFormat("0.000");
    
    // Colores
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color ERROR_COLOR = new Color(239, 68, 68);
    private final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    
    public DCResultsPanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel superior con métricas clave
        add(createMetricsPanel(), BorderLayout.NORTH);
        
        // Área de resultados detallados
        add(createDetailedResultsPanel(), BorderLayout.CENTER);
    }
    
    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // --- MODIFICACIÓN ---
        // Voltaje
        JPanel voltagePanel = createMetricCard("Voltaje Nodal (Va)", "0.000 V", SUCCESS_COLOR);
        voltageLabel = (JLabel) ((JPanel) voltagePanel.getComponent(0)).getComponent(1);
        
        // Corriente
        JPanel currentPanel = createMetricCard("Corriente Eq. (I_eq)", "0.000 A", SUCCESS_COLOR);
        currentLabel = (JLabel) ((JPanel) currentPanel.getComponent(0)).getComponent(1);
        
        // Resistencia
        JPanel resistancePanel = createMetricCard("Resistencia Eq. (R_eq)", "0.000 Ω", WARNING_COLOR);
        resistanceLabel = (JLabel) ((JPanel) resistancePanel.getComponent(0)).getComponent(1);
        // --- FIN MODIFICACIÓN ---

        // Potencia
        JPanel powerPanel = createMetricCard("Potencia Total", "0.000 W", ERROR_COLOR);
        powerLabel = (JLabel) ((JPanel) powerPanel.getComponent(0)).getComponent(1);
        
        panel.add(voltagePanel);
        panel.add(currentPanel);
        panel.add(resistancePanel);
        panel.add(powerPanel);
        
        return panel;
    }
    
    private JPanel createMetricCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(100, 116, 139));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(color);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(valueLabel, BorderLayout.CENTER);
        
        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }
    
    private JScrollPane createDetailedResultsPanel() {
        resultsArea = new JTextArea(15, 50);
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        resultsArea.setBackground(Color.WHITE);
        resultsArea.setForeground(new Color(30, 41, 59));
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
        resultsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        resultsArea.setText(
            "=== SIMULADOR DE CIRCUITOS DC ===\n\n" +
            "Resultados de simulación:\n\n" +
            "• Configure un circuito DC\n" +
            "• Seleccione método de análisis\n" +
            "• Ejecute la simulación\n" +
            "• Vea los resultados aquí\n\n" +
            "Métodos disponibles:\n" +
            "  - Ley de Ohm\n" +
            "  - Leyes de Kirchhoff\n" +
            "  - Análisis de Mallas\n" +
            "  - Análisis Nodal\n" +
            "  - Teorema de Thevenin\n"
        );
        
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        return scrollPane;
    }
    
    public void updateResults(DCSimulationResult result) {
        if (result == null) {
            clearResults();
            return;
        }
        
        // --- MODIFICACIÓN ---
        // Actualizar métricas principales
        voltageLabel.setText(df.format(result.getCalculatedVoltage()) + " V");
        currentLabel.setText(df.format(result.getTotalCurrent()) + " A");
        resistanceLabel.setText(df.format(result.getTotalResistance()) + " Ω");
        powerLabel.setText(df.format(result.getTotalPower()) + " W");
        // --- FIN MODIFICACIÓN ---

        // Actualizar área de resultados detallados
        updateDetailedResults(result);
    }
    
    private void updateDetailedResults(DCSimulationResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RESULTADOS DE SIMULACIÓN DC ===\n\n");
        
        sb.append("INFORMACIÓN GENERAL:\n");
        sb.append("• Método utilizado: ").append(result.getMethodUsed()).append("\n");
        sb.append("• Configuración: ").append(result.getCircuitConfiguration()).append("\n");
        sb.append("• Timestamp: ").append(new java.util.Date(result.getTimestamp())).append("\n\n");
        
        sb.append("PARÁMETROS PRINCIPALES:\n");
        // --- MODIFICACIÓN ---
        sb.append("• Voltaje Nodal (Va): ").append(df.format(result.getCalculatedVoltage())).append(" V\n");
        sb.append("• Resistencia Eq. (R_eq): ").append(df.format(result.getTotalResistance())).append(" Ω\n");
        sb.append("• Corriente Eq. (I_eq): ").append(df.format(result.getTotalCurrent())).append(" A\n");
        // --- FIN MODIFICACIÓN ---
        sb.append("• Potencia total: ").append(df.format(result.getTotalPower())).append(" W\n");
        sb.append("• Potencia disipada: ").append(df.format(result.getPowerDissipated())).append(" W\n");
        sb.append("• Eficiencia: ").append(df.format(result.getEfficiency())).append(" %\n\n");
        
        sb.append("CORRIENTES POR RAMA:\n");
        double[] branchCurrents = result.getBranchCurrents();
        for (int i = 0; i < branchCurrents.length; i++) {
            String direction = branchCurrents[i] >= 0 ? "↓" : "↑";
            sb.append(String.format("• Rama %d: %s %s A\n", 
                i + 1, direction, df.format(Math.abs(branchCurrents[i]))));
        }
        
        sb.append("\nTENSIONES EN COMPONENTES:\n");
        double[] componentVoltages = result.getComponentVoltages();
        for (int i = 0; i < componentVoltages.length; i++) {
            sb.append(String.format("• Componente %d: %.2f V\n", i + 1, componentVoltages[i]));
        }
        
        sb.append("\nVALIDACIÓN:\n");
        if (result.isCircuitValid()) {
            sb.append("• ✓ Circuito válido\n");
            sb.append("• ✓ Conservación de energía verificada\n");
        } else {
            sb.append("• ✗ Circuito puede tener problemas\n");
            sb.append("• Verifique conexiones y valores\n");
        }
        
        resultsArea.setText(sb.toString());
        resultsArea.setCaretPosition(0);
    }
    
    public void clearResults() {
        voltageLabel.setText("0.000 V");
        currentLabel.setText("0.000 A");
        resistanceLabel.setText("0.000 Ω");
        powerLabel.setText("0.000 W");
        
        resultsArea.setText(
            "=== SIMULADOR DE CIRCUITOS DC ===\n\n" +
            "Esperando simulación...\n\n" +
            "Configure el circuito y ejecute la simulación para ver los resultados."
        );
    }
    
    public void showError(String errorMessage) {
        resultsArea.setText(
            "=== ERROR EN SIMULACIÓN ===\n\n" +
            "Se produjo un error durante la simulación:\n\n" +
            errorMessage + "\n\n" +
            "Por favor:\n" +
            "• Verifique los valores de los componentes\n" +
            "• Asegúrese de que el circuito sea válido\n" +
            "• Intente con otro método de análisis\n"
        );
        
        // Resetear métricas
        clearResults();
    }
}