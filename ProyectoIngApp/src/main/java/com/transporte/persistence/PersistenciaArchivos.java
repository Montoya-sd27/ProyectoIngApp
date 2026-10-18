package com.transporte.persistence;

import com.transporte.model.Autobus;
import com.transporte.model.CargaDatosLogin;
import com.transporte.model.FlotaModel;
import com.transporte.repository.ItinerarioRepositorioArchivo;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * Conecta los datos de la aplicación con los archivos de texto de una carpeta:
 *   usuarios.txt  -> usuarios (registro e inicio de sesión)
 *   unidades.txt  -> unidades de la flota
 *   rutas.txt     -> itinerarios (ruta, chofer, unidad y horario)
 *   pagos_movil.txt -> pagos móviles recibidos, contra los que se validan las recargas de saldo
 *
 * Cada método "activar..." primero lee el archivo (si existe) y deja los datos cargados;
 * desde ese momento cualquier cambio vuelve a escribir el archivo correspondiente.
 */
public final class PersistenciaArchivos {

    /** Propiedad del sistema para cambiar la carpeta de datos (-Druta.datos=...). */
    public static final String PROPIEDAD_CARPETA = "ruta.datos";
    public static final String CARPETA_POR_DEFECTO = "datos";

    public static final String ARCHIVO_USUARIOS = "usuarios.txt";
    public static final String ARCHIVO_UNIDADES = "unidades.txt";
    public static final String ARCHIVO_RUTAS = "rutas.txt";
    public static final String ARCHIVO_PAGOS = "pagos_movil.txt";

    private final Path carpeta;

    public PersistenciaArchivos(Path carpeta) {
        this.carpeta = carpeta;
    }

    /** Usa la carpeta "datos" junto al lugar desde donde se ejecuta el programa. */
    public static PersistenciaArchivos porDefecto() {
        return new PersistenciaArchivos(Path.of(System.getProperty(PROPIEDAD_CARPETA, CARPETA_POR_DEFECTO)));
    }

    public Path getCarpeta() { return carpeta; }

    /** Carga los usuarios guardados y registra cada nuevo usuario en usuarios.txt. */
    public void activarUsuarios() {
        UsuarioArchivo almacen = new UsuarioArchivo(carpeta.resolve(ARCHIVO_USUARIOS));
        CargaDatosLogin.restaurar(almacen.cargar());
        CargaDatosLogin.establecerOyente(() ->
                guardarSinFallar("los usuarios", () -> almacen.guardar(CargaDatosLogin.listarUsuarios())));
    }

    /** Carga las unidades guardadas en la flota y guarda en unidades.txt cada cambio posterior. */
    public void activarUnidades(FlotaModel flota) {
        UnidadArchivo almacen = new UnidadArchivo(carpeta.resolve(ARCHIVO_UNIDADES));
        for (Autobus unidad : almacen.cargar()) {
            try {
                flota.agregarAutobus(unidad);
            } catch (IllegalArgumentException e) {
                System.err.println("[Persistencia] Unidad " + unidad.getPlaca() + " ignorada: " + e.getMessage());
            }
        }
        flota.agregarOyente(() ->
                guardarSinFallar("las unidades", () -> almacen.guardar(flota.getListaAutobuses())));
    }

    /**
     * Carga los itinerarios guardados (enlazados con las unidades de la flota) y los guarda
     * en rutas.txt. Debe llamarse después de {@link #activarUnidades(FlotaModel)}.
     */
    public ItinerarioRepositorioArchivo activarItinerarios(FlotaModel flota) {
        ItinerarioRepositorioArchivo repositorio =
                new ItinerarioRepositorioArchivo(carpeta.resolve(ARCHIVO_RUTAS), flota);
        // si cambia una unidad (placa, modelo, estado...) los itinerarios que la usan se vuelven a guardar
        flota.agregarOyente(repositorio::persistir);
        return repositorio;
    }

    /** Deja listo el archivo de pagos móviles (si no existe lo crea con pagos de ejemplo) y lo devuelve. */
    public PagoMovilArchivo activarPagos() {
        PagoMovilArchivo pagos = new PagoMovilArchivo(carpeta.resolve(ARCHIVO_PAGOS));
        try {
            pagos.crearSiNoExiste();
        } catch (UncheckedIOException e) {
            System.err.println("[Persistencia] No se pudo crear el archivo de pagos: " + e.getMessage());
        }
        return pagos;
    }

    private static void guardarSinFallar(String que, Runnable guardado) {
        try {
            guardado.run();
        } catch (UncheckedIOException e) {
            System.err.println("[Persistencia] No se pudieron guardar " + que + ": " + e.getMessage());
        }
    }
}
