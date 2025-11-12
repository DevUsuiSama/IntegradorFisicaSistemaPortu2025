package com.simulador.engine.dc;

/**
 * Métodos de análisis disponibles para circuitos DC
 */
public enum DCAnalysisMethod {
    OHM_LAW("Ley de Ohm"),
    KIRCHHOFF_LAWS("Leyes de Kirchhoff"),
    MESH_ANALYSIS("Análisis de Mallas"),
    NODE_ANALYSIS("Análisis Nodal"),
    THEVENIN_THEOREM("Teorema de Thevenin"),
    NORTON_THEOREM("Teorema de Norton"),
    SOURCE_TRANSFORMATION("Transformación de Fuentes");
    
    private final String displayName;
    
    DCAnalysisMethod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}