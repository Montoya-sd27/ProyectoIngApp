package com.transporte.view;

import com.transporte.model.ReglaNegocioException;
import com.transporte.model.TarifaSolidariaServicio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Ventana de interfaz gráfica para la Historia de Usuario THU-002: Consulta de Tarifas.
 * Permite a los usuarios y administradores consultar el monto final a pagar
 * según el perfil seleccionado (Público General, Estudiante, Empleado) aplicando el
 * patrón de diseño Strategy a través de TarifaSolidariaServicio.
 */
public class ConsultaTarifaVentana extends JFrame {

    // Componentes del formulario de consulta
    private JComboBox<String> cmbRutas;
    private JComboBox<String> cmbRol;
    private JLabel lblTarifaBase;
    private JLabel lblTarifaCalculada;
    private JLabel lblDescripcionDescuento;
    private JButton btnVolver;
    private JButton btnCerrarSesion;

    // Servicios y callbacks de navegación
    private final TarifaSolidariaServicio servicioTarifa;
    private final Runnable onVolver;
    private final Runnable onCerrarSesion;

    // Estructura de datos para almacenar las rutas leídas
    private List<String[]> listaRutas;

    /**
     * Constructor por defecto sin callbacks de navegación.
     */
    public ConsultaTarifaVentana() {
        this(null, null);
    }

    /**
     * Constructor principal con callbacks para orquestación mediante Mediator (AppNavegacion).
     *
     * @param onVolver Callback para regresar a la pantalla de itinerarios.
     * @param onCerrarSesion Callback para cerrar sesión y regresar al login.
     */
    public ConsultaTarifaVentana(Runnable onVolver, Runnable onCerrarSesion) {
        super("Ruta Central - THU-002: Consulta de Tarifas (Estrategia Solidaria)");
        this.onVolver = onVolver;
        this.onCerrarSesion = onCerrarSesion;
        this.servicioTarifa = new TarifaSolidariaServicio();

        this.initComponents();
        this.cargarRutas();
    }

