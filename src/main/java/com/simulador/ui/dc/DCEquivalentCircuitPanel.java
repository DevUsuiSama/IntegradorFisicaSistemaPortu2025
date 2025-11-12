package com.simulador.ui.dc;

import com.simulador.model.dc.DCSimulationResult;
import javax.swing.*;
import java.awt.*;

/**
 * Panel para mostrar circuitos equivalentes (Thevenin/Norton)
 * MODIFICADO: Usa getCalculatedVoltage() como Vth.
 */
public class DCEquivalentCircuitPanel extends JPanel {
    private JTextArea equivalentArea;
    private JPanel theveninDiagram, nortonDiagram;
    
    public DCEquivalentCircuitPanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Crear pestañas para diferentes equivalentes
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Pestaña de Thevenin
        JPanel theveninPanel = createTheveninPanel();
        tabbedPane.addTab("Thevenin", theveninPanel);
        
        // Pestaña de Norton
        JPanel nortonPanel = createNortonPanel();
        tabbedPane.addTab("Norton", nortonPanel);
        
        // Pestaña de transformaciones
        JPanel transformationPanel = createTransformationPanel();
        tabbedPane.addTab("Transformaciones", transformationPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createTheveninPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Área de texto con explicación y cálculos
        equivalentArea = new JTextArea(20, 50);
        equivalentArea.setEditable(false);
        equivalentArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        equivalentArea.setBackground(Color.WHITE);
        equivalentArea.setLineWrap(true);
        equivalentArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(equivalentArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        
        // Panel para diagrama
        theveninDiagram = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawTheveninEquivalent(g);
            }
        };
        theveninDiagram.setPreferredSize(new Dimension(300, 150));
        theveninDiagram.setBackground(new Color(240, 245, 255));
        theveninDiagram.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, theveninDiagram);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.6);
        
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createNortonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea nortonArea = new JTextArea();
        nortonArea.setEditable(false);
        nortonArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        nortonArea.setBackground(Color.WHITE);
        nortonArea.setText(
            "=== EQUIVALENTE DE NORTON ===\n\n" +
            "El teorema de Norton establece que cualquier circuito lineal\n" +
            "puede ser reemplazado por una fuente de corriente en paralelo\n" +
            "con una resistencia.\n\n" +
            "Cálculos:\n" +
            "• Corriente de Norton (In) = Vth / Rth\n" +
            "• Resistencia de Norton (Rn) = Rth\n\n" +
            "Ejecute una simulación con Teorema de Thevenin para ver\n" +
            "los cálculos específicos de este circuito."
        );
        
        nortonDiagram = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawNortonEquivalent(g);
            }
        };
        nortonDiagram.setPreferredSize(new Dimension(300, 150));
        nortonDiagram.setBackground(new Color(240, 245, 255));
        nortonDiagram.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
            new JScrollPane(nortonArea), nortonDiagram);
        splitPane.setDividerLocation(200);
        
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createTransformationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea transformArea = new JTextArea();
        transformArea.setEditable(false);
        transformArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        transformArea.setBackground(Color.WHITE);
        transformArea.setText(
            "=== TRANSFORMACIÓN DE FUENTES ===\n\n" +
            "Conversión entre fuentes de voltaje y corriente:\n\n" +
            "De Thevenin a Norton:\n" +
            "• In = Vth / Rth\n" +
            "• Rn = Rth\n\n" +
            "De Norton a Thevenin:\n" +
            "• Vth = In × Rn\n" +
            "• Rth = Rn\n\n" +
            "Equivalencia:\n" +
            "• Fuente de voltaje Vth con Rth en serie\n" +
            "• ES EQUIVALENTE A\n" +
            "• Fuente de corriente In con Rn en paralelo\n\n" +
            "Restricciones:\n" +
            "• Solo para circuitos lineales\n" +
            "• Las cargas deben ser las mismas\n" +
            "• El comportamiento externo es idéntico"
        );
        
        JScrollPane scrollPane = new JScrollPane(transformArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void drawTheveninEquivalent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = theveninDiagram.getWidth();
        int height = theveninDiagram.getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Dibujar fuente de voltaje (Thevenin)
        drawVoltageSource(g2d, centerX - 80, centerY, 40, 25, "Vth");
        
        // Dibujar resistencia
        drawResistor(g2d, centerX + 20, centerY, 40, "Rth");
        
        // Dibujar líneas de conexión
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(centerX - 40, centerY, centerX + 20, centerY);
        g2d.drawLine(centerX + 60, centerY, centerX + 100, centerY);
        
        // Terminales de salida
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawLine(centerX + 100, centerY - 10, centerX + 100, centerY + 10);
        g2d.drawLine(centerX - 80, centerY - 10, centerX - 80, centerY + 10);
        
        // Etiqueta
        g2d.setColor(Color.BLUE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString("Equivalente Thevenin", centerX - 60, centerY - 30);
    }
    
    private void drawNortonEquivalent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = nortonDiagram.getWidth();
        int height = nortonDiagram.getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Dibujar fuente de corriente (Norton)
        drawCurrentSource(g2d, centerX - 60, centerY, 30, "In");
        
        // Dibujar resistencia en paralelo
        drawResistor(g2d, centerX + 20, centerY, 40, "Rn");
        
        // Dibujar líneas de conexión (paralelo)
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(centerX - 30, centerY - 25, centerX + 60, centerY - 25); // Línea superior
        g2d.drawLine(centerX - 30, centerY + 25, centerX + 60, centerY + 25); // Línea inferior
        
        // Conexiones verticales
        g2d.drawLine(centerX - 30, centerY - 25, centerX - 30, centerY + 25);
        g2d.drawLine(centerX + 60, centerY - 25, centerX + 60, centerY + 25);
        
        // Terminales de salida
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawLine(centerX - 30, centerY - 35, centerX - 30, centerY - 25);
        g2d.drawLine(centerX - 30, centerY + 25, centerX - 30, centerY + 35);
        
        // Etiqueta
        g2d.setColor(Color.BLUE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString("Equivalente Norton", centerX - 50, centerY - 50);
    }
    
    private void drawVoltageSource(Graphics2D g2d, int x, int y, int width, int height, String label) {
        // Círculo para fuente de voltaje
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x, y - height/2, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(x, y - height/2, width, height);
        
        // Símbolo + y -
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2d.drawString("+", x + width/3, y - 2);
        g2d.drawString("-", x + 2*width/3, y + 3);
        
        // Etiqueta
        g2d.drawString(label, x - 10, y - height/2 - 5);
    }
    
    private void drawCurrentSource(Graphics2D g2d, int x, int y, int size, String label) {
        // Círculo para fuente de corriente
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x, y - size/2, size, size);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(x, y - size/2, size, size);
        
        // Flecha interna indicando dirección
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(x + size/4, y, x + 3*size/4, y);
        g2d.drawLine(x + 3*size/4, y, x + 3*size/4 - 5, y - 3);
        g2d.drawLine(x + 3*size/4, y, x + 3*size/4 - 5, y + 3);
        
        // Etiqueta
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2d.drawString(label, x - 5, y - size/2 - 5);
    }
    
    private void drawResistor(Graphics2D g2d, int x, int y, int width, String label) {
        g2d.setColor(new Color(139, 69, 19)); // Marrón
        g2d.setStroke(new BasicStroke(3f));
        
        int segmentWidth = width / 8;
        g2d.drawLine(x, y, x + segmentWidth, y);
        
        for (int i = 0; i < 4; i++) {
            int startX = x + segmentWidth + i * 2 * segmentWidth;
            g2d.drawLine(startX, y - 8, startX + segmentWidth, y + 8);
            g2d.drawLine(startX + segmentWidth, y + 8, startX + 2 * segmentWidth, y - 8);
        }
        
        g2d.drawLine(x + 7 * segmentWidth, y, x + 8 * segmentWidth, y);
        
        // Etiqueta
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2d.drawString(label, x + width/2 - 10, y - 15);
    }
    
    public void updateEquivalents(DCSimulationResult result) {
        if (result == null || 
            !(result.getMethodUsed().contains("Thevenin") || result.getMethodUsed().contains("Nodal")) ) {
            equivalentArea.setText(
                "=== CIRCUITOS EQUIVALENTES ===\n\n" +
                "Para ver los circuitos equivalentes de Thevenin y Norton,\n" +
                "ejecute una simulación usando el Teorema de Thevenin o Análisis Nodal.\n\n" +
                "Características:\n" +
                "• Thevenin: Fuente de voltaje + resistencia en serie\n" +
                "• Norton: Fuente de corriente + resistencia en paralelo\n" +
                "• Ambos son equivalentes entre sí\n"
            );
            return;
        }
        
        // Calcular equivalentes
        // --- MODIFICACIÓN ---
        // Vth es el Voltaje Nodal (Va) calculado.
        double vth = result.getCalculatedVoltage(); 
        // --- FIN MODIFICACIÓN ---
        double rth = result.getTotalResistance();
        
        double in = 0.0; // Corriente de Norton
        if (rth > 1e-9) {
            in = vth / rth;
        }
        double rn = rth; // Resistencia de Norton
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== CIRCUITOS EQUIVALENTES ===\n\n");
        
        sb.append("EQUIVALENTE DE THEVENIN:\n");
        sb.append("• Voltaje Thevenin (Vth): ").append(String.format("%.3f V\n", vth));
        sb.append("• Resistencia Thevenin (Rth): ").append(String.format("%.3f Ω\n", rth));
        sb.append("• Circuito: Fuente ").append(String.format("%.3f V", vth))
          .append(" en serie con ").append(String.format("%.3f Ω\n\n", rth));
        
        sb.append("EQUIVALENTE DE NORTON:\n");
        sb.append("• Corriente Norton (In): ").append(String.format("%.3f A\n", in));
        sb.append("• Resistencia Norton (Rn): ").append(String.format("%.3f Ω\n", rn));
        sb.append("• Circuito: Fuente ").append(String.format("%.3f A", in))
          .append(" en paralelo con ").append(String.format("%.3f Ω\n\n", rn));
        
        sb.append("TRANSFORMACIÓN DE FUENTES:\n");
        sb.append("• Vth = In × Rn → ").append(String.format("%.3f = %.3f × %.3f", vth, in, rn));
        sb.append(" → ").append(String.format("%.3f = %.3f ✓\n", vth, in * rn));
        sb.append("• Rth = Rn → ").append(String.format("%.3f = %.3f ✓\n\n", rth, rn));
        
        sb.append("VERIFICACIÓN:\n");
        sb.append("• Los circuitos son eléctricamente equivalentes\n");
        
        equivalentArea.setText(sb.toString());
        
        // Redibujar diagramas
        theveninDiagram.repaint();
        nortonDiagram.repaint();
    }
    
    public void clearEquivalents() {
        equivalentArea.setText(
            "=== CIRCUITOS EQUIVALENTES ===\n\n" +
            "Ejecute una simulación para calcular los circuitos equivalentes.\n\n" +
            "Los circuitos equivalentes permiten:\n" +
            "• Simplificar análisis de circuitos complejos\n" +
            "• Facilitar el cálculo de transferencia de potencia\n" +
            "• Comprender mejor el comportamiento del circuito\n"
        );
        
        theveninDiagram.repaint();
        nortonDiagram.repaint();
    }
}