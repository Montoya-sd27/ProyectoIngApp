package com.transporte;

import com.transporte.controller.TarifaConfiguracionController;
import com.transporte.model.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TarifaConfiguracionControllerTest {

    private TarifaConfiguracionController controller;

    @BeforeEach
    public void setUp(@TempDir Path tempDir) throws IOException {
        System.setProperty("ruta.datos", tempDir.toString());

        File archivoRutas = new File(tempDir.toFile(), "rutas.txt");
        try (PrintWriter pw = new PrintWriter(new FileWriter(archivoRutas))) {
            pw.println("RUTA|1|EXTRAURBANA|R-E3|Tazón|Petare|Directo|15123456|José Gómez|5ta|AB123CD|Yutong|40|ACTIVO|LUNES|05:00|07:00|120.00");
            pw.println("RUTA|2|URBANA|R-U1|Plaza Venezuela|UCV|Directo|15123456|Pedro|5ta|AB456EF|Yutong|40|ACTIVO|LUNES|07:00|08:00|50.00");
        }

        controller = new TarifaConfiguracionController();
    }

    @Test
    public void testObtenerRutasExtraurbanasFiltraCorrectamente() throws ReglaNegocioException {
        List<String[]> extraurbanas = controller.obtenerRutasExtraurbanas();
        assertEquals(1, extraurbanas.size());
        assertEquals("R-E3", extraurbanas.get(0)[3]);
    }

    @Test
    public void testActualizarTarifaBaseExitoso() throws ReglaNegocioException {
        controller.actualizarTarifaBaseRuta("R-E3", 180.00);

        List<String[]> extraurbanas = controller.obtenerRutasExtraurbanas();
        assertEquals("180.00", extraurbanas.get(0)[extraurbanas.get(0).length - 1]);
    }

    @Test
    public void testActualizarTarifaNegativaLanzaExcepcion() {
        assertThrows(ReglaNegocioException.class, () -> {
            controller.actualizarTarifaBaseRuta("R-E3", -50.00);
        });
    }

    @Test
    public void testActualizarTarifaRutaInexistenteLanzaExcepcion() {
        assertThrows(ReglaNegocioException.class, () -> {
            controller.actualizarTarifaBaseRuta("R-INEXISTENTE", 150.00);
        });
    }
}