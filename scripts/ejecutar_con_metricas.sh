#!/bin/bash

echo "=== Ejecutando Simulador RLC con Métricas del Sistema ==="

# Compilar el proyecto
echo "Compilando proyecto..."
mvn clean package -q

if [ $? -ne 0 ]; then
    echo "Error en la compilación"
    exit 1
fi

echo "Compilación completada"

# Crear directorio para métricas
METRICS_DIR="metricas_ejecucion_$(date +%Y%m%d_%H%M%S)"
mkdir -p $METRICS_DIR

echo "Métricas se guardarán en: $METRICS_DIR"

# Capturar estado inicial de memoria
echo "Capturando estado inicial de memoria..."
free -h > $METRICS_DIR/memoria_inicio.txt

# Ejecutar simulador con time para métricas de tiempo
echo "Ejecutando simulador..."
time java -jar target/simuladordefisica-1.0-SNAPSHOT.jar 2>&1 | tee $METRICS_DIR/salida_simulador.txt

# Métricas del sistema durante ejecución (en segundo plano)
echo "Capturando métricas del sistema..."
top -b -d 1 > $METRICS_DIR/metricas_sistema.txt &
TOP_PID=$!

vmstat 1 60 > $METRICS_DIR/metricas_vmstat.txt &
VMSTAT_PID=$!

# Esperar a que termine el simulador
wait

# Detener captura de métricas
kill $TOP_PID 2>/dev/null
kill $VMSTAT_PID 2>/dev/null

# Estado final de memoria
echo "Capturando estado final de memoria..."
free -h > $METRICS_DIR/memoria_fin.txt

echo "Ejecución completada"
echo "Métricas guardadas en: $METRICS_DIR"
echo ""
echo "Resumen de archivos generados:"
ls -la $METRICS_DIR/