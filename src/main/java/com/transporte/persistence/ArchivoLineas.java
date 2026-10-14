package com.transporte.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Lectura y escritura de archivos de texto donde cada línea es un objeto y sus campos
 * van separados por "|". Los caracteres especiales dentro de un campo se escapan
 * (\\ para la barra, \| para el separador, \n y \r para saltos de línea).
 */
public final class ArchivoLineas {

    public static final char SEPARADOR = '|';

    private ArchivoLineas() { }

    // ---------- Formato de una línea ----------

    /** Une los campos en una sola línea, escapando lo necesario. */
    public static String unir(String... campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) sb.append(SEPARADOR);
            sb.append(escapar(campos[i]));
        }
        return sb.toString();
    }

    /** Separa una línea en sus campos, deshaciendo el escape. */
    public static List<String> dividir(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '\\' && i + 1 < linea.length()) {
                char siguiente = linea.charAt(++i);
                switch (siguiente) {
                    case 'n': actual.append('\n'); break;
                    case 'r': actual.append('\r'); break;
                    default: actual.append(siguiente);
                }
            } else if (c == SEPARADOR) {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos;
    }

    private static String escapar(String texto) {
        if (texto == null) return "";
        StringBuilder sb = new StringBuilder(texto.length() + 4);
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case SEPARADOR: sb.append("\\|"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    // ---------- Acceso al archivo ----------

    /** Líneas no vacías del archivo. Si el archivo todavía no existe devuelve una lista vacía. */
    public static List<String> leer(Path archivo) {
        if (!Files.exists(archivo)) return new ArrayList<>();
        try {
            List<String> todas = new ArrayList<>(Files.readAllLines(archivo, StandardCharsets.UTF_8));
            // algunos editores añaden una marca BOM al inicio del archivo
            if (!todas.isEmpty() && todas.get(0).startsWith("\uFEFF")) {
                todas.set(0, todas.get(0).substring(1));
            }
            List<String> lineas = new ArrayList<>();
            for (String linea : todas) {
                if (!linea.trim().isEmpty()) lineas.add(linea);
            }
            return lineas;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + archivo, e);
        }
    }

    /**
     * Reemplaza el contenido del archivo por las líneas indicadas. Se escribe primero en un
     * archivo temporal y luego se renombra, para no dejar el archivo a medias si algo falla.
     */
    public static void escribir(Path archivo, List<String> lineas) {
        try {
            Path carpeta = archivo.toAbsolutePath().getParent();
            if (carpeta != null) Files.createDirectories(carpeta);
            Path temporal = archivo.resolveSibling(archivo.getFileName() + ".tmp");
            Files.write(temporal, lineas, StandardCharsets.UTF_8);
            try {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo escribir " + archivo, e);
        }
    }

    /** Informa por consola una línea que no se pudo interpretar (se ignora y se sigue con las demás). */
    public static void advertirLineaInvalida(Path archivo, int numeroLinea, String motivo) {
        System.err.println("[Persistencia] " + archivo.getFileName() + ", registro " + numeroLinea
                + " ignorada: " + motivo);
    }
}
