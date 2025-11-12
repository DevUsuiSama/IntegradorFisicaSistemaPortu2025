package com.simulador.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal del simulador de circuitos RLC con planificación
 * Main window for RLC circuit simulator with scheduling
 */
public class MainSimulatorFrame extends JFrame {
    private RLCSimulator physicsSimulator;
    
    public MainSimulatorFrame() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Simulador de Circuitos RLC - Algoritmos de Planificación");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1200, 800));
        
        // Crear el simulador principal
        physicsSimulator = new RLCSimulator();
        
        // --- INICIO DE MODIFICACIÓN ---
        // Añadir la barra de menú creada en el panel principal
        setJMenuBar(physicsSimulator.getAppMenuBar());
        // --- FIN DE MODIFICACIÓN ---
        
        setLayout(new BorderLayout());
        add(physicsSimulator, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
        
        setupSafeClose();
    }
    
    private void setupSafeClose() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
           public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeApplication();
            }
        });
    }
    
    private void closeApplication() {
        if (physicsSimulator != null) {
            physicsSimulator.disposeResources();
        }
        dispose();
        System.exit(0);
    }
    
    public static void main(String[] args) {
        setupLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Iniciando Simulador RLC con Planificación...");
                MainSimulatorFrame mainFrame = new MainSimulatorFrame();
                mainFrame.setVisible(true);
                System.out.println("Simulador iniciado correctamente");
                
                // Información para el usuario
                System.out.println("\n=== INSTRUCCIONES PARA EJECUCIÓN EN UBUNTU ===");
                System.out.println("1. Compilar: mvn clean package");
                System.out.println("2. Ejecutar con métricas:");
                System.out.println("    time java -jar simuladorRLC.jar");
                System.out.println("    top -b -d 1 > metricas_sistema.txt");
                System.out.println("    vmstat 1 60 > metricas_vmstat.txt");
                System.out.println("    free -h > memoria_inicio.txt");
                System.out.println("3. Los algoritmos de planificación están integrados");
                System.out.println("    en la simulación de circuitos RLC");
                
            } catch (Exception e) {
                handleStartupError(e);
            }
        });
    }
    
    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private static void handleStartupError(Exception e) {
        System.err.println("Error iniciando la aplicación: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(null,
            "Error iniciando la aplicación:\n" + e.getMessage(),
            "Error de Inicio", JOptionPane.ERROR_MESSAGE);
    }
}