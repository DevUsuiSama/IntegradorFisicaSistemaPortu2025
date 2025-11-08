package com.simulador.model;

import com.simulador.utils.LanguageManager;
import java.util.Objects;

/**
 * Representa un componente del circuito RLC
 * Represents an RLC circuit component
 */
public class CircuitComponent {
    private String type;
    private double value;
    private LanguageManager languageManager;
    
    public CircuitComponent(String type, double value) {
        this.type = type;
        this.value = value;
        this.languageManager = LanguageManager.getInstance();
    }
    
    public String getType() { 
        return type; 
    }
    
    public double getValue() { 
        return value; 
    }
    
    public double getResistance() {
        return type.equals("Resistance") ? value : 0;
    }
    
    public double getInductance() {
        return type.equals("Inductor") ? value : 0;
    }
    
    public double getCapacitance() {
        return type.equals("Capacitor") ? value : 0;
    }
    
    @Override
    public String toString() {
        String displayType = "";
        String unit = "";
        
        switch(type) {
            case "Resistance":
                displayType = languageManager.getTranslation("resistance");
                unit = "Ω";
                break;
            case "Inductor":
                displayType = languageManager.getTranslation("inductor");
                unit = "H";
                break;
            case "Capacitor":
                displayType = languageManager.getTranslation("capacitor");
                unit = "F";
                break;
        }
        
        return displayType + ": " + value + " " + unit;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CircuitComponent that = (CircuitComponent) obj;
        return Double.compare(that.value, value) == 0 && type.equals(that.type);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}