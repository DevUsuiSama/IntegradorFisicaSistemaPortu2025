package com.simulador.engine.dc;

import com.simulador.model.dc.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Estrategia para análisis usando Ley de Ohm (Principio de Responsabilidad Única)
 * MODIFICADO: Ahora obtiene el voltaje de las fuentes DENTRO de las ramas,
 * en lugar de un voltaje global.
 */
public class DCOhmLawStrategy implements DCAnalysisStrategy {
    
    @Override
    public DCSimulationResult analyze(DCCircuit circuit) {
        if (!validateCircuit(circuit)) {
            throw new IllegalArgumentException("Circuito no válido para análisis con Ley de Ohm. Requiere 1 fuente y 1+ resistores.");
        }
        
        // --- MODIFICACIÓN ---
        // Obtener el voltaje de la(s) fuente(s) DENTRO de las ramas
        double sourceVoltage = circuit.getTotalSourceVoltage();
        // --- FIN MODIFICACIÓN ---

        double totalResistance = calculateTotalResistance(circuit);
        double totalCurrent = 0;
        if (totalResistance > 1e-9) {
            totalCurrent = sourceVoltage / totalResistance;
        }
        
        double totalPower = sourceVoltage * totalCurrent;
        
        // Calcular corrientes por rama (simplificado para circuitos serie)
        double[] branchCurrents = calculateBranchCurrents(circuit, totalCurrent);
        double[] componentVoltages = calculateComponentVoltages(circuit, totalCurrent);
        
        return new DCSimulationResult(
            sourceVoltage, // El voltaje de la fuente es el "voltaje calculado" en este caso
            totalResistance,
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
        return "Ley de Ohm";
    }
    
    @Override
    public boolean validateCircuit(DCCircuit circuit) {
        // La Ley de Ohm es aplicable a circuitos simples serie/paralelo
        // con una fuente de voltaje clara.
        return circuit != null && 
               circuit.getTotalSourceVoltage() > 0 &&
               circuit.getBranches().stream()
                   .flatMap(branch -> branch.getComponents().stream())
                   .anyMatch(comp -> comp.getType() == DCComponentType.RESISTOR);
    }
    
    private double calculateTotalResistance(DCCircuit circuit) {
        if (circuit.getConfiguration().contains("Serie")) {
            // Resistencia total en serie: R_total = R1 + R2 + ...
            return circuit.getBranches().stream()
                .mapToDouble(DCBranch::getTotalResistance)
                .sum();
        } else {
            // Resistencia total en paralelo: 1/R_total = 1/R1 + 1/R2 + ...
            return 1.0 / circuit.getBranches().stream()
                .mapToDouble(branch -> 1.0 / branch.getTotalResistance())
                .sum();
        }
    }
    
    private double[] calculateBranchCurrents(DCCircuit circuit, double totalCurrent) {
        List<Double> currents = new ArrayList<>();
        double sourceVoltage = circuit.getTotalSourceVoltage();

        if (circuit.getConfiguration().contains("Serie")) {
            // En serie, misma corriente en todas las ramas
            for (int i = 0; i < circuit.getBranches().size(); i++) {
                currents.add(totalCurrent);
            }
        } else {
            // En paralelo, corriente se divide según resistencia
            for (DCBranch branch : circuit.getBranches()) {
                double branchResistance = branch.getTotalResistance();
                double branchCurrent = 0;
                if (branchResistance > 1e-9) {
                     branchCurrent = sourceVoltage / branchResistance;
                }
                currents.add(branchCurrent);
            }
        }
        
        return currents.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    private double[] calculateComponentVoltages(DCCircuit circuit, double totalCurrent) {
        List<Double> voltages = new ArrayList<>();
        
        for (DCBranch branch : circuit.getBranches()) {
            for (DCComponent comp : branch.getComponents()) {
                if (comp.getType() == DCComponentType.RESISTOR) {
                    double voltage = comp.getValue() * totalCurrent;
                    voltages.add(voltage);
                } else if (comp.getType() == DCComponentType.BATTERY || 
                          comp.getType() == DCComponentType.DC_SOURCE) {
                    voltages.add(comp.getValue());
                } else {
                    voltages.add(0.0); // Para amperímetros y voltímetros
                }
            }
        }
        
        return voltages.stream().mapToDouble(Double::doubleValue).toArray();
    }
}