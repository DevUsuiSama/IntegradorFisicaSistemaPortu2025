package com.simulador.ui;

import com.simulador.model.CircuitComponent;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel para mostrar un diagrama de circuito real con componentes
 * Panel for displaying a real circuit diagram with components
 */
public class CircuitDiagramPanel extends JPanel {
    private List<CircuitComponent> components;
    private int componentWidth = 60;
    private int wireLength = 50;
    
    public CircuitDiagramPanel() {
        setPreferredSize(new Dimension(800, 200));
        setBackground(Color.WHITE);
        setOpaque(true);
    }
    
    public void setComponents(List<CircuitComponent> components) {
        this.components = components;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Configurar renderizado de alta calidad
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        if (components == null || components.isEmpty()) {
            drawEmptyCircuit(g2d);
        } else {
            drawCircuitDiagram(g2d);
        }
    }
    
    private void drawEmptyCircuit(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();
        
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String message = "Circuito Vacio - Agregue componentes";
        int textWidth = g2d.getFontMetrics().stringWidth(message);
        g2d.drawString(message, (width - textWidth) / 2, height / 2);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String instruction = "Use los controles a la izquierda para agregar componentes R, L o C";
        textWidth = g2d.getFontMetrics().stringWidth(instruction);
        g2d.drawString(instruction, (width - textWidth) / 2, height / 2 + 25);
    }
    
    private void drawCircuitDiagram(Graphics2D g2d) {
        int startX = 50;
        int centerY = getHeight() / 2;
        
        // Dibujar fuente de voltaje
        drawVoltageSource(g2d, startX, centerY);
        
        int currentX = startX + componentWidth + wireLength;
        
        // Dibujar componentes en serie
        for (int i = 0; i < components.size(); i++) {
            CircuitComponent comp = components.get(i);
            drawComponent(g2d, comp, currentX, centerY, i);
            
            // Dibujar conexión
            if (i < components.size() - 1) {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(currentX + componentWidth, centerY, currentX + componentWidth + wireLength, centerY);
            }
            
            currentX += componentWidth + wireLength;
        }
        
        // Dibujar línea de retorno (tierra)
        drawGround(g2d, currentX, centerY);
        
        // Dibujar conexión de retorno
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(currentX, centerY, currentX, centerY + 50);
        g2d.drawLine(startX, centerY, startX, centerY + 50);
        g2d.drawLine(startX, centerY + 50, currentX, centerY + 50);
        
        // Dibujar etiquetas de corriente y voltaje
        drawCircuitLabels(g2d, startX, centerY, currentX);
    }
    
    private void drawVoltageSource(Graphics2D g2d, int x, int y) {
        // Dibujar círculo de fuente de voltaje
        g2d.setColor(new Color(0, 100, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x, y - 20, 40, 40);
        
        // Dibujar símbolo de voltaje (+ y -)
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("+", x + 15, y - 5);
        g2d.setColor(Color.BLACK);
        g2d.drawString("-", x + 15, y + 15);
        
        // Etiqueta de voltaje
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("V", x + 15, y - 30);
        
        // Conexiones
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(x + 40, y, x + 40 + wireLength, y);
    }
    
    private void drawComponent(Graphics2D g2d, CircuitComponent comp, int x, int y, int index) {
        String type = comp.getType();
        double value = comp.getValue();
        
        switch (type) {
            case "Resistance":
                drawResistor(g2d, x, y, value, index);
                break;
            case "Inductor":
                drawInductor(g2d, x, y, value, index);
                break;
            case "Capacitor":
                drawCapacitor(g2d, x, y, value, index);
                break;
        }
    }
    
    private void drawResistor(Graphics2D g2d, int x, int y, double value, int index) {
        // Cuerpo del resistor (rectángulo con líneas en zigzag)
        g2d.setColor(new Color(139, 69, 19)); // Marrón
        g2d.setStroke(new BasicStroke(3));
        
        // Rectángulo principal
        g2d.drawRect(x, y - 15, componentWidth, 30);
        
        // Líneas en zigzag dentro del resistor
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i < 4; i++) {
            int x1 = x + 5 + i * 15;
            int x2 = x + 5 + (i + 1) * 15;
            if (i % 2 == 0) {
                g2d.drawLine(x1, y - 10, x2, y + 10);
            } else {
                g2d.drawLine(x1, y + 10, x2, y - 10);
            }
        }
        
        // Terminales
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(x, y, x - 5, y);
        g2d.drawLine(x + componentWidth, y, x + componentWidth + 5, y);
        
        // Etiqueta
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        String label = String.format("R%d=%.1fΩ", index + 1, value);
        int textWidth = g2d.getFontMetrics().stringWidth(label);
        g2d.drawString(label, x + (componentWidth - textWidth) / 2, y - 20);
    }
    
