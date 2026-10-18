package com.transporte.model;


public class Empleado extends UsuarioConSaldo {
    public Empleado(String nombre, String apellido, String cedula, String correo, String contrasena) {
        super(nombre, apellido, cedula, correo, contrasena, "Empleado");
    }

    // para recuperar a un empleado guardado con su saldo
    public Empleado(String nombre, String apellido, String cedula, String correo, String contrasena, float saldo) {
        super(nombre, apellido, cedula, correo, contrasena, "Empleado", saldo);
    }
}
