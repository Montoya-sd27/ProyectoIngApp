package com.transporte.persistence;

import com.transporte.model.TarifaRuta;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda y lee las tarifas de las rutas en un archivo de texto, una línea por ruta:
 * TARIFA|idRuta|tarifaBase|pctEstudiante|pctEmpleado|pctPublico
 */
public final class TarifaArchivo {

    private static final String TIPO = "TARIFA";
    private static final int CAMPOS = 6;

    private final Path archivo;

    public TarifaArchivo(Path archivo) {
        this.archivo = archivo;
    }

    public Path getArchivo() { return archivo; }

    public List<TarifaRuta> cargar() {
        List<TarifaRuta> tarifas = new ArrayList<>();
        List<String> lineas = ArchivoLineas.leer(archivo);
        for (int i = 0; i < lineas.size(); i++) {
            List<String> c = ArchivoLineas.dividir(lineas.get(i));
            if (c.size() != CAMPOS || !TIPO.equals(c.get(0))) {
                ArchivoLineas.advertirLineaInvalida(archivo, i + 1, "formato incorrecto");
                continue;
            }
            try {
                tarifas.add(new TarifaRuta(c.get(1),
                        Double.parseDouble(c.get(2).trim()),
                        Double.parseDouble(c.get(3).trim()),
                        Double.parseDouble(c.get(4).trim()),
                        Double.parseDouble(c.get(5).trim())));
            } catch (IllegalArgumentException e) {
                ArchivoLineas.advertirLineaInvalida(archivo, i + 1, e.getMessage());
            }
        }
        return tarifas;
    }

    public Optional<TarifaRuta> buscar(String idRuta) {
        for (TarifaRuta t : cargar()) {
            if (t.getIdRuta().equalsIgnoreCase(idRuta)) return Optional.of(t);
        }
        return Optional.empty();
    }

    /** Agrega la tarifa o, si la ruta ya tenía una, la reemplaza. */
    public void guardar(TarifaRuta nueva) {
        List<TarifaRuta> tarifas = cargar();
        tarifas.removeIf(t -> t.getIdRuta().equalsIgnoreCase(nueva.getIdRuta()));
        tarifas.add(nueva);

        List<String> lineas = new ArrayList<>();
        for (TarifaRuta t : tarifas) {
            lineas.add(ArchivoLineas.unir(TIPO, t.getIdRuta(),
                    String.valueOf(t.getBase()),
                    String.valueOf(t.getPctEstudiante()),
                    String.valueOf(t.getPctEmpleado()),
                    String.valueOf(t.getPctPublico())));
        }
        ArchivoLineas.escribir(archivo, lineas);
    }
}
