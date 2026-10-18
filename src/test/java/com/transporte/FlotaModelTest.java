package com.transporte;

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

    // 1. Verificación de Creación (Create)
    @Test
    public void testCrearAutobus() {
        Autobus bus = new Autobus("BUS-01", "Modelo-A", 40, "Operativo");
        flota.agregarAutobus(bus);

        assertEquals(1, flota.getListaAutobuses().size(), "Debe existir 1 unidad registrada.");
        assertEquals("BUS-01", flota.obtenerAutobus(0).getPlaca());
    }

    // 2. Verificación de Modificación (Update)
    @Test
    public void testModificarAutobus() {
        Autobus bus = new Autobus("BUS-01", "Modelo-A", 40, "Operativo");
        flota.agregarAutobus(bus);

        Autobus busActualizado = new Autobus("BUS-01", "Modelo-B", 50, "En Mantenimiento");
        flota.actualizarAutobus(0, busActualizado);

        Autobus resultado = flota.obtenerAutobus(0);
        assertEquals("Modelo-B", resultado.getModelo());
        assertEquals(50, resultado.getCapacidad());
        assertEquals("En Mantenimiento", resultado.getEstado());
    }

    // 3. Verificación de Eliminación (Delete)
    @Test
    public void testEliminarAutobus() {
        Autobus bus = new Autobus("BUS-01", "Modelo-A", 40, "Operativo");
        flota.agregarAutobus(bus);

        flota.eliminarAutobus(0);

        assertEquals(0, flota.getListaAutobuses().size(), "La lista debe quedar vacía tras eliminar.");
    }

    // 4. Regla de negocio: Validación de placa única
    @Test
    public void testBloquearPlacaDuplicada() {
        Autobus bus1 = new Autobus("BUS-01", "Modelo-A", 40, "Operativo");
        flota.agregarAutobus(bus1);

        Autobus busDuplicado = new Autobus("bus-01", "Modelo-C", 30, "Operativo");

        assertThrows(IllegalArgumentException.class, () -> {
            flota.agregarAutobus(busDuplicado);
        }, "Debe bloquear el registro si la placa ya existe.");
    }

    // 5. Regla de negocio: Validación de capacidad válida (> 0)
    @Test
    public void testBloquearCapacidadInvalida() {
        Autobus busCapacidadCero = new Autobus("BUS-02", "Modelo-A", 0, "Operativo");
        Autobus busCapacidadNegativa = new Autobus("BUS-03", "Modelo-A", -5, "Operativo");

        assertThrows(IllegalArgumentException.class, () -> {
            flota.agregarAutobus(busCapacidadCero);
        }, "Debe bloquear el registro si la capacidad es 0.");

        assertThrows(IllegalArgumentException.class, () -> {
            flota.agregarAutobus(busCapacidadNegativa);
        }, "Debe bloquear el registro si la capacidad es negativa.");
    }
}