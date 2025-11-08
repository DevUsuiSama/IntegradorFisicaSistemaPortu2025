package com.simulador.engine;

import com.simulador.model.CircuitComponent;
import com.simulador.model.SimulationResult;
import java.util.List;

/**
 * Interfaz Strategy para diferentes métodos de simulación
 * Strategy interface for different simulation methods
 */
public interface SimulationStrategy {
    
    /**
     * Calcula los resultados del circuito
     * Calculates circuit results
     */
    SimulationResult calculate(List<CircuitComponent> components, 
                              double voltage, double frequency);
    
    /**
     * Obtiene el nombre del método
     * Gets the method name
     */
    String getName();
    
    /**
     * Obtiene la descripción del método
     * Gets the method description
     */
    String getDescription();
    
    /**
     * Valida si el método es aplicable para los componentes dados
     * Validates if the method is applicable for given components
     */
    default boolean isValidFor(List<CircuitComponent> components) {
        return components != null && !components.isEmpty();
    }
}