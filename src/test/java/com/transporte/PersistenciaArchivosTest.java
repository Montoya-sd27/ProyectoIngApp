package com.transporte;

import static org.junit.jupiter.api.Assertions.*;

import com.transporte.controller.ItinerarioController;
import com.transporte.controller.LoginController;
import com.transporte.model.*;
import com.transporte.persistence.ArchivoLineas;
import com.transporte.persistence.PersistenciaArchivos;
import com.transporte.repository.Catalogo;
import com.transporte.repository.ItinerarioRepositorioArchivo;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Pruebas de la persistencia en archivos .txt (cada prueba usa su propia carpeta temporal). */
class PersistenciaArchivosTest {

    @TempDir
    Path carpeta;

    private Path usuarios;
    private Path unidades;
    private Path rutas;

    @BeforeEach
    void preparar() {
        CargaDatosLogin.reiniciar();
        CargaDatosLogin.establecerOyente(null);
        usuarios = carpeta.resolve("usuarios.txt");
        unidades = carpeta.resolve("unidades.txt");
        rutas = carpeta.resolve("rutas.txt");
    }

    @AfterEach
    void limpiar() {
        // los usuarios son estáticos: se deja todo como estaba para no afectar a otras pruebas
        CargaDatosLogin.establecerOyente(null);
        CargaDatosLogin.reiniciar();
    }

    // ---------- Formato de las líneas ----------

    @Test
    void escapaYRecuperaLosCaracteresEspeciales() {
        String linea = ArchivoLineas.unir("A|B", "C\\D", "uno\ndos", null, "ñandú");

        assertFalse(linea.contains("\n"), "cada objeto debe ocupar una sola línea");
        assertEquals(List.of("A|B", "C\\D", "uno\ndos", "", "ñandú"), ArchivoLineas.dividir(linea));
    }

    @Test
    void sinArchivosNoSeCreaNadaAlArrancar() {
        PersistenciaArchivos persistencia = new PersistenciaArchivos(carpeta);
        FlotaModel flota = new FlotaModel();
        persistencia.activarUsuarios();
        persistencia.activarUnidades(flota);
        ItinerarioRepositorioArchivo repo = persistencia.activarItinerarios(flota);

        assertTrue(CargaDatosLogin.listarUsuarios().isEmpty());
        assertTrue(flota.getListaAutobuses().isEmpty());
        assertTrue(repo.listar().isEmpty());
        assertFalse(Files.exists(usuarios));
        assertFalse(Files.exists(unidades));
        assertFalse(Files.exists(rutas));
    }

    // ---------- Usuarios ----------

    @Test
    void losUsuariosRegistradosSobrevivenAUnReinicio() throws Exception {
        new PersistenciaArchivos(carpeta).activarUsuarios();
        LoginController login = new LoginController();
        assertTrue(login.registrarUsuario("Ana", "Pérez", "111", "ana@ucv.ve", "clave", "Estudiante"));
        assertTrue(login.registrarUsuario("Luis", "Gómez", "222", "luis@ucv.ve", "pa|ss", "Conductor"));

        List<String> lineas = Files.readAllLines(usuarios);
        assertEquals(2, lineas.size());
        assertEquals("USUARIO|Estudiante|Ana|Pérez|111|ana@ucv.ve|clave", lineas.get(0));

        // "reinicio" de la aplicación: se vacía la memoria y se vuelve a cargar desde el archivo
        CargaDatosLogin.establecerOyente(null);
        CargaDatosLogin.reiniciar();
        new PersistenciaArchivos(carpeta).activarUsuarios();

        assertTrue(login.iniciarSesion("ana@ucv.ve", "clave") instanceof Estudiante);
        assertTrue(login.iniciarSesion("luis@ucv.ve", "pa|ss") instanceof UsuarioConductor);
        assertNull(login.iniciarSesion("ana@ucv.ve", "otra"));
    }

    @Test
    void elAdministradorPorDefectoNoSeDuplicaAlReiniciar() throws Exception {
        LoginController login = new LoginController();
        new PersistenciaArchivos(carpeta).activarUsuarios();
        login.crearAdministradorPorDefecto();

        CargaDatosLogin.establecerOyente(null);
        CargaDatosLogin.reiniciar();
        new PersistenciaArchivos(carpeta).activarUsuarios();
        login.crearAdministradorPorDefecto();

        assertEquals(1, CargaDatosLogin.listarUsuarios().size());
        assertEquals(1, Files.readAllLines(usuarios).size());
        assertNotNull(login.iniciarSesion(LoginController.ADMIN_CORREO, LoginController.ADMIN_CLAVE));
    }

