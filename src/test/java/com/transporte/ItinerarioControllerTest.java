package com.transporte;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.transporte.controller.ItinerarioController;
import com.transporte.model.Autobus;
import com.transporte.model.Conductor;
import com.transporte.model.EstadoRecorrido;
import com.transporte.model.FlotaModel;
import com.transporte.model.Horario;
import com.transporte.model.Itinerario;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.Ruta;
import com.transporte.model.RutaFactory;
import com.transporte.repository.Catalogo;
import com.transporte.repository.ItinerarioRepositorioMemoria;

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

    // ---------- Recorridos que cruzan la medianoche ----------

    private Horario h(DayOfWeek dia, String s, String l) throws ReglaNegocioException {
        return new Horario(dia, LocalTime.parse(s), LocalTime.parse(l));
    }

    @Test
    void permiteRecorridoQueTerminaALaMedianoche() throws Exception {
        Horario nocturno = h("22:00", "00:00");
        assertTrue(nocturno.cruzaMedianoche());
        assertEquals(2, nocturno.getDuracion().toHours());
        assertDoesNotThrow(() -> controller.crear(ruta, bus1, c1, nocturno, EstadoRecorrido.PROGRAMADO));
    }

    @Test
    void permiteRecorridoQueTerminaDespuesDeLaMedianoche() throws Exception {
        assertTrue(h("23:30", "01:15").cruzaMedianoche());
        assertFalse(h("07:00", "08:00").cruzaMedianoche());
    }

    @Test
    void rechazaHorasIguales() {
        assertThrows(ReglaNegocioException.class, () -> h("08:00", "08:00"));
    }

    @Test
    void rechazaRecorridoNocturnoDemasiadoLargo() {
        // 20:00 → 09:00 del día siguiente serían 13 horas: se toma como un error al escribir las horas
        assertThrows(ReglaNegocioException.class, () -> h("20:00", "09:00"));
    }

    @Test
    void detectaChoqueConRecorridoQueSigueEnLaMadrugada() throws Exception {
        controller.crear(ruta, bus1, c1, h(DayOfWeek.MONDAY, "23:00", "01:00"), EstadoRecorrido.PROGRAMADO);
        // el martes a las 00:30 la unidad todavía está en el recorrido del lunes por la noche
        assertThrows(ReglaNegocioException.class,
                () -> controller.crear(ruta, bus1, c2, h(DayOfWeek.TUESDAY, "00:30", "02:00"), EstadoRecorrido.PROGRAMADO));
    }

    @Test
    void permiteUnRecorridoJustoCuandoTerminaElNocturno() throws Exception {
        controller.crear(ruta, bus1, c1, h(DayOfWeek.MONDAY, "22:00", "00:00"), EstadoRecorrido.PROGRAMADO);
        assertDoesNotThrow(() -> controller.crear(ruta, bus1, c1, h(DayOfWeek.TUESDAY, "00:00", "01:00"), EstadoRecorrido.PROGRAMADO));
    }

    @Test
    void detectaChoqueEntreDomingoNocturnoYLunesDeMadrugada() throws Exception {
        controller.crear(ruta, bus1, c1, h(DayOfWeek.SUNDAY, "23:00", "01:00"), EstadoRecorrido.PROGRAMADO);
        assertThrows(ReglaNegocioException.class,
                () -> controller.crear(ruta, bus1, c2, h(DayOfWeek.MONDAY, "00:30", "02:00"), EstadoRecorrido.PROGRAMADO));
    }

    @Test
    void noHayChoqueEnDiasDistintos() throws Exception {
        controller.crear(ruta, bus1, c1, h(DayOfWeek.MONDAY, "08:00", "09:00"), EstadoRecorrido.PROGRAMADO);
        assertDoesNotThrow(() -> controller.crear(ruta, bus1, c1, h(DayOfWeek.TUESDAY, "08:00", "09:00"), EstadoRecorrido.PROGRAMADO));
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