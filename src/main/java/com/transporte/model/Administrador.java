package com.transporte.model;

public class Administrador extends Usuario {
    public Administrador(String nombre, String apellido, String cedula, String correo, String contrasena) {
        super(nombre, apellido, cedula, correo, contrasena, "Administrador");
    }
}