    // ---------- Unidades ----------

    @Test
    void lasUnidadesSeGuardanAlAgregarEditarCambiarEstadoYEliminar() throws Exception {
        FlotaModel flota = new FlotaModel();
        new PersistenciaArchivos(carpeta).activarUnidades(flota);

        flota.agregarAutobus(new Autobus("abc123", "Encava", 40, "Operativo"));
        flota.agregarAutobus(new Autobus("XYZ789", "Yutong", 45, "Operativo"));
        assertEquals(List.of("UNIDAD|ABC123|Encava|40|Operativo", "UNIDAD|XYZ789|Yutong|45|Operativo"),
                Files.readAllLines(unidades));

        flota.actualizarAutobus(0, new Autobus("ABC123", "Encava", 50, "En Mantenimiento"));
        assertEquals("UNIDAD|ABC123|Encava|50|En Mantenimiento", Files.readAllLines(unidades).get(0));

        // lo que hace el botón "Cambiar estado": modifica la unidad directamente y avisa del cambio
        flota.obtenerAutobus(1).setEstado("Fuera de Servicio");
        flota.notificarCambio();
        assertEquals("UNIDAD|XYZ789|Yutong|45|Fuera de Servicio", Files.readAllLines(unidades).get(1));

        flota.eliminarAutobus(0);
        assertEquals(List.of("UNIDAD|XYZ789|Yutong|45|Fuera de Servicio"), Files.readAllLines(unidades));

        FlotaModel despuesDeReiniciar = new FlotaModel();
        new PersistenciaArchivos(carpeta).activarUnidades(despuesDeReiniciar);
        assertEquals(1, despuesDeReiniciar.getListaAutobuses().size());
        Autobus cargado = despuesDeReiniciar.obtenerAutobus(0);
        assertEquals("XYZ789", cargado.getPlaca());
        assertEquals(45, cargado.getCapacidad());
        assertEquals("Fuera de Servicio", cargado.getEstado());
    }

    @Test
    void lasLineasInvalidasORepetidasDeUnidadesSeIgnoran() throws Exception {
        Files.write(unidades, List.of(
                "UNIDAD|AAA111|Encava|40|Operativo",
                "UNIDAD|AAA111|Repetida|30|Operativo",
                "texto suelto sin formato",
                "UNIDAD|BBB222|Yutong|muchos|Operativo",
                "UNIDAD|CCC333|Yutong|0|Operativo",
                ""));

        FlotaModel flota = new FlotaModel();
        new PersistenciaArchivos(carpeta).activarUnidades(flota);

        assertEquals(1, flota.getListaAutobuses().size());
        assertEquals("AAA111", flota.obtenerAutobus(0).getPlaca());
    }

    // ---------- Rutas (itinerarios) ----------

    private Itinerario crearItinerario(ItinerarioController controller, Autobus bus, Ruta ruta,
                                       DayOfWeek dia, EstadoRecorrido estado) throws Exception {
        Conductor chofer = new Conductor("V-1", "Carlos Pérez", "5ta");
        Horario horario = new Horario(dia, LocalTime.of(7, 0), LocalTime.of(8, 30));
        return controller.crear(ruta, bus, chofer, horario, estado);
    }

    private Ruta rutaUrbana() {
        return RutaFactory.crear(RutaFactory.TipoRuta.URBANA, "RU-01", "UCV", "Plaza Venezuela",
                "Los Chaguaramos, Tamanaco");
    }

