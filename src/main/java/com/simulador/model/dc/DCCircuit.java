package com.simulador.model.dc;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un circuito DC completo (Principio de Responsabilidad Única)
 * MODIFICADO: Se eliminó sourceVoltage y batteryCount. El circuito
 * es ahora un contenedor de ramas cuya topología se define por 'configuration'.
 */
public class DCCircuit {
    private final String id;
    private final List<DCBranch> branches;
    private String configuration;
    
    public DCCircuit(String configuration) {
        this.id = "DCCircuit_" + System.currentTimeMillis();
        this.branches = new ArrayList<>();
        this.configuration = configuration;
    }
    
    public DCCircuit() {
        this("Serie"); // Configuración por defecto
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
    public String getConfiguration() { return configuration; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }
    
    /**
     * Calcula la resistencia total basado en la configuración.
     * ADVERTENCIA: Esta es una simplificación y solo funciona para
     * circuitos serie/paralelo puros.
     */
    public double getTotalResistance() {
        if (branches.isEmpty()) {
            return 0;
        }
        
        if (configuration.contains("Serie")) {
            // Serie: R_total = suma de todas las resistencias en todas las ramas
            return branches.stream()
                .mapToDouble(DCBranch::getTotalResistance)
                .sum();
        } else {
            // Paralelo: 1/R_total = suma de 1/R de cada rama
            double totalInverseResistance = branches.stream()
                .mapToDouble(branch -> 1.0 / branch.getTotalResistance())
                .sum();
            return 1.0 / totalInverseResistance;
        }
    }
    
    /**
     * Calcula el voltaje total de las fuentes (solo para serie simple).
     * ADVERTENCIA: Esta es una simplificación.
     */
    public double getTotalSourceVoltage() {
        return branches.stream()
            .mapToDouble(DCBranch::getTotalVoltage)
            .sum();
    }
    
    public boolean isValid() {
        // Un circuito es válido si tiene al menos una rama con componentes.
        return !branches.isEmpty() &&
               branches.stream().anyMatch(DCBranch::hasComponents);
    }
    
    @Override
    public String toString() {
        return String.format("Circuito DC: %s, %d ramas", 
            configuration, branches.size());
    }
}