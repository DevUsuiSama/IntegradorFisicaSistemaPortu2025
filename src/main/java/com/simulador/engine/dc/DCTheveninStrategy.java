package com.simulador.engine.dc;

import com.simulador.model.dc.*;
import java.util.*;

/**
 * Estrategia para análisis usando Teorema de Thevenin
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
        double totalCurrent = vth / (rth + getLoadResistance(circuit));
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
               circuit.getSourceVoltage() > 0 &&
               circuit.getBranches().size() >= 1;
    }
    
    private double calculateTheveninVoltage(DCCircuit circuit) {
        // Vth = Voltaje en circuito abierto
        // Simplificación: voltaje en la primera rama
        if (!circuit.getBranches().isEmpty()) {
            DCBranch firstBranch = circuit.getBranches().get(0);
            double branchResistance = firstBranch.getTotalResistance();
            double totalResistance = circuit.getTotalResistance();
            
            if (totalResistance > 0) {
                return circuit.getSourceVoltage() * (branchResistance / totalResistance);
            }
        }
        
        return circuit.getSourceVoltage();
    }
    
    private double calculateTheveninResistance(DCCircuit circuit) {
        // Rth = Resistencia equivalente con fuentes anuladas
        // Anular fuentes = cortocircuitar fuentes de voltaje
        
        if (circuit.getConfiguration().contains("Serie")) {
            // En serie: Rth = suma de resistencias (excepto rama de carga)
            return circuit.getBranches().stream()
                .limit(Math.max(1, circuit.getBranches().size() - 1))
                .flatMap(branch -> branch.getComponents().stream())
                .filter(comp -> comp.getType() == DCComponentType.RESISTOR)
                .mapToDouble(DCComponent::getValue)
                .sum();
        } else {
            // En paralelo: Rth = resistencia equivalente de todas las ramas en paralelo
            return 1.0 / circuit.getBranches().stream()
                .flatMap(branch -> branch.getComponents().stream())
                .filter(comp -> comp.getType() == DCComponentType.RESISTOR)
                .mapToDouble(comp -> 1.0 / comp.getValue())
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
            // En serie, misma corriente en todas las ramas
            Arrays.fill(branchCurrents, totalCurrent);
        } else {
            // En paralelo, corriente se divide según resistencia
            for (int i = 0; i < branchCurrents.length; i++) {
                DCBranch branch = circuit.getBranches().get(i);
                double branchResistance = branch.getTotalResistance();
                if (branchResistance > 0) {
                    branchCurrents[i] = circuit.getSourceVoltage() / branchResistance;
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
    
    // Método adicional para obtener el circuito equivalente de Thevenin
    public DCCircuit getTheveninEquivalent(DCCircuit originalCircuit) {
        double vth = calculateTheveninVoltage(originalCircuit);
        double rth = calculateTheveninResistance(originalCircuit);
        
        DCCircuit equivalent = new DCCircuit(vth, "Serie", 1);
        DCBranch branch = new DCBranch(1);
        branch.addComponent(new DCComponent(DCComponentType.DC_SOURCE, vth, "Vth", 1));
        branch.addComponent(new DCComponent(DCComponentType.RESISTOR, rth, "Rth", 1));
        equivalent.addBranch(branch);
        
        return equivalent;
    }
}