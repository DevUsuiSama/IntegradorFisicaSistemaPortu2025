package com.simulador.utils;

/**
 * Interfaz Observer para notificaciones de simulación
 * Observer interface for simulation notifications
 */
public interface SimulationObserver {
    /**
     * Se llama cuando una simulación se completa exitosamente
     * Called when a simulation completes successfully
     */
    void onSimulationComplete(Object result);
    
    /**
     * Se llama cuando ocurre un error en la simulación
     * Called when a simulation error occurs
     */
    void onSimulationError(String error);
    
    /**
     * Se llama cuando una simulación inicia (método opcional)
     * Called when a simulation starts (optional method)
     */
    default void onSimulationStart() {
        // Implementación por defecto vacía para compatibilidad
    }
}