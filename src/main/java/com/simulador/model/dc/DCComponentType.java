package com.simulador.model.dc;

/**
 * Tipos de componentes para circuitos DC
 */
public enum DCComponentType {
    RESISTOR("Resistencia", "Ω", "R"),
    BATTERY("Batería", "V", "B"),
    DC_SOURCE("Fuente DC", "V", "S"),
    AMMETER("Amperímetro", "A", "A"),
    VOLTMETER("Voltímetro", "V", "V");
    
    private final String displayName;
    private final String unit;
    private final String symbol;
    
    DCComponentType(String displayName, String unit, String symbol) {
        this.displayName = displayName;
        this.unit = unit;
        this.symbol = symbol;
    }
    
    public String getDisplayName() { return displayName; }
    public String getUnit() { return unit; }
    public String getSymbol() { return symbol; }
}