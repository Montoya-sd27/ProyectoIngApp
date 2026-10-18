package com.transporte.repository;

import com.transporte.model.Itinerario;
import java.util.List;
import java.util.Optional;

/** PATRÓN REPOSITORY: abstrae el almacenamiento (hoy memoria; mañana BD o archivo). */
public interface ItinerarioRepositorio {
    String siguienteId();
    void guardar(Itinerario itinerario);     // inserta o reemplaza por id
    void eliminar(String id);
    Optional<Itinerario> buscarPorId(String id);
    List<Itinerario> listar();
    void agregarObservador(ItinerarioObserver observer);
}
