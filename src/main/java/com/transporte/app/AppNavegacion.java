package com.transporte.app;

import com.transporte.controller.AutobusController;
import com.transporte.controller.ItinerarioController;
import com.transporte.controller.RecargaController;
import com.transporte.controller.RutasDisponiblesController;
import com.transporte.controller.TarifaConfiguracionController;
import com.transporte.controller.UsuarioAdminController;
import com.transporte.model.Autobus;
import com.transporte.model.Administrador;
import com.transporte.model.FlotaModel;
import com.transporte.model.Ruta;
import com.transporte.model.Usuario;
import com.transporte.model.UsuarioConSaldo;
import com.transporte.repository.Catalogo;
import com.transporte.repository.ItinerarioRepositorio;
import com.transporte.view.ConsultaTarifaVentana;
import com.transporte.persistence.PersistenciaArchivos;
import com.transporte.view.GestionFlotaVentana;
import com.transporte.view.GestionTarifasVentana;
import com.transporte.view.ItinerarioView;
import com.transporte.view.NavegacionAdmin;
import com.transporte.view.RegistroUsuarioAdminVentana;
import com.transporte.view.VentanaBienvenida;
import com.transporte.view.VentanaInicioUsuario;
import com.transporte.view.VentanaLogin;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * PATRÓN MEDIATOR + SINGLETON: único punto que conecta los módulos entre sí.
 * Es dueño de los datos compartidos (FlotaModel, repositorio de itinerarios) y decide
 * qué ventana se abre, de modo que ningún módulo llama directamente a las pantallas de otro.
 *
 *  - Login: {@link #mostrarLogin()} abre la ventana de inicio de sesión; al autenticarse, el
 *           Administrador entra directo a Gestión de itinerarios (desde ahí llega a Gestión de unidades
 *           y al backoffice de registro de usuarios). El estudiante y el empleado entran a su pantalla
 *           de inicio y el conductor ve una pantalla de bienvenida.
 */
public final class AppNavegacion implements NavegacionAdmin {

    private static AppNavegacion instancia;

    // ---- Datos compartidos entre módulos (fuente única de verdad) ----
    private final FlotaModel flota = new FlotaModel();
    private final Catalogo catalogo = Catalogo.conDatosDemo(flota);
    private final ItinerarioRepositorio itinerarios;
    private final RecargaController recarga;
    private final TarifaConfiguracionController tarifas;

    private VentanaLogin ventanaLogin;
    private ItinerarioView ventanaItinerarios;
    private GestionFlotaVentana ventanaFlota;
    private RegistroUsuarioAdminVentana ventanaUsuarios;
    private GestionTarifasVentana ventanaTarifas;
    private ConsultaTarifaVentana ventanaConsultaTarifas;
    private Usuario usuarioActual;   // quien inició sesión (el backoffice exige que sea Administrador)

    private AppNavegacion() {
        // Los datos se leen de la carpeta "datos" al arrancar y se guardan en cada cambio.
        PersistenciaArchivos persistencia = PersistenciaArchivos.porDefecto();
        persistencia.activarUsuarios();
        persistencia.activarUnidades(flota);
        itinerarios = persistencia.activarItinerarios(flota);
        recarga = new RecargaController(persistencia.activarPagos());
        tarifas = new TarifaConfiguracionController(this::rutasConfigurables, persistencia.activarTarifas());
    }

    public static synchronized AppNavegacion getInstance() {
        if (instancia == null) instancia = new AppNavegacion();
        return instancia;
    }

    public FlotaModel getFlota() { return flota; }

    /** Rutas urbanas y extraurbanas con tarifa configurable: las del catálogo y las que ya tienen itinerarios. */
    private List<Ruta> rutasConfigurables() {
        List<Ruta> todas = new ArrayList<>(catalogo.getRutas());
        itinerarios.listar().forEach(it -> todas.add(it.getRuta()));
        return todas;
    }

    // ---------- Inicio de sesión ----------
    /** Abre la ventana de login (pantalla de arranque y destino de "Cerrar sesión"). */
    public void mostrarLogin() {
        if (ventanaLogin != null && ventanaLogin.isDisplayable()) {
            ventanaLogin.toFront();
            return;
        }
        ventanaLogin = new VentanaLogin(this::alAutenticar);
        ventanaLogin.setVisible(true);
    }

    /** Se ejecuta cuando el login valida las credenciales: se enruta según el rol. */
    private void alAutenticar(Usuario usuario) {
    usuarioActual = usuario;
        if (usuario instanceof Administrador) {
            ventanaLogin.dispose();
            ventanaLogin = null;
            entrarComoAdministrador();
        } else if (usuario instanceof UsuarioConSaldo) {
            // Estudiante y empleado comparten la misma pantalla de inicio; al cerrarla se vuelve al login.
            ventanaLogin.dispose();
            ventanaLogin = null;
            new VentanaInicioUsuario((UsuarioConSaldo) usuario, new RutasDisponiblesController(itinerarios), recarga,
                    tarifas, this::mostrarLogin).setVisible(true);
        } else {
            // El conductor aún no tiene pantalla propia: al cerrar la bienvenida se vuelve al login.
            ventanaLogin.dispose();
            ventanaLogin = null;
            new VentanaBienvenida(usuario, this::mostrarLogin).setVisible(true);
        }
    }

    // ---------- Flujo del administrador ----------
    /** Pantalla de inicio del administrador: gestión de itinerarios. */
    public void entrarComoAdministrador() {
        if (ventanaItinerarios != null && ventanaItinerarios.isDisplayable()) {
            ventanaItinerarios.toFront();
            return;
        }
        ItinerarioController controller = new ItinerarioController(itinerarios, catalogo);
        ventanaItinerarios = new ItinerarioView(controller, this);
        ventanaItinerarios.setVisible(true);
    }

    /** Abre la gestión de unidades (módulo de flota) sobre el mismo FlotaModel. */
    @Override
    public void irAGestionUnidades() {
        if (ventanaFlota != null && ventanaFlota.isDisplayable()) {
            ventanaFlota.toFront();
            return;
        }
        GestionFlotaVentana vista = new GestionFlotaVentana(() -> {
            int r = JOptionPane.showConfirmDialog(ventanaFlota, "¿Desea cerrar sesión?", "Cerrar sesión",
                    JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) cerrarSesion();
        }, this::irAItinerarios);
        // Cerrar esta ventana NO debe cerrar toda la aplicación.
        vista.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        new AutobusController(flota, vista);

        // La ventana nace con la tabla vacía: se cargan las unidades que ya existen en el modelo.
        for (Autobus b : flota.getListaAutobuses()) {
            vista.getModeloTabla().addRow(new Object[]{b.getPlaca(), b.getModelo(), b.getCapacidad(), b.getEstado()});
        }

        // Al volver, la tabla de itinerarios se refresca (la placa o el estado pudieron cambiar).
        vista.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (ventanaItinerarios != null) ventanaItinerarios.itinerariosActualizados();
            }
        });
        ventanaFlota = vista;
        vista.setVisible(true);
    }

    /** Abre el backoffice donde el administrador registra usuarios (comparten datos con el login). */
    @Override
    public void irABackofficeUsuarios() {
        if (ventanaUsuarios != null && ventanaUsuarios.isDisplayable()) {
            ventanaUsuarios.toFront();
            return;
        }
        ventanaUsuarios = new RegistroUsuarioAdminVentana(new UsuarioAdminController(usuarioActual), this);
        ventanaUsuarios.setVisible(true);
    }

    /** Abre la pantalla de configuración de tarifas de cobro solidario (THU-001). */
    @Override
    public void irAGestionTarifas() {
        if (ventanaTarifas != null && ventanaTarifas.isDisplayable()) {
            ventanaTarifas.toFront();
            return;
        }
        ventanaTarifas = new GestionTarifasVentana(this::irAItinerarios, tarifas);
        ventanaTarifas.setVisible(true);
    }
    /** Abre la pantalla de consulta de tarifas (THU-002). */
    @Override
    public void irAConsultaTarifas() {
        if (ventanaConsultaTarifas != null && ventanaConsultaTarifas.isDisplayable()) {
            ventanaConsultaTarifas.toFront();
            return;
        }
        try {
            ventanaConsultaTarifas = new ConsultaTarifaVentana(this::irAItinerarios, this::cerrarSesion);
            ventanaConsultaTarifas.setVisible(true);
        } catch (Throwable t) {
            t.printStackTrace();
            JOptionPane.showMessageDialog(ventanaItinerarios,
                    "No se pudo abrir Consulta de Tarifas:\n" + t.toString(),
                    "Error al Cargar Ventana", JOptionPane.ERROR_MESSAGE);
        }
    }
    /** Vuelve a itinerarios: cierra unidades, backoffice y tarifas (si están abiertos) y trae itinerarios al frente. */
    @Override
    public void irAItinerarios() {
        if (ventanaFlota != null) { ventanaFlota.dispose(); ventanaFlota = null; }
        if (ventanaUsuarios != null) { ventanaUsuarios.dispose(); ventanaUsuarios = null; }
        if (ventanaTarifas != null) { ventanaTarifas.dispose(); ventanaTarifas = null; }
        entrarComoAdministrador();
    }

    @Override
    public void cerrarSesion() {
        if (ventanaFlota != null) { ventanaFlota.dispose(); ventanaFlota = null; }
        if (ventanaUsuarios != null) { ventanaUsuarios.dispose(); ventanaUsuarios = null; }
        if (ventanaTarifas != null) { ventanaTarifas.dispose(); ventanaTarifas = null; }
        if (ventanaItinerarios != null) { ventanaItinerarios.dispose(); ventanaItinerarios = null; }
        mostrarLogin();
    }
}
