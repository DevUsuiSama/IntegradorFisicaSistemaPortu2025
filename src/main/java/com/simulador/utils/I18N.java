package com.simulador.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Sistema de internacionalización para múltiples idiomas
 * Internationalization system for multiple languages
 */
public class I18N {
    private static final Map<String, Map<String, String>> translations = new HashMap<>();
    private static String currentLang = "es";
    
    static {
        initializeTranslations();
    }
    
    private static void initializeTranslations() {
        // Español
        Map<String, String> es = new HashMap<>();
        es.put("title", "Simulador de Circuitos RLC");
        es.put("resistance", "Resistencia");
        es.put("inductor", "Inductor");
        es.put("capacitor", "Capacitor");
        // ... (solo las traducciones esenciales para que compile)
        translations.put("es", es);
        
        // Português
        Map<String, String> pt = new HashMap<>();
        pt.put("resistance", "Resistencia");
        pt.put("inductor", "Indutor");
        pt.put("capacitor", "Capacitor");
        translations.put("pt", pt);
        
        // English
        Map<String, String> en = new HashMap<>();
        en.put("resistance", "Resistance");
        en.put("inductor", "Inductor");
        en.put("capacitor", "Capacitor");
        translations.put("en", en);
    }
    
    public static String get(String key) {
        Map<String, String> langMap = translations.get(currentLang);
        if (langMap == null) {
            langMap = translations.get("es");
        }
        return langMap.getOrDefault(key, key);
    }
    
    public static void setLanguage(String lang) {
        if (translations.containsKey(lang)) {
            currentLang = lang;
        }
    }
}