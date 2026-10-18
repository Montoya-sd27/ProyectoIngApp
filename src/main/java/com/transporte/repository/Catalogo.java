package com.transporte.repository;

import com.transporte.model.*;
import com.transporte.model.RutaFactory.TipoRuta;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos de referencia para armar itinerarios.
 *  - Unidades: salen en vivo del {@link FlotaModel} (módulo de gestión de flota).
 *  - Rutas y conductores: datos de demostración (aún no hay módulo que los gestione).
 */
public final class Catalogo {

    private final FlotaModel flota;
    private final List<Ruta> rutas = new ArrayList<>();
    private final List<Conductor> conductores = new ArrayList<>();

    private Catalogo(FlotaModel flota) {
        this.flota = flota;
    }

    /** Catálogo con rutas y conductores de demostración. */
    public static Catalogo conDatosDemo(FlotaModel flota) {
        Catalogo c = new Catalogo(flota);
        c.rutas.add(RutaFactory.crear(TipoRuta.URBANA, "RU-01", "UCV", "Plaza Venezuela", "Los Chaguaramos, Tamanaco"));
        c.rutas.add(RutaFactory.crear(TipoRuta.URBANA, "RU-02", "UCV", "Chacaíto", "Plaza Venezuela, Sabana Grande"));
        c.rutas.add(RutaFactory.crear(TipoRuta.EXTRAURBANA, "RE-01", "UCV", "Los Teques", "Los Teques"));
        c.rutas.add(RutaFactory.crear(TipoRuta.EXTRAURBANA, "RE-02", "UCV", "Guarenas", "Guarenas"));

        c.conductores.add(new Conductor("V-12345678", "Carlos Pérez", "5ta"));
        c.conductores.add(new Conductor("V-87654321", "María Gómez", "5ta"));
        c.conductores.add(new Conductor("V-11223344", "José Rodríguez", "4ta"));
        return c;
    }

    /** Catálogo sin rutas ni conductores (para pruebas). */
    public static Catalogo vacio(FlotaModel flota) {
        return new Catalogo(flota);
    }

    public void agregarRuta(Ruta r) { rutas.add(r); }
    public void agregarConductor(Conductor c) { conductores.add(c); }

    public List<Ruta> getRutas() { return rutas; }
    public List<Autobus> getUnidades() { return flota.getListaAutobuses(); }
    public List<Conductor> getConductores() { return conductores; }
}
