package com.simulador.scheduler;

import com.simulador.model.CircuitSimulationTask;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Algoritmo Shortest Job First (SJF)
 * Prioriza procesos con menor duración estimada
 */
public class ShortestJobFirstScheduler implements SchedulerStrategy {
    private volatile boolean running = false;
    private ExecutorService executor;
    
    @Override
    public void schedule(List<CircuitSimulationTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            System.out.println("Lista de tareas vacía para SJF");
            return;
        }
        
        running = true;
        executor = Executors.newFixedThreadPool(Math.min(4, tasks.size())); // Mejorar concurrencia
        
        new Thread(() -> {
            System.out.println("=== Iniciando planificación Shortest Job First ===");
            System.out.printf("Total de tareas: %d%n", tasks.size());
            
            // Ordenar por duración estimada (más corta primero)
            PriorityQueue<CircuitSimulationTask> queue = new PriorityQueue<>(
                (t1, t2) -> Long.compare(t1.getEstimatedDuration(), t2.getEstimatedDuration())
            );
            queue.addAll(tasks);
            
            for (CircuitSimulationTask task : queue) {
                if (!running) break;
                
                try {
                    System.out.printf("Ejecutando tarea (duración: %d ms): %s%n", 
                        task.getEstimatedDuration(), task);
                    
                    // Ejecutar la tarea
                    executor.submit(task).get(); // Esperar a que termine
                    
                    System.out.printf("Tarea %d completada%n", task.getId());
                    
                } catch (Exception e) {
                    System.out.println("Error ejecutando tarea " + task.getId() + ": " + e.getMessage());
                    if (!running) break;
                }
            }
            
            if (running) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            
            running = false;
            System.out.println("=== Planificación SJF finalizada ===");
            
        }).start();
    }
    
    @Override
    public String getName() {
        return "Shortest Job First (SJF)";
    }
    
    @Override
    public String getDescription() {
        return "Prioriza tareas con menor duración estimada (no apropiativo)";
    }
    
    @Override
    public void interrupt() {
        running = false;
        if (executor != null) {
            executor.shutdownNow();
        }
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
}