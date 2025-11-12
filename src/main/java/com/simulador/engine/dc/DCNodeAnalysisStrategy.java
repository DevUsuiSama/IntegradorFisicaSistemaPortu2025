package com.simulador.engine.dc;

import com.simulador.model.dc.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Estrategia para análisis nodal en circuitos DC
 * * ACTUALIZADA: Esta implementación ahora resuelve circuitos paralelos
 * de N ramas (como el problema P28.23) encontrando el voltaje nodal 'Va'
 * y luego calculando las corrientes de cada rama.
 */
public class DCNodeAnalysisStrategy implements DCAnalysisStrategy {
    
    @Override
    public DCSimulationResult analyze(DCCircuit circuit) {
        if (!validateCircuit(circuit)) {
            throw new IllegalArgumentException("Circuito no válido para análisis nodal simple. Se requieren ramas con resistencia.");
        }
        
        // --- Implementación de Análisis Nodal para 1 Nodo (Problema P28.23) ---
        // Asume que todas las ramas están en paralelo entre un nodo 'Va' (superior)
        // y un nodo de referencia (tierra, 0V, inferior).
        
        // Ecuación KCL en 'Va': Σ( (Va - V_fuente_rama) / R_rama ) = 0
        // Reordenando: Va * Σ(1/R_rama) = Σ(V_fuente_rama / R_rama)
        // O: Va * G_total = I_total_fuentes_equivalentes
        
        double totalConductance = 0.0; // G_total (Σ(1/R))
        double totalCurrentSource = 0.0; // I_total (Σ(V/R))

        for (DCBranch branch : circuit.getBranches()) {
            double R_branch = branch.getTotalResistance();
            double V_branch = branch.getTotalVoltage(); // Voltaje de la fuente en la rama

            // Solo consideramos ramas que tienen resistencia.
            if (R_branch > 1e-9) { // Evitar división por cero
                double G_branch = 1.0 / R_branch;
                totalConductance += G_branch;
                
                // V_branch / R_branch es el término de la fuente de corriente equivalente
                totalCurrentSource += (V_branch / R_branch);
            } else if (V_branch != 0) {
                // Si una rama tiene una fuente de voltaje ideal (sin R),
                // el voltaje nodal 'Va' está fijado por esa fuente.
                // Esta es una implementación más simple y asume que no ocurre.
                throw new IllegalArgumentException("Análisis nodal simple no soporta fuentes de voltaje ideales en paralelo.");
            }
            // Si R=0 y V=0 (cortocircuito), Va sería 0.
        }

        // 1. Resolver para el Voltaje Nodal (Va)
        if (totalConductance < 1e-9) {
            throw new ArithmeticException("Circuito abierto o sin conductancia. No se puede resolver.");
        }
        double Va = totalCurrentSource / totalConductance;

        // 2. Calcular corrientes de rama (I_rama) usando el Va encontrado
        double[] branchCurrents = new double[circuit.getBranches().size()];
        for (int i = 0; i < circuit.getBranches().size(); i++) {
            DCBranch branch = circuit.getBranches().get(i);
            double R_branch = branch.getTotalResistance();
            double V_branch = branch.getTotalVoltage();

            if (R_branch > 1e-9) {
                // I_rama = (Va - V_rama) / R_rama
                // Esta es la corriente que fluye *desde* Va *hacia* la rama.
                // Positivo = fluye "hacia abajo" (hacia tierra).
                // Negativo = fluye "hacia arriba" (hacia Va).
                branchCurrents[i] = (Va - V_branch) / R_branch;
            } else {
                branchCurrents[i] = 0.0; // Rama sin resistencia (o supernodo, no manejado aquí)
            }
        }

        // 3. Calcular voltajes de componentes (caída de tensión en R)
        double[] componentVoltages = calculateComponentVoltages(circuit, branchCurrents, Va);

        // 4. Calcular totales para el 'DCSimulationResult'
        
        // Resistencia total equivalente (combinación paralela de todas las R)
        double totalResistance = 1.0 / totalConductance; 
        
        // Corriente total = Corriente total de las fuentes equivalentes
        double totalCurrent = totalCurrentSource; 
        
        // Potencia total disipada por las resistencias = Va * I_total_fuentes
        // También P = Va^2 / R_equivalente
        double totalPower = Va * totalCurrentSource; 

        return new DCSimulationResult(
            Va, // Usamos Va (voltaje nodal) como el "SourceVoltage" del resultado
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
        // Esta estrategia ahora es válida si hay al menos una rama
        // con resistencia para evitar la división por cero.
        return circuit != null && 
               circuit.getBranches().size() >= 1 &&
               circuit.getBranches().stream().anyMatch(b -> b.getTotalResistance() > 1e-9);
    }
    
    /**
     * Calcula los voltajes de los componentes individuales.
     * Para resistores, calcula la caída de tensión (V = I * R).
     * Para fuentes, simplemente reporta su valor nominal.
     */
    private double[] calculateComponentVoltages(DCCircuit circuit, double[] branchCurrents, double Va) {
        List<Double> voltages = new ArrayList<>();
        int branchIndex = 0;
        
        for (DCBranch branch : circuit.getBranches()) {
            double branchCurrent = branchCurrents[branchIndex];
            
            for (DCComponent comp : branch.getComponents()) {
                if (comp.getType() == DCComponentType.RESISTOR) {
                    // La caída de tensión en el resistor es V = I * R
                    // El 'branchCurrent' es la corriente que fluye a través de toda la rama.
                    double voltageDrop = branchCurrent * comp.getValue();
                    voltages.add(voltageDrop);
                } else if (comp.getType() == DCComponentType.BATTERY || 
                           comp.getType() == DCComponentType.DC_SOURCE) {
                    // Simplemente reportamos el valor de la fuente
                    voltages.add(comp.getValue());
                } else {
                    voltages.add(0.0); // Amperímetros, etc.
                }
            }
            branchIndex++;
        }
        
        return voltages.stream().mapToDouble(Double::doubleValue).toArray();
    }
}