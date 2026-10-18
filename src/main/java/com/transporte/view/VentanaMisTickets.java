package com.transporte.view;

import com.transporte.controller.CobroService;
import com.transporte.model.Ticket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Mis tickets: lista los tickets emitidos al usuario y muestra el ticket seleccionado (por defecto el último). */
public class VentanaMisTickets extends JFrame {
    private JTable tablaTickets;
    private DefaultTableModel modeloTabla;
    private final JTextArea vistaTicket = new JTextArea();
    private final CobroService cobroService;
    private final String correoUsuario;
    private List<Ticket> tickets = List.of();

    public VentanaMisTickets(String correoUsuario) {
        this.correoUsuario = correoUsuario;
        this.cobroService = new CobroService();

        setTitle("Mis Tickets Digitales - Ruta Central");
        setSize(680, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel Superior
        JPanel panelHeader = new JPanel();
        panelHeader.setBackground(new Color(0, 51, 102));
        JLabel lblTitulo = new JLabel("Mis Tickets");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        panelHeader.add(lblTitulo);
        add(panelHeader, BorderLayout.NORTH);

        // Tabla de Tickets
        String[] columnas = {"ID Ticket", "Ruta", "Rol", "Monto (Bs.)", "Fecha y Hora"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
        tablaTickets = new JTable(modeloTabla);
        tablaTickets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaTickets.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) mostrarSeleccionado();
        });
        JScrollPane scrollTabla = new JScrollPane(tablaTickets);
        scrollTabla.setPreferredSize(new Dimension(640, 190));

        // Vista del ticket seleccionado
        vistaTicket.setFont(new Font("Monospaced", Font.PLAIN, 13));
        vistaTicket.setEditable(false);
        vistaTicket.setBorder(new EmptyBorder(10, 14, 10, 14));
        JScrollPane scrollTicket = new JScrollPane(vistaTicket);
        scrollTicket.setBorder(BorderFactory.createTitledBorder("Ticket"));

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setBorder(new EmptyBorder(0, 10, 0, 10));
        centro.add(scrollTabla, BorderLayout.NORTH);
        centro.add(scrollTicket, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        // Cargar datos
        cargarTickets();

        // Botón Cerrar
        JPanel panelInferior = new JPanel();
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        panelInferior.add(btnCerrar);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void cargarTickets() {
        modeloTabla.setRowCount(0);
        tickets = cobroService.obtenerTicketsPorUsuario(correoUsuario);
        for (Ticket t : tickets) {
            modeloTabla.addRow(new Object[]{
                t.getIdTicket(),
                t.getRuta(),
                t.getRolUsuario(),
                String.format("%.2f", t.getMontoCobrado()),
                t.getFechaHora()
            });
        }
        if (tickets.isEmpty()) {
            vistaTicket.setText("Todavía no tiene tickets emitidos.\nReserve un cupo y pague para obtener su ticket digital.");
        } else {
            // el último ticket emitido queda seleccionado: es el que se acaba de imprimir
            int ultimo = tickets.size() - 1;
            tablaTickets.setRowSelectionInterval(ultimo, ultimo);
            tablaTickets.scrollRectToVisible(tablaTickets.getCellRect(ultimo, 0, true));
        }
    }

    private void mostrarSeleccionado() {
        int fila = tablaTickets.getSelectedRow();
        if (fila >= 0 && fila < tickets.size()) {
            vistaTicket.setText(textoTicket(tickets.get(fila)));
            vistaTicket.setCaretPosition(0);
        }
    }

    /** Texto del ticket digital (el mismo que se muestra al pagar y en "Mis tickets"). */
    public static String textoTicket(Ticket ticket) {
        return "====================================\n" +
               "           TICKET DIGITAL          \n" +
               "====================================\n" +
               " ID Ticket : " + ticket.getIdTicket() + "\n" +
               " Usuario   : " + ticket.getCorreoUsuario() + "\n" +
               " Rol       : " + ticket.getRolUsuario() + "\n" +
               " Ruta      : " + ticket.getRuta() + "\n" +
               " Cobrado   : Bs. " + String.format("%.2f", ticket.getMontoCobrado()) + "\n" +
               " Fecha     : " + ticket.getFechaHora() + "\n" +
               "====================================\n" +
               "   ¡Acceso Autorizado - Buen Viaje!";
    }

    public static void mostrarComprobanteTicket(Component padre, Ticket ticket) {
        JTextArea textArea = new JTextArea(textoTicket(ticket));
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        JOptionPane.showMessageDialog(padre, new JScrollPane(textArea), "Ticket Emitido Exitosamente", JOptionPane.INFORMATION_MESSAGE);
    }
}
