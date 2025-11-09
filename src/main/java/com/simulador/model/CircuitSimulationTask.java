package com.simulador.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Representa una tarea de simulación de circuito eléctrico para planificación
 * Represents an electrical circuit simulation task for scheduling
 */
public class CircuitSimulationTask implements Runnable, Comparable<CircuitSimulationTask> {
    private static final AtomicLong idGenerator = new AtomicLong(1);
    
    private final long id;
    private final String name;
    private final Complexity complexity;
    private final long estimatedDuration;
    private final long creationTime;
    
    private long startTime;
    private long finishTime;
    private volatile TaskState state;
    private Thread executionThread;
    
    public enum Complexity {
        SIMPLE("Simple", 1000, 5000),    // 1-5 segundos
        MEDIUM("Medio", 5000, 15000),    // 5-15 segundos  
        COMPLEX("Complejo", 15000, 30000); // 15-30 segundos
        
        private final String displayName;
        private final long minDuration;
        private final long maxDuration;
        
        Complexity(String displayName, long minDuration, long maxDuration) {
            this.displayName = displayName;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
        }
        
        public String getDisplayName() { return displayName; }
        public long getMinDuration() { return minDuration; }
        public long getMaxDuration() { return maxDuration; }
    }
    
    public enum TaskState {
        CREATED("Creada"),
        READY("Lista"),
        RUNNING("Ejecutando"),
        PAUSED("Pausada"),
        COMPLETED("Completada"),
        INTERRUPTED("Interrumpida");
        
        private final String displayName;
        
        TaskState(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public CircuitSimulationTask(String name, Complexity complexity) {
        this.id = idGenerator.getAndIncrement();
        this.name = name;
        this.complexity = complexity;
        this.estimatedDuration = calculateEstimatedDuration(complexity);
        this.creationTime = System.currentTimeMillis();
        this.state = TaskState.CREATED;
    }
    
    private long calculateEstimatedDuration(Complexity complexity) {
        long range = complexity.getMaxDuration() - complexity.getMinDuration();
        return complexity.getMinDuration() + (long)(Math.random() * range);
    }
    
    @Override
    public void run() {
        this.state = TaskState.RUNNING;
        this.startTime = System.currentTimeMillis();
        this.executionThread = Thread.currentThread();
        
        try {
            // Simular el procesamiento de la simulación del circuito
            System.out.printf("[Tarea %d] Iniciando simulación: %s (%s)%n", 
                id, name, complexity.getDisplayName());
            
            // Simular trabajo con sleep
            long start = System.currentTimeMillis();
            long elapsed = 0;
            
            while (elapsed < estimatedDuration && !Thread.currentThread().isInterrupted()) {
                long remaining = estimatedDuration - elapsed;
                long sleepTime = Math.min(remaining, 100); // Actualizar cada 100ms
                
                Thread.sleep(sleepTime);
                elapsed = System.currentTimeMillis() - start;
                
                // Simular progreso
                if (elapsed % 1000 == 0) {
                    int progress = (int)((elapsed * 100) / estimatedDuration);
                    System.out.printf("[Tarea %d] Progreso: %d%%%n", id, progress);
                }
            }
            
            if (!Thread.currentThread().isInterrupted()) {
                this.finishTime = System.currentTimeMillis();
                this.state = TaskState.COMPLETED;
                System.out.printf("[Tarea %d] Simulación completada en %d ms%n", 
                    id, getExecutionTime());
            } else {
                this.state = TaskState.INTERRUPTED;
                System.out.printf("[Tarea %d] Simulación interrumpida%n", id);
            }
            
        } catch (InterruptedException e) {
            this.state = TaskState.INTERRUPTED;
            System.out.printf("[Tarea %d] Simulación interrumpida%n", id);
            Thread.currentThread().interrupt();
        } finally {
            this.executionThread = null;
        }
    }
    
    public void pause() {
        if (state == TaskState.RUNNING && executionThread != null) {
            executionThread.interrupt();
            state = TaskState.PAUSED;
        }
    }
    
    public void resume() {
        if (state == TaskState.PAUSED) {
            state = TaskState.READY;
        }
    }
    
    public void interrupt() {
        if (executionThread != null) {
            executionThread.interrupt();
        }
        state = TaskState.INTERRUPTED;
    }
    
    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public Complexity getComplexity() { return complexity; }
    public long getEstimatedDuration() { return estimatedDuration; }
    public long getCreationTime() { return creationTime; }
    public long getStartTime() { return startTime; }
    public long getFinishTime() { return finishTime; }
    public TaskState getState() { return state; }
    
    public long getWaitingTime() {
        if (startTime == 0) return 0;
        return startTime - creationTime;
    }
    
    public long getExecutionTime() {
        if (finishTime == 0 || startTime == 0) return 0;
        return finishTime - startTime;
    }
    
    public long getTurnaroundTime() {
        if (finishTime == 0) return 0;
        return finishTime - creationTime;
    }
    
    public double getProgress() {
        if (startTime == 0) return 0;
        if (finishTime > 0) return 100.0;
        
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.min(100.0, (elapsed * 100.0) / estimatedDuration);
    }
    
    @Override
    public int compareTo(CircuitSimulationTask other) {
        return Long.compare(this.creationTime, other.creationTime);
    }
    
    @Override
    public String toString() {
        return String.format("Tarea[%d: %s, %s, %dms]", 
            id, name, complexity.getDisplayName(), estimatedDuration);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CircuitSimulationTask that = (CircuitSimulationTask) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}