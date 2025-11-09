package com.simulador.scheduler;

import com.simulador.model.CircuitSimulationTask;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador principal para la planificación de procesos
 * Main controller for process scheduling
 */
public class ProcessScheduler {
    private SchedulerStrategy currentStrategy;
    private final List<CircuitSimulationTask> tasks;
    private SchedulerMetrics metrics;
    private boolean simulationRunning;
    private final PropertyChangeSupport propertyChangeSupport;
    
    // Constantes para eventos de propiedad
    public static final String PROPERTY_MESSAGE = "message";
    public static final String PROPERTY_SIMULATION_STATE = "simulationState";
    public static final String PROPERTY_TASKS_UPDATED = "tasksUpdated";
    
    public ProcessScheduler() {
        this.tasks = new ArrayList<>();
        this.simulationRunning = false;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }
    
    // Métodos para manejar PropertyChangeListeners
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }
    
    public void setStrategy(SchedulerStrategy strategy) {
        if (simulationRunning) {
            throw new IllegalStateException("No se puede cambiar estrategia durante simulación");
        }
        this.currentStrategy = strategy;
        firePropertyChange(PROPERTY_MESSAGE, null, "Estrategia cambiada a: " + strategy.getName());
    }
    
    public void addTask(CircuitSimulationTask task) {
        tasks.add(task);
        firePropertyChange(PROPERTY_MESSAGE, null, "Tarea agregada: " + task.getName());
        firePropertyChange(PROPERTY_TASKS_UPDATED, null, tasks);
    }
    
    public void addTasks(List<CircuitSimulationTask> newTasks) {
        tasks.addAll(newTasks);
        firePropertyChange(PROPERTY_MESSAGE, null, newTasks.size() + " tareas agregadas");
        firePropertyChange(PROPERTY_TASKS_UPDATED, null, tasks);
    }
    
    public void clearTasks() {
        if (simulationRunning) {
            throw new IllegalStateException("No se puede limpiar durante simulación");
        }
        tasks.clear();
        firePropertyChange(PROPERTY_MESSAGE, null, "Todas las tareas eliminadas");
        firePropertyChange(PROPERTY_TASKS_UPDATED, null, tasks);
    }
    
    public void startSimulation() {
        if (currentStrategy == null) {
            throw new IllegalStateException("No se ha seleccionado algoritmo de planificación");
        }
        if (tasks.isEmpty()) {
            throw new IllegalStateException("No hay tareas para planificar");
        }
        if (simulationRunning) {
            throw new IllegalStateException("Simulación ya en ejecución");
        }
        
        simulationRunning = true;
        this.metrics = new SchedulerMetrics(new ArrayList<>(tasks));
        
        firePropertyChange(PROPERTY_MESSAGE, null, "Iniciando simulación con " + currentStrategy.getName());
        firePropertyChange(PROPERTY_SIMULATION_STATE, false, true);
        
        // Ejecutar en hilo separado
        new Thread(() -> {
            try {
                currentStrategy.schedule(tasks);
                
                // Monitorear finalización
                while (currentStrategy.isRunning()) {
                    Thread.sleep(100);
                }
                
                metrics.setEndTime();
                simulationRunning = false;
                
                // Mostrar métricas
                metrics.printMetrics(currentStrategy.getName());
                firePropertyChange(PROPERTY_MESSAGE, null, "Simulación completada");
                firePropertyChange(PROPERTY_SIMULATION_STATE, true, false);
                
            } catch (InterruptedException e) {
                simulationRunning = false;
                firePropertyChange(PROPERTY_MESSAGE, null, "Simulación interrumpida");
                firePropertyChange(PROPERTY_SIMULATION_STATE, true, false);
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    public void stopSimulation() {
        if (currentStrategy != null && simulationRunning) {
            currentStrategy.interrupt();
            simulationRunning = false;
            firePropertyChange(PROPERTY_MESSAGE, null, "Simulación detenida");
            firePropertyChange(PROPERTY_SIMULATION_STATE, true, false);
        }
    }
    
    public List<CircuitSimulationTask> generateHomogeneousBatch(CircuitSimulationTask.Complexity complexity, int count) {
        List<CircuitSimulationTask> batch = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String name = String.format("Circuito_%s_%d", complexity.getDisplayName(), i);
            batch.add(new CircuitSimulationTask(name, complexity));
        }
        return batch;
    }
    
    public List<CircuitSimulationTask> generateHeterogeneousBatch(int simpleCount, int mediumCount, int complexCount) {
        List<CircuitSimulationTask> batch = new ArrayList<>();
        
        // Agregar tareas simples
        for (int i = 1; i <= simpleCount; i++) {
            batch.add(new CircuitSimulationTask(
                String.format("Circuito_Simple_%d", i), 
                CircuitSimulationTask.Complexity.SIMPLE
            ));
        }
        
        // Agregar tareas medias
        for (int i = 1; i <= mediumCount; i++) {
            batch.add(new CircuitSimulationTask(
                String.format("Circuito_Medio_%d", i), 
                CircuitSimulationTask.Complexity.MEDIUM
            ));
        }
        
        // Agregar tareas complejas
        for (int i = 1; i <= complexCount; i++) {
            batch.add(new CircuitSimulationTask(
                String.format("Circuito_Complejo_%d", i), 
                CircuitSimulationTask.Complexity.COMPLEX
            ));
        }
        
        return batch;
    }
    
    // Getters
    public List<CircuitSimulationTask> getTasks() { return new ArrayList<>(tasks); }
    public SchedulerStrategy getCurrentStrategy() { return currentStrategy; }
    public boolean isSimulationRunning() { return simulationRunning; }
    public SchedulerMetrics getMetrics() { return metrics; }
    
    // Método helper para disparar eventos de propiedad
    private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}