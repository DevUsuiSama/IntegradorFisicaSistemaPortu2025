package com.simulador.engine;

import com.simulador.model.CircuitComponent;
import com.simulador.model.SimulationResult;
import java.util.List;

/**
 * Estrategia de cálculo analítico para circuitos RLC
 * Analytical calculation strategy for RLC circuits
 */
public class AnalyticalStrategy implements SimulationStrategy {
    
    @Override
    public String getName() { 
        return "Analytical"; 
    }
    
    @Override
    public String getDescription() {
        return "Solución analítica exacta para régimen permanente sinusoidal";
    }
    
    @Override
    public SimulationResult calculate(List<CircuitComponent> components, 
                                     double voltage, double frequency) {
        // Validaciones
        if (components == null || components.isEmpty()) {
            throw new IllegalArgumentException("Component list cannot be null or empty");
        }
        if (voltage <= 0) {
            throw new IllegalArgumentException("Voltage must be positive");
        }
        if (frequency <= 0) {
            throw new IllegalArgumentException("Frequency must be positive");
        }
        
        try {
            // Calcular valores totales de componentes
            double totalR = 0, totalL = 0, totalC = 0;
            
            for (CircuitComponent comp : components) {
                totalR += comp.getResistance();
                totalL += comp.getInductance();
                totalC += comp.getCapacitance();
            }
            
            // Evitar división por cero
            if (totalC <= 0) totalC = 1e-12;
            
            // Cálculos de impedancia
            double w = 2 * Math.PI * frequency;
            double XL = w * totalL;
            double XC = 1.0 / (w * totalC);
            double X = XL - XC;
            double Z = Math.sqrt(totalR * totalR + X * X);
            
            // Validar impedancia
            if (Z <= 0) {
                throw new ArithmeticException("Impedance must be positive");
            }
            
            // Cálculos de corriente y potencia
            double I = voltage / Z;
            double phi = Math.atan2(X, totalR);
            
            double P = voltage * I * Math.cos(phi);
            double Q = voltage * I * Math.sin(phi);
            double S = voltage * I;
            double pf = Math.cos(phi);
            
            // Validar resultados
            SimulationResult result = new SimulationResult(Z, I, phi, P, Q, S, pf);
            if (!result.isValid()) {
                throw new ArithmeticException("Invalid simulation results");
            }
            
            return result;
            
        } catch (ArithmeticException e) {
            throw new RuntimeException("Mathematical error in calculation: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error in analytical calculation: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isValidFor(List<CircuitComponent> components) {
        if (components == null || components.isEmpty()) return false;
        
        // El método analítico es válido para cualquier configuración RLC
        return true;
    }
}