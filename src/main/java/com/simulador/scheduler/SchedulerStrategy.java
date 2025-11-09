package com.simulador.scheduler;

import com.simulador.model.CircuitSimulationTask;
import java.util.List;

/**
 * Interfaz Strategy para algoritmos de planificación
 * Strategy interface for scheduling algorithms
 */
public interface SchedulerStrategy {
    
    /**
     * Planifica y ejecuta las tareas según el algoritmo específico
     * Schedules and executes tasks according to the specific algorithm
     */
    void schedule(List<CircuitSimulationTask> tasks);
    
    /**
     * Obtiene el nombre del algoritmo
     * Gets the algorithm name
     */
    String getName();
    
    /**
     * Obtiene la descripción del algoritmo
     * Gets the algorithm description  
     */
    String getDescription();
    
    /**
     * Interrumpe la ejecución actual
     * Interrupts current execution
     */
    void interrupt();
    
    /**
     * Verifica si el planificador está ejecutando
     * Checks if scheduler is running
     */
    boolean isRunning();
}