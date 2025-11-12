package com.simulador.engine.dc;

import com.simulador.model.dc.*;
import java.util.*;

/**
 * Estrategia para análisis usando Leyes de Kirchhoff
 */
public class DCKirchhoffStrategy implements DCAnalysisStrategy {
    
    @Override
    public DCSimulationResult analyze(DCCircuit circuit) {
        if (!validateCircuit(circuit)) {
            throw new IllegalArgumentException("Circuito no válido para análisis con Leyes de Kirchhoff");
        }
        
        // Implementación simplificada de análisis por mallas
        double[][] system = buildEquationSystem(circuit);
        double[] solutions = solveLinearSystem(system);
        
        double totalResistance = calculateEquivalentResistance(circuit, solutions);
        double sourceVoltage = circuit.getSourceVoltage();
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
               circuit.getSourceVoltage() > 0 &&
               circuit.getBranches().size() >= 1;
    }
    
    private double[][] buildEquationSystem(DCCircuit circuit) {
        // Implementación simplificada para circuito con 2 mallas
        int meshCount = Math.min(circuit.getBranches().size(), 2);
        double[][] system = new double[meshCount][meshCount + 1];
        
        // Para demostración, creamos un sistema simple
        if (meshCount == 1) {
            double totalR = circuit.getBranches().get(0).getComponents().stream()
                .filter(comp -> comp.getType() == DCComponentType.RESISTOR)
                .mapToDouble(DCComponent::getValue)
                .sum();
            system[0][0] = totalR;
            system[0][1] = circuit.getSourceVoltage();
        } else {
            // Sistema para 2 mallas
            double r1 = getBranchResistance(circuit.getBranches().get(0));
            double r2 = getBranchResistance(circuit.getBranches().get(1));
            system[0][0] = r1 + r2;  // R1 + R2 para I1
            system[0][1] = -r2;      // -R2 para I2
            system[0][2] = circuit.getSourceVoltage();
            
            system[1][0] = -r2;      // -R2 para I1
            system[1][1] = r2;       // R2 para I2
            system[1][2] = 0;        // Sin fuente en segunda malla
        }
        
        return system;
    }
    
    private double[] solveLinearSystem(double[][] system) {
        // Implementación simplificada usando eliminación gaussiana
        int n = system.length;
        double[] solutions = new double[n];
        
        // Para demostración, retornamos soluciones aproximadas
        for (int i = 0; i < n; i++) {
            solutions[i] = system[i][n] / system[i][i];
        }
        
        return solutions;
    }
    
    private double getBranchResistance(DCBranch branch) {
        return branch.getComponents().stream()
            .filter(comp -> comp.getType() == DCComponentType.RESISTOR)
            .mapToDouble(DCComponent::getValue)
            .sum();
    }
    
    private double calculateEquivalentResistance(DCCircuit circuit, double[] solutions) {
        return circuit.getSourceVoltage() / solutions[0];
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