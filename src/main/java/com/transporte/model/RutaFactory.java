package com.transporte.model;

import java.util.Arrays;
import java.util.List;

// PATRÓN FACTORY METHOD: centraliza la creación de rutas según su tipo, de modo que el resto del sistema no depende de las clases concretas RutaUrbana / RutaExtraurbana
public final class RutaFactory {

    public enum TipoRuta { URBANA, EXTRAURBANA }

    private RutaFactory() { }

    public static Ruta crear(TipoRuta tipo, String id, String origen, String destino, String extra) {
        switch (tipo) {
            case URBANA:
                List<String> paradas = extra == null || extra.isBlank()
                        ? List.of()
                        : Arrays.stream(extra.split(",")).map(String::trim).toList();
                return new RutaUrbana(id, origen, destino, paradas);
            case EXTRAURBANA:
                return new RutaExtraurbana(id, origen, destino, extra);
            default:
                throw new IllegalArgumentException("Tipo de ruta desconocido: " + tipo);
        }
    }
}
