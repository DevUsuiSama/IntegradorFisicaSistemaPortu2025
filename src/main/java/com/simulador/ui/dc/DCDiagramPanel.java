package com.simulador.ui.dc;

import com.simulador.model.dc.DCCircuit;
import com.simulador.model.dc.DCComponent;
import com.simulador.model.dc.DCBranch;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel para visualizar diagramas de circuitos DC
 */
public class DCDiagramPanel extends JPanel {
    private DCCircuit circuit;
    private Color backgroundColor = new Color(240, 245, 255);
    private Color wireColor = new Color(50, 50, 50);
    private Color resistorColor = new Color(139, 69, 19);
    private Color batteryColor = new Color(200, 200, 200);
    
    public DCDiagramPanel() {
        setBackground(backgroundColor);
        setPreferredSize(new Dimension(600, 300));
    }
    
    public void setCircuit(DCCircuit circuit) {
        this.circuit = circuit;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (circuit == null) {
            drawEmptyState(g2d);
        } else {
            drawCircuit(g2d);
        }
    }
    
    private void drawEmptyState(Graphics2D g2d) {
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        String message = "Diagrama de Circuito DC";
        int textWidth = g2d.getFontMetrics().stringWidth(message);
        g2d.drawString(message, (getWidth() - textWidth) / 2, getHeight() / 2);
        
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        String info = "Configure el circuito para ver el diagrama";
        int infoWidth = g2d.getFontMetrics().stringWidth(info);
        g2d.drawString(info, (getWidth() - infoWidth) / 2, getHeight() / 2 + 25);
    }
    
    private void drawCircuit(Graphics2D g2d) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int circuitWidth = Math.min(getWidth() - 100, 400);
        int circuitHeight = Math.min(getHeight() - 60, 150);
        
        // Dibujar fuente DC
        drawBattery(g2d, centerX - circuitWidth/2 + 30, centerY, 40, 20);
        
        // Dibujar según configuración
        switch (circuit.getConfiguration()) {
            case "Serie":
                drawSeriesCircuit(g2d, centerX, centerY, circuitWidth, circuitHeight);
                break;
            case "Paralelo":
                drawParallelCircuit(g2d, centerX, centerY, circuitWidth, circuitHeight);
                break;
            default:
                drawMixedCircuit(g2d, centerX, centerY, circuitWidth, circuitHeight);
        }
        
        // Etiqueta de voltaje
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString(circuit.getSourceVoltage() + " V DC", centerX - circuitWidth/2 + 15, centerY - 30);
    }
    
    private void drawBattery(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(batteryColor);
        g2d.fillRect(x, y - height/2, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y - height/2, width, height);
        
        // Terminales
        g2d.fillRect(x - 5, y - height/4, 5, height/2);
        g2d.fillRect(x + width, y - height/4, 5, height/2);
        
        // Símbolos + y -
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2d.drawString("+", x + width/4, y - 2);
        g2d.drawString("-", x + 3*width/4, y - 2);
    }
    
    private void drawResistor(Graphics2D g2d, int x, int y, int width, String label) {
        g2d.setColor(resistorColor);
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
        if (label != null) {
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2d.drawString(label, x + width/2 - 10, y - 15);
        }
    }
    
    private void drawSeriesCircuit(Graphics2D g2d, int centerX, int centerY, int circuitWidth, int circuitHeight) {
        int startX = centerX - circuitWidth/2 + 80;
        int y = centerY;
        
        g2d.setColor(wireColor);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(startX, y, startX + circuitWidth - 100, y);
        
        // Dibujar resistores
        List<DCBranch> branches = circuit.getBranches();
        if (!branches.isEmpty()) {
            List<DCComponent> components = branches.get(0).getComponents();
            int compWidth = 60;
            int spacing = 20;
            int currentX = startX + 20;
            
            for (int i = 0; i < components.size(); i++) {
                DCComponent comp = components.get(i);
                if (comp.getType() == com.simulador.model.dc.DCComponentType.RESISTOR) {
                    drawResistor(g2d, currentX, y, compWidth, comp.getValue() + "Ω");
                    currentX += compWidth + spacing;
                }
            }
        }
        
        // Línea de retorno
        g2d.drawLine(startX + circuitWidth - 100, y, startX + circuitWidth - 100, y + 40);
        g2d.drawLine(startX + circuitWidth - 100, y + 40, startX, y + 40);
        g2d.drawLine(startX, y + 40, startX, y);
    }
    
    private void drawParallelCircuit(Graphics2D g2d, int centerX, int centerY, int circuitWidth, int circuitHeight) {
        int startX = centerX - circuitWidth/2 + 80;
        int y = centerY;
        int branchSpacing = 40;
        
        g2d.setColor(wireColor);
        g2d.setStroke(new BasicStroke(2f));
        
        List<DCBranch> branches = circuit.getBranches();
        for (int i = 0; i < branches.size(); i++) {
            int branchY = y + i * branchSpacing - (branches.size() - 1) * branchSpacing / 2;
            
            // Rama
            g2d.drawLine(startX, branchY, startX + 80, branchY);
            
            // Resistor en la rama
            List<DCComponent> components = branches.get(i).getComponents();
            if (!components.isEmpty()) {
                DCComponent comp = components.get(0); // Tomar primer componente
                if (comp.getType() == com.simulador.model.dc.DCComponentType.RESISTOR) {
                    drawResistor(g2d, startX + 90, branchY, 40, comp.getValue() + "Ω");
                }
            }
            
            g2d.drawLine(startX + 140, branchY, startX + circuitWidth - 100, branchY);
        }
        
        // Conexiones verticales
        g2d.drawLine(startX + 80, y - branchSpacing, startX + 80, y + branchSpacing);
        g2d.drawLine(startX + circuitWidth - 100, y - branchSpacing, startX + circuitWidth - 100, y + branchSpacing);
        
        // Conexiones a la batería
        g2d.drawLine(startX + circuitWidth - 100, y, startX + circuitWidth - 60, y);
        g2d.drawLine(startX, y, startX - 40, y);
    }
    
    private void drawMixedCircuit(Graphics2D g2d, int centerX, int centerY, int circuitWidth, int circuitHeight) {
        drawSeriesCircuit(g2d, centerX, centerY, circuitWidth, circuitHeight);
        
        g2d.setColor(Color.BLUE);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2d.drawString("Configuración Mixta", centerX - 40, centerY + 70);
    }
}