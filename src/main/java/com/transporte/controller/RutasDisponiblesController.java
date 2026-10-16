package com.transporte.controller;

import com.transporte.model.Autobus;
import com.transporte.model.EstadoRecorrido;
import com.transporte.model.Itinerario;
import com.transporte.model.Ruta;
import com.transporte.model.RutaExtraurbana;
import com.transporte.model.RutaFactory.TipoRuta;
import com.transporte.repository.ItinerarioRepositorio;
import java.util.Comparator;
import java.util.List;

/** Rutas que el estudiante o el empleado puede ver para reservar un cupo. */
public class RutasDisponiblesController {

    private final ItinerarioRepositorio repositorio;

    public RutasDisponiblesController(ItinerarioRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /** Itinerarios programados del tipo indicado, ordenados por día y hora de salida. */
    public List<Itinerario> disponibles(TipoRuta tipo) {
        return repositorio.listar().stream()
                .filter(it -> it.getEstadoRecorrido() == EstadoRecorrido.PROGRAMADO)
                .filter(it -> tipoDe(it.getRuta()) == tipo)
                .sorted(Comparator
                        .comparing((Itinerario it) -> it.getHorario().getDia())
                        .thenComparing(it -> it.getHorario().getSalida()))
                .toList();
    }

    /**
     * Cupos que quedan en el itinerario: la capacidad de la unidad si está operativa y 0 si no lo está.
     * Todavía no se descuentan reservas porque aún no se pueden hacer.
     */
    public int cuposDisponibles(Itinerario itinerario) {
        Autobus unidad = itinerario.getUnidad();
        return ItinerarioController.esOperativa(unidad) ? unidad.getCapacidad() : 0;
    }

    private static TipoRuta tipoDe(Ruta ruta) {
        return ruta instanceof RutaExtraurbana ? TipoRuta.EXTRAURBANA : TipoRuta.URBANA;
    }
}
