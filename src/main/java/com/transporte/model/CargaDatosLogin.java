package com.transporte.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Base de Datos" en memoria
public class CargaDatosLogin {
    
    //lista todos los usuarios
    private static List<Usuario> listaUsuarios = new ArrayList<>();

    //  agregar un usuario nuevo a la lista
    public static void agregarUsuario(Usuario nuevoUsuario) {
        listaUsuarios.add(nuevoUsuario);
    }

    // buscamos un usuario por su correo
    public static Usuario buscarPorCorreo(String correo) {
        
        for (Usuario u : listaUsuarios) {
            if (u.getCorreo().equalsIgnoreCase(correo)) { //devolvemos ese usuario
                return u;
            }
        }
        return null;//no lo encontramos
    }

    /** Copia de solo lectura de todos los usuarios registrados (para el backoffice). */
    public static List<Usuario> listarUsuarios() {
        return Collections.unmodifiableList(new ArrayList<>(listaUsuarios));
    }

    /** Vacía la "base de datos" en memoria. Pensado para las pruebas unitarias (cada prueba parte de cero). */
    public static void reiniciar() {
        listaUsuarios.clear();
    }

    // un usuario ya existe 
    public static boolean existeUsuario(String correo, String cedula) {
        for (Usuario u : listaUsuarios) {
            if (u.getCorreo().equalsIgnoreCase(correo) || u.getCedula().equals(cedula)) {
                return true; // Ya existe
            }
        }
        return false; // no existe
    }
}