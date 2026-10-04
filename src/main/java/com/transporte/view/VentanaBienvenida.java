package com.transporte.view;

import com.transporte.model.Usuario;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Pantalla pequeña que confirma con qué rol se inició sesión. Es un ejemplo para mostrar que el
 * login funciona con los roles que todavía no tienen pantallas propias (estudiante, empleado, conductor).
 */
public class VentanaBienvenida extends JFrame {

    public VentanaBienvenida(Usuario usuario, Runnable onCerrar) {
        super("Ruta Central - Sesión iniciada");
        setSize(480, 360);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(EstiloUI.encabezado(null), BorderLayout.NORTH);   // sin botón de cerrar sesión arriba

        JLabel saludo = new JLabel("¡Bienvenido, " + usuario.getNombre() + "!", SwingConstants.CENTER);
        saludo.setFont(EstiloUI.FUENTE_TITULO);
        saludo.setForeground(EstiloUI.AZUL);
        saludo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel rol = new JLabel("Iniciaste sesión como " + describirRol(usuario.getRol()), SwingConstants.CENTER);
        rol.setFont(EstiloUI.FUENTE.deriveFont(Font.BOLD, 16f));
        rol.setForeground(EstiloUI.AZUL);
        rol.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nota = new JLabel("<html><div style='text-align:center; width:320px'>"
                + "Esta pantalla es un ejemplo para comprobar que el inicio de sesión funciona. "
                + "Las opciones de este rol estarán disponibles próximamente.</div></html>", SwingConstants.CENTER);
        nota.setFont(EstiloUI.FUENTE);
        nota.setAlignmentX(Component.CENTER_ALIGNMENT);

        EstiloUI.BotonRedondeado cerrar = EstiloUI.botonRojo("Cerrar sesión");
        cerrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        cerrar.addActionListener(e -> dispose());   // al cerrarse se vuelve al login (ver windowClosed)

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(EstiloUI.BLANCO);
        tarjeta.setBorder(new EmptyBorder(24, 24, 24, 24));
        tarjeta.add(saludo);
        tarjeta.add(Box.createVerticalStrut(14));
        tarjeta.add(rol);
        tarjeta.add(Box.createVerticalStrut(14));
        tarjeta.add(nota);
        tarjeta.add(Box.createVerticalStrut(22));
        tarjeta.add(cerrar);

        JPanel fondo = new JPanel(new BorderLayout());
        fondo.setBackground(EstiloUI.GRIS);
        fondo.setBorder(new EmptyBorder(20, 30, 20, 30));
        fondo.add(tarjeta, BorderLayout.CENTER);
        add(fondo, BorderLayout.CENTER);

        // tanto el botón como la X de la ventana terminan aquí: se regresa al inicio de sesión
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                onCerrar.run();
            }
        });
    }

    /** El conductor se muestra también como "chofer". */
    private static String describirRol(String rol) {
        return "Conductor".equals(rol) ? "Conductor (chofer)" : rol;
    }
}
