package com.transporte.model;

/** Clase base de las rutas (diagrama de clases: Rutas-RutaUrbana-RutaExtraurbana). */
public abstract class Ruta {
    private final String idRuta;
    private final String origen;
    private final String destino;

    protected Ruta(String idRuta, String origen, String destino) {
        this.idRuta = idRuta;
        this.origen = origen;
        this.destino = destino;
    }

    public String getIdRuta() { return idRuta; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }

    /** "Urbana" o "Extraurbana". */
    public abstract String getTipo();

    /** Información propia de cada tipo (paradas o ciudad dormitorio). */
    public abstract String getDetalle();

    @Override
    public String toString() {
        return idRuta + " · " + origen + " → " + destino;
    }
}
