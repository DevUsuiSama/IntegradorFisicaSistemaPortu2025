package com.simulador.engine.dc;

import com.simulador.model.dc.DCCircuit;
import com.simulador.model.dc.DCSimulationResult;
import java.util.HashMap;
import java.util.Map;

/**
 * Motor principal para simulación de circuitos DC (Principio Abierto/Cerrado)
 * ACTUALIZADO: El método initializeStrategies() ahora registra todas
 * las estrategias disponibles en el paquete.
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
        
        // --- INICIO DE MODIFICACIÓN ---
        // Agregar las estrategias faltantes que ya existen en el proyecto
        strategies.put(DCAnalysisMethod.MESH_ANALYSIS, new DCMeshAnalysisStrategy());
        strategies.put(DCAnalysisMethod.NODE_ANALYSIS, new DCNodeAnalysisStrategy());
        strategies.put(DCAnalysisMethod.THEVENIN_THEOREM, new DCTheveninStrategy());
        // (Nota: Norton y Source Transformation no tienen clases de estrategia en tu dump)
        // --- FIN DE MODIFICACIÓN ---
    }
    
    public void setAnalysisMethod(DCAnalysisMethod method) {
        DCAnalysisStrategy strategy = strategies.get(method);
        if (strategy != null) {
            this.currentStrategy = strategy;
        } else {
            // Si la estrategia no está registrada (ej. Norton), usa Ohm como fallback
            System.err.println("Advertencia: Método " + method + " no implementado. Usando Ley de Ohm.");
            this.currentStrategy = strategies.get(DCAnalysisMethod.OHM_LAW);
            // throw new IllegalArgumentException("Método de análisis no soportado: " + method);
        }
    }
    
    public DCSimulationResult simulate(DCCircuit circuit) {
        if (circuit == null || !circuit.isValid()) {
            throw new IllegalArgumentException("Circuito DC no válido para simulación");
        }
        
        if (!currentStrategy.validateCircuit(circuit)) {
            // Fallback a Ley de Ohm si la estrategia actual no es compatible
            DCAnalysisStrategy fallback = strategies.get(DCAnalysisMethod.OHM_LAW);
            if(fallback.validateCircuit(circuit)) {
                 return fallback.analyze(circuit);
            } else {
                throw new IllegalArgumentException("El circuito no es válido para la estrategia seleccionada ni para la Ley de Ohm.");
            }
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
        // Retorna solo los métodos que han sido registrados
        return strategies.keySet().toArray(new DCAnalysisMethod[0]);
    }
    
    public boolean isMethodSupported(DCAnalysisMethod method) {
        return strategies.containsKey(method);
    }
}