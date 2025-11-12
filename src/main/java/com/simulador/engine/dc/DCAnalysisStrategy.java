package com.simulador.engine.dc;

import com.simulador.model.dc.DCCircuit;
import com.simulador.model.dc.DCSimulationResult;

/**
 * Interfaz para estrategias de análisis de circuitos DC (Principio de Inversión de Dependencias)
 */
public interface DCAnalysisStrategy {
    /**
     * Analiza el circuito DC y retorna los resultados
     */
    DCSimulationResult analyze(DCCircuit circuit);
    
    /**
     * Retorna el nombre del método de análisis
     */
    String getMethodName();
    
    /**
     * Valida si el circuito es compatible con este método de análisis
     */
    boolean validateCircuit(DCCircuit circuit);
}