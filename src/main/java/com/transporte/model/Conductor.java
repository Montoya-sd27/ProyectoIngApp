package com.transporte.model;

public class Conductor {
    private final String cedula;
    private final String nombre;
    private final String licencia;

    public Conductor(String cedula, String nombre, String licencia) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.licencia = licencia;
    }

    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }
    public String getLicencia() { return licencia; }

    @Override
    public String toString() {
        return nombre + " (" + cedula + ")";
    }
}
