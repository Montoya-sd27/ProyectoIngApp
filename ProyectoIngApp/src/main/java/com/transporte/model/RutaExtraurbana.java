package com.transporte.model;

public class RutaExtraurbana extends Ruta {
    private final String ciudadDormitorio;

    public RutaExtraurbana(String idRuta, String origen, String destino, String ciudadDormitorio) {
        super(idRuta, origen, destino);
        this.ciudadDormitorio = ciudadDormitorio;
    }

    public String getCiudadDormitorio() { return ciudadDormitorio; }

    @Override public String getTipo() { return "Extraurbana"; }

    @Override public String getDetalle() { return ciudadDormitorio; }
}
