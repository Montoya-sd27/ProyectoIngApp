package com.transporte.controller;

import com.transporte.model.Autobus;
import com.transporte.model.EstadoRecorrido;
import com.transporte.model.Itinerario;
import com.transporte.model.ReglaNegocioException;
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
     * Cupos que quedan en el itinerario: la capacidad de la unidad menos los cupos ya reservados,
     * o 0 si la unidad no está operativa.
     */
    public int cuposDisponibles(Itinerario itinerario) {
        Autobus unidad = itinerario.getUnidad();
        if (!ItinerarioController.esOperativa(unidad)) return 0;
        return Math.max(0, unidad.getCapacidad() - itinerario.getCuposReservados());
    }

    public synchronized void reservarCupo(Itinerario itinerario) throws ReglaNegocioException {
        if (cuposDisponibles(itinerario) <= 0) {
            throw new ReglaNegocioException("No quedan cupos disponibles en esta ruta.");
        }
        itinerario.reservarCupo();
        repositorio.guardar(itinerario);
    }

    private static TipoRuta tipoDe(Ruta ruta) {
        return ruta instanceof RutaExtraurbana ? TipoRuta.EXTRAURBANA : TipoRuta.URBANA;
    }
}
