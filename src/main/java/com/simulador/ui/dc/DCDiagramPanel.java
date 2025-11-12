package com.simulador.ui.dc;

import com.simulador.model.dc.DCCircuit;
import com.simulador.model.dc.DCComponent;
import com.simulador.model.dc.DCBranch;
import com.simulador.model.dc.DCComponentType;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel para visualizar diagramas de circuitos DC
 * MODIFICADO: Rediseñado para dibujar componentes DENTRO de las ramas
 * en lugar de una fuente de alimentación global.
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
        
        if (circuit == null || circuit.getBranchCount() == 0) {
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
        String info = "Configure el circuito y agregue componentes";
        int infoWidth = g2d.getFontMetrics().stringWidth(info);
        g2d.drawString(info, (getWidth() - infoWidth) / 2, getHeight() / 2 + 25);
    }
    
    private void drawCircuit(Graphics2D g2d) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        
        // Dibujar según configuración
        if ("Paralelo".equals(circuit.getConfiguration())) {
            drawParallelCircuit(g2d, centerX, centerY);
        } else {
            drawSeriesCircuit(g2d, centerX, centerY);
        }
    }
    
    private void drawBattery(Graphics2D g2d, int x, int y, int width, int height, double voltage) {
        g2d.setColor(batteryColor);
        g2d.fillRect(x, y - height/2, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y - height/2, width, height);
        
        // Símbolos + y -
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2d.drawString("+", x + 5, y + 4);
        g2d.drawString("-", x + width - 10, y + 4);
        
        // Etiqueta de voltaje
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2d.drawString(String.format("%.0f V", voltage), x + width/2 - 15, y - height/2 - 5);
    }
    
    private void drawResistor(Graphics2D g2d, int x, int y, int width, String label) {
        g2d.setColor(resistorColor);
        g2d.setStroke(new BasicStroke(3f));
        
        int segmentWidth = width / 8;
        int startX = x - width/2;
        
        g2d.setColor(wireColor);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(startX, y, startX + segmentWidth, y); // Cable izquierdo

        // Zigzag del resistor
        int zigzagX = startX + segmentWidth;
        for (int i = 0; i < 3; i++) {
            g2d.drawLine(zigzagX, (i%2 == 0) ? y-8 : y+8, zigzagX + segmentWidth*2, (i%2 == 0) ? y+8 : y-8);
            zigzagX += segmentWidth*2;
        }
        
        g2d.setColor(wireColor);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(startX + 7 * segmentWidth, y, startX + 8 * segmentWidth, y); // Cable derecho
        
        // Etiqueta
        if (label != null) {
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2d.drawString(label, x - 10, y - 15);
        }
    }
    
    private void drawSeriesCircuit(Graphics2D g2d, int centerX, int centerY) {
        int startX = 50;
        int y = centerY;
        int componentWidth = 60;
        int spacing = 20;

        g2d.setColor(wireColor);
        g2d.setStroke(new BasicStroke(2f));
        
        // Línea de tierra
        g2d.drawLine(startX, y + 50, getWidth() - startX, y + 50);
        
        // Conexión inicial
        g2d.drawLine(startX, y, startX, y + 50);
        
        int currentX = startX;
        
        // Dibujar componentes en serie de todas las ramas
        for (DCBranch branch : circuit.getBranches()) {
            for (DCComponent comp : branch.getComponents()) {
                currentX += (componentWidth/2 + spacing);
                
                if (comp.getType() == DCComponentType.RESISTOR) {
                    drawResistor(g2d, currentX, y, componentWidth - 20, comp.getValue() + "Ω");
                } else if (comp.getType() == DCComponentType.BATTERY || comp.getType() == DCComponentType.DC_SOURCE) {
                    drawBattery(g2d, currentX - 15, y, 30, 20, comp.getValue());
                }
                
                currentX += (componentWidth/2 + spacing);
                g2d.setColor(wireColor);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawLine(currentX - (componentWidth/2 + spacing), y, currentX, y);
            }
        }
        
        // Conexión final
        g2d.drawLine(currentX, y, getWidth() - startX, y);
        g2d.drawLine(getWidth() - startX, y, getWidth() - startX, y + 50);
    }
    
    private void drawParallelCircuit(Graphics2D g2d, int centerX, int centerY) {
        int numBranches = circuit.getBranches().size();
        if (numBranches == 0) return;
        
        int totalHeight = (numBranches - 1) * 60;
        int startY = centerY - totalHeight / 2;
        int startX = 80;
        int endX = getWidth() - 80;
        int componentWidth = 80;

        g2d.setColor(wireColor);
        g2d.setStroke(new BasicStroke(2f));
        
        // Dibujar nodos verticales (barras colectoras)
        g2d.drawLine(startX, startY - 20, startX, startY + totalHeight + 20);
        g2d.drawLine(endX, startY - 20, endX, startY + totalHeight + 20);
        
        // Dibujar nodos horizontales superior e inferior
        g2d.drawLine(startX, startY - 20, endX, startY - 20); // Nodo superior (Va)
        g2d.drawLine(startX, startY + totalHeight + 20, endX, startY + totalHeight + 20); // Nodo inferior (Tierra)
        
        // Dibujar cada rama
        for (int i = 0; i < numBranches; i++) {
            DCBranch branch = circuit.getBranches().get(i);
            int branchY = startY + i * 60;
            
            // Conexiones de la rama a los nodos
            g2d.drawLine(startX, branchY, startX + 50, branchY);
            g2d.drawLine(endX - 50, branchY, endX, branchY);
            
            // Dibujar componentes en la rama (simplificado: 2 componentes max)
            int currentX = startX + 70;
            List<DCComponent> components = branch.getComponents();
            
            if (components.isEmpty()) {
                // Solo un cable
                g2d.drawLine(startX + 50, branchY, endX - 50, branchY);
            } else {
                for (DCComponent comp : components) {
                    if (comp.getType() == DCComponentType.RESISTOR) {
                        drawResistor(g2d, currentX, branchY, componentWidth, comp.getValue() + "Ω");
                    } else if (comp.getType() == DCComponentType.BATTERY || comp.getType() == DCComponentType.DC_SOURCE) {
                        drawBattery(g2d, currentX - 15, branchY, 30, 20, comp.getValue());
                    }
                    currentX += componentWidth + 10;
                }
                // Conectar último componente
                g2d.drawLine(currentX - componentWidth/2 - 10, branchY, endX - 50, branchY);
            }
        }
    }
}