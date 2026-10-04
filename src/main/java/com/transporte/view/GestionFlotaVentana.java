package com.transporte.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GestionFlotaVentana extends JFrame {
    private JTextField txtPlaca;
    private JTextField txtModelo;
    private JTextField txtCapacidad;
    private JComboBox<String> cmbEstado;
    private JTable tblAutobuses;
    private DefaultTableModel modeloTabla;
    private JScrollPane scrollTabla;

    private JButton btnAgregar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnCambiarEstado;
    private JButton btnLimpiarSeleccion;

    private final Runnable onCerrarSesion;
    private final Runnable onVolver;

    public GestionFlotaVentana() {
        this(null, null);
    }

    /**
     * @param onCerrarSesion acción del botón "Cerrar sesión" del encabezado (null = sin botón).
     */
    public GestionFlotaVentana(Runnable onCerrarSesion) {
        this(onCerrarSesion, null);
    }

    /**
     * @param onCerrarSesion acción del botón "Cerrar sesión" del encabezado (null = sin botón).
     * @param onVolver       acción del botón "Volver a itinerarios" (null = sin botón).
     */
    public GestionFlotaVentana(Runnable onCerrarSesion, Runnable onVolver) {
        this.onCerrarSesion = onCerrarSesion;
        this.onVolver = onVolver;
        initComponents();
    }

    private void initComponents() {
        setTitle("Ruta Central - Gestión de unidades");
        setSize(1000, 640);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Listener para destildar la tabla al hacer clic en cualquier parte en blanco
        MouseAdapter destildarTablaListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                tblAutobuses.clearSelection();
            }
        };

        // 1. Campos del formulario (fondo gris de la paleta)
        txtPlaca = new JTextField();
        txtModelo = new JTextField();
        txtCapacidad = new JTextField();
        cmbEstado = new JComboBox<>(new String[]{"Operativo", "En Mantenimiento", "Fuera de Servicio"});

        // FILTROS EN TIEMPO REAL
        configurarFiltroTexto((AbstractDocument) txtPlaca.getDocument(), 10, "[a-zA-Z0-9\\-]*", true);
        configurarFiltroTexto((AbstractDocument) txtModelo.getDocument(), 40, "[a-zA-Z0-9\\s\\-\\.]*", false);
        configurarFiltroTexto((AbstractDocument) txtCapacidad.getDocument(), 3, "\\d*", false);

        EstiloUI.estilizarCampo(txtPlaca);
        EstiloUI.estilizarCampo(txtModelo);
        EstiloUI.estilizarCampo(txtCapacidad);
        EstiloUI.estilizarCombo(cmbEstado);

        JPanel pnlFormulario = new JPanel(new GridLayout(2, 4, 12, 4));
        pnlFormulario.setOpaque(false);
        pnlFormulario.add(EstiloUI.etiqueta("Placa / ID:"));
        pnlFormulario.add(EstiloUI.etiqueta("Modelo:"));
        pnlFormulario.add(EstiloUI.etiqueta("Capacidad:"));
        pnlFormulario.add(EstiloUI.etiqueta("Estado:"));
        pnlFormulario.add(txtPlaca);
        pnlFormulario.add(txtModelo);
        pnlFormulario.add(txtCapacidad);
        pnlFormulario.add(cmbEstado);
        pnlFormulario.addMouseListener(destildarTablaListener);

        // 2. Tabla (JTable)
        String[] columnas = {"Placa / ID", "Modelo", "Capacidad", "Estado Operacional"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblAutobuses = new JTable(modeloTabla);
        tblAutobuses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblAutobuses.setRowSelectionAllowed(true);
        tblAutobuses.setColumnSelectionAllowed(false);
        tblAutobuses.setRowHeight(28);
        tblAutobuses.setFont(EstiloUI.FUENTE);
        tblAutobuses.setBackground(EstiloUI.GRIS);
        tblAutobuses.setGridColor(EstiloUI.BLANCO);

        // Renderizador: fila seleccionada en azul con texto blanco, sin marco de foco
        DefaultTableCellRenderer renderizador = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                if (isSelected) {
                    c.setBackground(EstiloUI.AZUL);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }
                setBorder(noFocusBorder);
                return c;
            }
        };
        for (int i = 0; i < tblAutobuses.getColumnCount(); i++) {
            tblAutobuses.getColumnModel().getColumn(i).setCellRenderer(renderizador);
        }

        // Encabezado de la tabla en azul
        DefaultTableCellRenderer cabecera = new DefaultTableCellRenderer();
        cabecera.setBackground(EstiloUI.AZUL);
        cabecera.setForeground(Color.WHITE);
        cabecera.setHorizontalAlignment(SwingConstants.CENTER);
        cabecera.setFont(EstiloUI.FUENTE.deriveFont(Font.BOLD));
        tblAutobuses.getTableHeader().setDefaultRenderer(cabecera);
        tblAutobuses.getTableHeader().setReorderingAllowed(false);

        // Destildar al hacer clic en el espacio vacío debajo de las filas en la tabla
        tblAutobuses.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tblAutobuses.rowAtPoint(e.getPoint());
                if (fila == -1) {
                    tblAutobuses.clearSelection();
                }
            }
        });

        scrollTabla = new JScrollPane(tblAutobuses);
        scrollTabla.setBorder(BorderFactory.createLineBorder(EstiloUI.GRIS, 2));
        scrollTabla.getViewport().setBackground(EstiloUI.GRIS);
        scrollTabla.getViewport().addMouseListener(destildarTablaListener);

        // 3. Botones de acción (verde / rojo de la paleta; azul y amarillo para los secundarios)
        btnAgregar = EstiloUI.botonVerde("Agregar Unidad");
        btnEditar = EstiloUI.botonVerde("Editar Seleccionado");
        btnEliminar = EstiloUI.botonRojo("Eliminar Unidad");
        btnCambiarEstado = EstiloUI.botonAzul("Alternar Estado");
        btnLimpiarSeleccion = EstiloUI.botonAmarillo("Limpiar Selección");

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        pnlBotones.setOpaque(false);
        pnlBotones.add(btnAgregar);
        pnlBotones.add(btnEditar);
        pnlBotones.add(btnEliminar);
        pnlBotones.add(btnCambiarEstado);
        pnlBotones.add(btnLimpiarSeleccion);
        pnlBotones.addMouseListener(destildarTablaListener);

        // 4. Tarjeta blanca sobre fondo gris, con encabezado azul (igual que la pantalla de itinerarios)
        JLabel titulo = new JLabel("Gestión de unidades", SwingConstants.CENTER);
        titulo.setFont(EstiloUI.FUENTE_TITULO);
        titulo.setForeground(EstiloUI.AZUL);
        JLabel subtitulo = new JLabel("Registre y administre la flota de transporte", SwingConstants.CENTER);
        subtitulo.setForeground(EstiloUI.AZUL);
        JPanel textoCabecera = new JPanel(new GridLayout(2, 1));
        textoCabecera.setOpaque(false);
        textoCabecera.add(titulo);
        textoCabecera.add(subtitulo);

        JPanel cabeceraCompleta = new JPanel(new BorderLayout(0, 8));
        cabeceraCompleta.setOpaque(false);
        if (onVolver != null) {
            JButton btnVolver = EstiloUI.botonAzul("← Volver a itinerarios");
            btnVolver.addActionListener(e -> onVolver.run());
            JPanel barraVolver = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            barraVolver.setOpaque(false);
            barraVolver.add(btnVolver);
            cabeceraCompleta.add(barraVolver, BorderLayout.NORTH);
        }
        cabeceraCompleta.add(textoCabecera, BorderLayout.CENTER);

        JPanel norte = new JPanel(new BorderLayout(0, 12));
        norte.setOpaque(false);
        norte.add(cabeceraCompleta, BorderLayout.NORTH);
        norte.add(pnlFormulario, BorderLayout.CENTER);

        JPanel tarjeta = new JPanel(new BorderLayout(0, 12));
        tarjeta.setBackground(EstiloUI.BLANCO);
        tarjeta.setBorder(new EmptyBorder(20, 24, 20, 24));
        tarjeta.add(norte, BorderLayout.NORTH);
        tarjeta.add(scrollTabla, BorderLayout.CENTER);
        tarjeta.add(pnlBotones, BorderLayout.SOUTH);
        tarjeta.addMouseListener(destildarTablaListener);

        JPanel fondo = new JPanel(new BorderLayout());
        fondo.setBackground(EstiloUI.GRIS);
        fondo.setBorder(new EmptyBorder(20, 30, 20, 30));
        fondo.add(tarjeta, BorderLayout.CENTER);
        fondo.addMouseListener(destildarTablaListener);

        add(EstiloUI.encabezado(onCerrarSesion), BorderLayout.NORTH);
        add(fondo, BorderLayout.CENTER);

        // Clic en cualquier borde libre de la ventana destilda la tabla
        this.getContentPane().addMouseListener(destildarTablaListener);
    }

    private void configurarFiltroTexto(AbstractDocument doc, int maxCaracteres, String patronRegex, boolean forzarMayusculas) {
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) return;
                procesar(fb, offset, 0, string, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) return;
                procesar(fb, offset, length, text, attrs);
            }

            private void procesar(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                int longitudFutura = fb.getDocument().getLength() - length + text.length();
                if (text.matches(patronRegex) && longitudFutura <= maxCaracteres) {
                    String textoFinal = forzarMayusculas ? text.toUpperCase() : text;
                    super.replace(fb, offset, length, textoFinal, attrs);
                }
            }
        });
    }

    public void limpiarSeleccionYCampos() {
        tblAutobuses.clearSelection();
        txtPlaca.setText("");
        txtModelo.setText("");
        txtCapacidad.setText("");
        cmbEstado.setSelectedIndex(0);
    }

    public JTextField getTxtPlaca() {
        return txtPlaca;
    }

    public JTextField getTxtModelo() {
        return txtModelo;
    }

    public JTextField getTxtCapacidad() {
        return txtCapacidad;
    }

    public JComboBox<String> getCmbEstado() {
        return cmbEstado;
    }

    public JTable getTablaAutobuses() {
        return tblAutobuses;
    }

    public DefaultTableModel getModeloTabla() {
        return modeloTabla;
    }

    public JButton getBtnAgregar() {
        return btnAgregar;
    }

    public JButton getBtnEditar() {
        return btnEditar;
    }

    public JButton getBtnEliminar() {
        return btnEliminar;
    }

    public JButton getBtnCambiarEstado() {
        return btnCambiarEstado;
    }

    public JButton getBtnLimpiarSeleccion() {
        return btnLimpiarSeleccion;
    }
}