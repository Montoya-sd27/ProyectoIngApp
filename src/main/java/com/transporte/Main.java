 package com.transporte;

import javax.swing.SwingUtilities;

import com.transporte.app.AppNavegacion;
import com.transporte.controller.LoginController;
import com.transporte.model.Autobus;
import com.transporte.model.FlotaModel;

public class Main {

    
    private static final boolean CARGAR_DATOS_DEMO = false;

    public static void main(String[] args) {
        // Al crearse, AppNavegacion carga los datos guardados (usuarios, unidades y rutas).
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
        // Con los datos guardados en archivo, la demo solo agrega las unidades que todavía no existen.
        Autobus[] demo = {
            new Autobus("ABC123", "Encava", 40, "Operativo"),
            new Autobus("XYZ789", "Yutong", 45, "Operativo"),
            new Autobus("MNT456", "Bluebird", 35, "En Mantenimiento")
        };
        for (Autobus bus : demo) {
            if (!flota.existePlaca(bus.getPlaca())) flota.agregarAutobus(bus);
        }
    }
}
