package model;

public class Conductor {
    private String cedula;
    private String nombre;
    private String licencia;

    public Conductor(String cedula, String nombre, String licencia) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.licencia = licencia;
    }

    public String getNombre() { return nombre; }
    public String getCedula() { return cedula; }

    @Override
    public String toString() {
        return nombre + " (C.I. " + cedula + ")";
    }
}