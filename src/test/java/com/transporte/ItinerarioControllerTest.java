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

class ItinerarioControllerTest {

    private ItinerarioController controller;
    private Ruta ruta;
    private Autobus bus1, bus2, busTaller;
    private Conductor c1, c2;

    @BeforeEach
    void setUp() {
        controller = new ItinerarioController(ItinerarioRepositorioMemoria.crearAislado(), Catalogo.vacio(new FlotaModel()));
        ruta = RutaFactory.crear(RutaFactory.TipoRuta.EXTRAURBANA, "RE-01", "UCV", "Los Teques", "Los Teques");
        bus1 = new Autobus("AAA111", "Encava", 40, "Operativo");
        bus2 = new Autobus("BBB222", "Yutong", 45, "Operativo");
        busTaller = new Autobus("CCC333", "Bluebird", 35, "En Mantenimiento");
        c1 = new Conductor("V-1", "Ana", "5ta");
        c2 = new Conductor("V-2", "Luis", "5ta");
    }

    private Horario h(String s, String l) throws ReglaNegocioException {
        return new Horario(DayOfWeek.MONDAY, LocalTime.parse(s), LocalTime.parse(l));
    }

    @Test
    void creaItinerarioValido() throws Exception {
        Itinerario it = controller.crear(ruta, bus1, c1, h("07:00", "08:00"), EstadoRecorrido.PROGRAMADO);
        assertEquals("IT-001", it.getIdItinerario());
        assertEquals(1, controller.listar().size());
    }

    @Test
    void rechazaHorarioInvertido() {
        assertThrows(ReglaNegocioException.class, () -> h("09:00", "08:00"));
    }

    @Test
    void rechazaUnidadNoOperativa() {
        assertThrows(ReglaNegocioException.class,
                () -> controller.crear(ruta, busTaller, c1, h("07:00", "08:00"), EstadoRecorrido.PROGRAMADO));
    }

    @Test
    void rechazaUnidadDuplicadaEnMismoHorario() throws Exception {
        controller.crear(ruta, bus1, c1, h("07:00", "08:00"), EstadoRecorrido.PROGRAMADO);
        assertThrows(ReglaNegocioException.class,
                () -> controller.crear(ruta, bus1, c2, h("07:30", "08:30"), EstadoRecorrido.PROGRAMADO));
    }

    @Test
    void rechazaConductorDuplicadoEnMismoHorario() throws Exception {
        controller.crear(ruta, bus1, c1, h("07:00", "08:00"), EstadoRecorrido.PROGRAMADO);
        assertThrows(ReglaNegocioException.class,
                () -> controller.crear(ruta, bus2, c1, h("07:30", "08:30"), EstadoRecorrido.PROGRAMADO));
    }

    @Test
    void permiteMismosRecursosEnHorariosDistintos() throws Exception {
        controller.crear(ruta, bus1, c1, h("07:00", "08:00"), EstadoRecorrido.PROGRAMADO);
        assertDoesNotThrow(() -> controller.crear(ruta, bus1, c1, h("08:00", "09:00"), EstadoRecorrido.PROGRAMADO));
    }

    @Test
    void noEliminaItinerarioEnCurso() throws Exception {
        Itinerario it = controller.crear(ruta, bus1, c1, h("07:00", "08:00"), EstadoRecorrido.EN_CURSO);
        assertThrows(ReglaNegocioException.class, () -> controller.eliminar(it.getIdItinerario()));
    }

    @Test
    void editaYElimina() throws Exception {
        Itinerario it = controller.crear(ruta, bus1, c1, h("07:00", "08:00"), EstadoRecorrido.PROGRAMADO);
        controller.editar(it.getIdItinerario(), ruta, bus2, c1, h("07:00", "08:00"), EstadoRecorrido.PROGRAMADO);
        assertEquals("BBB222", controller.buscar(it.getIdItinerario()).getUnidad().getPlaca());
        controller.eliminar(it.getIdItinerario());
        assertTrue(controller.listar().isEmpty());
    }
}
