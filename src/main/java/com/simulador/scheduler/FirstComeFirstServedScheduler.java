package com.simulador.scheduler;

import com.simulador.model.CircuitSimulationTask;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Algoritmo First-Come, First-Served (FCFS)
 * Ejecuta procesos en orden de llegada
 */
public class FirstComeFirstServedScheduler implements SchedulerStrategy {
    private volatile boolean running = false;
    private ExecutorService executor;
    
    @Override
    public void schedule(List<CircuitSimulationTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            System.out.println("Lista de tareas vacía para FCFS");
            return;
        }
        
        running = true;
        executor = Executors.newSingleThreadExecutor();
        
        // Ejecutar en hilo separado para no bloquear la UI
        new Thread(() -> {
            System.out.println("=== Iniciando planificación FCFS ===");
            System.out.printf("Total de tareas: %d%n", tasks.size());
            
            for (CircuitSimulationTask task : tasks) {
                if (!running) break;
                
                try {
                    System.out.printf("Ejecutando tarea: %s%n", task);
                    executor.submit(task);
                    
                    // Esperar a que termine la tarea actual antes de continuar
                    while (!executor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                        if (!running) {
                            executor.shutdownNow();
                            break;
                        }
                    }
                    
                } catch (InterruptedException e) {
                    System.out.println("Planificación FCFS interrumpida");
                    Thread.currentThread().interrupt();
                    break;
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
            System.out.println("=== Planificación FCFS finalizada ===");
            
        }).start();
    }
    
    @Override
    public String getName() {
        return "First-Come, First-Served (FCFS)";
    }
    
    @Override
    public String getDescription() {
        return "Ejecuta procesos en orden de llegada (no apropiativo)";
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