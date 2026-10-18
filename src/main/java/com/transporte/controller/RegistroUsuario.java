package com.transporte.controller;

import com.transporte.model.CargaDatosLogin;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.Usuario;
import java.util.List;

/**
 * PATRÓN TEMPLATE METHOD: registrar() fija los pasos del registro y las subclases solo cambian
 * lo que las diferencia (qué roles permiten y si piden confirmar la contraseña).
 * Al guardar, el Observer de CargaDatosLogin escribe usuarios.txt.
 */
public abstract class RegistroUsuario {

    private final LoginController login;

    protected RegistroUsuario(LoginController login) {
        this.login = login;
    }

    /** Método plantilla: el orden de los pasos no se puede cambiar en las subclases. */
    public final Usuario registrar(String nombre, String apellido, String cedula, String correo,
                                   String contrasena, String confirmacion, String rol) throws ReglaNegocioException {
        verificarSolicitante();
        verificarRol(rol);
        ValidadorUsuario.Datos datos = ValidadorUsuario.validar(nombre, apellido, cedula, correo, contrasena);
        verificarConfirmacion(datos.contrasena(), confirmacion);
        if (!login.registrarUsuario(datos.nombre(), datos.apellido(), datos.cedula(),
                datos.correo(), datos.contrasena(), rol)) {
            throw new ReglaNegocioException("El correo o la cédula ya están registrados.");
        }
        return CargaDatosLogin.buscarPorCorreo(datos.correo());
    }
    /** Gancho: por defecto cualquiera puede registrarse; el registro de admin exige que quien lo pide sea admin. */
    protected void verificarSolicitante() throws ReglaNegocioException { }

    /** Paso que cambia: roles que este tipo de registro permite crear. */
    public abstract List<String> rolesPermitidos();

    /** Gancho: por defecto no pide confirmar la contraseña. */
    protected void verificarConfirmacion(String contrasena, String confirmacion) throws ReglaNegocioException { }

    /** Gancho: mensaje cuando el rol no está permitido. */
    protected String mensajeRolNoPermitido(String rol) {
        return "Rol no permitido: " + rol + ". Solo se registran " + String.join(", ", rolesPermitidos()) + ".";
    }

    private void verificarRol(String rol) throws ReglaNegocioException {
        if (rol == null || !rolesPermitidos().contains(rol)) {
            throw new ReglaNegocioException(mensajeRolNoPermitido(rol));
        }
    }
}