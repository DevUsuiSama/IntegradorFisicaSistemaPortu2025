package com.simulador.model.dc;

import java.util.Arrays;

/**
 * Resultados de una simulación de circuito DC (Principio de Responsabilidad Única)
 */
public class DCSimulationResult {
    private final double sourceVoltage;
    private final double totalResistance;
    private final double totalCurrent;
    private final double totalPower;
    private final double[] branchCurrents;
    private final double[] componentVoltages;
    private final String methodUsed;
    private final String circuitConfiguration;
    private final long timestamp;
    
    public DCSimulationResult(double sourceVoltage, double totalResistance, 
                             double totalCurrent, double totalPower,
                             double[] branchCurrents, double[] componentVoltages,
                             String methodUsed, String circuitConfiguration) {
        this.sourceVoltage = sourceVoltage;
        this.totalResistance = totalResistance;
        this.totalCurrent = totalCurrent;
        this.totalPower = totalPower;
        this.branchCurrents = branchCurrents != null ? Arrays.copyOf(branchCurrents, branchCurrents.length) : new double[0];
        this.componentVoltages = componentVoltages != null ? Arrays.copyOf(componentVoltages, componentVoltages.length) : new double[0];
        this.methodUsed = methodUsed;
        this.circuitConfiguration = circuitConfiguration;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters
    public double getSourceVoltage() { return sourceVoltage; }
    public double getTotalResistance() { return totalResistance; }
    public double getTotalCurrent() { return totalCurrent; }
    public double getTotalPower() { return totalPower; }
    public double[] getBranchCurrents() { return Arrays.copyOf(branchCurrents, branchCurrents.length); }
    public double[] getComponentVoltages() { return Arrays.copyOf(componentVoltages, componentVoltages.length); }
    public String getMethodUsed() { return methodUsed; }
    public String getCircuitConfiguration() { return circuitConfiguration; }
    public long getTimestamp() { return timestamp; }
    
    public double getPowerDissipated() {
        return totalCurrent * totalCurrent * totalResistance;
    }
    
    public double getEfficiency() {
        return (getPowerDissipated() / totalPower) * 100;
    }
    
    public boolean isCircuitValid() {
        return totalResistance > 0 && totalCurrent >= 0 && totalPower >= 0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Resultado DC [V=%.2fV, R=%.2fΩ, I=%.3fA, P=%.3fW, Método=%s]",
            sourceVoltage, totalResistance, totalCurrent, totalPower, methodUsed
        );
    }
}