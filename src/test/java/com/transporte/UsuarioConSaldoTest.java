package com.transporte;

import static org.junit.jupiter.api.Assertions.*;

import com.transporte.controller.LoginController;
import com.transporte.model.Administrador;
import com.transporte.model.CargaDatosLogin;
import com.transporte.model.Empleado;
import com.transporte.model.Estudiante;
import com.transporte.model.Usuario;
import com.transporte.model.UsuarioConSaldo;
import com.transporte.model.UsuarioConductor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UsuarioConSaldoTest {

    @BeforeEach
    void limpiar() {
        CargaDatosLogin.reiniciar();
    }

    @Test
    void estudianteYEmpleadoEmpiezanConSaldoCero() {
        assertEquals(0f, new Estudiante("Ana", "Lopez", "V-1", "ana@ucv.ve", "x").getSaldo());
        assertEquals(0f, new Empleado("Luis", "Perez", "V-2", "luis@ucv.ve", "x").getSaldo());
    }

    @Test
    void estudianteYEmpleadoConservanSuRol() {
        UsuarioConSaldo estudiante = new Estudiante("Ana", "Lopez", "V-1", "ana@ucv.ve", "x");
        UsuarioConSaldo empleado = new Empleado("Luis", "Perez", "V-2", "luis@ucv.ve", "x");

        assertEquals("Estudiante", estudiante.getRol());
        assertEquals("Empleado", empleado.getRol());
        assertEquals("Ana", estudiante.getNombre());
    }

    @Test
    void administradorYConductorNoManejanSaldo() {
        Usuario admin = new Administrador("Root", "Admin", "V-0", "admin@ucv.ve", "x");
        Usuario conductor = new UsuarioConductor("Pedro", "Ruiz", "V-3", "pedro@ucv.ve", "x");

        assertFalse(admin instanceof UsuarioConSaldo);
        assertFalse(conductor instanceof UsuarioConSaldo);
    }

    @Test
    void elLoginDevuelveUsuariosConSaldoParaEstudianteYEmpleado() {
        LoginController login = new LoginController();
        login.registrarUsuario("Ana", "Lopez", "V-1", "ana@ucv.ve", "x", "Estudiante");
        login.registrarUsuario("Luis", "Perez", "V-2", "luis@ucv.ve", "x", "Empleado");
        login.registrarUsuario("Pedro", "Ruiz", "V-3", "pedro@ucv.ve", "x", "Conductor");

        assertTrue(login.iniciarSesion("ana@ucv.ve", "x") instanceof UsuarioConSaldo);
        assertTrue(login.iniciarSesion("luis@ucv.ve", "x") instanceof UsuarioConSaldo);
        assertFalse(login.iniciarSesion("pedro@ucv.ve", "x") instanceof UsuarioConSaldo);
    }
}