    @Test
    void cadaItinerarioSeGuardaEnUnaLineaConRutaChoferYHorarios() throws Exception {
        FlotaModel flota = new FlotaModel();
        Autobus bus = new Autobus("AAA111", "Encava", 40, "Operativo");
        flota.agregarAutobus(bus);
        ItinerarioRepositorioArchivo repo = new ItinerarioRepositorioArchivo(rutas, flota);
        ItinerarioController controller = new ItinerarioController(repo, Catalogo.vacio(flota));

        crearItinerario(controller, bus, rutaUrbana(), DayOfWeek.MONDAY, EstadoRecorrido.PROGRAMADO);

        assertEquals(List.of("RUTA|IT-001|URBANA|RU-01|UCV|Plaza Venezuela|Los Chaguaramos, Tamanaco"
                        + "|V-1|Carlos Pérez|5ta|AAA111|Encava|40|Operativo|MONDAY|07:00|08:30|PROGRAMADO"),
                Files.readAllLines(rutas));

        // "reinicio": un repositorio nuevo lee el mismo archivo
        ItinerarioRepositorioArchivo recargado = new ItinerarioRepositorioArchivo(rutas, flota);
        assertEquals(1, recargado.listar().size());
        Itinerario it = recargado.listar().get(0);
        assertEquals("IT-001", it.getIdItinerario());
        assertTrue(it.getRuta() instanceof RutaUrbana);
        assertEquals(List.of("Los Chaguaramos", "Tamanaco"), ((RutaUrbana) it.getRuta()).getParadasIntermedias());
        assertEquals("Carlos Pérez", it.getConductor().getNombre());
        assertEquals("5ta", it.getConductor().getLicencia());
        assertSame(bus, it.getUnidad());
        assertEquals(DayOfWeek.MONDAY, it.getHorario().getDia());
        assertEquals(LocalTime.of(7, 0), it.getHorario().getSalida());
        assertEquals(LocalTime.of(8, 30), it.getHorario().getLlegada());
        assertEquals(EstadoRecorrido.PROGRAMADO, it.getEstadoRecorrido());
    }

    @Test
    void laRutaExtraurbanaSeRecuperaYElSiguienteIdContinua() throws Exception {
        FlotaModel flota = new FlotaModel();
        Autobus bus = new Autobus("AAA111", "Encava", 40, "Operativo");
        flota.agregarAutobus(bus);
        ItinerarioRepositorioArchivo repo = new ItinerarioRepositorioArchivo(rutas, flota);
        ItinerarioController controller = new ItinerarioController(repo, Catalogo.vacio(flota));

        crearItinerario(controller, bus, rutaUrbana(), DayOfWeek.MONDAY, EstadoRecorrido.PROGRAMADO);
        Ruta extraurbana = RutaFactory.crear(RutaFactory.TipoRuta.EXTRAURBANA, "RE-01", "UCV", "Los Teques", "Los Teques");
        crearItinerario(controller, bus, extraurbana, DayOfWeek.TUESDAY, EstadoRecorrido.DETENIDO);

        ItinerarioRepositorioArchivo recargado = new ItinerarioRepositorioArchivo(rutas, flota);
        assertEquals(2, recargado.listar().size());
        Itinerario segundo = recargado.buscarPorId("IT-002").orElseThrow();
        assertTrue(segundo.getRuta() instanceof RutaExtraurbana);
        assertEquals("Los Teques", ((RutaExtraurbana) segundo.getRuta()).getCiudadDormitorio());
        assertEquals(EstadoRecorrido.DETENIDO, segundo.getEstadoRecorrido());
        assertEquals("IT-003", recargado.siguienteId());
    }

    @Test
    void eliminarUnItinerarioActualizaElArchivo() throws Exception {
        FlotaModel flota = new FlotaModel();
        Autobus bus = new Autobus("AAA111", "Encava", 40, "Operativo");
        flota.agregarAutobus(bus);
        ItinerarioRepositorioArchivo repo = new ItinerarioRepositorioArchivo(rutas, flota);
        ItinerarioController controller = new ItinerarioController(repo, Catalogo.vacio(flota));
        crearItinerario(controller, bus, rutaUrbana(), DayOfWeek.MONDAY, EstadoRecorrido.PROGRAMADO);
        crearItinerario(controller, bus, rutaUrbana(), DayOfWeek.TUESDAY, EstadoRecorrido.PROGRAMADO);

        controller.eliminar("IT-001");

        List<String> lineas = Files.readAllLines(rutas);
        assertEquals(1, lineas.size());
        assertTrue(lineas.get(0).startsWith("RUTA|IT-002|"));
    }

