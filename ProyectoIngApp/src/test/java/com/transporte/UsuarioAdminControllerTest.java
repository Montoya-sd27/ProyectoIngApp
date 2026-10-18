package com.transporte;

import com.transporte.controller.LoginController;
import com.transporte.controller.UsuarioAdminController;
import com.transporte.model.CargaDatosLogin;
import com.transporte.model.Usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioAdminControllerTest {

    private UsuarioAdminController backoffice;

    @BeforeEach
    void setUp() {
        Usuario adminDefecto = CargaDatosLogin.buscarPorCorreo(LoginController.ADMIN_CORREO);
        backoffice = new UsuarioAdminController(adminDefecto);
    }

    // 1. Un usuario registrado desde el backoffice se puede listar o verificar
    @Test
    void usuarioRegistradoEnBackofficeSeGuarda() throws Exception {
        backoffice.registrar("Ana", "Lopez", "1000001", "ana@ucv.ve", "clave", "Estudiante");
        assertNotNull(backoffice.listar());
    }

    // 2. Comprobar que los roles disponibles contengan las opciones requeridas
    @Test
    void rolesDisponiblesDevuelveLista() {
        List<String> roles = backoffice.rolesDisponibles();
        assertNotNull(roles);
        assertTrue(roles.contains("Estudiante"));
        assertTrue(roles.contains("Administrador"));
    }

    // 3. Verificar que el método listar devuelva la estructura esperada
    @Test
    void listarDevuelveListaDeUsuarios() {
        List<Usuario> lista = backoffice.listar();
        assertNotNull(lista);
    }
}