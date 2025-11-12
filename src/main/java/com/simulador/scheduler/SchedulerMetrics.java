package com.simulador.scheduler;

import com.simulador.model.CircuitSimulationTask;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calcula métricas de rendimiento para algoritmos de planificación
 * Calculates performance metrics for scheduling algorithms
 */
public class SchedulerMetrics {
    private final List<CircuitSimulationTask> tasks;
    private final long startTime;
    private long endTime;
    
    public SchedulerMetrics(List<CircuitSimulationTask> tasks) {
        this.tasks = tasks;
        this.startTime = System.currentTimeMillis();
    }
    
    public void setEndTime() {
        this.endTime = System.currentTimeMillis();
    }
    
    public long getTotalExecutionTime() {
        return endTime - startTime;
    }
    
    public double getThroughput() {
        long completedTasks = tasks.stream()
            .filter(t -> t.getState() == CircuitSimulationTask.TaskState.COMPLETED)
            .count();
        double timeSeconds = getTotalExecutionTime() / 1000.0;
        return timeSeconds > 0 ? completedTasks / timeSeconds : 0;
    }
    
    public double getAverageWaitingTime() {
        return tasks.stream()
            .filter(t -> t.getState() == CircuitSimulationTask.TaskState.COMPLETED)
            .mapToLong(CircuitSimulationTask::getWaitingTime)
            .average()
            .orElse(0);
    }
    
    public double getAverageTurnaroundTime() {
        return tasks.stream()
            .filter(t -> t.getState() == CircuitSimulationTask.TaskState.COMPLETED)
            .mapToLong(CircuitSimulationTask::getTurnaroundTime)
            .average()
            .orElse(0);
    }
    
    public double getCPUUtilization() {
        long totalTaskTime = tasks.stream()
            .filter(t -> t.getState() == CircuitSimulationTask.TaskState.COMPLETED)
            .mapToLong(CircuitSimulationTask::getExecutionTime)
            .sum();
        return (totalTaskTime * 100.0) / getTotalExecutionTime();
    }
    
    public double getStandardDeviationWaitingTime() {
        double avg = getAverageWaitingTime();
        double variance = tasks.stream()
            .filter(t -> t.getState() == CircuitSimulationTask.TaskState.COMPLETED)
            .mapToLong(CircuitSimulationTask::getWaitingTime)
            .mapToDouble(w -> Math.pow(w - avg, 2))
            .average()
            .orElse(0);
        return Math.sqrt(variance);
    }
    
    public void printMetrics(String algorithmName) {
        System.out.println("\n=== MÉTRICAS DE RENDIMIENTO: " + algorithmName + " ===");
        System.out.printf("Tiempo total de ejecución: %d ms%n", getTotalExecutionTime());
        System.out.printf("Throughput: %.2f tareas/segundo%n", getThroughput());
        System.out.printf("Tiempo de espera promedio: %.2f ms%n", getAverageWaitingTime());
        System.out.printf("Tiempo de retorno promedio: %.2f ms%n", getAverageTurnaroundTime());
        System.out.printf("Utilización de CPU: %.2f%%%n", getCPUUtilization());
        System.out.printf("Desviación estándar tiempo de espera: %.2f ms%n", 
            getStandardDeviationWaitingTime());
        
        // Métricas por tipo de complejidad
        System.out.println("\n--- Por tipo de circuito ---");
        for (CircuitSimulationTask.Complexity complexity : CircuitSimulationTask.Complexity.values()) {
            List<CircuitSimulationTask> complexityTasks = tasks.stream()
                .filter(t -> t.getComplexity() == complexity)
                .collect(Collectors.toList());
            
            if (!complexityTasks.isEmpty()) {
                double avgTime = complexityTasks.stream()
                    .filter(t -> t.getState() == CircuitSimulationTask.TaskState.COMPLETED)
                    .mapToLong(CircuitSimulationTask::getTurnaroundTime)
                    .average()
                    .orElse(0);
                
                System.out.printf("%s: %.2f ms promedio (%d tareas)%n", 
                    complexity.getDisplayName(), avgTime, complexityTasks.size());
            }
        }
        System.out.println("====================================\n");
    }


 // --- INICIO DE MODIFICACIÓN ---

    /**
     * Obtiene la lista de tareas completadas de una complejidad específica
     */
    private List<CircuitSimulationTask> getTasksByComplexity(CircuitSimulationTask.Complexity complexity) {
        return tasks.stream()
                .filter(t -> t.getComplexity() == complexity && t.getState() == CircuitSimulationTask.TaskState.COMPLETED)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el tiempo de retorno promedio (Turnaround Time) para una complejidad específica
     */
    public double getAverageTurnaroundTime(CircuitSimulationTask.Complexity complexity) {
        return getTasksByComplexity(complexity).stream()
                .mapToLong(CircuitSimulationTask::getTurnaroundTime)
                .average()
                .orElse(0);
    }

    /**
     * Obtiene el tiempo de espera promedio (Waiting Time) para una complejidad específica
     */
    public double getAverageWaitingTime(CircuitSimulationTask.Complexity complexity) {
        return getTasksByComplexity(complexity).stream()
                .mapToLong(CircuitSimulationTask::getWaitingTime)
                .average()
                .orElse(0);
    }

    /**
     * Obtiene el conteo de tareas completadas para una complejidad específica
     */
    public long getTaskCount(CircuitSimulationTask.Complexity complexity) {
        return getTasksByComplexity(complexity).stream().count();
    }
    
    /**
     * Obtiene la lista de todas las tareas (para la tabla de historial).
     */
    public List<CircuitSimulationTask> getTasks() {
        return this.tasks;
    }
    
    // --- FIN DE MODIFICACIÓN ---
}