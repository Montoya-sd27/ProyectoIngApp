package com.transporte.model;

// usuarios que manejan saldo en bolívares (estudiante y empleado)
public abstract class UsuarioConSaldo extends Usuario {

    // saldo disponible en Bs; todo usuario nuevo empieza en 0
    private float saldo = 0f;

    public UsuarioConSaldo(String nombre, String apellido, String cedula, String correo, String contrasena, String rol) {
        super(nombre, apellido, cedula, correo, contrasena, rol);
    }

    public float getSaldo() { return saldo; }
}
