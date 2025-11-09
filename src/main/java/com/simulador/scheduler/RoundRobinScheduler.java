package com.simulador.scheduler;

import com.simulador.model.CircuitSimulationTask;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Algoritmo Round Robin con quantum fijo
 * Asigna un quantum de tiempo a cada proceso
 */
public class RoundRobinScheduler implements SchedulerStrategy {
    private static final long QUANTUM = 100; // 100 ms
    private static final long CONTEXT_SWITCH_TIME = 10; // 10 ms para cambio de contexto
    private volatile boolean running = false;
    private ExecutorService executor;
    
    @Override
    public void schedule(List<CircuitSimulationTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            System.out.println("Lista de tareas vacía para Round Robin");
            return;
        }
        
        running = true;
        executor = Executors.newFixedThreadPool(1);
        
        new Thread(() -> {
            System.out.println("=== Iniciando planificación Round Robin ===");
            System.out.printf("Total de tareas: %d, Quantum: %d ms%n", tasks.size(), QUANTUM);
            
            Queue<CircuitSimulationTask> readyQueue = new LinkedList<>(tasks);
            AtomicInteger completedTasks = new AtomicInteger(0);
            int totalTasks = tasks.size();
            int cycles = 0;
            
            while (!readyQueue.isEmpty() && running) {
                cycles++;
                CircuitSimulationTask task = readyQueue.poll();
                
                // Si la tarea ya está completada, continuar con la siguiente
                if (task.getState() == CircuitSimulationTask.TaskState.COMPLETED) {
                    completedTasks.incrementAndGet();
                    continue;
                }
                
                System.out.printf("Ciclo %d - Ejecutando tarea: %s%n", cycles, task);
                
                // Simular ejecución por quantum
                final CircuitSimulationTask currentTask = task;
                executor.submit(() -> {
                    try {
                        // Calcular tiempo restante para esta tarea
                        long remainingTime = currentTask.getEstimatedDuration() - 
                                           currentTask.getExecutionTime();
                        
                        if (remainingTime > 0) {
                            // Ejecutar por el quantum o el tiempo restante (lo que sea menor)
                            long executionTime = Math.min(QUANTUM, remainingTime);
                            
                            // Simular la ejecución
                            Thread.sleep(executionTime);
                            
                            // Actualizar el progreso de la tarea
                            long newRemainingTime = remainingTime - executionTime;
                            
                            System.out.printf("Tarea %d ejecutada por %d ms, restante: %d ms%n",
                                currentTask.getId(), executionTime, newRemainingTime);
                            
                            // Si la tarea no ha terminado, volver a la cola
                            if (newRemainingTime > 0) {
                                synchronized(readyQueue) {
                                    readyQueue.offer(currentTask);
                                    System.out.printf("Tarea %d vuelve a la cola (restante: %d ms)%n",
                                        currentTask.getId(), newRemainingTime);
                                }
                            } else {
                                // Tarea completada
                                completedTasks.incrementAndGet();
                                System.out.printf("Tarea %d COMPLETADA%n", currentTask.getId());
                            }
                        } else {
                            // Tarea ya debería estar completada
                            completedTasks.incrementAndGet();
                            System.out.printf("Tarea %d ya completada%n", currentTask.getId());
                        }
                        
                    } catch (InterruptedException e) {
                        System.out.printf("Tarea %d interrumpida durante quantum%n", 
                            currentTask.getId());
                        Thread.currentThread().interrupt();
                        
                        // Si fue interrumpida, volver a la cola si no está completada
                        if (currentTask.getState() != CircuitSimulationTask.TaskState.COMPLETED) {
                            synchronized(readyQueue) {
                                readyQueue.offer(currentTask);
                            }
                        }
                    }
                });
                
                // Simular tiempo de cambio de contexto entre tareas
                try {
                    Thread.sleep(CONTEXT_SWITCH_TIME);
                } catch (InterruptedException e) {
                    System.out.println("Planificación Round Robin interrumpida durante cambio de contexto");
                    break;
                }
                
                // Pequeña pausa para evitar saturación del CPU
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    break;
                }
            }
            
            // Esperar a que terminen todas las tareas en ejecución
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    System.out.println("Forzando terminación de tareas pendientes...");
                    executor.shutdownNow();
                    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                        System.err.println("No se pudieron terminar todas las tareas");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            running = false;
            int finalCompleted = completedTasks.get();
            System.out.printf("=== Planificación Round Robin finalizada ===%n");
            System.out.printf("Ciclos ejecutados: %d%n", cycles);
            System.out.printf("Tareas completadas: %d/%d%n", finalCompleted, totalTasks);
            System.out.printf("Eficiencia: %.1f%%%n", (finalCompleted * 100.0) / totalTasks);
            
        }).start();
    }
    
    @Override
    public String getName() {
        return "Round Robin (RR)";
    }
    
    @Override
    public String getDescription() {
        return "Planificación por turnos con quantum fijo de " + QUANTUM + "ms y cambio de contexto de " + CONTEXT_SWITCH_TIME + "ms";
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