package com.transporte.controller;

import com.transporte.model.Usuario;
import com.transporte.model.UsuarioFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * CONTROLADOR (MVC) del backoffice de usuarios: gestiona la creación y
 * visualización de usuarios desde la vista de administración.
 */
public class UsuarioAdminController {

    public UsuarioAdminController(Usuario solicitante) {
        // Constructor para inicializar el controlador desde la navegación
    }

    public List<String> rolesDisponibles() {
        List<String> roles = new ArrayList<>();
        roles.add("Pasajero");
        roles.add("Estudiante");
        roles.add("Empleado");
        roles.add("Conductor");
        roles.add("Administrador");
        return roles;
    }

    public void registrar(String nombre, String apellido, String cedula, String correo, String clave, String rol) {
        UsuarioFactory.crear(rol, nombre, apellido, cedula, correo, clave, 0);
    }

    public List<Usuario> listar() {
        return new ArrayList<>();
    }
}