package com.transporte.persistence;

import com.transporte.model.Administrador;
import com.transporte.model.Empleado;
import com.transporte.model.Estudiante;
import com.transporte.model.Usuario;
import com.transporte.model.UsuarioConductor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Guarda y lee los usuarios (registro e inicio de sesión) en un archivo de texto.
 * Una línea por usuario: USUARIO|rol|nombre|apellido|cedula|correo|contrasena
 */
public final class UsuarioArchivo {

    private static final String TIPO = "USUARIO";
    private static final int CAMPOS = 7;

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
            if (c.size() != CAMPOS || !TIPO.equals(c.get(0))) {
                ArchivoLineas.advertirLineaInvalida(archivo, i + 1, "formato incorrecto");
                continue;
            }
            Usuario u = crear(c.get(1), c.get(2), c.get(3), c.get(4), c.get(5), c.get(6));
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
            lineas.add(ArchivoLineas.unir(TIPO, u.getRol(), u.getNombre(), u.getApellido(),
                    u.getCedula(), u.getCorreo(), u.getContrasena()));
        }
        ArchivoLineas.escribir(archivo, lineas);
    }

    private static Usuario crear(String rol, String nombre, String apellido, String cedula,
                                 String correo, String contrasena) {
        switch (rol) {
            case "Administrador": return new Administrador(nombre, apellido, cedula, correo, contrasena);
            case "Estudiante": return new Estudiante(nombre, apellido, cedula, correo, contrasena);
            case "Empleado": return new Empleado(nombre, apellido, cedula, correo, contrasena);
            case "Conductor": return new UsuarioConductor(nombre, apellido, cedula, correo, contrasena);
            default: return null;
        }
    }
}
