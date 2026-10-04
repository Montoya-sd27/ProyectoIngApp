package com.transporte.model;


public class Empleado extends Usuario {
    public Empleado(String nombre, String apellido, String cedula, String correo, String contrasena) {
        super(nombre, apellido, cedula, correo, contrasena, "Empleado");
    }
}