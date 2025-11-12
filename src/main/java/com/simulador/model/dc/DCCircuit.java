package com.simulador.model.dc;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un circuito DC completo (Principio de Responsabilidad Única)
 */
public class DCCircuit {
    private final String id;
    private final List<DCBranch> branches;
    private double sourceVoltage;
    private String configuration;
    private int batteryCount;
    
    public DCCircuit(double sourceVoltage, String configuration, int batteryCount) {
        this.id = "DCCircuit_" + System.currentTimeMillis();
        this.branches = new ArrayList<>();
        this.sourceVoltage = sourceVoltage;
        this.configuration = configuration;
        this.batteryCount = batteryCount;
    }
    
    public DCCircuit() {
        this(12.0, "Serie", 1);
    }
    
    public void addBranch(DCBranch branch) {
        branches.add(branch);
    }
    
    public void removeBranch(DCBranch branch) {
        branches.remove(branch);
    }
    
    public void removeBranch(int index) {
        if (index >= 0 && index < branches.size()) {
            branches.remove(index);
        }
    }
    
    public DCBranch getBranch(int index) {
        if (index >= 0 && index < branches.size()) {
            return branches.get(index);
        }
        return null;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public List<DCBranch> getBranches() { return new ArrayList<>(branches); }
    public int getBranchCount() { return branches.size(); }
    public double getSourceVoltage() { return sourceVoltage; }
    public void setSourceVoltage(double sourceVoltage) { this.sourceVoltage = sourceVoltage; }
    public String getConfiguration() { return configuration; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }
    public int getBatteryCount() { return batteryCount; }
    public void setBatteryCount(int batteryCount) { this.batteryCount = batteryCount; }
    
    public double getTotalResistance() {
        if (configuration.contains("Serie")) {
            // Serie: R_total = suma de todas las resistencias
            return branches.stream()
                .flatMap(branch -> branch.getComponents().stream())
                .mapToDouble(DCComponent::getResistance)
                .sum();
        } else {
            // Paralelo: 1/R_total = suma de 1/R de cada rama
            return 1.0 / branches.stream()
                .mapToDouble(branch -> 1.0 / branch.getTotalResistance())
                .sum();
        }
    }
    
    public boolean isValid() {
        return sourceVoltage > 0 && 
               !branches.isEmpty() &&
               branches.stream().anyMatch(DCBranch::hasComponents);
    }
    
    @Override
    public String toString() {
        return String.format("Circuito DC: %dV, %s, %d ramas", 
            sourceVoltage, configuration, branches.size());
    }
}