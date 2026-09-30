package model;

public class Unidad {
    private String placa;
    private String modelo;
    private int capacidadPasajeros;
    private String estadoOperativo;

    public Unidad(String placa, String modelo, int capacidadPasajeros, String estadoOperativo) {
        this.placa = placa;
        this.modelo = modelo;
        this.capacidadPasajeros = capacidadPasajeros;
        this.estadoOperativo = estadoOperativo;
    }

    public String getPlaca() { return placa; }
    public String getEstadoOperativo() { return estadoOperativo; }

    @Override
    public String toString() {
        return "Unidad " + placa + " (" + modelo + ")";
    }
}