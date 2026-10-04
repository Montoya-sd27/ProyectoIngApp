package com.transporte.model;

public class Estudiante extends Usuario {

    // le pasamos los datos al constructor de la clase usuario
    public Estudiante(String nombre, String apellido, String cedula, String correo, String contrasena) {
        super(nombre, apellido, cedula, correo, contrasena, "Estudiante"); // su rol siempre sera estudiante
    }
}