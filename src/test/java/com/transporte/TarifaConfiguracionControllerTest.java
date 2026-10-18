package com.transporte;

import static org.junit.jupiter.api.Assertions.*;

import com.transporte.controller.CobroService;
import com.transporte.controller.TarifaConfiguracionController;
import com.transporte.model.*;
import com.transporte.model.RutaFactory.TipoRuta;
import com.transporte.persistence.TarifaArchivo;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TarifaConfiguracionControllerTest {

    private TarifaConfiguracionController controller;
    private TarifaArchivo archivo;

    @BeforeEach
    public void setUp(@TempDir Path tempDir) {
        System.setProperty("ruta.datos", tempDir.toString());
        archivo = new TarifaArchivo(tempDir.resolve("tarifas.txt"));
        List<Ruta> rutas = List.of(
                RutaFactory.crear(TipoRuta.URBANA, "RU-01", "UCV", "Plaza Venezuela", "Los Chaguaramos"),
                RutaFactory.crear(TipoRuta.EXTRAURBANA, "RE-01", "UCV", "Los Teques", "Los Teques"));
        controller = new TarifaConfiguracionController(() -> rutas, archivo);
    }

    @Test
    public void listaRutasUrbanasYExtraurbanas() throws ReglaNegocioException {
        assertEquals(2, controller.obtenerRutas(null).size());
        assertEquals("RU-01", controller.obtenerRutas(TipoRuta.URBANA).get(0).getIdRuta());
        assertEquals("RE-01", controller.obtenerRutas(TipoRuta.EXTRAURBANA).get(0).getIdRuta());
    }

    @Test
    public void sinConfigurarSeUsaLaTarifaPorDefecto() throws ReglaNegocioException {
        TarifaRuta t = controller.tarifaDe("RU-01");
        assertEquals(TarifaRuta.BASE_POR_DEFECTO, t.getBase(), 0.001);
        assertTrue(controller.tarifaConfigurada("RU-01").isEmpty());
    }

    @Test
    public void sePuedeCambiarLaTarifaDeUnaRutaUrbana() throws ReglaNegocioException {
        controller.establecerTarifa("RU-01", 20.0, 25, 80, 100);

        TarifaRuta t = controller.tarifaDe("RU-01");
        assertEquals(20.0, t.getBase(), 0.001);
        assertEquals(5.0, t.precioPara("ESTUDIANTE"), 0.001);
        assertEquals(16.0, t.precioPara("EMPLEADO"), 0.001);
    }

    @Test
    public void unCambioDelAdminSeVeEnLaSiguienteConsulta() throws ReglaNegocioException {
        controller.establecerTarifa("RE-01", 100.0, 50, 100, 100);
        assertEquals(50.0, controller.tarifaDe("RE-01").precioPara("ESTUDIANTE"), 0.001);

        controller.establecerTarifa("RE-01", 180.0, 30, 100, 100);
        assertEquals(54.0, controller.tarifaDe("RE-01").precioPara("ESTUDIANTE"), 0.001);
    }

    @Test
    public void guardarUnaRutaNoAfectaLaTarifaDeLasOtras() throws ReglaNegocioException {
        controller.establecerTarifa("RE-01", 100.0, 50, 100, 100);
        controller.establecerTarifa("RU-01", 8.0, 50, 100, 100);

        assertEquals(100.0, controller.tarifaDe("RE-01").getBase(), 0.001);
        assertEquals(8.0, controller.tarifaDe("RU-01").getBase(), 0.001);
    }

    @Test
    public void tarifaBaseInvalidaLanzaExcepcion() {
        assertThrows(ReglaNegocioException.class, () -> controller.establecerTarifa("RU-01", -50.0, 50, 100, 100));
        assertThrows(ReglaNegocioException.class, () -> controller.establecerTarifa("RU-01", 0, 50, 100, 100));
    }

    @Test
    public void porcentajeFueraDeRangoLanzaExcepcion() {
        assertThrows(ReglaNegocioException.class, () -> controller.establecerTarifa("RU-01", 10.0, 150, 100, 100));
    }

    @Test
    public void elCobroUsaElPorcentajeConfiguradoPorElAdmin() throws Exception {
        controller.establecerTarifa("RU-01", 40.0, 25, 90, 100);
        CobroService cobro = new CobroService();
        UsuarioConSaldo est = new Estudiante("Ana", "Lopez", "29111222", "ana@ucv.ve", "123");
        est.recargar(100f);

        Ticket ticket = cobro.pagarCupo(est, "RU-01 UCV → Plaza Venezuela", controller.tarifaDe("RU-01"));

        assertEquals(10.0, ticket.getMontoCobrado(), 0.001);   // 25% de 40
        assertEquals(90f, est.getSaldo(), 0.001f);             // el saldo se descontó
    }

    @Test
    public void saldoInsuficienteNoEmiteTicketNiDescuenta() throws Exception {
        controller.establecerTarifa("RU-01", 40.0, 50, 100, 100);
        CobroService cobro = new CobroService();
        UsuarioConSaldo emp = new Empleado("Juan", "Gomez", "15111222", "juan.sinsaldo@ucv.ve", "123");
        emp.recargar(5f);

        assertThrows(ReglaNegocioException.class,
                () -> cobro.pagarCupo(emp, "RU-01", controller.tarifaDe("RU-01")));
        assertEquals(5f, emp.getSaldo(), 0.001f);
        assertTrue(cobro.obtenerTicketsPorUsuario("juan.sinsaldo@ucv.ve").isEmpty());
    }
}
