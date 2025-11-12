package com.simulador.engine.dc;

import com.simulador.model.dc.*;
import java.util.*;

/**
 * Estrategia para análisis nodal en circuitos DC
 */
public class DCNodeAnalysisStrategy implements DCAnalysisStrategy {
    
    @Override
    public DCSimulationResult analyze(DCCircuit circuit) {
        if (!validateCircuit(circuit)) {
            throw new IllegalArgumentException("Circuito no válido para análisis nodal");
        }
        
        double[][] equationSystem = buildNodeEquationSystem(circuit);
        double[] nodeVoltages = solveLinearSystem(equationSystem);
        double[] branchCurrents = calculateBranchCurrents(circuit, nodeVoltages);
        double[] componentVoltages = calculateComponentVoltages(circuit, branchCurrents, nodeVoltages);
        
        double totalResistance = calculateTotalResistance(circuit, branchCurrents);
        double totalCurrent = calculateTotalCurrent(branchCurrents);
        double totalPower = circuit.getSourceVoltage() * totalCurrent;
        
        return new DCSimulationResult(
            circuit.getSourceVoltage(),
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
        return "Análisis Nodal";
    }
    
    @Override
    public boolean validateCircuit(DCCircuit circuit) {
        return circuit != null && 
               circuit.getSourceVoltage() > 0 &&
               circuit.getBranches().size() >= 2; // Mínimo 2 nodos para análisis interesante
    }
    
    private double[][] buildNodeEquationSystem(DCCircuit circuit) {
        int nodeCount = Math.min(circuit.getBranches().size() + 1, 4); // n ramas -> n+1 nodos
        double[][] system = new double[nodeCount - 1][nodeCount]; // Ignoramos el nodo de referencia
        
        // Para cada nodo (excepto referencia)
        for (int i = 0; i < nodeCount - 1; i++) {
            double selfConductance = calculateSelfConductance(circuit, i);
            system[i][i] = selfConductance;
            
            // Conductancias mutuas
            for (int j = 0; j < nodeCount - 1; j++) {
                if (i != j) {
                    double mutualConductance = calculateMutualConductance(circuit, i, j);
                    system[i][j] = -mutualConductance;
                }
            }
            
            // Fuentes de corriente equivalentes
            system[i][nodeCount - 1] = calculateNodeCurrentSource(circuit, i);
        }
        
        return system;
    }
    
    private double calculateSelfConductance(DCCircuit circuit, int nodeIndex) {
        double conductance = 0;
        
        // Sumar conductancias de todas las ramas conectadas al nodo
        for (DCBranch branch : circuit.getBranches()) {
            if (isBranchConnectedToNode(branch, nodeIndex)) {
                conductance += 1.0 / branch.getTotalResistance();
            }
        }
        
        return conductance;
    }
    
    private double calculateMutualConductance(DCCircuit circuit, int node1, int node2) {
        // Conductancia entre nodos adyacentes
        if (Math.abs(node1 - node2) == 1) {
            // Buscar rama que conecte estos nodos
            for (DCBranch branch : circuit.getBranches()) {
                if (isBranchConnectingNodes(branch, node1, node2)) {
                    return 1.0 / branch.getTotalResistance();
                }
            }
        }
        return 0;
    }
    
    private double calculateNodeCurrentSource(DCCircuit circuit, int nodeIndex) {
        // Fuentes de voltaje convertidas a fuentes de corriente equivalentes
        if (nodeIndex == 0) {
            return circuit.getSourceVoltage() / getReferenceResistance(circuit);
        }
        return 0;
    }
    
    private boolean isBranchConnectedToNode(DCBranch branch, int nodeIndex) {
        // Simplificación: cada rama está conectada a dos nodos consecutivos
        return branch.getBranchNumber() == nodeIndex || 
               branch.getBranchNumber() == nodeIndex + 1;
    }
    
    private boolean isBranchConnectingNodes(DCBranch branch, int node1, int node2) {
        return (branch.getBranchNumber() == node1 && branch.getBranchNumber() + 1 == node2) ||
               (branch.getBranchNumber() == node2 && branch.getBranchNumber() + 1 == node1);
    }
    
    private double getReferenceResistance(DCCircuit circuit) {
        // Resistencia de referencia para conversión de fuente
        return circuit.getBranches().stream()
            .mapToDouble(DCBranch::getTotalResistance)
            .findFirst()
            .orElse(10.0);
    }
    
    private double[] solveLinearSystem(double[][] system) {
        int n = system.length;
        int m = system[0].length;
        double[] solutions = new double[n];
        
        // Resolución simplificada
        for (int i = 0; i < n; i++) {
            if (system[i][i] != 0) {
                solutions[i] = system[i][m - 1] / system[i][i];
            }
        }
        
        return solutions;
    }
    
    private double[] calculateBranchCurrents(DCCircuit circuit, double[] nodeVoltages) {
        double[] branchCurrents = new double[circuit.getBranches().size()];
        
        for (int i = 0; i < branchCurrents.length; i++) {
            if (i < nodeVoltages.length) {
                // I = (V1 - V2) / R
                double v1 = i > 0 ? nodeVoltages[i - 1] : circuit.getSourceVoltage();
                double v2 = i < nodeVoltages.length ? nodeVoltages[i] : 0;
                double resistance = circuit.getBranches().get(i).getTotalResistance();
                
                branchCurrents[i] = (v1 - v2) / resistance;
            } else {
                branchCurrents[i] = 0;
            }
        }
        
        return branchCurrents;
    }
    
    private double[] calculateComponentVoltages(DCCircuit circuit, double[] branchCurrents, double[] nodeVoltages) {
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
        
        // Agregar voltajes de nodo
        for (double nodeVoltage : nodeVoltages) {
            voltages.add(nodeVoltage);
        }
        
        return voltages.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    private double calculateTotalResistance(DCCircuit circuit, double[] branchCurrents) {
        return circuit.getSourceVoltage() / Arrays.stream(branchCurrents).sum();
    }
    
    private double calculateTotalCurrent(double[] branchCurrents) {
        return Arrays.stream(branchCurrents).sum();
    }
}