 package com.transporte;

import javax.swing.SwingUtilities;

import com.transporte.app.AppNavegacion;
import com.transporte.controller.LoginController;
import com.transporte.model.Autobus;
import com.transporte.model.FlotaModel;

public class Main {

    
    private static final boolean CARGAR_DATOS_DEMO = false;

    public static void main(String[] args) {
        AppNavegacion nav = AppNavegacion.getInstance();

        // El administrador por defecto siempre tiene q existir.
        new LoginController().crearAdministradorPorDefecto();

        if (CARGAR_DATOS_DEMO) {
            cargarFlotaDemo(nav.getFlota());
        }

        // Pantalla de arranque.
        SwingUtilities.invokeLater(nav::mostrarLogin);
    }

    private static void cargarFlotaDemo(FlotaModel flota) {
        flota.agregarAutobus(new Autobus("ABC123", "Encava", 40, "Operativo"));
        flota.agregarAutobus(new Autobus("XYZ789", "Yutong", 45, "Operativo"));
        flota.agregarAutobus(new Autobus("MNT456", "Bluebird", 35, "En Mantenimiento"));
    }
}
