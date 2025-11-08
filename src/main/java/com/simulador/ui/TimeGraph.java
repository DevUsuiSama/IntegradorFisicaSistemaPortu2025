package com.simulador.ui;

import com.simulador.model.SimulationResult;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Gráfico de dominio de tiempo para corriente del circuito
 * Time domain graph for circuit current
 */
public class TimeGraph extends BaseGraph {
    private SimulationResult result;
    private Path2D.Double currentCurve;
    
    public TimeGraph(SimulationResult result) {
        this.result = result;
        this.currentCurve = new Path2D.Double();
        setPreferredSize(new Dimension(800, 550));
    }
    
    @Override
    protected void drawGraph(Graphics2D g2d) {
        drawAxes(g2d, "Tiempo (s)", "Corriente (A)");
        drawGrid(g2d, 10, 8);
        
        if (result == null) {
            drawNoDataMessage(g2d);
            return;
        }
        
        double maxCurrent = Math.abs(result.getCurrent()) * 1.5;
        if (maxCurrent < 0.001) maxCurrent = 0.001;
        
        drawYScale(g2d, -maxCurrent, maxCurrent, 8, "%.3f");
        
        double totalTime = 0.05; // 50 ms
        drawXScale(g2d, 0, totalTime, 10, "%.3f");
        
        drawCurrentCurve(g2d, maxCurrent, totalTime);
        drawInfoBox(g2d, maxCurrent);
    }
    
    private void drawCurrentCurve(Graphics2D g2d, double maxCurrent, double totalTime) {
        int width = getWidth();
        int height = getHeight();
        int graphWidth = width - 2 * padding;
        int graphHeight = height - 2 * padding;
        
        g2d.setColor(new Color(0, 100, 255, 200));
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        currentCurve.reset();
        int points = 500;
        boolean firstPoint = true;
        
        for (int i = 0; i < points; i++) {
            double t = (i * totalTime) / points;
            double current = calculateInstantaneousCurrent(t);
            
            int x = padding + (int)(t * graphWidth / totalTime);
            int y = height - padding - (int)((current + maxCurrent) * graphHeight / (2 * maxCurrent));
            
            if (firstPoint) {
                currentCurve.moveTo(x, y);
                firstPoint = false;
            } else {
                currentCurve.lineTo(x, y);
            }
        }
        
        g2d.draw(currentCurve);
        
        // Dibujar línea cero
        g2d.setColor(new Color(150, 150, 150, 150));
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 
                                     0, new float[]{5, 5}, 0));
        int zeroY = height - padding - (int)(maxCurrent * graphHeight / (2 * maxCurrent));
        g2d.drawLine(padding, zeroY, width - padding, zeroY);
    }
    
    private double calculateInstantaneousCurrent(double time) {
        double amplitude = result.getCurrent();
        double frequency = 60.0; // Hz
        double phase = result.getPhaseAngle();
        
        return amplitude * Math.sin(2 * Math.PI * frequency * time + phase);
    }
    
    private void drawInfoBox(Graphics2D g2d, double maxCurrent) {
        int width = getWidth();
        
        g2d.setColor(new Color(240, 240, 240, 200));
        g2d.fillRoundRect(width - 200, 20, 180, 90, 10, 10);
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Información de Corriente", width - 190, 35);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.drawString(String.format("Corriente Pico: %.3f A", result.getCurrent()), width - 190, 55);
        g2d.drawString(String.format("Fase: %.1f°", Math.toDegrees(result.getPhaseAngle())), width - 190, 70);
        g2d.drawString("Frecuencia: 60 Hz", width - 190, 85);
        g2d.drawString(String.format("Impedancia: %.2f Ω", result.getImpedance()), width - 190, 100);
    }
    
    private void drawNoDataMessage(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();
        
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String message = "No hay datos de simulación disponibles";
        int textWidth = g2d.getFontMetrics().stringWidth(message);
        g2d.drawString(message, (width - textWidth) / 2, height / 2);
    }
    
    public void setResult(SimulationResult result) {
        this.result = result;
        repaint();
    }
}