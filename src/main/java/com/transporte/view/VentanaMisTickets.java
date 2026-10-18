package com.transporte.view;

import com.transporte.controller.CobroService;
import com.transporte.model.Ticket;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaMisTickets extends JFrame {
    private JTable tablaTickets;
    private DefaultTableModel modeloTabla;
    private CobroService cobroService;
    private String correoUsuario;

    public VentanaMisTickets(String correoUsuario) {
        this.correoUsuario = correoUsuario;
        this.cobroService = new CobroService();

        setTitle("Mis Tickets Digitilizados - Ruta Central");
        setSize(650, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel Superior
        JPanel panelHeader = new JPanel();
        panelHeader.setBackground(new Color(0, 51, 102));
        JLabel lblTitulo = new JLabel("Historial de Tickets Emitidos");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        panelHeader.add(lblTitulo);
        add(panelHeader, BorderLayout.NORTH);

        // Tabla de Tickets
        String[] columnas = {"ID Ticket", "Ruta", "Rol", "Monto ($)", "Fecha y Hora"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaTickets = new JTable(modeloTabla);
        add(new JScrollPane(tablaTickets), BorderLayout.CENTER);

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
        List<Ticket> tickets = cobroService.obtenerTicketsPorUsuario(correoUsuario);
        for (Ticket t : tickets) {
            Object[] fila = {
                t.getIdTicket(),
                t.getRuta(),
                t.getRolUsuario(),
                String.format("%.2f", t.getMontoCobrado()),
                t.getFechaHora()
            };
            modeloTabla.addRow(fila);
        }
    }

    public static void mostrarComprobanteTicket(Component padre, Ticket ticket) {
    String mensaje = "====================================\n" +
                     "           TICKET DIGITAL          \n" +
                     "====================================\n" +
                     " ID Ticket : " + ticket.getIdTicket() + "\n" +
                     " Usuario   : " + ticket.getCorreoUsuario() + "\n" +
                     " Rol       : " + ticket.getRolUsuario() + "\n" +
                     " Ruta      : " + ticket.getRuta() + "\n" +
                     " Cobrado   : $" + String.format("%.2f", ticket.getMontoCobrado()) + "\n" +
                     " Fecha     : " + ticket.getFechaHora() + "\n" +
                     "====================================\n" +
                     "   ¡Acceso Autorizado - Buen Viaje!";

    JTextArea textArea = new JTextArea(mensaje);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    textArea.setEditable(false);

    JOptionPane.showMessageDialog(padre, new JScrollPane(textArea), "Ticket Emitido Exitosamente", JOptionPane.INFORMATION_MESSAGE);
}
}