package com.simulador.engine.dc;

import com.simulador.model.dc.DCCircuit;
import com.simulador.model.dc.DCSimulationResult;
import java.util.HashMap;
import java.util.Map;

/**
 * Motor principal para simulación de circuitos DC (Principio Abierto/Cerrado)
 */
public class DCCircuitEngine {
    private final Map<DCAnalysisMethod, DCAnalysisStrategy> strategies;
    private DCAnalysisStrategy currentStrategy;
    
    public DCCircuitEngine() {
        this.strategies = new HashMap<>();
        initializeStrategies();
        // Estrategia por defecto
        this.currentStrategy = strategies.get(DCAnalysisMethod.OHM_LAW);
    }
    
    private void initializeStrategies() {
        // Registro de todas las estrategias disponibles
        strategies.put(DCAnalysisMethod.OHM_LAW, new DCOhmLawStrategy());
        strategies.put(DCAnalysisMethod.KIRCHHOFF_LAWS, new DCKirchhoffStrategy());
        // Pueden agregarse más estrategias aquí sin modificar la clase
    }
    
    public void setAnalysisMethod(DCAnalysisMethod method) {
        DCAnalysisStrategy strategy = strategies.get(method);
        if (strategy != null) {
            this.currentStrategy = strategy;
        } else {
            throw new IllegalArgumentException("Método de análisis no soportado: " + method);
        }
    }
    
    public DCSimulationResult simulate(DCCircuit circuit) {
        if (circuit == null || !circuit.isValid()) {
            throw new IllegalArgumentException("Circuito DC no válido para simulación");
        }
        
        if (!currentStrategy.validateCircuit(circuit)) {
            // Fallback a Ley de Ohm si la estrategia actual no es compatible
            DCAnalysisStrategy fallback = strategies.get(DCAnalysisMethod.OHM_LAW);
            return fallback.analyze(circuit);
        }
        
        return currentStrategy.analyze(circuit);
    }
    
    public DCAnalysisMethod getCurrentMethod() {
        for (Map.Entry<DCAnalysisMethod, DCAnalysisStrategy> entry : strategies.entrySet()) {
            if (entry.getValue() == currentStrategy) {
                return entry.getKey();
            }
        }
        return DCAnalysisMethod.OHM_LAW;
    }
    
    public DCAnalysisMethod[] getAvailableMethods() {
        return strategies.keySet().toArray(new DCAnalysisMethod[0]);
    }
    
    public boolean isMethodSupported(DCAnalysisMethod method) {
        return strategies.containsKey(method);
    }
}