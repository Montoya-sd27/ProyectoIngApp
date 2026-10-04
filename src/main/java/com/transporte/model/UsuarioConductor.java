package com.transporte.model;

public class UsuarioConductor extends Usuario {
    public UsuarioConductor(String nombre, String apellido, String cedula, String correo, String contrasena) {
        super(nombre, apellido, cedula, correo, contrasena, "Conductor");
    }
}