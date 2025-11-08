package com.simulador.utils;

import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 * Gestor especializado de idiomas para el simulador RLC
 * Maneja español y portugués con JSON interno
 */
public class LanguageManager {

    private static LanguageManager instance;
    private Map<String, Map<String, String>> translations;
    private String currentLanguage;

    private static final Map<String, Object> LANGUAGE_JSON = Map.ofEntries(
        Map.entry("es", Map.ofEntries(
            Map.entry("title", "Simulador de Circuitos RLC"),
            Map.entry("controls", "Controles de Entrada"),
            Map.entry("language", "Idioma:"),
            Map.entry("voltage", "Voltaje (V):"),
            Map.entry("frequency", "Frecuencia (Hz):"),
            Map.entry("method", "Método:"),
            Map.entry("preset", "Circuito Predefinido:"),
            Map.entry("component_type", "Tipo:"),
            Map.entry("value", "Valor:"),
            Map.entry("add_component", "Agregar Componente"),
            Map.entry("component_list", "Lista de Componentes:"),
            Map.entry("remove_selected", "Eliminar Seleccionado"),
            Map.entry("circuit_diagram", "Diagrama del Circuito"),
            Map.entry("results", "Resultados y Gráficos"),
            Map.entry("simulate", "Simular Circuito"),
            Map.entry("view_graphs", "Ver Gráficos"),
            Map.entry("clear_all", "Limpiar Todo"),
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
            Map.entry("simulation_results", "=== RESULTADOS DE SIMULACIÓN ==="),
            Map.entry("impedance", "• Impedancia:"),
            Map.entry("current", "• Corriente:"),
            Map.entry("phase_angle", "• Ángulo de Fase:"),
            Map.entry("active_power", "• Potencia Activa:"),
            Map.entry("reactive_power", "• Potencia Reactiva:"),
            Map.entry("apparent_power", "• Potencia Aparente:"),
            Map.entry("power_factor", "• Factor de Potencia:"),
            Map.entry("inductive_circuit", "→ Circuito INDUCTIVO (corriente atrasada)"),
            Map.entry("capacitive_circuit", "→ Circuito CAPACITIVO (corriente adelantada)"),
            Map.entry("resistive_circuit", "→ Circuito RESISTIVO (corriente en fase)"),
            Map.entry("simulation_in_progress", "Simulación en progreso..."),
            Map.entry("please_wait", "Por favor espere..."),
            Map.entry("empty_circuit", "Circuito Vacío"),
            Map.entry("add_components_start", "Agregue componentes para comenzar"),
            Map.entry("instructions", "Instrucciones:"),
            Map.entry("instruction1", "1. Agregue componentes (R, L, C) al circuito"),
            Map.entry("instruction2", "2. Configure voltaje y frecuencia"),
            Map.entry("instruction3", "3. Seleccione método de simulación"),
            Map.entry("instruction4", "4. Haga clic en 'Simular Circuito'"),
            Map.entry("features", "Características:"),
            Map.entry("feature1", "• Análisis en dominio de tiempo y frecuencia"),
            Map.entry("feature2", "• Diagramas fasoriales interactivos"),
            Map.entry("feature3", "• Múltiples métodos de cálculo"),
            Map.entry("feature4", "• Circuitos predefinidos"),
            Map.entry("feature5", "• Soporte multiidioma"),
            Map.entry("error", "Error"),
            Map.entry("information", "Información"),
            Map.entry("component_value_positive", "El valor del componente debe ser positivo"),
            Map.entry("select_component_remove", "Seleccione un componente para eliminar"),
            Map.entry("add_least_one_component", "Agregue al menos un componente al circuito"),
            Map.entry("voltage_range", "El voltaje debe estar entre 0.1 y 1000 V"),
            Map.entry("frequency_range", "La frecuencia debe estar entre 0.1 y 10000 Hz"),
            Map.entry("enter_numeric_values", "Ingrese valores numéricos válidos para voltaje y frecuencia"),
            Map.entry("circuit_cleared", "Circuito limpiado. Listo para nueva simulación."),
            Map.entry("circuit_results_cleared", "Circuito y resultados limpiados"),
            Map.entry("preset_loaded", "Circuito predefinido '%s' cargado"),
            Map.entry("simulation_error", "Error en simulación: %s"),
            // Nuevas traducciones para GraphWindow
            Map.entry("graph_window_title", "Gráficos del Circuito RLC - Simulador"),
            Map.entry("time_tab", "Tiempo"),
            Map.entry("frequency_tab", "Frecuencia"), 
            Map.entry("phasor_tab", "Fasorial"),
            Map.entry("waveforms_tab", "Ondas"),
            Map.entry("time_domain", "Dominio del Tiempo"),
            Map.entry("frequency_response", "Respuesta en Frecuencia"),
            Map.entry("phasor_diagram", "Diagrama Fasorial"),
            Map.entry("waveforms", "Formas de Onda"),
            Map.entry("refresh_graphs", "Actualizar Gráficos"),
            Map.entry("save_as_image", "Guardar como Imagen"),
            Map.entry("close", "Cerrar"),
            Map.entry("save_image_not_implemented", "Funcionalidad de guardar imagen no implementada en esta versión.\nUse la función de captura de pantalla de su sistema.")
        )),
        Map.entry("pt", Map.ofEntries(
            Map.entry("title", "Simulador de Circuitos RLC"),
            Map.entry("controls", "Controles de Entrada"),
            Map.entry("language", "Idioma:"),
            Map.entry("voltage", "Tensão (V):"),
            Map.entry("frequency", "Frequência (Hz):"),
            Map.entry("method", "Método:"),
            Map.entry("preset", "Circuito Predefinido:"),
            Map.entry("component_type", "Tipo:"),
            Map.entry("value", "Valor:"),
            Map.entry("add_component", "Adicionar Componente"),
            Map.entry("component_list", "Lista de Componentes:"),
            Map.entry("remove_selected", "Remover Selecionado"),
            Map.entry("circuit_diagram", "Diagrama do Circuito"),
            Map.entry("results", "Resultados e Gráficos"),
            Map.entry("simulate", "Simular Circuito"),
            Map.entry("view_graphs", "Ver Gráficos"),
            Map.entry("clear_all", "Limpar Tudo"),
            Map.entry("resistance", "Resistência"),
            Map.entry("inductor", "Indutor"),
            Map.entry("capacitor", "Capacitor"),
            Map.entry("custom", "Personalizado"),
            Map.entry("underdamped", "Subamortecido"),
            Map.entry("critical", "Crítico"),
            Map.entry("overdamped", "Superamortecido"),
            Map.entry("series_rlc", "RLC Série"),
            Map.entry("high_pass", "Filtro Passa Altas"),
            Map.entry("low_pass", "Filtro Passa Baixas"),
            Map.entry("analytical", "Analítico"),
            Map.entry("euler", "Euler"),
            Map.entry("runge_kutta", "Runge-Kutta4"),
            Map.entry("simulation_results", "=== RESULTADOS DA SIMULAÇÃO ==="),
            Map.entry("impedance", "• Impedância:"),
            Map.entry("current", "• Corrente:"),
            Map.entry("phase_angle", "• Ângulo de Fase:"),
            Map.entry("active_power", "• Potência Ativa:"),
            Map.entry("reactive_power", "• Potência Reativa:"),
            Map.entry("apparent_power", "• Potência Aparente:"),
            Map.entry("power_factor", "• Fator de Potência:"),
            Map.entry("inductive_circuit", "→ Circuito INDUTIVO (corrente atrasada)"),
            Map.entry("capacitive_circuit", "→ Circuito CAPACITIVO (corrente adiantada)"),
            Map.entry("resistive_circuit", "→ Circuito RESISTIVO (corrente em fase)"),
            Map.entry("simulation_in_progress", "Simulação em andamento..."),
            Map.entry("please_wait", "Por favor aguarde..."),
            Map.entry("empty_circuit", "Circuito Vazio"),
            Map.entry("add_components_start", "Adicione componentes para começar"),
            Map.entry("instructions", "Instruções:"),
            Map.entry("instruction1", "1. Adicione componentes (R, L, C) ao circuito"),
            Map.entry("instruction2", "2. Configure tensão e frequência"),
            Map.entry("instruction3", "3. Selecione o método de simulação"),
            Map.entry("instruction4", "4. Clique em 'Simular Circuito'"),
            Map.entry("features", "Características:"),
            Map.entry("feature1", "• Análise no domínio do tempo e frequência"),
            Map.entry("feature2", "• Diagramas fasoriais interativos"),
            Map.entry("feature3", "• Múltiplos métodos de cálculo"),
            Map.entry("feature4", "• Circuitos predefinidos"),
            Map.entry("feature5", "• Suporte multilíngue"),
            Map.entry("error", "Erro"),
            Map.entry("information", "Informação"),
            Map.entry("component_value_positive", "O valor do componente deve ser positivo"),
            Map.entry("select_component_remove", "Selecione um componente para remover"),
            Map.entry("add_least_one_component", "Adicione pelo menos um componente ao circuito"),
            Map.entry("voltage_range", "A tensão deve estar entre 0.1 e 1000 V"),
            Map.entry("frequency_range", "A frequência deve estar entre 0.1 e 10000 Hz"),
            Map.entry("enter_numeric_values", "Insira valores numéricos válidos para tensão e frequência"),
            Map.entry("circuit_cleared", "Circuito limpo. Pronto para nova simulação."),
            Map.entry("circuit_results_cleared", "Circuito e resultados limpos"),
            Map.entry("preset_loaded", "Circuito predefinido '%s' carregado"),
            Map.entry("simulation_error", "Erro na simulação: %s"),
            // Nuevas traducciones para GraphWindow
            Map.entry("graph_window_title", "Gráficos do Circuito RLC - Simulador"),
            Map.entry("time_tab", "Tempo"),
            Map.entry("frequency_tab", "Frequência"),
            Map.entry("phasor_tab", "Fasorial"), 
            Map.entry("waveforms_tab", "Ondas"),
            Map.entry("time_domain", "Domínio do Tempo"),
            Map.entry("frequency_response", "Resposta em Frequência"),
            Map.entry("phasor_diagram", "Diagrama Fasorial"),
            Map.entry("waveforms", "Formas de Onda"),
            Map.entry("refresh_graphs", "Atualizar Gráficos"),
            Map.entry("save_as_image", "Salvar como Imagem"),
            Map.entry("close", "Fechar"),
            Map.entry("save_image_not_implemented", "Funcionalidade de salvar imagem não implementada nesta versão.\nUse a função de captura de tela do seu sistema.")
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
        comboBox.removeAllItems();
        for (String key : keys) {
            comboBox.addItem(getTranslation(key));
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