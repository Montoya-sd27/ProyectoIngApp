package com.transporte.controller;

import com.transporte.model.ReglaNegocioException;

/**
 * PATRÓN STRATEGY (contexto): solo aplica la estrategia (ReglaValidacion) que corresponde a cada campo.
 * Ningún campo admite "|" porque es el separador de usuarios.txt.
 */
public final class ValidadorUsuario {

    public static final int NOMBRE_MIN = 2;
    public static final int NOMBRE_MAX = 40;
    public static final int CEDULA_MIN_DIGITOS = 6;
    public static final int CEDULA_MAX_DIGITOS = 8;
    public static final int CORREO_MAX = 254;

    private static final ReglaValidacion NOMBRE = new ReglasUsuario.Nombre("nombre");
    private static final ReglaValidacion APELLIDO = new ReglasUsuario.Nombre("apellido");
    private static final ReglaValidacion CEDULA = new ReglasUsuario.Cedula();
    private static final ReglaValidacion CORREO = new ReglasUsuario.Correo();

    private ValidadorUsuario() { }

    public record Datos(String nombre, String apellido, String cedula, String correo, String contrasena) { }

    public static Datos validar(String nombre, String apellido, String cedula,
                                String correo, String contrasena) throws ReglaNegocioException {
        String p = contrasena == null ? "" : contrasena;
        if (ReglasUsuario.limpiar(nombre).isEmpty() || ReglasUsuario.limpiar(apellido).isEmpty()
                || ReglasUsuario.limpiar(cedula).isEmpty() || ReglasUsuario.limpiar(correo).isEmpty() || p.isEmpty()) {
            throw new ReglaNegocioException("Por favor, llene todos los campos.");
        }
        return new Datos(NOMBRE.validar(nombre), APELLIDO.validar(apellido),
                CEDULA.validar(cedula), CORREO.validar(correo), p);
    }
}