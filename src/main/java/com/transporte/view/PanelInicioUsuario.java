package com.transporte.view;

import com.transporte.controller.RecargaController;
import com.transporte.controller.RutasDisponiblesController;
import com.transporte.model.Itinerario;
import com.transporte.model.RutaFactory.TipoRuta;
import com.transporte.model.UsuarioConSaldo;
import java.awt.*;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Contenido de la pantalla de inicio del estudiante y del empleado:
 *  - saludo con el nombre y el rol,
 *  - selector de tipo de ruta (urbana / extraurbana),
 *  - lista de rutas disponibles con sus cupos,
 *  - abajo a la izquierda, el saldo en Bs con el botón para recargarlo.
 */
public class PanelInicioUsuario extends JPanel {

    private static final Locale ES_VE = new Locale("es", "VE");
    private static final Color VERDE_CUPOS = new Color(0x1E9E1E);
    private static final Color ROJO_CUPOS = new Color(0xE02020);
    private static final Color GRIS_TEXTO = new Color(0x8C7F7F);

    private final UsuarioConSaldo usuario;
    private final RutasDisponiblesController rutas;
    private final RecargaController recarga;

    private final BotonTipoRuta btnUrbana = new BotonTipoRuta("Ruta Urbana", "Área metropolitana");
    private final BotonTipoRuta btnExtraurbana = new BotonTipoRuta("Ruta Extraurbana", "Zonas periféricas");
    private final JLabel lblTituloRutas = new JLabel();
    private final JPanel listaRutas = new JPanel();
    private final JScrollPane scrollRutas = new JScrollPane(listaRutas);
    private final JLabel lblSaldo = new JLabel();
    private final EstiloUI.BotonRedondeado btnRecargar = EstiloUI.botonAmarillo("Recargar saldo");

    public PanelInicioUsuario(UsuarioConSaldo usuario, RutasDisponiblesController rutas, RecargaController recarga) {
        this.usuario = usuario;
        this.rutas = rutas;
        this.recarga = recarga;
        setLayout(new BorderLayout(0, 12));
        setBackground(EstiloUI.GRIS);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel arriba = new JPanel();
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.setOpaque(false);
        arriba.add(crearTarjetaSaludo());
        arriba.add(Box.createVerticalStrut(12));
        arriba.add(crearTarjetaTipoRuta());

        add(arriba, BorderLayout.NORTH);
        add(crearTarjetaRutas(), BorderLayout.CENTER);
        add(crearBarraSaldo(), BorderLayout.SOUTH);

        actualizarSaldo();
        mostrarRutas(TipoRuta.URBANA);
    }

    // ---------- Saludo ----------

    // tarjeta blanca con "Hola, nombre" y el rol
    private JPanel crearTarjetaSaludo() {
        JLabel saludo = new JLabel("Hola, " + usuario.getNombre());
        saludo.setFont(EstiloUI.FUENTE_TITULO);
        saludo.setForeground(EstiloUI.AZUL);
        saludo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel rol = new JLabel(usuario.getRol());
        rol.setFont(EstiloUI.FUENTE.deriveFont(Font.BOLD, 15f));
        rol.setForeground(Color.GRAY);
        rol.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tarjeta = tarjetaBlanca(new EmptyBorder(14, 18, 14, 18));
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.add(saludo);
        tarjeta.add(Box.createVerticalStrut(2));
        tarjeta.add(rol);
        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tarjeta;
    }

    // ---------- Tipo de ruta ----------

    private JPanel crearTarjetaTipoRuta() {
        JLabel titulo = new JLabel("Tipo de Ruta");
        titulo.setFont(EstiloUI.FUENTE.deriveFont(18f));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnUrbana.addActionListener(e -> mostrarRutas(TipoRuta.URBANA));
        btnExtraurbana.addActionListener(e -> mostrarRutas(TipoRuta.EXTRAURBANA));
        JPanel botones = new JPanel(new GridLayout(1, 2, 10, 0));
        botones.setOpaque(false);
        botones.add(btnUrbana);
        botones.add(btnExtraurbana);
        botones.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tarjeta = tarjetaBlanca(new EmptyBorder(14, 18, 16, 18));
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(botones);
        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tarjeta;
    }

    // ---------- Lista de rutas ----------

