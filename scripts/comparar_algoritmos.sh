#!/bin/bash

echo "=== Comparación de Algoritmos de Planificación ==="

ALGORITMOS=("FCFS" "RoundRobin" "SJF")

for algoritmo in "${ALGORITMOS[@]}"; do
    echo ""
    echo "--- Ejecutando con $algoritmo ---"
    
    # Crear directorio para resultados
    RESULT_DIR="resultados_${algoritmo}_$(date +%Y%m%d_%H%M%S)"
    mkdir -p $RESULT_DIR
    
    # Ejecutar con algoritmo específico
    time java -Dalgoritmo.planificacion=$algoritmo -jar target/simuladordefisica-1.0-SNAPSHOT.jar 2>&1 | tee $RESULT_DIR/ejecucion.log
    
    # Métricas del sistema
    echo "Procesando métricas..."
    
    # Extraer tiempos de ejecución
    grep "real" $RESULT_DIR/ejecucion.log > $RESULT_DIR/tiempos.txt
    
    echo "Resultados de $algoritmo guardados en: $RESULT_DIR"
done

echo ""
echo "=== Comparación Completada ==="
echo "Revise los directorios resultados_* para análisis comparativo"