package com.transporte.view;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

// Paleta y utilidades de estilo (son las mismas para todas las vistas)
public final class EstiloUI {
    public static final Color AZUL = new Color(0x003366);
    public static final Color GRIS = new Color(0xE9E1E1);
    public static final Color BLANCO = new Color(0xFFFFFF);
    public static final Color ROJO = new Color(0xF89C9C);
    public static final Color VERDE = new Color(0x90F590);
    public static final Color AMARILLO = new Color(0xEAB915);

    public static final Font FUENTE = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.PLAIN, 22);

    private static final Locale ES = new Locale("es", "VE");

    private EstiloUI() { }

    public static String diaEs(DayOfWeek d) {
        String s = d.getDisplayName(TextStyle.FULL, ES);
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // Barra superior azul con el nombre de la app y botón "Cerrar sesión"
    public static JPanel encabezado(Runnable onCerrarSesion) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AZUL);
        p.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel titulo = new JLabel("Ruta Central");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("SansSerif", Font.PLAIN, 20));

        p.add(titulo, BorderLayout.WEST);
        if (onCerrarSesion != null) {
            BotonRedondeado cerrar = new BotonRedondeado("Cerrar sesión", AZUL, AMARILLO);
            cerrar.setForeground(AMARILLO);
            cerrar.addActionListener(e -> onCerrarSesion.run());
            p.add(cerrar, BorderLayout.EAST);
        }
        return p;
    }

    // Campo/combobox con fondo gris, como en el formulario de registro de unidad
    public static void estilizarCampo(JComponent c) {
        c.setBackground(GRIS);
        c.setFont(FUENTE);
        c.setBorder(new EmptyBorder(6, 8, 6, 8));
    }

    public static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(AZUL);
        l.setFont(FUENTE);
        return l;
    }

    public static void estilizarCombo(JComboBox<?> c) {
        c.setBackground(GRIS);
        c.setFont(FUENTE);
        c.setBorder(new LineBorder(GRIS, 4));
    }

    // Botón de esquinas redondeadas con borde del color indicado
    public static class BotonRedondeado extends JButton {
        private final Color fondo;
        private final Color borde;

        public BotonRedondeado(String texto, Color fondo, Color borde) {
            super(texto);
            this.fondo = fondo;
            this.borde = borde;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(Color.DARK_GRAY);
            setFont(new Font("SansSerif", Font.PLAIN, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(6, 18, 6, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? fondo.darker() : fondo);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.setColor(borde);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static BotonRedondeado botonVerde(String texto) {
        return new BotonRedondeado(texto, VERDE, VERDE.darker().darker());
    }

    // Botón azul con texto blanco (acciones de navegación)
    public static BotonRedondeado botonAzul(String texto) {
        BotonRedondeado b = new BotonRedondeado(texto, AZUL, AZUL);
        b.setForeground(Color.WHITE);
        return b;
    }

    // Botón amarillo (acciones secundarias)
    public static BotonRedondeado botonAmarillo(String texto) {
        return new BotonRedondeado(texto, AMARILLO, AMARILLO.darker().darker());
    }

    public static BotonRedondeado botonRojo(String texto) {
        return new BotonRedondeado(texto, ROJO, ROJO.darker().darker());
    }
}
