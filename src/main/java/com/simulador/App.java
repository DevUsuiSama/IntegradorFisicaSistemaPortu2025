package com.simulador;

import com.simulador.ui.MainSimulatorFrame;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada principal de la aplicación
 * Main application entry point
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Iniciando Simulador...");
        System.out.println("Java version: " + System.getProperty("java.version"));
        
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Creando interfaz gráfica...");
                // Usar el nuevo MainSimulatorFrame
                MainSimulatorFrame.main(args);
            } catch (Exception e) {
                System.err.println("Error iniciando aplicación: " + e.getMessage());
                e.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(null, 
                    "Error: " + e.getMessage(), "Error", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}