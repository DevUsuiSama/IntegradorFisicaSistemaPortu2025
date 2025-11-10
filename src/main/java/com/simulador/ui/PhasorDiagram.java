package com.simulador.ui;

import com.simulador.model.CircuitComponent;
import com.simulador.model.SimulationResult;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * Diagrama fasorial para representar magnitudes y fases
 * Phasor diagram for representing magnitudes and phases
 */
public class PhasorDiagram extends BaseGraph {
    private SimulationResult result;
    private List<CircuitComponent> components;

    public PhasorDiagram(SimulationResult result, List<CircuitComponent> components) {
        this.result = result;
        this.components = components;
        setPreferredSize(new Dimension(800, 600));
    }

    @Override
    protected void drawGraph(Graphics2D g2d) {
        if (result == null || components == null || components.isEmpty()) {
            drawNoDataMessage(g2d);
            return;
        }

        drawPhasorDiagram(g2d);
    }

    private void drawPhasorDiagram(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 3;

        drawReferenceSystem(g2d, centerX, centerY, radius);
        drawPhasors(g2d, centerX, centerY, radius);
        drawInfoPanel(g2d);
        drawLegend(g2d);
    }

    private void drawReferenceSystem(Graphics2D g2d, int centerX, int centerY, int radius) {
        // Círculo de referencia
        g2d.setColor(new Color(240, 240, 240));
        g2d.fill(new Ellipse2D.Double(centerX - radius, centerY - radius,
                radius * 2, radius * 2));
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(new Ellipse2D.Double(centerX - radius, centerY - radius,
                radius * 2, radius * 2));

        // Ejes coordenados
        g2d.setColor(axisColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(centerX - radius - 20, centerY, centerX + radius + 20, centerY);
        g2d.drawLine(centerX, centerY - radius - 20, centerX, centerY + radius + 20);

        // Etiquetas de ejes
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.drawString("Real", centerX + radius + 5, centerY + 5);
        g2d.drawString("Imag", centerX + 5, centerY - radius - 5);
        g2d.drawString("+j", centerX + 5, centerY - radius - 25);
        g2d.drawString("-j", centerX + 5, centerY + radius + 20);

        // Escalas
        drawScales(g2d, centerX, centerY, radius);
    }

    private void drawScales(Graphics2D g2d, int centerX, int centerY, int radius) {
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2d.setStroke(new BasicStroke(1));

        // Escala en eje real
        for (int i = 1; i <= 4; i++) {
            int x = centerX + (i * radius) / 4;
            g2d.drawLine(x, centerY - 5, x, centerY + 5);
            g2d.drawString(String.valueOf(i), x - 3, centerY + 20);

            x = centerX - (i * radius) / 4;
            g2d.drawLine(x, centerY - 5, x, centerY + 5);
            g2d.drawString(String.valueOf(-i), x - 5, centerY + 20);
        }

        // Escala en eje imaginario
        for (int i = 1; i <= 4; i++) {
            int y = centerY - (i * radius) / 4;
            g2d.drawLine(centerX - 5, y, centerX + 5, y);
            g2d.drawString(String.valueOf(i) + "j", centerX + 10, y + 3);

            y = centerY + (i * radius) / 4;
            g2d.drawLine(centerX - 5, y, centerX + 5, y);
            g2d.drawString(String.valueOf(-i) + "j", centerX + 10, y + 3);
        }
    }

    private void drawPhasors(Graphics2D g2d, int centerX, int centerY, int radius) {
        // Calcular valores de componentes
        double totalR = 0, totalL = 0, totalC = 0;
        for (CircuitComponent comp : components) {
            totalR += comp.getResistance();
            totalL += comp.getInductance();
            totalC += comp.getCapacitance();
        }
        if (totalC <= 0)
            totalC = 1e12;

        double w = 2 * Math.PI * 60; // 60 Hz
        double I = result.getCurrent();
        double VR = I * totalR;
        double VL = I * w * totalL;
        double VC = I / (w * totalC);
        double V = result.getApparentPower() / I;

        // Encontrar escala apropiada
        double maxVoltage = Math.max(Math.max(VR, VL), Math.max(VC, V));
        if (maxVoltage < 0.001)
            maxVoltage = 1;
        double scale = radius * 0.8 / maxVoltage;

        // Dibujar fasores
        drawPhasor(g2d, centerX, centerY, VR * scale, 0,
                new Color(255, 140, 0), "V_R", String.format("%.2f V", VR));

        drawPhasor(g2d, centerX, centerY, 0, -VL * scale,
                new Color(147, 51, 234), "V_L", String.format("%.2f V", VL));

        drawPhasor(g2d, centerX, centerY, 0, VC * scale,
                new Color(0, 200, 200), "V_C", String.format("%.2f V", VC));

        // Fasor de corriente (referencia)
        double currentScale = radius * 0.4 / Math.max(I, 0.001);
        drawPhasor(g2d, centerX, centerY, I * currentScale, 0,
                new Color(0, 100, 255), "I", String.format("%.3f A", I));

        // Fasor de voltaje total
        double vx = V * Math.cos(result.getPhaseAngle()) * scale;
        double vy = -V * Math.sin(result.getPhaseAngle()) * scale;
        drawPhasor(g2d, centerX, centerY, vx, vy,
                new Color(255, 0, 0), "V", String.format("%.2f V", V));

        // Dibujar ángulo de fase
        drawPhaseAngle(g2d, centerX, centerY, result.getPhaseAngle());
    }

    private void drawPhasor(Graphics2D g2d, int startX, int startY,
            double dx, double dy, Color color,
            String label, String value) {
        if (Math.abs(dx) < 0.1 && Math.abs(dy) < 0.1)
            return;

        int endX = startX + (int) dx;
        int endY = startY + (int) dy;

        // Línea del fasor
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(startX, startY, endX, endY);

        // Cabeza de flecha
        drawArrowHead(g2d, startX, startY, endX, endY, color);

        // Etiqueta
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        int labelX = startX + (int) (dx * 0.6);
        int labelY = startY + (int) (dy * 0.6);

        // Fondo para etiqueta
        g2d.setColor(new Color(255, 255, 255, 200));
        int textWidth = g2d.getFontMetrics().stringWidth(label);
        g2d.fillRoundRect(labelX - 3, labelY - 12, textWidth + 6, 18, 5, 5);

        g2d.setColor(color);
        g2d.drawString(label, labelX, labelY);

        // Valor
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawString(value, endX + 5, endY - 5);
    }

    private void drawArrowHead(Graphics2D g2d, int startX, int startY,
            int endX, int endY, Color color) {
        double angle = Math.atan2(endY - startY, endX - startX);
        int arrowSize = 12;

        int x1 = endX - (int) (arrowSize * Math.cos(angle - Math.PI / 6));
        int y1 = endY - (int) (arrowSize * Math.sin(angle - Math.PI / 6));
        int x2 = endX - (int) (arrowSize * Math.cos(angle + Math.PI / 6));
        int y2 = endY - (int) (arrowSize * Math.sin(angle + Math.PI / 6));

        g2d.setColor(color);
        g2d.fillPolygon(new int[] { endX, x1, x2 }, new int[] { endY, y1, y2 }, 3);
    }

    private void drawPhaseAngle(Graphics2D g2d, int centerX, int centerY, double phaseAngle) {
        if (Math.abs(phaseAngle) < 0.01)
            return;

        int arcRadius = 50;
        g2d.setColor(new Color(0, 150, 0, 180));
        g2d.setStroke(new BasicStroke(2));

        double startAngle = 0;
        double angleExtent = -Math.toDegrees(phaseAngle);

        Arc2D arc = new Arc2D.Double(centerX - arcRadius, centerY - arcRadius,
                2 * arcRadius, 2 * arcRadius,
                startAngle, angleExtent, Arc2D.OPEN);
        g2d.draw(arc);

        // Etiqueta del ángulo
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.setColor(new Color(0, 100, 0));
        String angleText = String.format("φ = %.1f°", Math.toDegrees(phaseAngle));

        int textX = centerX + arcRadius + 15;
        int textY = centerY + (phaseAngle > 0 ? 25 : -15);

        // Fondo para texto
        g2d.setColor(new Color(240, 255, 240, 220));
        int textWidth = g2d.getFontMetrics().stringWidth(angleText);
        g2d.fillRoundRect(textX - 5, textY - 15, textWidth + 10, 20, 5, 5);

        g2d.setColor(new Color(0, 100, 0));
        g2d.drawString(angleText, textX, textY);
    }

    private void drawInfoPanel(Graphics2D g2d) {
        double totalR = 0, totalL = 0, totalC = 0;
        for (CircuitComponent comp : components) {
            totalR += comp.getResistance();
            totalL += comp.getInductance();
            totalC += comp.getCapacitance();
        }

        double w = 2 * Math.PI * 60;
        double XL = w * totalL;
        double XC = 1.0 / (w * totalC);
        double Z = Math.sqrt(totalR * totalR + Math.pow(XL - XC, 2));

        String[] infoLines = {
                String.format("Impedancia: %.2f Ω", Z),
                String.format("Resistencia: %.2f Ω", totalR),
                String.format("Reactancia: %.2f Ω", XL - XC),
                String.format("Ángulo Fase: %.1f°", Math.toDegrees(result.getPhaseAngle())),
                String.format("Factor Potencia: %.3f", result.getPowerFactor())
        };

        drawInfoPanel(g2d, "Parámetros del Circuito", infoLines);
    }

    private void drawLegend(Graphics2D g2d) {
        String[] labels = {
                "I - Corriente (referencia)",
                "V_R - Voltaje Resistivo",
                "V_L - Voltaje Inductivo (+90°)",
                "V_C - Voltaje Capacitivo (-90°)",
                "V - Voltaje Total (resultante)"
        };

        Color[] colors = {
                new Color(0, 100, 255),
                new Color(255, 140, 0),
                new Color(147, 51, 234),
                new Color(0, 200, 200),
                new Color(255, 0, 0)
        };

        drawLegend(g2d, labels, colors, 30, 60);
    }

    private void drawNoDataMessage(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();

        g2d.setColor(new Color(200, 0, 0));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        String message = "No hay datos para mostrar el diagrama fasorial";
        int textWidth = g2d.getFontMetrics().stringWidth(message);
        g2d.drawString(message, (width - textWidth) / 2, height / 2);

        String[] infoLines = {
                "Ejecute una simulación primero",
                "Agregue componentes al circuito",
                "Configure voltaje y frecuencia"
        };
        drawInfoPanel(g2d, "Instrucciones", infoLines);
    }

    public void setData(SimulationResult result, List<CircuitComponent> components) {
        this.result = result;
        this.components = components;
        repaint();
    }
}