    /**
     * Inicializa y organiza todos los componentes de la interfaz de usuario.
     */
    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 580);
        setMinimumSize(new Dimension(750, 500));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Encabezado superior azul de la aplicación
        add(construirEncabezadoSuperior(), BorderLayout.NORTH);

        // Panel central con fondo gris y tarjeta blanca centrada
        add(construirPanelCentral(), BorderLayout.CENTER);
    }

    /**
     * Construye el encabezado azul superior con botón de cerrar sesión.
     *
     * @return Panel superior configurado.
     */
    private JPanel construirEncabezadoSuperior() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(EstiloUI.AZUL);
        header.setBorder(new EmptyBorder(12, 24, 12, 24));

        JLabel logo = new JLabel("Ruta Central");
        logo.setFont(EstiloUI.FUENTE_TITULO);
        logo.setForeground(Color.WHITE);

        btnCerrarSesion = EstiloUI.botonAzul("Cerrar sesión");
        btnCerrarSesion.setForeground(Color.YELLOW);
        btnCerrarSesion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onCerrarSesion != null) {
                    onCerrarSesion.run();
                }
                dispose();
            }
        });

        header.add(logo, BorderLayout.WEST);
        header.add(btnCerrarSesion, BorderLayout.EAST);
        return header;
    }

    /**
     * Construye el contenedor principal gris que alberga la tarjeta de consulta.
     *
     * @return Panel contenedor central.
     */
    private JPanel construirPanelCentral() {
        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(EstiloUI.GRIS);

        JPanel tarjeta = new JPanel(new BorderLayout(0, 16));
        tarjeta.setBackground(EstiloUI.BLANCO);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 230, 238), 1, true),
                new EmptyBorder(24, 32, 24, 32)
        ));
        tarjeta.setPreferredSize(new Dimension(750, 440));

        // Título e instrucciones dentro de la tarjeta
        JPanel cabeceraTarjeta = new JPanel(new GridLayout(2, 1, 0, 4));
        cabeceraTarjeta.setOpaque(false);

        JLabel titulo = new JLabel("Consulta de Tarifas Solidarias", SwingConstants.CENTER);
        titulo.setFont(EstiloUI.FUENTE_TITULO);
        titulo.setForeground(EstiloUI.AZUL);

        JLabel subtitulo = new JLabel("Seleccione una ruta y perfil para consultar el costo y beneficio aplicado.", SwingConstants.CENTER);
        subtitulo.setFont(EstiloUI.FUENTE);
        subtitulo.setForeground(EstiloUI.AZUL);

        cabeceraTarjeta.add(titulo);
        cabeceraTarjeta.add(subtitulo);

        // Componentes del formulario central
        JPanel pnlFormulario = construirFormularioCampos();

        // Panel de acciones inferior (Botón Volver)
        JPanel pnlAcciones = construirPanelAcciones();

        tarjeta.add(cabeceraTarjeta, BorderLayout.NORTH);
        tarjeta.add(pnlFormulario, BorderLayout.CENTER);
        tarjeta.add(pnlAcciones, BorderLayout.SOUTH);

        fondo.add(tarjeta);
        return fondo;
    }

    /**
     * Construye la grilla con los combos y campos de resultado.
     *
     * @return Panel de campos de entrada y salida.
     */
    private JPanel construirFormularioCampos() {
        JPanel grid = new JPanel(new GridLayout(5, 2, 16, 14));
        grid.setOpaque(false);

        // 1. Selector de Ruta
        JLabel lblRuta = EstiloUI.etiqueta("Seleccionar Ruta:");
        cmbRutas = new JComboBox<>();
        EstiloUI.estilizarCombo(cmbRutas);
        cmbRutas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarCalculo();
            }
        });

        // 2. Selector de Rol / Perfil
        JLabel lblRol = EstiloUI.etiqueta("Perfil de Usuario / Rol:");
        cmbRol = new JComboBox<>(new String[]{"Público General", "Estudiante", "Empleado"});
        EstiloUI.estilizarCombo(cmbRol);
        cmbRol.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarCalculo();
            }
        });

        // 3. Visualizador de Tarifa Base
        JLabel lblBaseTexto = EstiloUI.etiqueta("Tarifa Base Registrada:");
        lblTarifaBase = new JLabel("Bs. 0.00");
        lblTarifaBase.setFont(EstiloUI.FUENTE.deriveFont(Font.BOLD));

        // 4. Visualizador de Tarifa Calculada Final
        JLabel lblCalculadaTexto = EstiloUI.etiqueta("Tarifa Final a Pagar:");
        lblTarifaCalculada = new JLabel("Bs. 0.00");
        lblTarifaCalculada.setFont(EstiloUI.FUENTE_TITULO);
        lblTarifaCalculada.setForeground(EstiloUI.AZUL);

        // 5. Descripción del Descuento / Estrategia
        JLabel lblDescTexto = EstiloUI.etiqueta("Detalle del Beneficio:");
        lblDescripcionDescuento = new JLabel("Seleccione una ruta para calcular.");
        lblDescripcionDescuento.setFont(EstiloUI.FUENTE);
        lblDescripcionDescuento.setForeground(Color.DARK_GRAY);

        grid.add(lblRuta);
        grid.add(cmbRutas);

        grid.add(lblRol);
        grid.add(cmbRol);

        grid.add(lblBaseTexto);
        grid.add(lblTarifaBase);

        grid.add(lblCalculadaTexto);
        grid.add(lblTarifaCalculada);

        grid.add(lblDescTexto);
        grid.add(lblDescripcionDescuento);

        return grid;
    }

    /**
     * Construye el panel inferior con el botón de regreso al menú de administración.
     *
     * @return Panel con botón Volver.
     */
    private JPanel construirPanelAcciones() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnl.setOpaque(false);

        btnVolver = EstiloUI.botonAzul("Volver al Menú");
        btnVolver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onVolver != null) {
                    onVolver.run();
                }
                dispose();
            }
        });

        pnl.add(btnVolver);
        return pnl;
    }

    /**
     * Carga las rutas desde el servicio de datos y las despliega en el JComboBox.
     */
    private void cargarRutas() {
        try {
            listaRutas = servicioTarifa.obtenerRutas();
            cmbRutas.removeAllItems();

            for (String[] r : listaRutas) {
                String id = r.length > 3 ? r[3] : "";
                String origen = r.length > 4 ? r[4] : "";
                String destino = r.length > 5 ? r[5] : "";
                cmbRutas.addItem(id + " - " + origen + " a " + destino);
            }

            if (listaRutas != null && !listaRutas.isEmpty()) {
                cmbRutas.setSelectedIndex(0);
                actualizarCalculo();
            }
        } catch (ReglaNegocioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado al cargar las rutas: " + ex.getMessage(),
                    "Error de Carga", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Recalcula la tarifa dinámicamente según la ruta seleccionada y la estrategia de rol.
     * Aplica la extracción segura de la tarifa base para ignorar estados de texto como 'PROGRAMADO'.
     */
    private void actualizarCalculo() {
        if (listaRutas == null || listaRutas.isEmpty()) {
            return;
        }

        int index = cmbRutas.getSelectedIndex();
        if (index < 0 || index >= listaRutas.size()) {
            return;
        }

        String[] r = listaRutas.get(index);

        // Extracción segura del monto base numérico
        double base = servicioTarifa.extraerTarifaBase(r);

        String rol = (String) cmbRol.getSelectedItem();
        servicioTarifa.setEstrategiaPorRol(rol);

        double tarifaFinal = servicioTarifa.calcular(base);

        lblTarifaBase.setText(String.format("Bs. %.2f", base));
        lblTarifaCalculada.setText(String.format("Bs. %.2f", tarifaFinal));
        lblDescripcionDescuento.setText(servicioTarifa.getDescripcion());
    }
}