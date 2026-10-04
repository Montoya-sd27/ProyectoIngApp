package com.transporte.model;

/**
 * Itinerario: asocia Ruta + Autobus + Conductor + Horario + EstadoRecorrido. se construye con patron builder valida la regla denegocio
 */
public final class Itinerario {
    private final String idItinerario;
    private final Ruta ruta;
    private final Autobus unidad;
    private final Conductor conductor;
    private final Horario horario;
    private final EstadoRecorrido estadoRecorrido;

    private Itinerario(Builder b) {
        this.idItinerario = b.id;
        this.ruta = b.ruta;
        this.unidad = b.unidad;
        this.conductor = b.conductor;
        this.horario = b.horario;
        this.estadoRecorrido = b.estado;
    }

    public String getIdItinerario() { return idItinerario; }
    public Ruta getRuta() { return ruta; }
    public Autobus getUnidad() { return unidad; }
    public Conductor getConductor() { return conductor; }
    public Horario getHorario() { return horario; }
    public EstadoRecorrido getEstadoRecorrido() { return estadoRecorrido; }

    public static class Builder {
        private String id;
        private Ruta ruta;
        private Autobus unidad;
        private Conductor conductor;
        private Horario horario;
        private EstadoRecorrido estado = EstadoRecorrido.PROGRAMADO;

        public Builder id(String id) { this.id = id; return this; }
        public Builder ruta(Ruta ruta) { this.ruta = ruta; return this; }
        public Builder unidad(Autobus unidad) { this.unidad = unidad; return this; }
        public Builder conductor(Conductor conductor) { this.conductor = conductor; return this; }
        public Builder horario(Horario horario) { this.horario = horario; return this; }
        public Builder estado(EstadoRecorrido estado) { this.estado = estado; return this; }

        public Itinerario build() throws ReglaNegocioException {
            if (ruta == null) throw new ReglaNegocioException("Debe seleccionar una ruta.");
            if (unidad == null) throw new ReglaNegocioException("Debe seleccionar una unidad.");
            if (conductor == null) throw new ReglaNegocioException("Debe seleccionar un conductor.");
            if (horario == null) throw new ReglaNegocioException("Debe indicar el horario.");
            if (estado == null) throw new ReglaNegocioException("Debe indicar el estado del recorrido.");
            return new Itinerario(this);
        }
    }
}
