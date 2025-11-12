package com.simulador.model.dc;

import java.util.Arrays;

/**
 * Resultados de una simulación de circuito DC (Principio de Responsabilidad Única)
 * MODIFICADO: Se reemplazó sourceVoltage (entrada) por calculatedVoltage (salida).
 */
public class DCSimulationResult {
    private final double calculatedVoltage;
    private final double totalResistance;
    private final double totalCurrent;
    private final double totalPower;
    private final double[] branchCurrents;
    private final double[] componentVoltages;
    private final String methodUsed;
    private final String circuitConfiguration;
    private final long timestamp;
    
    public DCSimulationResult(double calculatedVoltage, double totalResistance, 
                             double totalCurrent, double totalPower,
                             double[] branchCurrents, double[] componentVoltages,
                             String methodUsed, String circuitConfiguration) {
        this.calculatedVoltage = calculatedVoltage;
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
    public double getCalculatedVoltage() { return calculatedVoltage; }
    public double getTotalResistance() { return totalResistance; }
    public double getTotalCurrent() { return totalCurrent; }
    public double getTotalPower() { return totalPower; }
    public double[] getBranchCurrents() { return Arrays.copyOf(branchCurrents, branchCurrents.length); }
    public double[] getComponentVoltages() { return Arrays.copyOf(componentVoltages, componentVoltages.length); }
    public String getMethodUsed() { return methodUsed; }
    public String getCircuitConfiguration() { return circuitConfiguration; }
    public long getTimestamp() { return timestamp; }
    
    public double getPowerDissipated() {
        // En un circuito resistivo, la potencia total suministrada
        // por las fuentes es igual a la potencia total disipada.
        // P_total = P_disipada
        if (totalPower > 0) {
            return totalPower;
        }
        // Fallback si la potencia total es 0 o negativa
        return totalCurrent * totalCurrent * totalResistance;
    }
    
    public double getEfficiency() {
        // Asumiendo un circuito puramente resistivo, la eficiencia
        // de la "transmisión" (fuentes a resistencias) es 100%.
        if (totalPower <= 0) {
            return 0;
        }
        return (getPowerDissipated() / totalPower) * 100;
    }
    
    public boolean isCircuitValid() {
        return totalResistance > 0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Resultado DC [V_calc=%.2fV, R_eq=%.2fΩ, I_eq=%.3fA, P_total=%.3fW, Método=%s]",
            calculatedVoltage, totalResistance, totalCurrent, totalPower, methodUsed
        );
    }
}