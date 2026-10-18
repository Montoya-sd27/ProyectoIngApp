package com.transporte.model;

// usuarios que manejan saldo en bolívares (estudiante y empleado)
public abstract class UsuarioConSaldo extends Usuario {

    // saldo disponible en Bs; todo usuario nuevo empieza en 0
    private float saldo;

    public UsuarioConSaldo(String nombre, String apellido, String cedula, String correo, String contrasena, String rol) {
        this(nombre, apellido, cedula, correo, contrasena, rol, 0f);
    }

    // para recuperar a un usuario guardado con el saldo que tenía
    public UsuarioConSaldo(String nombre, String apellido, String cedula, String correo, String contrasena, String rol, float saldo) {
        super(nombre, apellido, cedula, correo, contrasena, rol);
        this.saldo = saldo;
    }

    public float getSaldo() { return saldo; }

    /** Suma un monto (en Bs, mayor que cero) al saldo. */
    public void recargar(float monto) {
        if (!(monto > 0)) {
            throw new IllegalArgumentException("El monto a recargar debe ser mayor que cero.");
        }
        saldo += monto;
    }
}
