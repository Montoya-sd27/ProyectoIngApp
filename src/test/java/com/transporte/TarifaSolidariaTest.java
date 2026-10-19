package com.transporte;

import com.transporte.model.TarifaSolidariaServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TarifaSolidariaTest {

    private TarifaSolidariaServicio servicio;
    private final double TARIFA_BASE = 150.00;

    @BeforeEach
    public void setUp() {
        servicio = new TarifaSolidariaServicio();
    }

    @Test
    public void testEstrategiaEstudianteAbonoSubsidiado() {
        servicio.setEstrategiaPorRol("Estudiante");
        double esperado = 30.00; // 20% de 150.00
        double resultado = servicio.calcular(TARIFA_BASE);
        assertEquals(esperado, resultado, 0.01, "El estudiante debe pagar el 20% de la tarifa base.");
        assertEquals("20% (Subsidiado de tarifa base)", servicio.getDescripcion());
        assertTrue(servicio.getEstrategiaActual() instanceof TarifaSolidariaServicio.TarifaEstudianteStrategy);
    }

    @Test
    public void testEstrategiaPersonalUniversitarioIntermedio() {
        servicio.setEstrategiaPorRol("Empleado / Profesor");
        double esperado = 75.00; // 50% de 150.00
        double resultado = servicio.calcular(TARIFA_BASE);
        assertEquals(esperado, resultado, 0.01, "El personal docente y administrativo debe pagar el 50% de la tarifa base.");
        assertEquals("50% (Tarifa intermedia)", servicio.getDescripcion());
        assertTrue(servicio.getEstrategiaActual() instanceof TarifaSolidariaServicio.TarifaPersonalStrategy);
    }

    @Test
    public void testEstrategiaPublicoGeneralAbonaCienPorCiento() {
        servicio.setEstrategiaPorRol("Público General");
        double esperado = 150.00; // 100% de 150.00
        double resultado = servicio.calcular(TARIFA_BASE);
        assertEquals(esperado, resultado, 0.01, "El público general debe abonar el 100% de la tarifa base.");
        assertEquals("100% (Tarifa regular)", servicio.getDescripcion());
        assertTrue(servicio.getEstrategiaActual() instanceof TarifaSolidariaServicio.TarifaPublicoGeneralStrategy);
    }

    @Test
    public void testTarifaBaseNegativaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> servicio.calcular(-20.0),
                "Una tarifa base negativa debe lanzar IllegalArgumentException.");
    }
}