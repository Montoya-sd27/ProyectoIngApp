package com.transporte.model;

import com.transporte.model.Autobus;
import com.transporte.model.FlotaModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FlotaModelTest {

    private FlotaModel flota;

    @BeforeEach
    public void setUp() {
        flota = new FlotaModel();
    }

    @Test
    public void testCrearAutobus() {
        Autobus bus = new Autobus("BUS-01", "Modelo-A", 40, "Operativo");
        flota.agregarAutobus(bus);

        assertEquals(1, flota.getListaAutobuses().size(), "Debe existir 1 unidad registrada.");
        assertEquals("BUS-01", flota.obtenerAutobus(0).getPlaca());
    }

    @Test
    public void testModificarAutobus() {
        Autobus bus = new Autobus("BUS-01", "Modelo-A", 40, "Operativo");
        flota.agregarAutobus(bus);

        Autobus busActualizado = new Autobus("BUS-01", "Modelo-B", 50, "En Mantenimiento");
        flota.modificarAutobus(0, busActualizado);

        assertEquals("Modelo-B", flota.obtenerAutobus(0).getModelo());
        assertEquals(50, flota.obtenerAutobus(0).getCapacidad());
    }
}