    private JPanel crearTarjetaRutas() {
        lblTituloRutas.setFont(EstiloUI.FUENTE.deriveFont(20f));
        lblTituloRutas.setBorder(new EmptyBorder(0, 0, 8, 0));

        listaRutas.setLayout(new BoxLayout(listaRutas, BoxLayout.Y_AXIS));
        listaRutas.setBackground(EstiloUI.BLANCO);
        scrollRutas.setBorder(null);
        scrollRutas.getViewport().setBackground(EstiloUI.BLANCO);
        scrollRutas.getVerticalScrollBar().setUnitIncrement(16);
        scrollRutas.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel tarjeta = tarjetaBlanca(new EmptyBorder(14, 18, 10, 18));
        tarjeta.setLayout(new BorderLayout());
        tarjeta.add(lblTituloRutas, BorderLayout.NORTH);
        tarjeta.add(scrollRutas, BorderLayout.CENTER);
        return tarjeta;
    }

    // muestra las rutas del tipo elegido y marca el botón correspondiente
    private void mostrarRutas(TipoRuta tipo) {
        boolean urbana = tipo == TipoRuta.URBANA;
        btnUrbana.setSeleccionado(urbana);
        btnExtraurbana.setSeleccionado(!urbana);
        lblTituloRutas.setText(urbana ? "Rutas Urbanas" : "Rutas Extraurbanas");

        listaRutas.removeAll();
        List<Itinerario> disponibles = rutas.disponibles(tipo);
        if (disponibles.isEmpty()) {
            JLabel vacio = new JLabel("No hay rutas " + (urbana ? "urbanas" : "extraurbanas") + " disponibles por ahora.");
            vacio.setFont(EstiloUI.FUENTE);
            vacio.setForeground(GRIS_TEXTO);
            vacio.setBorder(new EmptyBorder(8, 0, 8, 0));
            vacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            listaRutas.add(vacio);
        }
        for (Itinerario it : disponibles) {
            listaRutas.add(crearFilaRuta(it));
        }
        listaRutas.revalidate();
        listaRutas.repaint();
        SwingUtilities.invokeLater(() -> scrollRutas.getVerticalScrollBar().setValue(0));
    }

    // una ruta: nombre, punto de salida y horario a la izquierda; cupos y botón a la derecha
    private JPanel crearFilaRuta(Itinerario it) {
        JLabel nombre = new JLabel(it.getRuta().getIdRuta() + " " + it.getRuta().getDestino());
        nombre.setFont(EstiloUI.FUENTE.deriveFont(Font.PLAIN, 16f));
        nombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel salida = new JLabel(it.getRuta().getOrigen(), new Icono(Icono.Forma.PIN, GRIS_TEXTO), SwingConstants.LEFT);
        JLabel horario = new JLabel(textoHorario(it), new Icono(Icono.Forma.RELOJ, GRIS_TEXTO), SwingConstants.LEFT);
        for (JLabel l : new JLabel[]{salida, horario}) {
            l.setFont(EstiloUI.FUENTE.deriveFont(13f));
            l.setForeground(GRIS_TEXTO);
            l.setIconTextGap(6);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        JPanel datos = new JPanel();
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
        datos.setOpaque(false);
        datos.add(nombre);
        datos.add(Box.createVerticalStrut(2));
        datos.add(salida);
        datos.add(Box.createVerticalStrut(2));
        datos.add(horario);

        int cupos = rutas.cuposDisponibles(it);
        Color colorCupos = cupos > 0 ? VERDE_CUPOS : ROJO_CUPOS;
        JLabel lblCupos = new JLabel(cupos + (cupos == 1 ? " Cupo disponible" : " Cupos disponibles"),
                new Icono(Icono.Forma.PUNTO, colorCupos), SwingConstants.RIGHT);
        lblCupos.setFont(EstiloUI.FUENTE.deriveFont(11f));
        lblCupos.setForeground(colorCupos);
        lblCupos.setIconTextGap(4);
        lblCupos.setAlignmentX(Component.RIGHT_ALIGNMENT);

        EstiloUI.BotonRedondeado reservar = EstiloUI.botonAmarillo("Reservar cupo");
        reservar.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel derecha = new JPanel();
        derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS));
        derecha.setOpaque(false);
        derecha.add(lblCupos);
        derecha.add(Box.createVerticalStrut(4));
        derecha.add(reservar);

