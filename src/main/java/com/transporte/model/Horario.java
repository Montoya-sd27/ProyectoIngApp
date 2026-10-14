package com.transporte.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

/*
e.
 */
public final class Horario {

    /** Un recorrido que cruza la medianoche no puede durar más que esto (evita confundirlo con un error al escribir las horas). */
    public static final Duration DURACION_MAXIMA_NOCTURNA = Duration.ofHours(12);

    private static final long SEGUNDOS_DIA = 24L * 60 * 60;
    private static final long SEGUNDOS_SEMANA = 7 * SEGUNDOS_DIA;

    private final DayOfWeek dia;
    private final LocalTime salida;
    private final LocalTime llegada;

    public Horario(DayOfWeek dia, LocalTime salida, LocalTime llegada) throws ReglaNegocioException {
        if (dia == null || salida == null || llegada == null) {
            throw new ReglaNegocioException("El horario requiere día, hora de salida y hora de llegada.");
        }
        if (salida.equals(llegada)) {
            throw new ReglaNegocioException("La hora de salida y la hora de llegada no pueden ser iguales.");
        }
        if (llegada.isBefore(salida) && duracionEntre(salida, llegada).compareTo(DURACION_MAXIMA_NOCTURNA) > 0) {
            throw new ReglaNegocioException("La hora de salida debe ser anterior a la hora de llegada. "
                    + "Si el recorrido termina después de la medianoche, debe durar como máximo "
                    + DURACION_MAXIMA_NOCTURNA.toHours() + " horas.");
        }
        this.dia = dia;
        this.salida = salida;
        this.llegada = llegada;
    }

    public DayOfWeek getDia() { return dia; }
    public LocalTime getSalida() { return salida; }
    public LocalTime getLlegada() { return llegada; }

    /** true si el recorrido sale un día y llega al día siguiente (por ejemplo 22:00 → 00:00). */
    public boolean cruzaMedianoche() {
        return llegada.isBefore(salida);
    }

    public Duration getDuracion() {
        return duracionEntre(salida, llegada);
    }

    /* Dos horarios se solapan si sus intervalos se cruzan, teniendo en cuenta los que pasan a la madrugada siguiente. */
    public boolean seSolapaCon(Horario otro) {
        long inicio = inicioEnSemana();
        long fin = inicio + getDuracion().getSeconds();
        long otroInicio = otro.inicioEnSemana();
        long otroFin = otroInicio + otro.getDuracion().getSeconds();
        // la semana es circular: un recorrido del domingo por la noche puede chocar con uno del lunes de madrugada
        for (int vuelta = -1; vuelta <= 1; vuelta++) {
            long desplazamiento = vuelta * SEGUNDOS_SEMANA;
            if (inicio < otroFin + desplazamiento && otroInicio + desplazamiento < fin) return true;
        }
        return false;
    }

    private long inicioEnSemana() {
        return (dia.getValue() - 1) * SEGUNDOS_DIA + salida.toSecondOfDay();
    }

    private static Duration duracionEntre(LocalTime salida, LocalTime llegada) {
        long segundos = llegada.toSecondOfDay() - salida.toSecondOfDay();
        if (segundos < 0) segundos += SEGUNDOS_DIA;
        return Duration.ofSeconds(segundos);
    }
}