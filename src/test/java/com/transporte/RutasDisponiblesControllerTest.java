package com.transporte;

import static org.junit.jupiter.api.Assertions.*;

import com.transporte.controller.ItinerarioController;
import com.transporte.controller.RutasDisponiblesController;
import com.transporte.model.*;
import com.transporte.model.RutaFactory.TipoRuta;
import com.transporte.repository.Catalogo;
import com.transporte.repository.ItinerarioRepositorio;
import com.transporte.repository.ItinerarioRepositorioMemoria;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RutasDisponiblesControllerTest {

    private ItinerarioController itinerarios;
    private RutasDisponiblesController rutas;
    private Autobus bus1;
    private Autobus bus2;
    private Conductor chofer1;
    private Conductor chofer2;

    @BeforeEach
    void preparar() {
        FlotaModel flota = new FlotaModel();
        bus1 = new Autobus("AAA111", "Encava", 12, "Operativo");
        bus2 = new Autobus("BBB222", "Yutong", 40, "Operativo");
        flota.agregarAutobus(bus1);
        flota.agregarAutobus(bus2);
        chofer1 = new Conductor("V-1", "Ana", "5ta");
        chofer2 = new Conductor("V-2", "Luis", "5ta");
        ItinerarioRepositorio repositorio = ItinerarioRepositorioMemoria.crearAislado();
        itinerarios = new ItinerarioController(repositorio, Catalogo.vacio(flota));
        rutas = new RutasDisponiblesController(repositorio);
    }

    private Ruta urbana(String id, String origen, String destino) {
        return RutaFactory.crear(TipoRuta.URBANA, id, origen, destino, null);
    }

    private Ruta extraurbana(String id, String origen, String destino) {
        return RutaFactory.crear(TipoRuta.EXTRAURBANA, id, origen, destino, destino);
    }

    private Itinerario crear(Ruta ruta, Autobus bus, Conductor chofer, DayOfWeek dia, String salida, String llegada,
                             EstadoRecorrido estado) throws Exception {
        Horario h = new Horario(dia, LocalTime.parse(salida), LocalTime.parse(llegada));
        return itinerarios.crear(ruta, bus, chofer, h, estado);
    }

    @Test
    void sinItinerariosNoHayRutasDisponibles() {
        assertTrue(rutas.disponibles(TipoRuta.URBANA).isEmpty());
        assertTrue(rutas.disponibles(TipoRuta.EXTRAURBANA).isEmpty());
    }

    @Test
    void separaLasRutasUrbanasDeLasExtraurbanas() throws Exception {
        crear(urbana("RU-01", "Plaza Caracas", "Caracas Centro"), bus1, chofer1, DayOfWeek.MONDAY, "06:00", "07:00", EstadoRecorrido.PROGRAMADO);
        crear(extraurbana("RE-01", "Petare", "Los Teques"), bus2, chofer2, DayOfWeek.MONDAY, "05:00", "07:00", EstadoRecorrido.PROGRAMADO);

        List<Itinerario> urbanas = rutas.disponibles(TipoRuta.URBANA);
        List<Itinerario> extraurbanas = rutas.disponibles(TipoRuta.EXTRAURBANA);

        assertEquals(1, urbanas.size());
        assertEquals("RU-01", urbanas.get(0).getRuta().getIdRuta());
        assertEquals(1, extraurbanas.size());
        assertEquals("RE-01", extraurbanas.get(0).getRuta().getIdRuta());
    }

    @Test
    void soloSeMuestranLosItinerariosProgramados() throws Exception {
        crear(urbana("RU-01", "A", "B"), bus1, chofer1, DayOfWeek.MONDAY, "06:00", "07:00", EstadoRecorrido.PROGRAMADO);
        crear(urbana("RU-02", "A", "C"), bus1, chofer1, DayOfWeek.TUESDAY, "06:00", "07:00", EstadoRecorrido.EN_CURSO);
        crear(urbana("RU-03", "A", "D"), bus1, chofer1, DayOfWeek.WEDNESDAY, "06:00", "07:00", EstadoRecorrido.DETENIDO);
        crear(urbana("RU-04", "A", "E"), bus1, chofer1, DayOfWeek.THURSDAY, "06:00", "07:00", EstadoRecorrido.FINALIZADO);

        List<Itinerario> urbanas = rutas.disponibles(TipoRuta.URBANA);

        assertEquals(1, urbanas.size());
        assertEquals("RU-01", urbanas.get(0).getRuta().getIdRuta());
    }

    @Test
    void ordenaPorDiaYHoraDeSalida() throws Exception {
        crear(urbana("RU-03", "A", "C"), bus1, chofer1, DayOfWeek.TUESDAY, "05:00", "06:00", EstadoRecorrido.PROGRAMADO);
        crear(urbana("RU-02", "A", "B"), bus1, chofer1, DayOfWeek.MONDAY, "09:00", "10:00", EstadoRecorrido.PROGRAMADO);
        crear(urbana("RU-01", "A", "D"), bus2, chofer2, DayOfWeek.MONDAY, "06:00", "07:00", EstadoRecorrido.PROGRAMADO);

        List<Itinerario> urbanas = rutas.disponibles(TipoRuta.URBANA);

        assertEquals("RU-01", urbanas.get(0).getRuta().getIdRuta());
        assertEquals("RU-02", urbanas.get(1).getRuta().getIdRuta());
        assertEquals("RU-03", urbanas.get(2).getRuta().getIdRuta());
    }

    @Test
    void loscuposSonLaCapacidadDeLaUnidadOperativa() throws Exception {
        Itinerario it = crear(urbana("RU-01", "A", "B"), bus1, chofer1, DayOfWeek.MONDAY, "06:00", "07:00", EstadoRecorrido.PROGRAMADO);

        assertEquals(12, rutas.cuposDisponibles(it));
    }

    @Test
    void sinCuposSiLaUnidadYaNoEstaOperativa() throws Exception {
        Itinerario it = crear(urbana("RU-01", "A", "B"), bus1, chofer1, DayOfWeek.MONDAY, "06:00", "07:00", EstadoRecorrido.PROGRAMADO);

        bus1.setEstado("En Mantenimiento");

        assertEquals(0, rutas.cuposDisponibles(it));
    }

    @Test
    void cadaReservaBajaLosCuposYNoSePuedeReservarConCero() throws Exception {
        Autobus pequeno = new Autobus("CCC333", "Mini", 2, "Operativo");
        Itinerario it = crear(urbana("RU-01", "A", "B"), pequeno, chofer1, DayOfWeek.MONDAY, "06:00", "07:00", EstadoRecorrido.PROGRAMADO);

        rutas.reservarCupo(it);
        assertEquals(1, rutas.cuposDisponibles(it));
        rutas.reservarCupo(it);
        assertEquals(0, rutas.cuposDisponibles(it));

        assertThrows(ReglaNegocioException.class, () -> rutas.reservarCupo(it));
        assertEquals(0, rutas.cuposDisponibles(it));
    }
}
