package com.transporte.controller;

import com.transporte.model.*;
import com.transporte.repository.Catalogo;
import com.transporte.repository.ItinerarioObserver;
import com.transporte.repository.ItinerarioRepositorio;
import java.util.List;

/**
 * CONTROLADOR (MVC): recibe las acciones de la vista, aplica las reglas de negocio
 * y delega la persistencia en el repositorio. La vista nunca toca el repositorio.
 */
public class ItinerarioController {

    private final ItinerarioRepositorio repositorio;
    private final Catalogo catalogo;

    public ItinerarioController(ItinerarioRepositorio repositorio, Catalogo catalogo) {
        this.repositorio = repositorio;
        this.catalogo = catalogo;
    }

    // ---------- Consultas para la vista ----------
    public List<Itinerario> listar() { return repositorio.listar(); }

    public Itinerario buscar(String id) throws ReglaNegocioException {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("El itinerario " + id + " no existe."));
    }

    public List<Ruta> rutasDisponibles() { return catalogo.getRutas(); }

    /** Unidades de la flota con estado "Operativo" (datos en vivo del módulo de flota). */
    public List<Autobus> unidadesOperativas() {
        return catalogo.getUnidades().stream().filter(ItinerarioController::esOperativa).toList();
    }

    private static boolean esOperativa(Autobus unidad) {
        return unidad.getEstado() != null && unidad.getEstado().trim().equalsIgnoreCase("Operativo");
    }

    public List<Conductor> conductoresDisponibles() { return catalogo.getConductores(); }

    public void suscribir(ItinerarioObserver observer) { repositorio.agregarObservador(observer); }

    // ---------- Casos de uso ----------
    public Itinerario crear(Ruta ruta, Autobus unidad, Conductor conductor,
                            Horario horario, EstadoRecorrido estado) throws ReglaNegocioException {
        Itinerario nuevo = construir(repositorio.siguienteId(), ruta, unidad, conductor, horario, estado);
        validarReglas(nuevo);
        repositorio.guardar(nuevo);
        return nuevo;
    }

    public Itinerario editar(String id, Ruta ruta, Autobus unidad, Conductor conductor,
                             Horario horario, EstadoRecorrido estado) throws ReglaNegocioException {
        Itinerario actual = buscar(id);
        if (!actual.getEstadoRecorrido().esModificable()) {
            throw new ReglaNegocioException("No se puede modificar un itinerario "
                    + actual.getEstadoRecorrido().toString().toLowerCase() + ".");
        }
        Itinerario actualizado = construir(id, ruta, unidad, conductor, horario, estado);
        validarReglas(actualizado);
        repositorio.guardar(actualizado);
        return actualizado;
    }

    public void eliminar(String id) throws ReglaNegocioException {
        Itinerario actual = buscar(id);
        if (!actual.getEstadoRecorrido().esModificable()) {
            throw new ReglaNegocioException("No se puede eliminar un itinerario "
                    + actual.getEstadoRecorrido().toString().toLowerCase() + ".");
        }
        repositorio.eliminar(id);
    }

    // ---------- Reglas de negocio ----------
    private Itinerario construir(String id, Ruta r, Autobus u, Conductor c, Horario h, EstadoRecorrido e)
            throws ReglaNegocioException {
        return new Itinerario.Builder().id(id).ruta(r).unidad(u).conductor(c).horario(h).estado(e).build();
    }

    private void validarReglas(Itinerario it) throws ReglaNegocioException {
        if (!esOperativa(it.getUnidad())) {
            throw new ReglaNegocioException("La unidad " + it.getUnidad().getPlaca()
                    + " no está operativa (" + it.getUnidad().getEstado() + ").");
        }
        for (Itinerario otro : repositorio.listar()) {
            if (otro.getIdItinerario().equals(it.getIdItinerario())) continue;
            if (otro.getEstadoRecorrido() == EstadoRecorrido.FINALIZADO) continue;
            if (!it.getHorario().seSolapaCon(otro.getHorario())) continue;

            if (otro.getUnidad().getPlaca().equals(it.getUnidad().getPlaca())) {
                throw new ReglaNegocioException("La unidad " + it.getUnidad().getPlaca()
                        + " ya está asignada al itinerario " + otro.getIdItinerario() + " en ese horario.");
            }
            if (otro.getConductor().getCedula().equals(it.getConductor().getCedula())) {
                throw new ReglaNegocioException("El conductor " + it.getConductor().getNombre()
                        + " ya está asignado al itinerario " + otro.getIdItinerario() + " en ese horario.");
            }
        }
    }
}