    private void drawInductor(Graphics2D g2d, int x, int y, double value, int index) {
        // Inductor (bobinas)
        g2d.setColor(new Color(75, 0, 130)); // Índigo
        g2d.setStroke(new BasicStroke(2));
        
        // Dibujar bobinas
        int coilCount = 5;
        int coilSpacing = componentWidth / coilCount;
        
        for (int i = 0; i < coilCount; i++) {
            int coilX = x + i * coilSpacing;
            g2d.drawArc(coilX, y - 15, coilSpacing, 30, 0, 180);
        }
        
        // Terminales
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(x, y, x - 5, y);
        g2d.drawLine(x + componentWidth, y, x + componentWidth + 5, y);
        
        // Etiqueta
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        String label = String.format("L%d=%.4fH", index + 1, value);
        int textWidth = g2d.getFontMetrics().stringWidth(label);
        g2d.drawString(label, x + (componentWidth - textWidth) / 2, y - 20);
    }
    
    private void drawCapacitor(Graphics2D g2d, int x, int y, double value, int index) {
        // Capacitor (dos líneas paralelas)
        g2d.setColor(new Color(0, 100, 0)); // Verde oscuro
        g2d.setStroke(new BasicStroke(3));
        
        // Placas del capacitor
        g2d.drawLine(x + 20, y - 20, x + 20, y + 20);
        g2d.drawLine(x + 40, y - 20, x + 40, y + 20);
        
        // Terminales
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(x, y, x + 20, y);
        g2d.drawLine(x + 40, y, x + componentWidth, y);
        
        // Símbolo de polaridad (si es capacitor electrolítico)
        if (value >= 1e-6) { // Suponer que valores grandes son electrolíticos
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString("+", x + 15, y - 25);
            g2d.setColor(Color.BLACK);
            g2d.drawString("-", x + 45, y - 25);
        }
        
        // Etiqueta
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        String unit = value >= 1e-6 ? "μF" : "pF";
        double displayValue = value >= 1e-6 ? value * 1e6 : value * 1e12;
        String label = String.format("C%d=%.1f%s", index + 1, displayValue, unit);
        int textWidth = g2d.getFontMetrics().stringWidth(label);
        g2d.drawString(label, x + (componentWidth - textWidth) / 2, y - 30);
    }
    
    private void drawGround(Graphics2D g2d, int x, int y) {
        // Símbolo de tierra
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        
        // Línea horizontal
        g2d.drawLine(x - 10, y + 20, x + 10, y + 20);
        // Líneas verticales
        g2d.drawLine(x - 5, y + 25, x - 5, y + 20);
        g2d.drawLine(x, y + 30, x, y + 20);
        g2d.drawLine(x + 5, y + 25, x + 5, y + 20);
        
        // Etiqueta
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("GND", x - 10, y + 45);
    }
    
    private void drawCircuitLabels(Graphics2D g2d, int startX, int centerY, int endX) {
        // Flecha de corriente
        g2d.setColor(new Color(200, 0, 0));
        g2d.setStroke(new BasicStroke(2));
        
        int currentArrowY = centerY - 60;
        int arrowLength = endX - startX - 50;
        
        // Línea de corriente
        g2d.drawLine(startX + 20, currentArrowY, startX + 20 + arrowLength, currentArrowY);
        
        // Flecha
        int[] xPoints = {startX + 20 + arrowLength - 10, startX + 20 + arrowLength, startX + 20 + arrowLength - 10};
        int[] yPoints = {currentArrowY - 5, currentArrowY, currentArrowY + 5};
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // Etiqueta de corriente
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("I", startX + 20 + arrowLength / 2 - 5, currentArrowY - 10);
        
        // Indicar que es un circuito serie
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.drawString("Circuito RLC Serie", startX, centerY - 80);
        
        // Información del voltaje aplicado
        g2d.drawString("Fuente de Corriente Alterna (AC)", startX, centerY + 80);
    }
    
    @Override
    public Dimension getPreferredSize() {
        if (components == null || components.isEmpty()) {
            return new Dimension(800, 200);
        }
        
        // Calcular ancho basado en número de componentes
        int width = 150 + components.size() * (componentWidth + wireLength);
        return new Dimension(width, 200);
    }
    
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(600, 150);
    }
}