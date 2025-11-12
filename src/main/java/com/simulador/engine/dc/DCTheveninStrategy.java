package com.simulador.engine.dc;

import com.simulador.model.dc.*;
import java.util.*;

/**
 * Estrategia para análisis usando Teorema de Thevenin
 * MODIFICADO: Obtiene el voltaje de las ramas.
 * ADVERTENCIA: La lógica interna sigue siendo un prototipo simplificado.
 */
public class DCTheveninStrategy implements DCAnalysisStrategy {
    
    @Override
    public DCSimulationResult analyze(DCCircuit circuit) {
        if (!validateCircuit(circuit)) {
            throw new IllegalArgumentException("Circuito no válido para Teorema de Thevenin");
        }
        
        // Calcular equivalente de Thevenin
        double vth = calculateTheveninVoltage(circuit);
        double rth = calculateTheveninResistance(circuit);
        
        // Usar el equivalente para calcular corrientes
        double totalCurrent = 0;
        if (rth + getLoadResistance(circuit) > 1e-9) {
             totalCurrent = vth / (rth + getLoadResistance(circuit));
        }
        double totalPower = vth * totalCurrent;
        
        double[] branchCurrents = calculateBranchCurrentsFromThevenin(circuit, totalCurrent);
        double[] componentVoltages = calculateComponentVoltages(circuit, branchCurrents);
        
        return new DCSimulationResult(
            vth, // Usar Vth como voltaje equivalente
            rth, // Usar Rth como resistencia equivalente
            totalCurrent,
            totalPower,
            branchCurrents,
            componentVoltages,
            getMethodName(),
            circuit.getConfiguration()
        );
    }
    
    @Override
    public String getMethodName() {
        return "Teorema de Thevenin";
    }
    
    @Override
    public boolean validateCircuit(DCCircuit circuit) {
        return circuit != null && 
               circuit.getTotalSourceVoltage() > 0 &&
               circuit.getBranches().size() >= 1;
    }
    
    private double calculateTheveninVoltage(DCCircuit circuit) {
        // Vth = Voltaje en circuito abierto
        // Simplificación: voltaje en la primera rama
        // --- MODIFICACIÓN ---
        double sourceVoltage = circuit.getTotalSourceVoltage();
        // --- FIN MODIFICACIÓN ---

        if (!circuit.getBranches().isEmpty()) {
            DCBranch firstBranch = circuit.getBranches().get(0);
            double branchResistance = firstBranch.getTotalResistance();
            double totalResistance = circuit.getTotalResistance();
            
            if (totalResistance > 0) {
                return sourceVoltage * (branchResistance / totalResistance);
            }
        }
        
        return sourceVoltage;
    }
    
    private double calculateTheveninResistance(DCCircuit circuit) {
        // Rth = Resistencia equivalente con fuentes anuladas
        if (circuit.getConfiguration().contains("Serie")) {
            return circuit.getBranches().stream()
                .limit(Math.max(1, circuit.getBranches().size() - 1))
                .mapToDouble(DCBranch::getTotalResistance)
                .sum();
        } else {
            return 1.0 / circuit.getBranches().stream()
                .mapToDouble(branch -> 1.0 / branch.getTotalResistance())
                .sum();
        }
    }
    
    private double getLoadResistance(DCCircuit circuit) {
        // Resistencia de carga (última rama)
        if (!circuit.getBranches().isEmpty()) {
            DCBranch lastBranch = circuit.getBranches().get(circuit.getBranches().size() - 1);
            return lastBranch.getTotalResistance();
        }
        return 10.0; // Valor por defecto
    }
    
    private double[] calculateBranchCurrentsFromThevenin(DCCircuit circuit, double totalCurrent) {
        double[] branchCurrents = new double[circuit.getBranches().size()];
        
        if (circuit.getConfiguration().contains("Serie")) {
            Arrays.fill(branchCurrents, totalCurrent);
        } else {
            double sourceVoltage = circuit.getTotalSourceVoltage();
            for (int i = 0; i < branchCurrents.length; i++) {
                DCBranch branch = circuit.getBranches().get(i);
                double branchResistance = branch.getTotalResistance();
                if (branchResistance > 0) {
                    branchCurrents[i] = sourceVoltage / branchResistance;
                } else {
                    branchCurrents[i] = 0;
                }
            }
        }
        
        return branchCurrents;
    }
    
    private double[] calculateComponentVoltages(DCCircuit circuit, double[] branchCurrents) {
        List<Double> voltages = new ArrayList<>();
        int branchIndex = 0;
        
        for (DCBranch branch : circuit.getBranches()) {
            double branchCurrent = branchIndex < branchCurrents.length ? 
                branchCurrents[branchIndex] : 0;
                
            for (DCComponent comp : branch.getComponents()) {
                if (comp.getType() == DCComponentType.RESISTOR) {
                    double voltage = comp.getValue() * branchCurrent;
                    voltages.add(voltage);
                } else if (comp.getType() == DCComponentType.BATTERY || 
                          comp.getType() == DCComponentType.DC_SOURCE) {
                    voltages.add(comp.getValue());
                } else {
                    voltages.add(0.0);
                }
            }
            branchIndex++;
        }
        
        return voltages.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    public DCCircuit getTheveninEquivalent(DCCircuit originalCircuit) {
        double vth = calculateTheveninVoltage(originalCircuit);
        double rth = calculateTheveninResistance(originalCircuit);
        
        DCCircuit equivalent = new DCCircuit("Serie");
        DCBranch branch = new DCBranch(1);
        branch.addComponent(new DCComponent(DCComponentType.DC_SOURCE, vth, "Vth", 1));
        branch.addComponent(new DCComponent(DCComponentType.RESISTOR, rth, "Rth", 1));
        equivalent.addBranch(branch);
        
        return equivalent;
    }
}