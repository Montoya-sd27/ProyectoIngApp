package com.transporte.view;

import com.transporte.controller.RecargaController;
import com.transporte.controller.RutasDisponiblesController;
import com.transporte.controller.TarifaConfiguracionController;
import com.transporte.model.UsuarioConSaldo;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

/**
 * Ventana de inicio del estudiante y del empleado (tienen la misma estructura). El contenido
 * está en {@link PanelInicioUsuario}; aquí solo se pone el encabezado con "Cerrar sesión".
 */
public class VentanaInicioUsuario extends JFrame {

    public VentanaInicioUsuario(UsuarioConSaldo usuario, RutasDisponiblesController rutas, RecargaController recarga,
                                TarifaConfiguracionController tarifas, Runnable onCerrar) {
        super("Ruta Central - Inicio");
        setSize(480, 700);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // el botón "Cerrar sesión" del encabezado y la X de la ventana terminan en windowClosed
        add(EstiloUI.encabezado(this::dispose), BorderLayout.NORTH);
        add(new PanelInicioUsuario(usuario, rutas, recarga, tarifas), BorderLayout.CENTER);

        // tanto el botón como la X de la ventana regresan al inicio de sesión
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                onCerrar.run();
            }
        });
    }
}
