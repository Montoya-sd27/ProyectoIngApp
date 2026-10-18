package com.transporte;

import com.transporte.controller.CobroService;
import com.transporte.model.Ticket;
import com.transporte.model.Usuario;
import com.transporte.model.Estudiante;
import com.transporte.model.Empleado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CobroServiceTest {
    private CobroService cobroService;
    private Usuario usuarioEstudiante;
    private Usuario usuarioEmpleado;

    @BeforeEach
    public void setUp() {
        cobroService = new CobroService();

        usuarioEstudiante = new Estudiante("Pedro", "Pérez", "29111222", "pedro@ucv.ve", "123");
        usuarioEmpleado = new Empleado("Juan", "Gómez", "15111222", "juan@ucv.ve", "123");
    }

    @Test
    public void testCalcularTarifaEstudianteDescuento() {
        double tarifaBase = 10.0;
        double tarifaCalculada = cobroService.calcularTarifa(usuarioEstudiante.getRol(), tarifaBase);
        assertEquals(5.0, tarifaCalculada, 0.001);
    }

    @Test
    public void testCalcularTarifaEmpleadoCompleta() {
        double tarifaBase = 10.0;
        double tarifaCalculada = cobroService.calcularTarifa(usuarioEmpleado.getRol(), tarifaBase);
        assertEquals(10.0, tarifaCalculada, 0.001);
    }

    @Test
    public void testProcesarCobroExitosoEstudiante() throws Exception {
        double tarifaBase = 10.0;
        double saldoDisponible = 20.0;

        Ticket ticket = cobroService.procesarCobro(usuarioEstudiante, "Ruta Plaza Venezuela - UCV", tarifaBase, saldoDisponible);

        assertNotNull(ticket);
        assertEquals("Estudiante", ticket.getRolUsuario());
        assertEquals(5.0, ticket.getMontoCobrado(), 0.001);
    }

    @Test
    public void testProcesarCobroExitosoEmpleado() throws Exception {
        double tarifaBase = 10.0;
        double saldoDisponible = 20.0;

        Ticket ticket = cobroService.procesarCobro(usuarioEmpleado, "Ruta Plaza Venezuela - UCV", tarifaBase, saldoDisponible);

        assertNotNull(ticket);
        assertEquals("Empleado", ticket.getRolUsuario());
        assertEquals(10.0, ticket.getMontoCobrado(), 0.001);
    }

    @Test
    public void testProcesarCobroSaldoInsuficiente() {
        double tarifaBase = 10.0;
        double saldoInsuficiente = 2.0;

        assertThrows(Exception.class, () -> {
            cobroService.procesarCobro(usuarioEstudiante, "Ruta Plaza Venezuela - UCV", tarifaBase, saldoInsuficiente);
        });
    }
}