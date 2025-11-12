package com.simulador.engine.dc;

import com.simulador.model.dc.*;
import java.util.*;

/**
 * Estrategia para análisis usando Leyes de Kirchhoff
 * MODIFICADO: Obtiene el voltaje de las ramas.
 * ADVERTENCIA: La lógica interna sigue siendo un prototipo simplificado.
 */
public class DCKirchhoffStrategy implements DCAnalysisStrategy {
    
    @Override
    public DCSimulationResult analyze(DCCircuit circuit) {
        if (!validateCircuit(circuit)) {
            throw new IllegalArgumentException("Circuito no válido para análisis con Leyes de Kirchhoff");
        }
        
        // --- MODIFICACIÓN ---
        // Obtener el voltaje de la(s) fuente(s) DENTRO de las ramas
        double sourceVoltage = circuit.getTotalSourceVoltage();
        // --- FIN MODIFICACIÓN ---

        // Implementación simplificada de análisis por mallas
        double[][] system = buildEquationSystem(circuit, sourceVoltage);
        double[] solutions = solveLinearSystem(system);
        
        double totalResistance = 0.0;
        if (solutions[0] != 0) {
             totalResistance = sourceVoltage / solutions[0];
        }
        
        double totalCurrent = solutions[0]; // Corriente de la malla principal
        double totalPower = sourceVoltage * totalCurrent;
        
        double[] branchCurrents = extractBranchCurrents(solutions, circuit);
        double[] componentVoltages = calculateComponentVoltages(circuit, branchCurrents);
        
        return new DCSimulationResult(
            sourceVoltage,
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
        return "Leyes de Kirchhoff";
    }
    
    @Override
    public boolean validateCircuit(DCCircuit circuit) {
        return circuit != null && 
               circuit.getTotalSourceVoltage() > 0 &&
               circuit.getBranches().size() >= 1;
    }
    
    private double[][] buildEquationSystem(DCCircuit circuit, double sourceVoltage) {
        // Implementación simplificada para circuito con 2 mallas
        int meshCount = Math.min(circuit.getBranches().size(), 2);
        double[][] system = new double[meshCount][meshCount + 1];
        
        // Para demostración, creamos un sistema simple
        if (meshCount == 1) {
            double totalR = circuit.getBranches().get(0).getTotalResistance();
            system[0][0] = (totalR > 0) ? totalR : 1.0;
            system[0][1] = sourceVoltage;
        } else {
            // Sistema para 2 mallas
            double r1 = getBranchResistance(circuit.getBranches().get(0));
            double r2 = (circuit.getBranches().size() > 1) ? getBranchResistance(circuit.getBranches().get(1)) : 1.0;
            if (r2 == 0) r2 = 1.0;
            
            system[0][0] = r1 + r2;  // R1 + R2 para I1
            system[0][1] = -r2;      // -R2 para I2
            system[0][2] = sourceVoltage;
            
            system[1][0] = -r2;      // -R2 para I1
            system[1][1] = r2;       // R2 para I2
            system[1][2] = 0;        // Sin fuente en segunda malla
        }
        
        return system;
    }
    
    private double[] solveLinearSystem(double[][] system) {
        // Implementación simplificada
        int n = system.length;
        double[] solutions = new double[n];
        
        for (int i = 0; i < n; i++) {
            if(system[i][i] != 0) {
                 solutions[i] = system[i][n] / system[i][i];
            }
        }
        
        return solutions;
    }
    
    private double getBranchResistance(DCBranch branch) {
        return branch.getComponents().stream()
            .filter(comp -> comp.getType() == DCComponentType.RESISTOR)
            .mapToDouble(DCComponent::getValue)
            .sum();
    }
    
    private double[] extractBranchCurrents(double[] solutions, DCCircuit circuit) {
        double[] branchCurrents = new double[circuit.getBranches().size()];
        Arrays.fill(branchCurrents, solutions[0]); // Simplificado
        return branchCurrents;
    }
    
    private double[] calculateComponentVoltages(DCCircuit circuit, double[] branchCurrents) {
        List<Double> voltages = new ArrayList<>();
        int branchIndex = 0;
        
        for (DCBranch branch : circuit.getBranches()) {
            for (DCComponent comp : branch.getComponents()) {
                if (comp.getType() == DCComponentType.RESISTOR) {
                    double voltage = comp.getValue() * branchCurrents[branchIndex];
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
}