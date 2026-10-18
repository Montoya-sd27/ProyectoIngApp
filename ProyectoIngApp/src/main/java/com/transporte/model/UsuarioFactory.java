package com.transporte.model;

/** PATRÓN FACTORY METHOD: centraliza la creación de usuarios según su rol (igual que RutaFactory). */
public final class UsuarioFactory {

    private UsuarioFactory() { }

    /** Devuelve el usuario del rol indicado, o null si el rol no existe. */
    public static Usuario crear(String rol, String nombre, String apellido, String cedula,
                                String correo, String contrasena) {
        if (rol == null) return null;
        switch (rol) {
            case "Estudiante":    return new Estudiante(nombre, apellido, cedula, correo, contrasena);
            case "Empleado":      return new Empleado(nombre, apellido, cedula, correo, contrasena);
            case "Conductor":     return new UsuarioConductor(nombre, apellido, cedula, correo, contrasena);
            case "Administrador": return new Administrador(nombre, apellido, cedula, correo, contrasena);
            default:              return null;
        }
    }
        /** Igual que crear(...), pero para recuperar usuarios guardados: Estudiante y Empleado llevan su saldo. */
    public static Usuario crear(String rol, String nombre, String apellido, String cedula,
                                String correo, String contrasena, float saldo) {
        if (rol == null) return null;
        switch (rol) {
            case "Estudiante": return new Estudiante(nombre, apellido, cedula, correo, contrasena, saldo);
            case "Empleado":   return new Empleado(nombre, apellido, cedula, correo, contrasena, saldo);
            default:           return crear(rol, nombre, apellido, cedula, correo, contrasena);
        }
    }
}