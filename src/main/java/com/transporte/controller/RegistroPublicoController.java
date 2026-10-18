package com.transporte.controller;

import com.transporte.model.ReglaNegocioException;
import com.transporte.model.Usuario;
import java.util.List;

/** Controlador del registro desde el login: delega en RegistroPublico. */
public class RegistroPublicoController {

    private final RegistroUsuario registro;

    public RegistroPublicoController() {
        this(new LoginController());
    }

    public RegistroPublicoController(LoginController login) {
        this.registro = new RegistroPublico(login);
    }

    public List<String> rolesDisponibles() {
        return registro.rolesPermitidos();
    }

    public Usuario registrar(String nombre, String apellido, String cedula, String correo,
                             String contrasena, String confirmacion, String rol) throws ReglaNegocioException {
        return registro.registrar(nombre, apellido, cedula, correo, contrasena, confirmacion, rol);
    }
}