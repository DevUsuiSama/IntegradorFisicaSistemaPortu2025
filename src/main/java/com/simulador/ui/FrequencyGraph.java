package com.simulador.ui;

import com.simulador.model.CircuitComponent;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;

/**
 * Gráfico de respuesta en frecuencia del circuito
 * Circuit frequency response graph
 */
public class FrequencyGraph extends BaseGraph {
    private List<CircuitComponent> components;
    private Path2D.Double impedanceCurve;
    private double resonantFrequency;

    public FrequencyGraph(List<CircuitComponent> components) {
        this.components = components;
        this.impedanceCurve = new Path2D.Double();
        calculateResonantFrequency();
    }

    @Override
    protected void drawGraph(Graphics2D g2d) {
        drawAxes(g2d, "Frecuencia (Hz)", "Impedancia (Ω)");

        if (components == null || components.isEmpty()) {
            drawNoDataMessage(g2d);
            return;
        }

        // Calcular curva de impedancia
        double[] frequencies = new double[200];
        double[] impedances = new double[200];
        double maxZ = 0;
        double minZ = Double.MAX_VALUE;

        for (int i = 0; i < 200; i++) {
            frequencies[i] = 1 + i * 5; // 1 Hz to 1000 Hz
            impedances[i] = calculateImpedance(frequencies[i]);
            maxZ = Math.max(maxZ, impedances[i]);
            minZ = Math.min(minZ, impedances[i]);
        }

        // Ajustar escala si es necesario
        if (maxZ < 1)
            maxZ = 1;
        if (minZ > maxZ * 0.8)
            minZ = 0;

        drawGrid(g2d, 10, 8);
        drawYScale(g2d, minZ, maxZ, 8, "%.1f");
        drawXScale(g2d, 1, 1000, 10, "%.0f");

        drawImpedanceCurve(g2d, frequencies, impedances, maxZ, minZ);
        drawResonanceInfo(g2d);
        drawInfoPanel(g2d);
    }

    private void drawImpedanceCurve(Graphics2D g2d, double[] frequencies,
            double[] impedances, double maxZ, double minZ) {
        int width = getWidth();
        int height = getHeight();
        int graphWidth = width - 2 * padding;
        int graphHeight = height - 2 * padding;

        g2d.setColor(new Color(200, 0, 0, 200));
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        impedanceCurve.reset();
        boolean firstPoint = true;

        for (int i = 0; i < frequencies.length; i++) {
            int x = padding + (int) ((frequencies[i] - 1) * graphWidth / 999);
            int y = height - padding - (int) ((impedances[i] - minZ) * graphHeight / (maxZ - minZ));

            if (firstPoint) {
                impedanceCurve.moveTo(x, y);
                firstPoint = false;
            } else {
                impedanceCurve.lineTo(x, y);
            }
        }

        g2d.draw(impedanceCurve);

        // Rellenar área bajo la curva
        GradientPaint gradient = new GradientPaint(
                padding, height - padding, new Color(200, 0, 0, 50),
                padding, padding, new Color(200, 0, 0, 10));
        g2d.setPaint(gradient);

        Path2D fillArea = (Path2D) impedanceCurve.clone();
        fillArea.lineTo(width - padding, height - padding);
        fillArea.lineTo(padding, height - padding);
        fillArea.closePath();
        g2d.fill(fillArea);
    }

    private double calculateImpedance(double freq) {
        double totalR = 0, totalL = 0, totalC = 0;
        for (CircuitComponent c : components) {
            totalR += c.getResistance();
            totalL += c.getInductance();
            totalC += c.getCapacitance();
        }

        if (totalC <= 0)
            totalC = 1e12;

        double w = 2 * Math.PI * freq;
        double XL = w * totalL;
        double XC = 1.0 / (w * totalC);
        return Math.sqrt(totalR * totalR + Math.pow(XL - XC, 2));
    }

    private void calculateResonantFrequency() {
        double totalL = 0, totalC = 0;
        for (CircuitComponent c : components) {
            totalL += c.getInductance();
            totalC += c.getCapacitance();
        }

        if (totalL > 0 && totalC > 0 && totalC < 1e10) {
            resonantFrequency = 1.0 / (2 * Math.PI * Math.sqrt(totalL * totalC));
        } else {
            resonantFrequency = -1;
        }
    }

    private void drawResonanceInfo(Graphics2D g2d) {
        if (resonantFrequency > 0 && resonantFrequency <= 1000) {
            int width = getWidth();
            int height = getHeight();
            int graphWidth = width - 2 * padding;

            int xRes = padding + (int) ((resonantFrequency - 1) * graphWidth / 999);

            // Línea vertical en frecuencia de resonancia
            g2d.setColor(new Color(0, 150, 0, 180));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, new float[] { 5, 5 }, 0));
            g2d.drawLine(xRes, padding, xRes, height - padding);
        }
    }

    private void drawInfoPanel(Graphics2D g2d) {
        if (components == null || components.isEmpty()) {
            String[] infoLines = {
                    "No hay componentes en el circuito",
                    "Agregue componentes para ver",
                    "la respuesta en frecuencia"
            };
            drawInfoPanel(g2d, "Información", infoLines);
            return;
        }

        double totalR = 0, totalL = 0, totalC = 0;
        for (CircuitComponent c : components) {
            totalR += c.getResistance();
            totalL += c.getInductance();
            totalC += c.getCapacitance();
        }

        String[] infoLines;
        if (resonantFrequency > 0) {
            infoLines = new String[] {
                    String.format("Frec. Resonancia: %.1f Hz", resonantFrequency),
                    String.format("Resistencia: %.2f Ω", totalR),
                    String.format("Inductancia: %.4f H", totalL),
                    String.format("Capacitancia: %.6f F", totalC),
                    String.format("Ancho de Banda: %.1f Hz", calculateBandwidth())
            };
        } else {
            infoLines = new String[] {
                    "No hay resonancia definida",
                    String.format("Resistencia: %.2f Ω", totalR),
                    String.format("Inductancia: %.4f H", totalL),
                    String.format("Capacitancia: %.6f F", totalC)
            };
        }

        drawInfoPanel(g2d, "Parámetros del Circuito", infoLines);
    }

    private double calculateBandwidth() {
        if (resonantFrequency <= 0)
            return 0;

        double totalR = 0, totalL = 0;
        for (CircuitComponent c : components) {
            totalR += c.getResistance();
            totalL += c.getInductance();
        }

        if (totalL > 0) {
            return totalR / (2 * Math.PI * totalL);
        }
        return 0;
    }

    private void drawNoDataMessage(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();

        g2d.setColor(new Color(200, 0, 0));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        String message = "No hay componentes en el circuito";
        int textWidth = g2d.getFontMetrics().stringWidth(message);
        g2d.drawString(message, (width - textWidth) / 2, height / 2);

        String[] infoLines = {
                "Agregue componentes RLC al circuito",
                "para ver la respuesta en frecuencia"
        };
        drawInfoPanel(g2d, "Instrucciones", infoLines);
    }

    public void setComponents(List<CircuitComponent> components) {
        this.components = components;
        calculateResonantFrequency();
        repaint();
    }
}