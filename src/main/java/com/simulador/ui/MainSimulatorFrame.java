package com.simulador.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Ventana principal con pestañas de navegación y menú de configuración
 * Main window with navigation tabs and configuration menu
 */
public class MainSimulatorFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private RLCSimulator physicsSimulator;
    private JPanel osSimulatorPanel;
    private JMenuBar menuBar;
    private JMenu fileMenu, settingsMenu, languageMenu, helpMenu;
    private Map<String, Float> fontSizeMap;
    private float currentFontSize;

    public MainSimulatorFrame() {
        initializeFontSizes();
        initializeUI();
        setupMenu();
    }

    private void initializeFontSizes() {
        fontSizeMap = new HashMap<>();
        fontSizeMap.put("Pequeño", 12f);
        fontSizeMap.put("Mediano", 14f);
        fontSizeMap.put("Grande", 16f);
        fontSizeMap.put("Muy Grande", 18f);
        currentFontSize = 14f; // Tamaño por defecto
    }

    private void initializeUI() {
        setTitle("Simulador Integrado - Sistema Operativo y Física");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1400, 900)); // Aumentado para nuevo diseño

        // Crear el panel de pestañas
        tabbedPane = new JTabbedPane();

        // Pestaña de Simulador de Sistema Operativo
        osSimulatorPanel = createOSSimulatorPanel();
        tabbedPane.addTab("Simulador de Sistema Operativo", osSimulatorPanel);

        // Pestaña de Simulador de Física (ahora con diseño de dos columnas)
        physicsSimulator = new RLCSimulator();
        tabbedPane.addTab("Simulador de Física", physicsSimulator);

        // Layout principal
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);

        // Configurar cierre seguro
        setupSafeClose();
    }

    private JPanel createOSSimulatorPanel() {
        return new OSSimulatorPanel();
    }

    private void setupMenu() {
        menuBar = new JMenuBar();

        // Menú Archivo
        fileMenu = new JMenu("Archivo");

        JMenuItem exitItem = new JMenuItem("Salir");
        exitItem.addActionListener(e -> closeApplication());
        fileMenu.add(exitItem);

        // Menú Configuración
        settingsMenu = new JMenu("Configuración");

        // Submenú Tamaño de Letra
        JMenu fontSizeMenu = new JMenu("Tamaño de Letra");

        // Opciones de tamaño de letra
        String[] sizes = { "Pequeño", "Mediano", "Grande", "Muy Grande" };
        ButtonGroup fontSizeGroup = new ButtonGroup();

        for (String size : sizes) {
            JRadioButtonMenuItem sizeItem = new JRadioButtonMenuItem(size);
            sizeItem.setSelected(size.equals("Mediano")); // Seleccionar mediano por defecto

            sizeItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    changeFontSize(fontSizeMap.get(size));
                }
            });

            fontSizeGroup.add(sizeItem);
            fontSizeMenu.add(sizeItem);
        }

        settingsMenu.add(fontSizeMenu);
        settingsMenu.addSeparator();

        // Opción para resetear configuración
        JMenuItem resetItem = new JMenuItem("Restablecer Configuración");
        resetItem.addActionListener(e -> resetSettings());
        settingsMenu.add(resetItem);

        // NUEVO: Menú Idioma en la barra superior
        languageMenu = new JMenu("Idioma");

        // Opciones de idioma
        JRadioButtonMenuItem spanishItem = new JRadioButtonMenuItem("Español");
        JRadioButtonMenuItem portugueseItem = new JRadioButtonMenuItem("Português");

        // Seleccionar español por defecto
        spanishItem.setSelected(true);

        ButtonGroup languageGroup = new ButtonGroup();
        languageGroup.add(spanishItem);
        languageGroup.add(portugueseItem);

        // Listeners para cambiar idioma
        spanishItem.addActionListener(e -> changeLanguage("es"));
        portugueseItem.addActionListener(e -> changeLanguage("pt"));

        languageMenu.add(spanishItem);
        languageMenu.add(portugueseItem);

        // Menú Ayuda
        helpMenu = new JMenu("Ayuda");

        JMenuItem aboutItem = new JMenuItem("Acerca de");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        // Agregar menús a la barra en el orden correcto
        menuBar.add(fileMenu);
        menuBar.add(settingsMenu);
        menuBar.add(languageMenu); // NUEVO: Menú de idioma en la barra
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    // NUEVO: Método para cambiar idioma desde el menú
    private void changeLanguage(String languageCode) {
        // Actualizar el simulador de física si está activo
        if (physicsSimulator != null) {
            physicsSimulator.changeLanguage(languageCode);
        }

        // Actualizar textos de los menús
        updateMenuTexts(languageCode);

        // Actualizar título de la ventana según el idioma
        updateWindowTitle(languageCode);
    }

    // NUEVO: Actualizar textos de los menús según el idioma
    private void updateMenuTexts(String languageCode) {
        if ("es".equals(languageCode)) {
            fileMenu.setText("Archivo");
            settingsMenu.setText("Configuración");
            languageMenu.setText("Idioma");
            helpMenu.setText("Ayuda");

            // Actualizar textos de los items del menú
            updateMenuItemsSpanish();
        } else if ("pt".equals(languageCode)) {
            fileMenu.setText("Arquivo");
            settingsMenu.setText("Configuração");
            languageMenu.setText("Idioma");
            helpMenu.setText("Ajuda");

            // Actualizar textos de los items del menú
            updateMenuItemsPortuguese();
        }
    }

    // NUEVO: Actualizar items del menú en español
    private void updateMenuItemsSpanish() {
        // Menú Archivo
        fileMenu.getItem(0).setText("Salir");

        // Menú Configuración
        settingsMenu.getItem(0).setText("Tamaño de Letra");
        settingsMenu.getItem(2).setText("Restablecer Configuración");

        // Menú Ayuda
        helpMenu.getItem(0).setText("Acerca de");
    }

    // NUEVO: Actualizar items del menú en portugués
    private void updateMenuItemsPortuguese() {
        // Menú Archivo
        fileMenu.getItem(0).setText("Sair");

        // Menú Configuración
        settingsMenu.getItem(0).setText("Tamanho da Fonte");
        settingsMenu.getItem(2).setText("Restabelecer Configuração");

        // Menú Ayuda
        helpMenu.getItem(0).setText("Sobre");
    }

    // NUEVO: Actualizar título de la ventana según idioma
    private void updateWindowTitle(String languageCode) {
        if ("es".equals(languageCode)) {
            setTitle("Simulador Integrado - Sistema Operativo y Física");
        } else if ("pt".equals(languageCode)) {
            setTitle("Simulador Integrado - Sistema Operativo e Física");
        }
    }

    private void changeFontSize(float newSize) {
        this.currentFontSize = newSize;
        applyFontSizeToAllComponents();
    }

    private void applyFontSizeToAllComponents() {
        // Aplicar tamaño de fuente a todos los componentes de la interfaz
        updateComponentFonts(getContentPane());

        // Aplicar también al simulador de física si está activo
        if (physicsSimulator != null) {
            physicsSimulator.updateFontSize(currentFontSize);
        }

        // Revalidar y repintar la interfaz
        revalidate();
        repaint();
    }

    private void updateComponentFonts(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JComponent) {
                JComponent jcomp = (JComponent) comp;

                // Actualizar fuente según el tipo de componente
                if (jcomp instanceof JLabel) {
                    JLabel label = (JLabel) jcomp;
                    Font currentFont = label.getFont();
                    label.setFont(currentFont.deriveFont(currentFontSize));
                } else if (jcomp instanceof JButton) {
                    JButton button = (JButton) jcomp;
                    Font currentFont = button.getFont();
                    button.setFont(currentFont.deriveFont(currentFontSize));
                } else if (jcomp instanceof JTextField) {
                    JTextField textField = (JTextField) jcomp;
                    Font currentFont = textField.getFont();
                    textField.setFont(currentFont.deriveFont(currentFontSize));
                } else if (jcomp instanceof JComboBox) {
                    JComboBox<?> combo = (JComboBox<?>) jcomp;
                    Font currentFont = combo.getFont();
                    combo.setFont(currentFont.deriveFont(currentFontSize));
                } else if (jcomp instanceof JTextArea) {
                    JTextArea textArea = (JTextArea) jcomp;
                    Font currentFont = textArea.getFont();
                    textArea.setFont(currentFont.deriveFont(currentFontSize));
                } else if (jcomp instanceof JMenuBar) {
                    JMenuBar menuBar = (JMenuBar) jcomp;
                    Font currentFont = menuBar.getFont();
                    menuBar.setFont(currentFont.deriveFont(currentFontSize));
                } else if (jcomp instanceof JMenu) {
                    JMenu menu = (JMenu) jcomp;
                    Font currentFont = menu.getFont();
                    menu.setFont(currentFont.deriveFont(currentFontSize));
                } else if (jcomp instanceof JMenuItem) {
                    JMenuItem menuItem = (JMenuItem) jcomp;
                    Font currentFont = menuItem.getFont();
                    menuItem.setFont(currentFont.deriveFont(currentFontSize));
                } else if (jcomp instanceof JTabbedPane) {
                    JTabbedPane tabs = (JTabbedPane) jcomp;
                    Font currentFont = tabs.getFont();
                    tabs.setFont(currentFont.deriveFont(currentFontSize));
                }
            }

            // Recursivamente actualizar componentes hijos
            if (comp instanceof Container) {
                updateComponentFonts((Container) comp);
            }
        }
    }

    private void resetSettings() {
        currentFontSize = 14f;
        applyFontSizeToAllComponents();

        JOptionPane.showMessageDialog(this,
                "Configuración restablecida a valores por defecto",
                "Configuración Restablecida",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Simulador Integrado v1.0\n\n" +
                        "• Simulador de Sistema Operativo (En desarrollo)\n" +
                        "• Simulador de Circuitos RLC\n" +
                        "• Soporte para ajuste de tamaño de letra\n" +
                        "• Interfaz multi-pestañas\n\n" +
                        "Desarrollado para accesibilidad y usabilidad",
                "Acerca del Simulador",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupSafeClose() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeApplication();
            }
        });
    }

    private void closeApplication() {
        // Limpiar recursos del simulador de física
        if (physicsSimulator != null) {
            physicsSimulator.disposeResources();
        }

        // Limpiar recursos del simulador de SO
        Component[] components = tabbedPane.getComponents();
        for (Component comp : components) {
            if (comp instanceof OSSimulatorPanel) {
                ((OSSimulatorPanel) comp).dispose();
            }
        }

        dispose();
        System.exit(0);
    }

    public static void main(String[] args) {
        // Configurar look and feel
        setupLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Iniciando Simulador Integrado...");
                MainSimulatorFrame mainFrame = new MainSimulatorFrame();
                mainFrame.setVisible(true);
                System.out.println("Simulador Integrado iniciado correctamente");
            } catch (Exception e) {
                handleStartupError(e);
            }
        });
    }

    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("Look and feel del sistema configurado correctamente");
        } catch (Exception e1) {
            System.err.println("Error configurando look and feel del sistema: " + e1.getMessage());
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                System.out.println("Look and feel Nimbus configurado como fallback");
            } catch (Exception e2) {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    System.out.println("Look and feel cross-platform configurado como fallback");
                } catch (Exception e3) {
                    System.err.println("No se pudo configurar ningún look and feel: " + e3.getMessage());
                }
            }
        }
    }

    private static void handleStartupError(Exception e) {
        System.err.println("Error crítico iniciando la aplicación: " + e.getMessage());
        e.printStackTrace();

        JOptionPane.showMessageDialog(null,
                "Error iniciando la aplicación:\n" + e.getMessage() +
                        "\n\nVer consola para más detalles.",
                "Error de Inicio",
                JOptionPane.ERROR_MESSAGE);
    }
}