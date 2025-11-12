package com.simulador.model.dc;

/**
 * Representa un componente en un circuito DC (Principio de Responsabilidad Única)
 */
public class DCComponent {
    private final String id;
    private final DCComponentType type;
    private final double value;
    private final String name;
    private final int quantity;
    
    public DCComponent(DCComponentType type, double value, String name, int quantity) {
        this.id = generateId(type, name);
        this.type = type;
        this.value = value;
        this.name = name;
        this.quantity = quantity;
    }
    
    public DCComponent(DCComponentType type, double value) {
        this(type, value, type.getDisplayName(), 1);
    }
    
    private String generateId(DCComponentType type, String name) {
        return type.getSymbol() + "_" + name + "_" + System.currentTimeMillis();
    }
    
    // Getters
    public String getId() { return id; }
    public DCComponentType getType() { return type; }
    public double getValue() { return value; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    
    public double getResistance() {
        return type == DCComponentType.RESISTOR ? value : 0;
    }
    
    public double getVoltage() {
        return (type == DCComponentType.BATTERY || type == DCComponentType.DC_SOURCE) ? value : 0;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %.2f %s", name, value, type.getUnit());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DCComponent that = (DCComponent) obj;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}