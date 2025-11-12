package com.simulador.engine.dc;

import com.simulador.model.dc.*;
import java.util.*;

/**
 * Estrategia para análisis de mallas en circuitos DC
 * MODIFICADO: Obtiene el voltaje de las ramas.
 * ADVERTENCIA: La lógica interna sigue siendo un prototipo simplificado.
 */
public class DCMeshAnalysisStrategy implements DCAnalysisStrategy {
    
    @Override
    public DCSimulationResult analyze(DCCircuit circuit) {
        if (!validateCircuit(circuit)) {
            throw new IllegalArgumentException("Circuito no válido para análisis de mallas");
        }
        
        double[][] equationSystem = buildMeshEquationSystem(circuit);
        double[] meshCurrents = solveLinearSystem(equationSystem);
        double[] branchCurrents = calculateBranchCurrents(circuit, meshCurrents);
        double[] componentVoltages = calculateComponentVoltages(circuit, branchCurrents);
        
        double totalResistance = calculateTotalResistance(circuit, branchCurrents);
        double totalCurrent = calculateTotalCurrent(branchCurrents);
        
        // --- MODIFICACIÓN ---
        double sourceVoltage = circuit.getTotalSourceVoltage();
        // --- FIN MODIFICACIÓN ---
        double totalPower = sourceVoltage * totalCurrent;
        
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
        return "Análisis de Mallas";
    }
    
    @Override
    public boolean validateCircuit(DCCircuit circuit) {
        return circuit != null && 
               circuit.getBranches().size() >= 2; // Mínimo 2 mallas para análisis interesante
    }
    
    private double[][] buildMeshEquationSystem(DCCircuit circuit) {
        int meshCount = Math.min(circuit.getBranches().size(), 3); // Máximo 3 mallas para simplificar
        double[][] system = new double[meshCount][meshCount + 1];
        
        for (int i = 0; i < meshCount; i++) {
            double selfResistance = calculateSelfResistance(circuit, i);
            system[i][i] = selfResistance;
            
            for (int j = 0; j < meshCount; j++) {
                if (i != j) {
                    double mutualResistance = calculateMutualResistance(circuit, i, j);
                    system[i][j] = -mutualResistance;
                }
            }
            
            // --- MODIFICACIÓN ---
            // Obtener el voltaje de la rama asociada con esta malla
            system[i][meshCount] = circuit.getBranches().get(i).getTotalVoltage();
            // --- FIN MODIFICACIÓN ---
        }
        
        return system;
    }
    
    // ... (El resto de los métodos de esta clase son prototipos y no
    // ... dependen de sourceVoltage, por lo que se dejan como están)
    
    private double calculateSelfResistance(DCCircuit circuit, int meshIndex) {
        double resistance = 0;
        if (meshIndex < circuit.getBranches().size()) {
            DCBranch branch = circuit.getBranches().get(meshIndex);
            resistance += branch.getTotalResistance();
        }
        if (meshIndex > 0) {
            resistance += getSharedResistance(circuit, meshIndex, meshIndex - 1);
        }
        if (meshIndex < circuit.getBranches().size() - 1) {
            resistance += getSharedResistance(circuit, meshIndex, meshIndex + 1);
        }
        return (resistance > 0) ? resistance : 1.0;
    }
    
    private double calculateMutualResistance(DCCircuit circuit, int mesh1, int mesh2) {
        if (Math.abs(mesh1 - mesh2) == 1) {
            return getSharedResistance(circuit, mesh1, mesh2);
        }
        return 0;
    }
    
    private double getSharedResistance(DCCircuit circuit, int mesh1, int mesh2) {
        return 10.0; // Valor fijo para demostración
    }
    
    private double[] solveLinearSystem(double[][] system) {
        int n = system.length;
        double[] solutions = new double[n];
        
        for (int i = 0; i < n; i++) {
            double pivot = system[i][i];
            if (pivot != 0) {
                for (int j = i; j <= n; j++) {
                    system[i][j] /= pivot;
                }
            }
            for (int k = i + 1; k < n; k++) {
                double factor = system[k][i];
                for (int j = i; j <= n; j++) {
                    system[k][j] -= factor * system[i][j];
                }
            }
        }
        for (int i = n - 1; i >= 0; i--) {
            solutions[i] = system[i][n];
            for (int j = i + 1; j < n; j++) {
                solutions[i] -= system[i][j] * solutions[j];
            }
        }
        return solutions;
    }
    
    private double[] calculateBranchCurrents(DCCircuit circuit, double[] meshCurrents) {
        double[] branchCurrents = new double[circuit.getBranches().size()];
        for (int i = 0; i < branchCurrents.length; i++) {
            if (i < meshCurrents.length) {
                branchCurrents[i] = meshCurrents[i];
            } else {
                branchCurrents[i] = meshCurrents.length > 0 ? meshCurrents[0] : 0;
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
                    voltages.add(comp.getValue() * branchCurrent);
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
    
    private double calculateTotalResistance(DCCircuit circuit, double[] branchCurrents) {
         double totalCurrent = Arrays.stream(branchCurrents).sum();
         if (totalCurrent == 0) return 0;
        return circuit.getTotalSourceVoltage() / totalCurrent;
    }
    
    private double calculateTotalCurrent(double[] branchCurrents) {
        return Arrays.stream(branchCurrents).sum();
    }
}