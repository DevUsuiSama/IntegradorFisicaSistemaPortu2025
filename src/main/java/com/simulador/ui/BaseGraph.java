package com.simulador.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Clase base abstracta para gráficos del simulador RLC
 * Abstract base class for RLC simulator graphs
 */
public abstract class BaseGraph extends JPanel {
    protected int padding = 80;
    protected Color gridColor = new Color(240, 240, 240);
    protected Color axisColor = new Color(60, 60, 60);
    protected Color infoPanelColor = new Color(255, 255, 255, 230);
    protected Color infoTextColor = new Color(30, 30, 30);

    protected BasicStroke axisStroke = new BasicStroke(2.0f);
    protected BasicStroke gridStroke = new BasicStroke(1.0f);
    protected BasicStroke dataStroke = new BasicStroke(2.5f);

    protected Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
    protected Font scaleFont = new Font("Segoe UI", Font.PLAIN, 11);
    protected Font infoFont = new Font("Segoe UI", Font.PLAIN, 11);

    public BaseGraph() {
        setPreferredSize(new Dimension(800, 550));
        setBackground(Color.WHITE);
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

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

        g2d.setColor(axisColor);
        g2d.setStroke(axisStroke);
        g2d.drawLine(padding, padding, padding, height - padding);
        g2d.drawLine(padding, height - padding, width - padding, height - padding);

        g2d.setFont(labelFont);
        g2d.setColor(axisColor);

        g2d.drawString(yLabel, 20, height / 2);

        int xLabelWidth = g2d.getFontMetrics().stringWidth(xLabel);
        g2d.drawString(xLabel, width - padding - xLabelWidth, height - 20);
    }

    protected void drawGrid(Graphics2D g2d, int xDivs, int yDivs) {
        int width = getWidth();
        int height = getHeight();
        int graphWidth = width - 2 * padding;
        int graphHeight = height - 2 * padding;

        g2d.setColor(gridColor);
        g2d.setStroke(gridStroke);

        for (int i = 1; i < yDivs; i++) {
            int y = padding + (i * graphHeight) / yDivs;
            g2d.drawLine(padding, y, width - padding, y);
        }

        for (int i = 1; i < xDivs; i++) {
            int x = padding + (i * graphWidth) / xDivs;
            g2d.drawLine(x, padding, x, height - padding);
        }
    }

    // NUEVO MÉTODO: Dibujar panel de información en el lado derecho
    protected void drawInfoPanel(Graphics2D g2d, String title, String[] contentLines) {
        int width = getWidth();
        int panelWidth = 220;
        int panelHeight = 60 + contentLines.length * 18;
        int x = width - panelWidth - 20;
        int y = 30;

        // Fondo del panel semi-transparente
        g2d.setColor(infoPanelColor);
        g2d.fillRoundRect(x, y, panelWidth, panelHeight, 12, 12);

        // Borde del panel
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(x, y, panelWidth, panelHeight, 12, 12);

        // Título del panel
        g2d.setColor(infoTextColor);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString(title, x + 10, y + 20);

        // Contenido del panel
        g2d.setFont(infoFont);
        for (int i = 0; i < contentLines.length; i++) {
            g2d.drawString(contentLines[i], x + 10, y + 40 + i * 18);
        }
    }

    protected void drawYScale(Graphics2D g2d, double minValue, double maxValue, int divisions, String format) {
        int height = getHeight();
        int graphHeight = height - 2 * padding;

        g2d.setFont(scaleFont);
        g2d.setColor(axisColor);

        for (int i = 0; i <= divisions; i++) {
            int y = height - padding - (i * graphHeight) / divisions;
            double value = minValue + (i * (maxValue - minValue)) / divisions;

            g2d.drawLine(padding - 5, y, padding + 5, y);

            String text = String.format(format, value);
            int textWidth = g2d.getFontMetrics().stringWidth(text);
            g2d.drawString(text, padding - textWidth - 8, y + 4);
        }
    }

    protected void drawXScale(Graphics2D g2d, double minValue, double maxValue, int divisions, String format) {
        int width = getWidth();
        int height = getHeight();
        int graphWidth = width - 2 * padding;

        g2d.setFont(scaleFont);
        g2d.setColor(axisColor);

        for (int i = 0; i <= divisions; i++) {
            int x = padding + (i * graphWidth) / divisions;
            double value = minValue + (i * (maxValue - minValue)) / divisions;

            g2d.drawLine(x, height - padding - 5, x, height - padding + 5);

            String text = String.format(format, value);
            int textWidth = g2d.getFontMetrics().stringWidth(text);
            g2d.drawString(text, x - textWidth / 2, height - padding + 20);
        }
    }

    protected void drawLegend(Graphics2D g2d, String[] labels, Color[] colors, int x, int y) {
        g2d.setFont(infoFont);

        // Fondo de la leyenda
        g2d.setColor(infoPanelColor);
        int legendHeight = labels.length * 20 + 10;
        g2d.fillRoundRect(x - 5, y - 15, 200, legendHeight, 8, 8);

        // Borde de la leyenda
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawRoundRect(x - 5, y - 15, 200, legendHeight, 8, 8);

        // Elementos de la leyenda
        for (int i = 0; i < labels.length; i++) {
            if (i < colors.length) {
                g2d.setColor(colors[i]);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawLine(x, y + i * 20, x + 20, y + i * 20);
            }

            g2d.setColor(infoTextColor);
            g2d.drawString(labels[i], x + 25, y + i * 20 + 4);
        }
    }

    // Método auxiliar para mostrar mensaje cuando no hay datos
    protected void drawNoDataMessage(Graphics2D g2d, String message) {
        int width = getWidth();
        int height = getHeight();

        g2d.setColor(new Color(200, 0, 0));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        int textWidth = g2d.getFontMetrics().stringWidth(message);
        g2d.drawString(message, (width - textWidth) / 2, height / 2);
    }
}