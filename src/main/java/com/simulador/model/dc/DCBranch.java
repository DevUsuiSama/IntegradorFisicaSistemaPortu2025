package com.simulador.model.dc;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una rama en un circuito DC
 */
public class DCBranch {
    private final String id;
    private final List<DCComponent> components;
    private final int branchNumber;
    
    public DCBranch(int branchNumber) {
        this.id = "Branch_" + branchNumber;
        this.components = new ArrayList<>();
        this.branchNumber = branchNumber;
    }
    
    public void addComponent(DCComponent component) {
        components.add(component);
    }
    
    public void removeComponent(DCComponent component) {
        components.remove(component);
    }
    
    public void removeComponent(int index) {
        if (index >= 0 && index < components.size()) {
            components.remove(index);
        }
    }
    
    // Getters
    public String getId() { return id; }
    public List<DCComponent> getComponents() { return new ArrayList<>(components); }
    public int getBranchNumber() { return branchNumber; }
    public int getComponentCount() { return components.size(); }
    
    public double getTotalResistance() {
        return components.stream()
            .mapToDouble(DCComponent::getResistance)
            .sum();
    }
    
    public double getTotalVoltage() {
        return components.stream()
            .mapToDouble(DCComponent::getVoltage)
            .sum();
    }
    
    public boolean hasComponents() {
        return !components.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("Rama %d (%d componentes)", branchNumber, components.size());
    }
}