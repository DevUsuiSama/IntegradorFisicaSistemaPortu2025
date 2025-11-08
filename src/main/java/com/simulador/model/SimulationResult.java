package com.simulador.model;

/**
 * Contiene los resultados de una simulación de circuito RLC
 * Contains the results of an RLC circuit simulation
 */
public class SimulationResult {
    private double impedance;
    private double current;
    private double phaseAngle;
    private double activePower;
    private double reactivePower;
    private double apparentPower;
    private double powerFactor;
    
    public SimulationResult(double impedance, double current, double phaseAngle,
                          double activePower, double reactivePower, 
                          double apparentPower, double powerFactor) {
        this.impedance = impedance;
        this.current = current;
        this.phaseAngle = phaseAngle;
        this.activePower = activePower;
        this.reactivePower = reactivePower;
        this.apparentPower = apparentPower;
        this.powerFactor = powerFactor;
    }
    
    // Getters
    public double getImpedance() { 
        return impedance; 
    }
    
    public double getCurrent() { 
        return current; 
    }
    
    public double getPhaseAngle() { 
        return phaseAngle; 
    }
    
    public double getActivePower() { 
        return activePower; 
    }
    
    public double getReactivePower() { 
        return reactivePower; 
    }
    
    public double getApparentPower() { 
        return apparentPower; 
    }
    
    public double getPowerFactor() { 
        return powerFactor; 
    }
    
    /**
     * Valida que los resultados sean consistentes y físicamente posibles
     * Validates that results are consistent and physically possible
     */
    public boolean isValid() {
        return !Double.isNaN(impedance) && !Double.isInfinite(impedance) &&
               impedance >= 0 && current >= 0 && apparentPower >= 0 &&
               powerFactor >= -1 && powerFactor <= 1;
    }
    
    @Override
    public String toString() {
        return String.format(
            "SimulationResult[Z=%.3fΩ, I=%.3fA, φ=%.3frad, P=%.3fW, Q=%.3fVAR, S=%.3fVA, pf=%.3f]",
            impedance, current, phaseAngle, activePower, reactivePower, apparentPower, powerFactor
        );
    }
}