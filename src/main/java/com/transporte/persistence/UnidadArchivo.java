package com.transporte.persistence;

import com.transporte.model.Autobus;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y lee las unidades (autobuses) de la flota en un archivo de texto.
 * Una línea por unidad: UNIDAD|placa|modelo|capacidad|estado
 */
public final class UnidadArchivo {

    private static final String TIPO = "UNIDAD";
    private static final int CAMPOS = 5;

    private final Path archivo;

    public UnidadArchivo(Path archivo) {
        this.archivo = archivo;
    }

    public Path getArchivo() { return archivo; }

    public List<Autobus> cargar() {
        List<Autobus> unidades = new ArrayList<>();
        List<String> lineas = ArchivoLineas.leer(archivo);
        for (int i = 0; i < lineas.size(); i++) {
            List<String> c = ArchivoLineas.dividir(lineas.get(i));
            if (c.size() != CAMPOS || !TIPO.equals(c.get(0))) {
                ArchivoLineas.advertirLineaInvalida(archivo, i + 1, "formato incorrecto");
                continue;
            }
            try {
                unidades.add(new Autobus(c.get(1), c.get(2), Integer.parseInt(c.get(3).trim()), c.get(4)));
            } catch (NumberFormatException e) {
                ArchivoLineas.advertirLineaInvalida(archivo, i + 1, "capacidad no numérica: " + c.get(3));
            }
        }
        return unidades;
    }

    public void guardar(List<Autobus> unidades) {
        List<String> lineas = new ArrayList<>();
        for (Autobus b : unidades) {
            lineas.add(ArchivoLineas.unir(TIPO, b.getPlaca(), b.getModelo(),
                    String.valueOf(b.getCapacidad()), b.getEstado()));
        }
        ArchivoLineas.escribir(archivo, lineas);
    }
}
