package com.transporte;

import static org.junit.jupiter.api.Assertions.*;

import com.transporte.controller.ItinerarioController;
import com.transporte.model.*;
import com.transporte.repository.Catalogo;
import com.transporte.repository.ItinerarioRepositorioMemoria;
import java.time.DayOfWeek;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Verifica que itinerarios y flota comparten los mismos datos. */
class IntegracionFlotaItinerariosTest {

    private FlotaModel flota;
    private ItinerarioController controller;
    private Ruta ruta;
    private Conductor conductor;

    @BeforeEach
    void setUp() {
        flota = new FlotaModel();
        controller = new ItinerarioController(ItinerarioRepositorioMemoria.crearAislado(), Catalogo.vacio(flota));
        ruta = RutaFactory.crear(RutaFactory.TipoRuta.URBANA, "RU-01", "UCV", "Plaza Venezuela", "Tamanaco");
        conductor = new Conductor("V-1", "Ana", "5ta");
    }

    private Horario h() throws ReglaNegocioException {
        return new Horario(DayOfWeek.MONDAY, LocalTime.of(7, 0), LocalTime.of(8, 0));
    }

    @Test
    void unidadRegistradaEnFlotaApareceParaItinerarios() {
        assertTrue(controller.unidadesOperativas().isEmpty());
        flota.agregarAutobus(new Autobus("ab123cd", "Encava", 40, "Operativo"));
        assertEquals(1, controller.unidadesOperativas().size());
        assertEquals("AB123CD", controller.unidadesOperativas().get(0).getPlaca());
    }

    @Test
    void unidadEnMantenimientoOFueraDeServicioNoSeOfrece() {
        flota.agregarAutobus(new Autobus("AA111AA", "Encava", 40, "En Mantenimiento"));
        flota.agregarAutobus(new Autobus("BB222BB", "Yutong", 45, "Fuera de Servicio"));
        flota.agregarAutobus(new Autobus("CC333CC", "Bluebird", 35, "Operativo"));
        assertEquals(1, controller.unidadesOperativas().size());
        assertEquals("CC333CC", controller.unidadesOperativas().get(0).getPlaca());
    }

    @Test
    void editarUnidadEnFlotaSeReflejaEnElItinerarioExistente() throws Exception {
        flota.agregarAutobus(new Autobus("AA111AA", "Encava", 40, "Operativo"));
        Itinerario it = controller.crear(ruta, flota.obtenerAutobus(0), conductor, h(), EstadoRecorrido.PROGRAMADO);

        flota.actualizarAutobus(0, new Autobus("ZZ999ZZ", "Yutong", 50, "Operativo"));

        assertEquals("ZZ999ZZ", controller.buscar(it.getIdItinerario()).getUnidad().getPlaca());
        assertEquals("Yutong", controller.buscar(it.getIdItinerario()).getUnidad().getModelo());
    }

    @Test
    void unidadQuePasaAMantenimientoNoPuedeReasignarse() throws Exception {
        flota.agregarAutobus(new Autobus("AA111AA", "Encava", 40, "Operativo"));
        Autobus bus = flota.obtenerAutobus(0);
        Itinerario it = controller.crear(ruta, bus, conductor, h(), EstadoRecorrido.PROGRAMADO);

        bus.setEstado("En Mantenimiento");   /* lo que hace "Alternar Estado" en la ventana de flota*/

        assertThrows(ReglaNegocioException.class, () -> controller.editar(it.getIdItinerario(), ruta, bus,
                conductor, h(), EstadoRecorrido.PROGRAMADO));
    }
}
