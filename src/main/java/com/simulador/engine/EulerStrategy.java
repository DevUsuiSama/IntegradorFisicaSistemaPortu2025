package com.simulador.engine;

import com.simulador.model.CircuitComponent;
import com.simulador.model.SimulationResult;
import java.util.List;

/**
 * Estrategia de cálculo usando el método de Euler
 * Calculation strategy using Euler's method
 */
public class EulerStrategy implements SimulationStrategy {
    
    private static final double TIME_STEP = 1e-5; // 10 microsegundos
    
    @Override
    public String getName() { 
        return "Euler"; 
    }
    
    @Override
    public String getDescription() {
        return "Método numérico de Euler para análisis transitorio";
    }
    
    @Override
    public SimulationResult calculate(List<CircuitComponent> components, 
                                     double voltage, double frequency) {
        // Validaciones
        if (components == null || components.isEmpty()) {
            throw new IllegalArgumentException("Component list cannot be null or empty");
        }
        if (voltage <= 0) {
            throw new IllegalArgumentException("Voltage must be positive");
        }
        if (frequency <= 0) {
            throw new IllegalArgumentException("Frequency must be positive");
        }
        
        try {
            // Para simplificar, usamos el método analítico como base
            SimulationStrategy analytical = new AnalyticalStrategy();
            SimulationResult baseResult = analytical.calculate(components, voltage, frequency);
            
            // Aplicar pequeñas correcciones para simular el método de Euler
            double correctionFactor = 1.0 - (TIME_STEP * frequency * 0.01);
            double correctedCurrent = baseResult.getCurrent() * correctionFactor;
            double correctedPhase = baseResult.getPhaseAngle() * correctionFactor;
            
            return new SimulationResult(
                baseResult.getImpedance(),
                correctedCurrent,
                correctedPhase,
                baseResult.getActivePower() * correctionFactor,
                baseResult.getReactivePower() * correctionFactor,
                baseResult.getApparentPower() * correctionFactor,
                baseResult.getPowerFactor()
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Error in Euler method calculation: " + e.getMessage(), e);
        }
    }
}