        JPanel fila = new JPanel(new BorderLayout(8, 0));
        fila.setBackground(EstiloUI.BLANCO);
        fila.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, EstiloUI.GRIS), new EmptyBorder(8, 0, 8, 4)));
        fila.add(datos, BorderLayout.CENTER);
        fila.add(derecha, BorderLayout.EAST);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));
        return fila;
    }

    // "Lun, 6:00 am - 7:00 am"
    private static String textoHorario(Itinerario it) {
        String dia = EstiloUI.diaEs(it.getHorario().getDia()).substring(0, 3);
        return dia + ", " + hora(it.getHorario().getSalida()) + " - " + hora(it.getHorario().getLlegada());
    }

    private static String hora(LocalTime t) {
        int h = t.getHour() % 12 == 0 ? 12 : t.getHour() % 12;
        return String.format("%d:%02d %s", h, t.getMinute(), t.getHour() < 12 ? "am" : "pm");
    }

    // ---------- Saldo ----------

    // abajo a la izquierda: saldo en Bs y, al lado, el botón de recarga
    private JPanel crearBarraSaldo() {
        lblSaldo.setFont(EstiloUI.FUENTE.deriveFont(Font.BOLD, 16f));
        lblSaldo.setForeground(EstiloUI.AZUL);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        barra.setBackground(EstiloUI.BLANCO);
        barra.setBorder(new EmptyBorder(12, 20, 12, 20));
        barra.add(lblSaldo);
        barra.add(btnRecargar);
        btnRecargar.addActionListener(e -> abrirPagoMovil());

        // el FlowLayout se ajusta al contenido: la barra no se estira a todo el ancho
        JPanel contenedor = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        contenedor.setOpaque(false);
        contenedor.add(barra);
        return contenedor;
    }

    // abre los datos del pago móvil; al validarse un pago se actualiza el saldo que se ve en pantalla
    private void abrirPagoMovil() {
        new VentanaPagoMovil(SwingUtilities.getWindowAncestor(this), usuario, recarga, this::actualizarSaldo)
                .setVisible(true);
    }

    /** Muestra en pantalla el saldo actual del usuario, por ejemplo "Saldo: Bs. 1.250,50". */
    private void actualizarSaldo() {
        lblSaldo.setText(String.format(ES_VE, "Saldo: Bs. %,.2f", usuario.getSaldo()));
    }

    // ---------- Piezas de dibujo ----------

    private static JPanel tarjetaBlanca(EmptyBorder borde) {
        JPanel p = new JPanel();
        p.setBackground(EstiloUI.BLANCO);
        p.setBorder(borde);
        return p;
    }

    /** Botón azul de dos líneas (título y subtítulo); el seleccionado lleva un borde amarillo. */
    private static class BotonTipoRuta extends JButton {
        private boolean seleccionado;

        BotonTipoRuta(String titulo, String subtitulo) {
            super("<html><div style='text-align:center'><b>" + titulo
                    + "</b><br><span style='font-size:10px'>" + subtitulo + "</span></div></html>");
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.PLAIN, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(8, 10, 8, 10));
        }

        void setSeleccionado(boolean valor) {
            this.seleccionado = valor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? EstiloUI.AZUL.darker() : EstiloUI.AZUL);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            if (seleccionado) {
                g2.setColor(EstiloUI.AMARILLO);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Iconos pequeños dibujados a mano: ubicación, reloj y punto de color. */
    private static class Icono implements Icon {
        enum Forma { PIN, RELOJ, PUNTO }

        private static final int TAM = 14;
        private final Forma forma;
        private final Color color;

        Icono(Forma forma, Color color) {
            this.forma = forma;
            this.color = color;
        }

        @Override public int getIconWidth() { return forma == Forma.PUNTO ? 10 : TAM; }
        @Override public int getIconHeight() { return TAM; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            switch (forma) {
                case PIN:
                    g2.fillOval(x + 2, y + 1, 10, 10);
                    g2.fillPolygon(new int[]{x + 3, x + 11, x + 7}, new int[]{y + 8, y + 8, y + 13}, 3);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x + 5, y + 4, 4, 4);
                    break;
                case RELOJ:
                    g2.fillOval(x + 1, y + 1, 12, 12);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(1.4f));
                    g2.drawLine(x + 7, y + 7, x + 7, y + 3);
                    g2.drawLine(x + 7, y + 7, x + 10, y + 7);
                    break;
                default:
                    g2.fillOval(x, y + 2, 9, 9);
            }
            g2.dispose();
        }
    }
}
