package com.transporte.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Base de Datos" en memoria
public class CargaDatosLogin {
    
    //lista todos los usuarios
    private static List<Usuario> listaUsuarios = new ArrayList<>();

    // acción que se ejecuta cada vez que se registra un usuario (por ejemplo, guardar en archivo)
    private static Runnable oyente;

    /** Define la acción a ejecutar después de cada registro; null la quita. */
    public static void establecerOyente(Runnable nuevoOyente) {
        oyente = nuevoOyente;
    }

    /** Reemplaza todos los usuarios por los indicados, sin avisar al oyente (carga inicial desde archivo). */
    public static void restaurar(List<Usuario> usuarios) {
        listaUsuarios.clear();
        listaUsuarios.addAll(usuarios);
    }

    //  agregar un usuario nuevo a la lista
    public static void agregarUsuario(Usuario nuevoUsuario) {
        listaUsuarios.add(nuevoUsuario);
        notificarCambio();
    }

    /** Avisa de que cambió algún dato de un usuario (por ejemplo su saldo) para que se guarde. */
    public static void notificarCambio() {
        if (oyente != null) oyente.run();
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