package com.simulador.utils;

import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 * Gestor especializado de idiomas para el simulador RLC
 * Maneja español y portugués con JSON interno
 * MODIFICADO: Incluye todas las claves para la UI completa del RLCSimulator.
 */
public class LanguageManager {

    private static LanguageManager instance;
    private Map<String, Map<String, String>> translations;
    private String currentLanguage;

    // JSON INTERNO ACTUALIZADO CON TODAS LAS CLAVES
    private static final Map<String, Object> LANGUAGE_JSON = Map.ofEntries(
        Map.entry("es", Map.ofEntries(
            // --- Menú ---
            Map.entry("menu_options", "Opciones"),
            Map.entry("menu_language", "Idioma"),
            Map.entry("menu_font_size", "Tamaño de Letra"),
            Map.entry("font_small", "Pequeño"),
            Map.entry("font_medium", "Mediano"),
            Map.entry("font_large", "Grande"),
            Map.entry("lang_es", "Español"),
            Map.entry("lang_pt", "Português"),
            
            // --- Header ---
            Map.entry("header_title", "Simulador Avanzado de Circuitos RLC y DC"),
            Map.entry("header_subtitle", "Con Algoritmos de Planificación y Gestión de Memoria Virtual"),
            
            // --- Pestañas Principales (Izquierda) ---
            Map.entry("tab_rlc", "Circuito RLC"),
            Map.entry("tab_dc", "Circuito DC"),
            Map.entry("tab_process", "Procesos"),
            
            // --- Pestaña RLC ---
            Map.entry("rlc_power_supply", "Fuente de Alimentación"),
            Map.entry("rlc_voltage", "Voltaje (V):"),
            Map.entry("rlc_frequency", "Frecuencia (Hz):"),
            Map.entry("rlc_simulation_method", "Método de Simulación"),
            Map.entry("rlc_presets", "Circuitos Predefinidos"),
            Map.entry("rlc_add_components", "Agregar Componentes"),
            Map.entry("rlc_type", "Tipo:"),
            Map.entry("rlc_value", "Valor:"),
            Map.entry("rlc_add_button", "Agregar Componente"),
            Map.entry("rlc_component_list", "Componentes en el Circuito"),
            Map.entry("rlc_remove_button", "Eliminar Seleccionado"),
            Map.entry("rlc_actions", "Acciones"),
            Map.entry("rlc_simulate_button", "Simular Circuito"),
            Map.entry("rlc_clear_button", "Limpiar Todo"),
            Map.entry("resistance", "Resistencia"),
            Map.entry("inductor", "Inductor"),
            Map.entry("capacitor", "Capacitor"),
            Map.entry("custom", "Personalizado"),
            Map.entry("underdamped", "Subamortiguado"),
            Map.entry("critical", "Crítico"),
            Map.entry("overdamped", "Sobreamortiguado"),
            Map.entry("series_rlc", "RLC Serie"),
            Map.entry("high_pass", "Filtro Pasa Altos"),
            Map.entry("low_pass", "Filtro Pasa Bajos"),
            Map.entry("analytical", "Analítico"),
            Map.entry("euler", "Euler"),
            Map.entry("runge_kutta", "Runge-Kutta4"),

            // --- Pestaña DC ---
            Map.entry("dc_config_circuit", "Configuración del Circuito"),
            Map.entry("dc_num_branches", "Número de Ramas:"),
            Map.entry("dc_config", "Configuración:"),
            Map.entry("dc_config_series", "Serie"),
            Map.entry("dc_config_parallel", "Paralelo"),
            Map.entry("dc_config_mixed", "Mixto"),
            Map.entry("dc_add_components", "Agregar Componentes DC"),
            Map.entry("dc_type", "Tipo:"),
            Map.entry("dc_value", "Valor:"),
            Map.entry("dc_polarity", "Polaridad:"),
            Map.entry("dc_polarity_up", "Positivo Hacia Arriba"),
            Map.entry("dc_polarity_down", "Negativo Hacia Arriba"),
            Map.entry("dc_in_branch", "En Rama #:"),
            Map.entry("dc_add_button", "Agregar Componente DC"),
            Map.entry("dc_analysis_method", "Método de Análisis"),
            Map.entry("dc_actions", "Acciones DC"),
            Map.entry("dc_simulate_button", "Simular Circuito DC"),
            Map.entry("dc_clear_button", "Limpiar Circuito DC"),
            Map.entry("dc_type_battery", "Batería"),
            Map.entry("dc_type_resistor", "Resistencia"),
            Map.entry("dc_type_source", "Fuente DC"),
            Map.entry("dc_method_ohm", "Ley de Ohm"),
            Map.entry("dc_method_kirchhoff", "Leyes de Kirchhoff"),
            Map.entry("dc_method_mesh", "Análisis de Mallas"),
            Map.entry("dc_method_nodal", "Análisis Nodal"),
            Map.entry("dc_method_thevenin", "Teorema de Thevenin"),
            Map.entry("dc_method_norton", "Teorema de Norton"),
            Map.entry("dc_method_source", "Transformación de Fuentes"),

            // --- Pestaña Procesos ---
            Map.entry("proc_algorithm", "Algoritmo de Planificación"),
            Map.entry("proc_batch_type", "Tipo de Lote"),
            Map.entry("proc_batch_config", "Configuración del Lote"),
            Map.entry("proc_execution_control", "Control de Ejecución"),
            Map.entry("proc_select_algorithm", "Seleccione algoritmo:"),
            Map.entry("proc_algo_fcfs", "First-Come, First-Served (FCFS)"),
            Map.entry("proc_algo_rr", "Round Robin (RR)"),
            Map.entry("proc_algo_sjf", "Shortest Job First (SJF)"),
            Map.entry("proc_batch_config_label", "Configuración del lote:"),
            Map.entry("proc_batch_simple", "Homogéneo - Simple"),
            Map.entry("proc_batch_medium", "Homogéneo - Medio"),
            Map.entry("proc_batch_complex", "Homogéneo - Complejo"),
            Map.entry("proc_batch_mixed", "Heterogéneo - Mixto"),
            Map.entry("proc_simple", "Simples:"),
            Map.entry("proc_medium", "Medios:"),
            Map.entry("proc_complex", "Complejos:"),
            Map.entry("proc_generate_batch", "Generar Lote de Simulaciones"),
            Map.entry("proc_start_button", "Iniciar Planificación"),
            Map.entry("proc_stop_button", "Detener"),

            // --- Panel RLC (Derecha) ---
            Map.entry("rlc_diagram_title", "Diagrama del Circuito"),
            Map.entry("rlc_tab_visualization", "Visualización"),
            Map.entry("rlc_tab_results", "Resultados"),
            Map.entry("rlc_tab_analysis", "Análisis"),
            Map.entry("rlc_graph_type", "Tipo de Gráfico:"),
            Map.entry("rlc_graph_time", "Dominio de Tiempo"),
            Map.entry("rlc_graph_frequency", "Respuesta en Frecuencia"),
            Map.entry("rlc_graph_phasor", "Diagrama Fasorial"),
            Map.entry("rlc_graph_waveforms", "Formas de Onda"),
            Map.entry("rlc_results_placeholder_title", "=== Simulador Avanzado de Circuitos RLC ==="),
            Map.entry("rlc_results_placeholder_inst", "Instrucciones:"),
            Map.entry("rlc_analysis_placeholder_title", "=== ANÁLISIS DETALLADO DEL CIRCUITO ==="),

            // --- Panel DC (Derecha) ---
            Map.entry("dc_panel_title", "Simulador de Circuitos DC - Análisis Resistivo"),
            Map.entry("dc_diagram_title", "Diagrama del Circuito DC"),
            Map.entry("dc_tab_main_results", "Resultados Principales"),
            Map.entry("dc_tab_equivalents", "Circuitos Equivalentes"),
            Map.entry("dc_tab_detailed_analysis", "Análisis Detallado"),
            Map.entry("dc_analysis_placeholder_title", "=== ANÁLISIS DETALLADO DC ==="),

            // --- Panel Memoria (Derecha) ---
            Map.entry("mem_panel_title", "Memoria Virtual - Simulación de Procesos"),
            Map.entry("mem_system_status", "Estado del Sistema"),
            Map.entry("mem_total_frag", "Fragmentación Total:"),
            Map.entry("mem_current_usage", "Uso actual de Memoria:"),
            Map.entry("mem_avg_usage", "Uso promedio:"),
            Map.entry("mem_ext_frag", "Fragmentación Externa:"),
            Map.entry("mem_int_frag", "Fragmentación Interna:"),
            Map.entry("mem_semaphore", "Semáforo 'Recurso Compartido':"),
            Map.entry("mem_map_title", "Mapa de Memoria"),
            Map.entry("mem_current_process", "Proceso en Ejecución"),
            Map.entry("mem_queues_title", "Colas de Procesos"),
            Map.entry("mem_none", "Ninguno"),
            Map.entry("mem_empty", "Vacía"),
            Map.entry("mem_queue_ready", "=== COLA DE LISTOS ==="),
            Map.entry("mem_queue_running", "=== EN EJECUCIÓN ==="),
            Map.entry("mem_queue_completed", "=== COMPLETADOS ==="),

            // --- Mensajes y Errores ---
            Map.entry("info", "Información"),
            Map.entry("error", "Error"),
            Map.entry("dc_value_positive", "El valor de la resistencia debe ser positivo"),
            Map.entry("dc_branch_error", "La rama de destino no existe. Ajuste el 'Número de Ramas' primero."),
            Map.entry("dc_numeric_error", "Ingrese valores numéricos válidos para el componente DC"),
            Map.entry("dc_add_error", "Error al agregar componente DC: %s"),
            Map.entry("dc_add_success", "Componente DC agregado a la Rama %d"),
            Map.entry("dc_invalid_circuit", "Circuito DC no válido. Agregue componentes y configure el circuito."),
            Map.entry("dc_sim_error", "Error en simulación DC: %s"),
            Map.entry("dc_sim_success", "Simulación DC completada usando %s"),
            Map.entry("dc_cleared", "Circuito DC limpiado"),
            Map.entry("rlc_value_positive", "El valor del componente debe ser positivo"),
            Map.entry("rlc_select_to_remove", "Seleccione un componente para eliminar"),
            Map.entry("rlc_add_one_component", "Agregue al menos un componente al circuito"),
            Map.entry("rlc_voltage_range", "El voltaje debe estar entre 0.1 y 1000 V"),
            Map.entry("rlc_frequency_range", "La frecuencia debe estar entre 0.1 y 10000 Hz"),
            Map.entry("rlc_numeric_error", "Ingrese valores numéricos válidos para voltaje y frecuencia"),
            Map.entry("rlc_cleared", "Circuito limpiado. Listo para nueva simulación."),
            Map.entry("rlc_cleared_all", "Circuito y resultados limpiados"),
            Map.entry("rlc_preset_loaded", "Circuito predefinido '%s' cargado"),
            Map.entry("proc_batch_generated", "Lote generado: %d tareas"),
            Map.entry("proc_start_log", "Iniciando planificación con %s"),
            Map.entry("proc_stop_log", "Planificación detenida"),
            Map.entry("proc_start_error", "Error al iniciar planificación: %s"),
            Map.entry("sim_in_progress", "Simulación en progreso..."),
            Map.entry("sim_complete", "Simulación completada exitosamente"),
            Map.entry("sim_error_generic", "Error en la simulación: %s"),
            Map.entry("sim_error_details", "Error en simulación. Por favor, verifique los parámetros e intente nuevamente.\n\nDetalles del error: %s")
        )),
        Map.entry("pt", Map.ofEntries(
            // --- Menú ---
            Map.entry("menu_options", "Opções"),
            Map.entry("menu_language", "Idioma"),
            Map.entry("menu_font_size", "Tamanho da Fonte"),
            Map.entry("font_small", "Pequeno"),
            Map.entry("font_medium", "Médio"),
            Map.entry("font_large", "Grande"),
            Map.entry("lang_es", "Espanhol"),
            Map.entry("lang_pt", "Português"),

            // --- Header ---
            Map.entry("header_title", "Simulador Avançado de Circuitos RLC e DC"),
            Map.entry("header_subtitle", "Com Algoritmos de Agendamento e Gerenciamento de Memória Virtual"),

            // --- Pestañas Principales (Izquierda) ---
            Map.entry("tab_rlc", "Circuito RLC"),
            Map.entry("tab_dc", "Circuito DC"),
            Map.entry("tab_process", "Processos"),

            // --- Pestaña RLC ---
            Map.entry("rlc_power_supply", "Fonte de Alimentação"),
            Map.entry("rlc_voltage", "Tensão (V):"),
            Map.entry("rlc_frequency", "Frequência (Hz):"),
            Map.entry("rlc_simulation_method", "Método de Simulação"),
            Map.entry("rlc_presets", "Circuitos Predefinidos"),
            Map.entry("rlc_add_components", "Adicionar Componentes"),
            Map.entry("rlc_type", "Tipo:"),
            Map.entry("rlc_value", "Valor:"),
            Map.entry("rlc_add_button", "Adicionar Componente"),
            Map.entry("rlc_component_list", "Componentes no Circuito"),
            Map.entry("rlc_remove_button", "Remover Selecionado"),
            Map.entry("rlc_actions", "Ações"),
            Map.entry("rlc_simulate_button", "Simular Circuito"),
            Map.entry("rlc_clear_button", "Limpar Tudo"),
            Map.entry("resistance", "Resistência"),
            Map.entry("inductor", "Indutor"),
            Map.entry("capacitor", "Capacitor"),
            Map.entry("custom", "Personalizado"),
            Map.entry("underdamped", "Subamortecido"),
            Map.entry("critical", "Crítico"),
            Map.entry("overdamped", "Superamortecido"),
            Map.entry("series_rlc", "RLC Série"),
            Map.entry("high_pass", "Filtro Passa-Altas"),
            Map.entry("low_pass", "Filtro Passa-Baixas"),
            Map.entry("analytical", "Analítico"),
            Map.entry("euler", "Euler"),
            Map.entry("runge_kutta", "Runge-Kutta4"),

            // --- Pestaña DC ---
            Map.entry("dc_config_circuit", "Configuração do Circuito"),
            Map.entry("dc_num_branches", "Número de Ramos:"),
            Map.entry("dc_config", "Configuração:"),
            Map.entry("dc_config_series", "Série"),
            Map.entry("dc_config_parallel", "Paralelo"),
            Map.entry("dc_config_mixed", "Misto"),
            Map.entry("dc_add_components", "Adicionar Componentes DC"),
            Map.entry("dc_type", "Tipo:"),
            Map.entry("dc_value", "Valor:"),
            Map.entry("dc_polarity", "Polaridade:"),
            Map.entry("dc_polarity_up", "Positivo Para Cima"),
            Map.entry("dc_polarity_down", "Negativo Para Cima"),
            Map.entry("dc_in_branch", "No Ramo #:"),
            Map.entry("dc_add_button", "Adicionar Componente DC"),
            Map.entry("dc_analysis_method", "Método de Análise"),
            Map.entry("dc_actions", "Ações DC"),
            Map.entry("dc_simulate_button", "Simular Circuito DC"),
            Map.entry("dc_clear_button", "Limpar Circuito DC"),
            Map.entry("dc_type_battery", "Bateria"),
            Map.entry("dc_type_resistor", "Resistência"),
            Map.entry("dc_type_source", "Fonte DC"),
            Map.entry("dc_method_ohm", "Lei de Ohm"),
            Map.entry("dc_method_kirchhoff", "Leis de Kirchhoff"),
            Map.entry("dc_method_mesh", "Análise de Malhas"),
            Map.entry("dc_method_nodal", "Análise Nodal"),
            Map.entry("dc_method_thevenin", "Teorema de Thevenin"),
            Map.entry("dc_method_norton", "Teorema de Norton"),
            Map.entry("dc_method_source", "Transformação de Fontes"),

            // --- Pestaña Procesos ---
            Map.entry("proc_algorithm", "Algoritmo de Agendamento"),
            Map.entry("proc_batch_type", "Tipo de Lote"),
            Map.entry("proc_batch_config", "Configuração do Lote"),
            Map.entry("proc_execution_control", "Controle de Execução"),
            Map.entry("proc_select_algorithm", "Selecione o algoritmo:"),
            Map.entry("proc_algo_fcfs", "First-Come, First-Served (FCFS)"),
            Map.entry("proc_algo_rr", "Round Robin (RR)"),
            Map.entry("proc_algo_sjf", "Shortest Job First (SJF)"),
            Map.entry("proc_batch_config_label", "Configuração do lote:"),
            Map.entry("proc_batch_simple", "Homogêneo - Simples"),
            Map.entry("proc_batch_medium", "Homogêneo - Médio"),
            Map.entry("proc_batch_complex", "Homogêneo - Complexo"),
            Map.entry("proc_batch_mixed", "Heterogêneo - Misto"),
            Map.entry("proc_simple", "Simples:"),
            Map.entry("proc_medium", "Médios:"),
            Map.entry("proc_complex", "Complexos:"),
            Map.entry("proc_generate_batch", "Gerar Lote de Simulações"),
            Map.entry("proc_start_button", "Iniciar Agendamento"),
            Map.entry("proc_stop_button", "Parar"),

            // --- Panel RLC (Derecha) ---
            Map.entry("rlc_diagram_title", "Diagrama do Circuito"),
            Map.entry("rlc_tab_visualization", "Visualização"),
            Map.entry("rlc_tab_results", "Resultados"),
            Map.entry("rlc_tab_analysis", "Análise"),
            Map.entry("rlc_graph_type", "Tipo de Gráfico:"),
            Map.entry("rlc_graph_time", "Domínio do Tempo"),
            Map.entry("rlc_graph_frequency", "Resposta em Frequência"),
            Map.entry("rlc_graph_phasor", "Diagrama Fasorial"),
            Map.entry("rlc_graph_waveforms", "Formas de Onda"),
            Map.entry("rlc_results_placeholder_title", "=== Simulador Avançado de Circuitos RLC ==="),
            Map.entry("rlc_results_placeholder_inst", "Instruções:"),
            Map.entry("rlc_analysis_placeholder_title", "=== ANÁLISE DETALHADA DO CIRCUITO ==="),

            // --- Panel DC (Derecha) ---
            Map.entry("dc_panel_title", "Simulador de Circuitos DC - Análise Resistiva"),
            Map.entry("dc_diagram_title", "Diagrama do Circuito DC"),
            Map.entry("dc_tab_main_results", "Resultados Principais"),
            Map.entry("dc_tab_equivalents", "Circuitos Equivalentes"),
            Map.entry("dc_tab_detailed_analysis", "Análise Detalhada"),
            Map.entry("dc_analysis_placeholder_title", "=== ANÁLISE DETALHADA DC ==="),

            // --- Panel Memoria (Derecha) ---
            Map.entry("mem_panel_title", "Memória Virtual - Simulação de Processos"),
            Map.entry("mem_system_status", "Estado do Sistema"),
            Map.entry("mem_total_frag", "Fragmentação Total:"),
            Map.entry("mem_current_usage", "Uso atual de Memória:"),
            Map.entry("mem_avg_usage", "Uso médio:"),
            Map.entry("mem_ext_frag", "Fragmentação Externa:"),
            Map.entry("mem_int_frag", "Fragmentação Interna:"),
            Map.entry("mem_semaphore", "Semáforo 'Recurso Compartilhado':"),
            Map.entry("mem_map_title", "Mapa de Memória"),
            Map.entry("mem_current_process", "Processo em Execução"),
            Map.entry("mem_queues_title", "Filas de Processos"),
            Map.entry("mem_none", "Nenhum"),
            Map.entry("mem_empty", "Vazia"),
            Map.entry("mem_queue_ready", "=== FILA DE PRONTOS ==="),
            Map.entry("mem_queue_running", "=== EM EXECUÇÃO ==="),
            Map.entry("mem_queue_completed", "=== CONCLUÍDOS ==="),

            // --- Mensajes y Errores ---
            Map.entry("info", "Informação"),
            Map.entry("error", "Erro"),
            Map.entry("dc_value_positive", "O valor da resistência deve ser positivo"),
            Map.entry("dc_branch_error", "O ramo de destino não existe. Ajuste o 'Número de Ramos' primeiro."),
            Map.entry("dc_numeric_error", "Insira valores numéricos válidos para o componente DC"),
            Map.entry("dc_add_error", "Erro ao adicionar componente DC: %s"),
            Map.entry("dc_add_success", "Componente DC adicionado ao Ramo %d"),
            Map.entry("dc_invalid_circuit", "Circuito DC inválido. Adicione componentes e configure o circuito."),
            Map.entry("dc_sim_error", "Erro na simulação DC: %s"),
            Map.entry("dc_sim_success", "Simulação DC concluída usando %s"),
            Map.entry("dc_cleared", "Circuito DC limpo"),
            Map.entry("rlc_value_positive", "O valor do componente deve ser positivo"),
            Map.entry("rlc_select_to_remove", "Selecione um componente para remover"),
            Map.entry("rlc_add_one_component", "Adicione pelo menos um componente ao circuito"),
            Map.entry("rlc_voltage_range", "A tensão deve estar entre 0.1 e 1000 V"),
            Map.entry("rlc_frequency_range", "A frequência deve estar entre 0.1 e 10000 Hz"),
            Map.entry("rlc_numeric_error", "Insira valores numéricos válidos para tensão e frequência"),
            Map.entry("rlc_cleared", "Circuito limpo. Pronto para nova simulação."),
            Map.entry("rlc_cleared_all", "Circuito e resultados limpos"),
            Map.entry("rlc_preset_loaded", "Circuito predefinido '%s' carregado"),
            Map.entry("proc_batch_generated", "Lote gerado: %d tarefas"),
            Map.entry("proc_start_log", "Iniciando agendamento com %s"),
            Map.entry("proc_stop_log", "Agendamento parado"),
            Map.entry("proc_start_error", "Erro ao iniciar agendamento: %s"),
            Map.entry("sim_in_progress", "Simulação em andamento..."),
            Map.entry("sim_complete", "Simulação concluída com sucesso"),
            Map.entry("sim_error_generic", "Erro na simulação: %s"),
            Map.entry("sim_error_details", "Erro na simulação. Por favor, verifique os parâmetros e tente novamente.\n\nDetalhes do erro: %s")
        ))
    );

    
    private LanguageManager() {
        translations = new HashMap<>();
        currentLanguage = "es";
        loadTranslations();
    }
    
    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }
    
    @SuppressWarnings("unchecked")
    private void loadTranslations() {
        // Cargar traducciones desde el JSON interno
        for (String lang : LANGUAGE_JSON.keySet()) {
            Map<String, Object> langData = (Map<String, Object>) LANGUAGE_JSON.get(lang);
            Map<String, String> langTranslations = new HashMap<>();
            
            for (Map.Entry<String, Object> entry : langData.entrySet()) {
                langTranslations.put(entry.getKey(), entry.getValue().toString());
            }
            
            translations.put(lang, langTranslations);
        }
    }
    
    /**
     * Obtiene una traducción para la clave especificada
     */
    public String getTranslation(String key) {
        Map<String, String> langTranslations = translations.get(currentLanguage);
        if (langTranslations != null && langTranslations.containsKey(key)) {
            return langTranslations.get(key);
        }
        
        // Fallback a español si no encuentra la clave
        Map<String, String> fallback = translations.get("es");
        return fallback.getOrDefault(key, key);
    }
    
    /**
     * Obtiene una traducción formateada con parámetros
     */
    public String getFormattedTranslation(String key, Object... args) {
        String template = getTranslation(key);
        return String.format(template, args);
    }
    
    /**
     * Cambia el idioma actual
     */
    public void setLanguage(String language) {
        if (translations.containsKey(language)) {
            this.currentLanguage = language;
        } else {
            // Fallback a español
            this.currentLanguage = "es";
        }
    }
    
    /**
     * Obtiene el idioma actual
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    /**
     * Obtiene los idiomas disponibles
     */
    public String[] getAvailableLanguages() {
        return new String[]{"es", "pt"};
    }
    
    /**
     * Obtiene los nombres de los idiomas para mostrar en UI
     */
    public String[] getAvailableLanguageNames() {
        return new String[]{"Español", "Português"};
    }
    
    /**
     * Actualiza todos los textos de un combo box según el idioma actual
     */
    public void updateComboBox(JComboBox<String> comboBox, String[] keys) {
        int selectedIndex = comboBox.getSelectedIndex(); // Guardar selección
        comboBox.removeAllItems();
        for (String key : keys) {
            comboBox.addItem(getTranslation(key));
        }
        if (selectedIndex != -1 && selectedIndex < comboBox.getItemCount()) {
            comboBox.setSelectedIndex(selectedIndex); // Restaurar selección
        }
    }
    
    /**
     * Actualiza el texto de un componente Swing
     */
    public void updateComponentText(JComponent component, String key) {
        if (component instanceof JLabel) {
            ((JLabel) component).setText(getTranslation(key));
        } else if (component instanceof JButton) {
            ((JButton) component).setText(getTranslation(key));
        } else if (component instanceof JCheckBox) {
            ((JCheckBox) component).setText(getTranslation(key));
        } else if (component instanceof JRadioButton) {
            ((JRadioButton) component).setText(getTranslation(key));
        } else if (component instanceof JTabbedPane) {
            // Para JTabbedPane necesitamos manejo especial
            System.out.println("JTabbedPane requiere actualización manual por índice");
        }
    }
    
    /**
     * Actualiza el tooltip de un componente
     */
    public void updateToolTipText(JComponent component, String key) {
        component.setToolTipText(getTranslation(key));
    }
}