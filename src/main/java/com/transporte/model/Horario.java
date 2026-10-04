package com.transporte.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/* Objeto de valor inmutable: día de la semana + hora de salida y de llegada */
public final class Horario {
    private final DayOfWeek dia;
    private final LocalTime salida;
    private final LocalTime llegada;

    public Horario(DayOfWeek dia, LocalTime salida, LocalTime llegada) throws ReglaNegocioException {
        if (dia == null || salida == null || llegada == null) {
            throw new ReglaNegocioException("El horario requiere día, hora de salida y hora de llegada.");
        }
        if (!salida.isBefore(llegada)) {
            throw new ReglaNegocioException("La hora de salida debe ser anterior a la hora de llegada.");
        }
        this.dia = dia;
        this.salida = salida;
        this.llegada = llegada;
    }

    public DayOfWeek getDia() { return dia; }
    public LocalTime getSalida() { return salida; }
    public LocalTime getLlegada() { return llegada; }

    /* Dos horarios se solapan si son el mismo día y sus intervalos se cruzan. */
    public boolean seSolapaCon(Horario otro) {
        return dia == otro.dia && salida.isBefore(otro.llegada) && otro.salida.isBefore(llegada);
    }
}
