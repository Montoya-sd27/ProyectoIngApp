package com.transporte.persistence;

import com.transporte.model.Usuario;
import com.transporte.model.UsuarioConSaldo;
import com.transporte.model.UsuarioFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Guarda y lee los usuarios (registro e inicio de sesión) en un archivo de texto.
 * Una línea por usuario: USUARIO|rol|nombre|apellido|cedula|correo|contrasena
 * Los estudiantes y empleados llevan además un último campo con su saldo en Bs: ...|contrasena|saldo
 */
public final class UsuarioArchivo {

    private static final String TIPO = "USUARIO";
    private static final int CAMPOS = 7;
    private static final int CAMPOS_CON_SALDO = 8;

    private final Path archivo;

    public UsuarioArchivo(Path archivo) {
        this.archivo = archivo;
    }

    public Path getArchivo() { return archivo; }

    public List<Usuario> cargar() {
        List<Usuario> usuarios = new ArrayList<>();
        Set<String> correos = new HashSet<>();
        Set<String> cedulas = new HashSet<>();
        List<String> lineas = ArchivoLineas.leer(archivo);
        for (int i = 0; i < lineas.size(); i++) {
            List<String> c = ArchivoLineas.dividir(lineas.get(i));
            if ((c.size() != CAMPOS && c.size() != CAMPOS_CON_SALDO) || !TIPO.equals(c.get(0))) {
                ArchivoLineas.advertirLineaInvalida(archivo, i + 1, "formato incorrecto");
                continue;
            }
            float saldo = 0f;
            if (c.size() == CAMPOS_CON_SALDO) {
                try {
                    saldo = Float.parseFloat(c.get(7).trim().replace(',', '.'));
                } catch (NumberFormatException e) {
                    saldo = -1f;
                }
                if (!(saldo >= 0)) {
                    ArchivoLineas.advertirLineaInvalida(archivo, i + 1, "saldo inválido: " + c.get(7));
                    continue;
                }
            }
            Usuario u = crear(c.get(1), c.get(2), c.get(3), c.get(4), c.get(5), c.get(6), saldo);
            if (u == null) {
                ArchivoLineas.advertirLineaInvalida(archivo, i + 1, "rol desconocido: " + c.get(1));
                continue;
            }
            if (!correos.add(u.getCorreo().toLowerCase()) || !cedulas.add(u.getCedula())) {
                ArchivoLineas.advertirLineaInvalida(archivo, i + 1, "correo o cédula repetidos");
                continue;
            }
            usuarios.add(u);
        }
        return usuarios;
    }

    public void guardar(List<Usuario> usuarios) {
        List<String> lineas = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u instanceof UsuarioConSaldo) {
                lineas.add(ArchivoLineas.unir(TIPO, u.getRol(), u.getNombre(), u.getApellido(),
                        u.getCedula(), u.getCorreo(), u.getContrasena(),
                        String.valueOf(((UsuarioConSaldo) u).getSaldo())));
            } else {
                lineas.add(ArchivoLineas.unir(TIPO, u.getRol(), u.getNombre(), u.getApellido(),
                        u.getCedula(), u.getCorreo(), u.getContrasena()));
            }
        }
        ArchivoLineas.escribir(archivo, lineas);
    }

private static Usuario crear(String rol, String nombre, String apellido, String cedula,
                                 String correo, String contrasena, float saldo) {
        return UsuarioFactory.crear(rol, nombre, apellido, cedula, correo, contrasena, saldo);
    }
}
