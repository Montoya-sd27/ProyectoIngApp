package com.transporte.app;

import com.transporte.controller.AutobusController;
import com.transporte.controller.ItinerarioController;
import com.transporte.controller.UsuarioAdminController;
import com.transporte.model.Autobus;
import com.transporte.model.Administrador;
import com.transporte.model.FlotaModel;
import com.transporte.model.Usuario;
import com.transporte.repository.Catalogo;
import com.transporte.repository.ItinerarioRepositorio;
import com.transporte.repository.ItinerarioRepositorioMemoria;
import com.transporte.view.GestionFlotaVentana;
import com.transporte.view.ItinerarioView;
import com.transporte.view.NavegacionAdmin;
import com.transporte.view.RegistroUsuarioAdminVentana;
import com.transporte.view.VentanaBienvenida;
import com.transporte.view.VentanaLogin;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * PATRÓN MEDIATOR + SINGLETON: único punto que conecta los módulos entre sí.
 * Es dueño de los datos compartidos (FlotaModel, repositorio de itinerarios) y decide
 * qué ventana se abre, de modo que ningún módulo llama directamente a las pantallas de otro.
 *
 *  - Login: {@link #mostrarLogin()} abre la ventana de inicio de sesión; al autenticarse, el
 *           Administrador entra directo a Gestión de itinerarios (desde ahí llega a Gestión de unidades
 *           y al backoffice de registro de usuarios). Los demás roles ven una pantalla de bienvenida.
 */
public final class AppNavegacion implements NavegacionAdmin {

    private static AppNavegacion instancia;

    // ---- Datos compartidos entre módulos (fuente única de verdad) ----
    private final FlotaModel flota = new FlotaModel();
    private final Catalogo catalogo = Catalogo.conDatosDemo(flota);
    private final ItinerarioRepositorio itinerarios = ItinerarioRepositorioMemoria.getInstance();

    private VentanaLogin ventanaLogin;
    private ItinerarioView ventanaItinerarios;
    private GestionFlotaVentana ventanaFlota;
    private RegistroUsuarioAdminVentana ventanaUsuarios;

    private AppNavegacion() { }

    public static synchronized AppNavegacion getInstance() {
        if (instancia == null) instancia = new AppNavegacion();
        return instancia;
    }

    public FlotaModel getFlota() { return flota; }

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
        if (usuario instanceof Administrador) {
            ventanaLogin.dispose();
            ventanaLogin = null;
            entrarComoAdministrador();
        } else {
            // Estudiante, empleado y conductor aún no tienen pantallas propias: al cerrar la bienvenida se vuelve al login.
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
        ventanaUsuarios = new RegistroUsuarioAdminVentana(new UsuarioAdminController(), this);
        ventanaUsuarios.setVisible(true);
    }

    /** Vuelve a itinerarios: cierra unidades y backoffice (si están abiertos) y trae itinerarios al frente. */
    @Override
    public void irAItinerarios() {
        if (ventanaFlota != null) { ventanaFlota.dispose(); ventanaFlota = null; }
        if (ventanaUsuarios != null) { ventanaUsuarios.dispose(); ventanaUsuarios = null; }
        entrarComoAdministrador();
    }

    @Override
    public void cerrarSesion() {
        if (ventanaFlota != null) { ventanaFlota.dispose(); ventanaFlota = null; }
        if (ventanaUsuarios != null) { ventanaUsuarios.dispose(); ventanaUsuarios = null; }
        if (ventanaItinerarios != null) { ventanaItinerarios.dispose(); ventanaItinerarios = null; }
        mostrarLogin();
    }
}
