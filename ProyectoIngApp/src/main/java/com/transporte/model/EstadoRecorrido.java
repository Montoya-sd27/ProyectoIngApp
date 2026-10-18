package com.transporte.model;

/* Estados posibles de un itinerario. */
public enum EstadoRecorrido {
    PROGRAMADO("Programado"),
    EN_CURSO("En curso"),
    DETENIDO("Detenido"),
    FINALIZADO("Finalizado");

    private final String etiqueta;

    EstadoRecorrido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /* Un itinerario solo se puede modificar, eliminar si no está en curso ni finalizado. */
    public boolean esModificable() {
        return this == PROGRAMADO || this == DETENIDO;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