    @Test
    void editarUnaUnidadEnLaFlotaSeReflejaEnLasRutasGuardadas() throws Exception {
        PersistenciaArchivos persistencia = new PersistenciaArchivos(carpeta);
        FlotaModel flota = new FlotaModel();
        persistencia.activarUnidades(flota);
        flota.agregarAutobus(new Autobus("AAA111", "Encava", 40, "Operativo"));
        ItinerarioRepositorioArchivo repo = persistencia.activarItinerarios(flota);
        ItinerarioController controller = new ItinerarioController(repo, Catalogo.vacio(flota));
        crearItinerario(controller, flota.obtenerAutobus(0), rutaUrbana(), DayOfWeek.MONDAY, EstadoRecorrido.PROGRAMADO);

        flota.actualizarAutobus(0, new Autobus("BBB222", "Yutong", 45, "Operativo"));

        assertTrue(Files.readAllLines(rutas).get(0).contains("|BBB222|Yutong|45|Operativo|"));

        // al reiniciar, el itinerario vuelve a enlazarse con la unidad de la flota
        PersistenciaArchivos otra = new PersistenciaArchivos(carpeta);
        FlotaModel flotaNueva = new FlotaModel();
        otra.activarUnidades(flotaNueva);
        ItinerarioRepositorioArchivo repoNuevo = otra.activarItinerarios(flotaNueva);
        assertSame(flotaNueva.obtenerAutobus(0), repoNuevo.listar().get(0).getUnidad());
    }

    @Test
    void siSeEliminaLaUnidadElItinerarioConservaSusDatos() throws Exception {
        PersistenciaArchivos persistencia = new PersistenciaArchivos(carpeta);
        FlotaModel flota = new FlotaModel();
        persistencia.activarUnidades(flota);
        flota.agregarAutobus(new Autobus("AAA111", "Encava", 40, "Operativo"));
        ItinerarioRepositorioArchivo repo = persistencia.activarItinerarios(flota);
        ItinerarioController controller = new ItinerarioController(repo, Catalogo.vacio(flota));
        crearItinerario(controller, flota.obtenerAutobus(0), rutaUrbana(), DayOfWeek.MONDAY, EstadoRecorrido.PROGRAMADO);

        flota.eliminarAutobus(0);

        PersistenciaArchivos otra = new PersistenciaArchivos(carpeta);
        FlotaModel flotaNueva = new FlotaModel();
        otra.activarUnidades(flotaNueva);
        ItinerarioRepositorioArchivo repoNuevo = otra.activarItinerarios(flotaNueva);
        assertTrue(flotaNueva.getListaAutobuses().isEmpty());
        assertEquals(1, repoNuevo.listar().size());
        assertEquals("AAA111", repoNuevo.listar().get(0).getUnidad().getPlaca());
        assertEquals("Encava", repoNuevo.listar().get(0).getUnidad().getModelo());
    }

    @Test
    void lasLineasInvalidasDeRutasSeIgnoran() throws Exception {
        String valida = ArchivoLineas.unir("RUTA", "IT-005", "URBANA", "RU-01", "UCV", "Chacaíto", "",
                "V-1", "Carlos Pérez", "5ta", "AAA111", "Encava", "40", "Operativo",
                "FRIDAY", "09:00", "10:00", "PROGRAMADO");
        String tipoMalo = ArchivoLineas.unir("RUTA", "IT-006", "VOLADORA", "RU-01", "UCV", "Chacaíto", "",
                "V-1", "Carlos Pérez", "5ta", "AAA111", "Encava", "40", "Operativo",
                "FRIDAY", "09:00", "10:00", "PROGRAMADO");
        String horaMala = ArchivoLineas.unir("RUTA", "IT-007", "URBANA", "RU-01", "UCV", "Chacaíto", "",
                "V-1", "Carlos Pérez", "5ta", "AAA111", "Encava", "40", "Operativo",
                "FRIDAY", "11:00", "10:00", "PROGRAMADO");
        Files.write(rutas, List.of(valida, "esto no es una ruta", tipoMalo, horaMala));

        ItinerarioRepositorioArchivo repo = new ItinerarioRepositorioArchivo(rutas, new FlotaModel());

        assertEquals(1, repo.listar().size());
        assertEquals("IT-005", repo.listar().get(0).getIdItinerario());
        assertEquals("IT-006", repo.siguienteId());
    }
}
