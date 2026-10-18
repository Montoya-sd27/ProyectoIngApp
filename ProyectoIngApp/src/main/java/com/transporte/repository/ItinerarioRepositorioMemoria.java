package com.transporte.repository;

import com.transporte.model.Itinerario;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria. Usa el PATRÓN SINGLETON para que toda la aplicación
 * comparta una única fuente de datos, y es el "Subject" del PATRÓN OBSERVER.
 */
public final class ItinerarioRepositorioMemoria implements ItinerarioRepositorio {

    private static ItinerarioRepositorioMemoria instancia;

    private final Map<String, Itinerario> datos = new LinkedHashMap<>();
    private final List<ItinerarioObserver> observadores = new ArrayList<>();
    private int contador = 0;

    private ItinerarioRepositorioMemoria() { }

    public static synchronized ItinerarioRepositorioMemoria getInstance() {
        if (instancia == null) {
            instancia = new ItinerarioRepositorioMemoria();
        }
        return instancia;
    }

    /** Instancia independiente, útil para pruebas unitarias. */
    public static ItinerarioRepositorioMemoria crearAislado() {
        return new ItinerarioRepositorioMemoria();
    }

    @Override
    public synchronized String siguienteId() {
        contador++;
        return String.format("IT-%03d", contador);
    }

    @Override
    public void guardar(Itinerario itinerario) {
        datos.put(itinerario.getIdItinerario(), itinerario);
        notificar();
    }

    @Override
    public void eliminar(String id) {
        datos.remove(id);
        notificar();
    }

    @Override
    public Optional<Itinerario> buscarPorId(String id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<Itinerario> listar() {
        return new ArrayList<>(datos.values());
    }

    @Override
    public void agregarObservador(ItinerarioObserver observer) {
        observadores.add(observer);
    }

    private void notificar() {
        for (ItinerarioObserver o : observadores) {
            o.itinerariosActualizados();
        }
    }
}
