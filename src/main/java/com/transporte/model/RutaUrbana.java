package com.transporte.model;

import java.util.List;

public class RutaUrbana extends Ruta {
    private final List<String> paradasIntermedias;

    public RutaUrbana(String idRuta, String origen, String destino, List<String> paradasIntermedias) {
        super(idRuta, origen, destino);
        this.paradasIntermedias = List.copyOf(paradasIntermedias);
    }

    public List<String> getParadasIntermedias() { return paradasIntermedias; }

    @Override public String getTipo() { return "Urbana"; }

    @Override public String getDetalle() { return String.join(", ", paradasIntermedias); }
}
