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
            drawNoDataMessage(g2d, "No hay datos de simulación disponibles");
            drawInfoPanel(g2d, "Instrucciones", new String[] {
                    "Ejecute una simulación primero",
                    "Agregue componentes al circuito",
                    "Configure voltaje y frecuencia"
            });
            return;
        }

        double maxCurrent = Math.abs(result.getCurrent()) * 1.5;
        if (maxCurrent < 0.001)
            maxCurrent = 0.001;

        drawYScale(g2d, -maxCurrent, maxCurrent, 8, "%.3f");

        double totalTime = 0.05;
        drawXScale(g2d, 0, totalTime, 10, "%.3f");

        drawCurrentCurve(g2d, maxCurrent, totalTime);
        drawInfoPanel(g2d);
        drawLegend(g2d);
    }

    private void drawCurrentCurve(Graphics2D g2d, double maxCurrent, double totalTime) {
        int width = getWidth();
        int height = getHeight();
        int graphWidth = width - 2 * padding;
        int graphHeight = height - 2 * padding;

        g2d.setColor(new Color(0, 100, 255, 220));
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        currentCurve.reset();
        int points = 500;
        boolean firstPoint = true;

        for (int i = 0; i < points; i++) {
            double t = (i * totalTime) / points;
            double current = calculateInstantaneousCurrent(t);

            int x = padding + (int) (t * graphWidth / totalTime);
            int y = height - padding - (int) ((current + maxCurrent) * graphHeight / (2 * maxCurrent));

            if (firstPoint) {
                currentCurve.moveTo(x, y);
                firstPoint = false;
            } else {
                currentCurve.lineTo(x, y);
            }
        }

        g2d.draw(currentCurve);

        // Línea cero
        g2d.setColor(new Color(0, 100, 255, 220));
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[] { 5, 5 }, 0));
        int zeroY = height - padding - (int) (maxCurrent * graphHeight / (2 * maxCurrent));
        g2d.drawLine(padding, zeroY, width - padding, zeroY);

        // Relleno bajo la curva
        GradientPaint gradient = new GradientPaint(
                padding, height - padding, new Color(0, 100, 255, 40),
                padding, padding, new Color(0, 100, 255, 10));
        g2d.setPaint(gradient);

        Path2D fillArea = (Path2D) currentCurve.clone();
        fillArea.lineTo(width - padding, height - padding);
        fillArea.lineTo(padding, height - padding);
        fillArea.closePath();
        g2d.fill(fillArea);
    }

    private void drawInfoPanel(Graphics2D g2d) {
        String[] infoLines = {
                String.format("Corriente Pico: %.3f A", result.getCurrent()),
                String.format("Fase: %.1f°", Math.toDegrees(result.getPhaseAngle())),
                String.format("Impedancia: %.2f Ω", result.getImpedance()),
                "Frecuencia: 60 Hz",
                String.format("Potencia Activa: %.2f W", result.getActivePower())
        };

        drawInfoPanel(g2d, "Información de Corriente", infoLines);
    }

    private void drawLegend(Graphics2D g2d) {
        String[] legendLabels = { "Corriente del Circuito" };
        Color[] legendColors = { new Color(0, 100, 255) };
        drawLegend(g2d, legendLabels, legendColors, 30, 60);
    }

    private double calculateInstantaneousCurrent(double time) {
        double amplitude = result.getCurrent();
        double frequency = 60.0;
        double phase = result.getPhaseAngle();

        return amplitude * Math.sin(2 * Math.PI * frequency * time + phase);
    }

    public void setResult(SimulationResult result) {
        this.result = result;
        repaint();
    }
}