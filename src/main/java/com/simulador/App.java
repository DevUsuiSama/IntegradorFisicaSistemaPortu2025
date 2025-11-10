package com.simulador;

import com.simulador.ui.MainSimulatorFrame;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada principal - Simulador RLC con Planificación
 */
public class App {
    public static void main(String[] args) {
        System.out.println("=== Simulador de Circuitos RLC con Algoritmos de Planificación ===");
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("\nINSTRUCCIONES PARA EJECUCIÓN EN UBUNTU:");
        System.out.println("1. Compilar: mvn clean package");
        System.out.println("2. Ejecutar con métricas del sistema:");
        System.out.println("   time java -jar simuladorRLC.jar");
        System.out.println("   top -b -d 1 > metricas_sistema.txt");
        System.out.println("   vmstat 1 60 > metricas_vmstat.txt");
        System.out.println("   free -h > memoria_inicio.txt");
        System.out.println("\nLos algoritmos FCFS, Round Robin y SJF están integrados");
        System.out.println("en la simulación de circuitos eléctricos RLC.");
        
        SwingUtilities.invokeLater(() -> {
            try {
                MainSimulatorFrame.main(args);
            } catch (Exception e) {
                System.err.println("Error iniciando aplicación: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}