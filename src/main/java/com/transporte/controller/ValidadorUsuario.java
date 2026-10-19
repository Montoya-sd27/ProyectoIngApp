package com.transporte.controller;

import com.transporte.model.ReglaNegocioException;

/**
PATRÓN STRATEGY : solo aplica la estrategia (ReglaValidacion) que corresponde a cada campo.
 * Ningún campo admite "|" porque es el separador de usuarios.txt.
 */
public final class ValidadorUsuario {

    // Límites de longitud para las validaciones del formulario
    public static final int NOMBRE_MIN = 3;
    public static final int NOMBRE_MAX = 40;
    public static final int CEDULA_MIN_DIGITOS = 6;
    public static final int CEDULA_MAX_DIGITOS = 8;
    public static final int CORREO_MAX = 254;
    public static final int CONTRASENA_MIN = 8;

    // Estrategias de validación reutilizables por tipo de dato
    private static final ReglaValidacion NOMBRE = new ReglasUsuario.Nombre("nombre");
    private static final ReglaValidacion APELLIDO = new ReglasUsuario.Nombre("apellido");
    private static final ReglaValidacion CEDULA = new ReglasUsuario.Cedula();
    private static final ReglaValidacion CORREO = new ReglasUsuario.Correo();

    private ValidadorUsuario() { }

    // DTO inmutable para agrupar los datos ya limpios y validados
    public record Datos(String nombre, String apellido, String cedula, String correo, String contrasena) { }

    // Valida que no haya campos vacíos y ejecuta las reglas de negocio antes de registrar
    public static Datos validar(String nombre, String apellido, String cedula,
                                String correo, String contrasena) throws ReglaNegocioException {
        String p = contrasena == null ? "" : contrasena;
        
        // Verificación rápida de campos obligatorios
        if (ReglasUsuario.limpiar(nombre).isEmpty() || ReglasUsuario.limpiar(apellido).isEmpty()
                || ReglasUsuario.limpiar(cedula).isEmpty() || ReglasUsuario.limpiar(correo).isEmpty() || p.isEmpty()) {
            throw new ReglaNegocioException("Por favor, llene todos los campos.");
        }
        
        // VALIDACIÓN DE MÍNIMO DE CARACTERES EN CONTRASEÑA
if (p.length() < CONTRASENA_MIN) {
    throw new ReglaNegocioException("La contraseña debe tener al menos " + CONTRASENA_MIN + " caracteres.");
}

        // Aplicamos cada estrategia para sanitizar y retornar los datos validados
        return new Datos(NOMBRE.validar(nombre), APELLIDO.validar(apellido),
                CEDULA.validar(cedula), CORREO.validar(correo), p);
    }
}