package com.simulador.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Clase base abstracta para gráficos del simulador RLC
 * Abstract base class for RLC simulator graphs
 */
public abstract class BaseGraph extends JPanel {
    protected int padding = 60;
    protected Color gridColor = new Color(230, 230, 230);
    protected Color axisColor = Color.BLACK;
    protected BasicStroke axisStroke = new BasicStroke(2);
    protected BasicStroke gridStroke = new BasicStroke(1);
    protected Font labelFont = new Font("Arial", Font.BOLD, 12);
    protected Font scaleFont = new Font("Arial", Font.PLAIN, 10);
    
    public BaseGraph() {
        setPreferredSize(new Dimension(800, 550));
        setBackground(Color.WHITE);
        setOpaque(true);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Configurar renderizado de alta calidad
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
        
        drawGraph(g2d);
    }
    
    protected abstract void drawGraph(Graphics2D g2d);
    
    protected void drawAxes(Graphics2D g2d, String xLabel, String yLabel) {
        int width = getWidth();
        int height = getHeight();
        
        // Dibujar ejes
        g2d.setColor(axisColor);
        g2d.setStroke(axisStroke);
        g2d.drawLine(padding, padding, padding, height - padding); // Eje Y
        g2d.drawLine(padding, height - padding, width - padding, height - padding); // Eje X
        
        // Etiquetas de ejes
        g2d.setFont(labelFont);
        g2d.setColor(axisColor);
        g2d.drawString(yLabel, 15, padding - 10);
        g2d.drawString(xLabel, width - padding - 30, height - padding + 35);
    }
    
    protected void drawGrid(Graphics2D g2d, int xDivs, int yDivs) {
        int width = getWidth();
        int height = getHeight();
        int graphWidth = width - 2 * padding;
        int graphHeight = height - 2 * padding;
        
        g2d.setColor(gridColor);
        g2d.setStroke(gridStroke);
        
        // Líneas horizontales
        for (int i = 1; i < yDivs; i++) {
            int y = padding + (i * graphHeight) / yDivs;
            g2d.drawLine(padding, y, width - padding, y);
        }
        
        // Líneas verticales
        for (int i = 1; i < xDivs; i++) {
            int x = padding + (i * graphWidth) / xDivs;
            g2d.drawLine(x, padding, x, height - padding);
        }
    }
    
    protected void drawYScale(Graphics2D g2d, double minValue, double maxValue, int divisions, String format) {
        int height = getHeight();
        int graphHeight = height - 2 * padding;
        
        g2d.setFont(scaleFont);
        g2d.setColor(Color.GRAY);
        
        for (int i = 0; i <= divisions; i++) {
            int y = height - padding - (i * graphHeight) / divisions;
            double value = minValue + (i * (maxValue - minValue)) / divisions;
            
            // Marca de escala
            g2d.drawLine(padding - 5, y, padding + 5, y);
            
            // Texto del valor
            g2d.setColor(axisColor);
            String text = String.format(format, value);
            int textWidth = g2d.getFontMetrics().stringWidth(text);
            g2d.drawString(text, padding - textWidth - 8, y + 4);
            g2d.setColor(Color.GRAY);
        }
    }
    
    protected void drawXScale(Graphics2D g2d, double minValue, double maxValue, int divisions, String format) {
        int width = getWidth();
        int height = getHeight();
        int graphWidth = width - 2 * padding;
        
        g2d.setFont(scaleFont);
        g2d.setColor(Color.GRAY);
        
        for (int i = 0; i <= divisions; i++) {
            int x = padding + (i * graphWidth) / divisions;
            double value = minValue + (i * (maxValue - minValue)) / divisions;
            
            // Marca de escala
            g2d.drawLine(x, height - padding - 5, x, height - padding + 5);
            
            // Texto del valor
            g2d.setColor(axisColor);
            String text = String.format(format, value);
            int textWidth = g2d.getFontMetrics().stringWidth(text);
            g2d.drawString(text, x - textWidth / 2, height - padding + 20);
            g2d.setColor(Color.GRAY);
        }
    }
    
    protected void drawLegend(Graphics2D g2d, String[] labels, Color[] colors, int x, int y) {
        g2d.setFont(scaleFont);
        
        for (int i = 0; i < labels.length; i++) {
            if (i < colors.length) {
                g2d.setColor(colors[i]);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(x, y + i * 20, x + 20, y + i * 20);
            }
            
            g2d.setColor(Color.BLACK);
            g2d.drawString(labels[i], x + 25, y + i * 20 + 4);
        }
    }
}