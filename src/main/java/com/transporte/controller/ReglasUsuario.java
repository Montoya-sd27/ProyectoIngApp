package com.transporte.controller;

import com.transporte.model.ReglaNegocioException;
import java.util.regex.Pattern;

/** PATRÓN STRATEGY (estrategias concretas): una regla por tipo de dato del usuario. */
public final class ReglasUsuario {

    private ReglasUsuario() { }

    static String limpiar(String texto) {
        return texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
    }

    /** Nombre o apellido: solo letras (con tildes y ñ) y un espacio entre palabras. */
    public static final class Nombre implements ReglaValidacion {
        private static final Pattern SOLO_LETRAS = Pattern.compile("^[\\p{L}\\p{M}]+(?: [\\p{L}\\p{M}]+)*$");
        private final String campo;

        public Nombre(String campo) { this.campo = campo; }

        @Override
        public String validar(String valor) throws ReglaNegocioException {
            String texto = limpiar(valor);
            if (!SOLO_LETRAS.matcher(texto).matches()) {
                throw new ReglaNegocioException("El " + campo + " solo puede tener letras (sin números ni símbolos).");
            }
            if (texto.length() < ValidadorUsuario.NOMBRE_MIN || texto.length() > ValidadorUsuario.NOMBRE_MAX) {
                throw new ReglaNegocioException("El " + campo + " debe tener entre " + ValidadorUsuario.NOMBRE_MIN
                        + " y " + ValidadorUsuario.NOMBRE_MAX + " letras.");
            }
            return texto;
        }
    }

    /** Cédula: solo números, dentro del rango de dígitos y sin empezar por 0. */
    public static final class Cedula implements ReglaValidacion {
        @Override
        public String validar(String valor) throws ReglaNegocioException {
            String texto = limpiar(valor);
            if (!texto.matches("\\d+")) {
                throw new ReglaNegocioException("La cédula solo puede tener números (sin letras, puntos ni guiones).");
            }
            if (texto.length() < ValidadorUsuario.CEDULA_MIN_DIGITOS || texto.length() > ValidadorUsuario.CEDULA_MAX_DIGITOS) {
                throw new ReglaNegocioException("La cédula debe tener entre " + ValidadorUsuario.CEDULA_MIN_DIGITOS
                        + " y " + ValidadorUsuario.CEDULA_MAX_DIGITOS + " dígitos.");
            }
            if (texto.startsWith("0")) {
                throw new ReglaNegocioException("La cédula no puede empezar por 0.");
            }
            return texto;
        }
    }

    /** Correo: cualquier correo con formato usable (nombre@dominio.ext). */
    public static final class Correo implements ReglaValidacion {
        private static final int PARTE_LOCAL_MAX = 64;
        private static final Pattern FORMATO = Pattern.compile(
                "^[A-Za-z0-9]+(?:[._%+-][A-Za-z0-9]+)*"
                + "@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,24}$");

        @Override
        public String validar(String valor) throws ReglaNegocioException {
            String texto = limpiar(valor);
            int arroba = texto.indexOf('@');
            boolean largoValido = texto.length() <= ValidadorUsuario.CORREO_MAX && arroba >= 1 && arroba <= PARTE_LOCAL_MAX;
            if (!largoValido || !FORMATO.matcher(texto).matches()) {
                throw new ReglaNegocioException("El correo no tiene un formato válido (ejemplo: nombre@gmail.com).");
            }
            return texto;
        }
    }
}