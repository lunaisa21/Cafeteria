package Interfaz;

import com.datos.*;
import Operaciones.FachadaCafeteria;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interfaz grafica Swing del sistema de cafeteria.
 * Disenada para cumplir RQ01-NF a RQ05-NF y todos los requerimientos funcionales.
 */
public class VentanaPrincipal extends JFrame {

    private static final Color COLOR_PRIMARIO = new Color(93, 64, 55);
    private static final Color COLOR_ACENTO = new Color(46, 125, 50);
    private static final Color COLOR_PELIGRO = new Color(198, 40, 40);
    private static final Color COLOR_ALERTA_BG = new Color(255, 243, 205);
    private static final Color COLOR_ALERTA_BORDE = new Color(230, 81, 0);
    private static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FUENTE_TEXTO = new Font("Segoe UI", Font.PLAIN, 13);

    private final FachadaCafeteria fachada;

    // Alertas persistentes (RQ05-NF)
    private final DefaultListModel<String> modeloAlertas = new DefaultListModel<>();
    private final JList<String> listaAlertas = new JList<>(modeloAlertas);
    private final JPanel panelAlertas = new JPanel(new BorderLayout(8, 6));
    private final JLabel lblContadorAlertas = new JLabel("Alertas pendientes: 0");

    // Estado de sesion y pestaas
    private final JLabel lblUsuario = new JLabel("Sin sesion");
    private final JTabbedPane tabs = new JTabbedPane();

    // Tab Toma de Pedido (RQ01, RQ02, RQ03, RQ07)
    private final DefaultTableModel modeloMenu = new DefaultTableModel(
            new Object[]{"ID", "Producto", "Descripcion", "Precio", "Tipo"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tablaMenu = new JTable(modeloMenu);
    private List<ItemMenu> itemsVisibles = new ArrayList<>();

    private final DefaultComboBoxModel<String> modeloFiltro = new DefaultComboBoxModel<>(
            new String[]{"Todo disponible", "Solo vegetarianos", "Bebidas frias", "Bebidas calientes"});
    private final JComboBox<String> cmbFiltro = new JComboBox<>(modeloFiltro);

    private final JComboBox<String> cmbAdiciones = new JComboBox<>();
    private List<AdicionDecorator> adicionesDisponibles = new ArrayList<>();
    private Producto productoEnConstruccion;
    private final JLabel lblProductoActual = new JLabel("Seleccione un producto del menu...");
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
    private final JSpinner spnMesa = new JSpinner(new SpinnerNumberModel(1, 1, 40, 1));

    private Pedido pedidoActual;
    private final DefaultTableModel modeloLineasPedido = new DefaultTableModel(
            new Object[]{"Cantidad", "Producto y Adiciones", "Precio Unit.", "Subtotal"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tablaLineasPedido = new JTable(modeloLineasPedido);
    private final JLabel lblTotalesPedido = new JLabel("Subtotal: $0.00 | Descuento: $0.00 | Total: $0.00");

    // Tab Pedidos Activos (RQ05, RQ08)
    private final DefaultTableModel modeloPedidos = new DefaultTableModel(
            new Object[]{"ID", "Mesa", "Estado", "Pago", "Subtotal", "Desc.", "Total"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tablaPedidos = new JTable(modeloPedidos);
    private List<Pedido> listaPedidos = new ArrayList<>();

    // Tab Caja (RQ06, RF09)
    private final JTextArea txtReporteCaja = new JTextArea(16, 50);

    // Tabs Administrativos (RQ03-NF)
    private final DefaultTableModel modeloInventario = new DefaultTableModel(
            new Object[]{"ID", "Ingrediente", "Stock Actual", "Stock Minimo", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tablaInventario = new JTable(modeloInventario);
    private List<Ingrediente> listaIngredientes = new ArrayList<>();

    private final DefaultTableModel modeloPromos = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Dias", "Franja", "% Desc.", "Activa"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tablaPromos = new JTable(modeloPromos);
    private List<Promocion> listaPromos = new ArrayList<>();

    private final DefaultTableModel modeloAdminMenu = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Precio", "Disponible"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tablaAdminMenu = new JTable(modeloAdminMenu);
    private List<ItemMenu> listaTodosItems = new ArrayList<>();

    public VentanaPrincipal(FachadaCafeteria fachada) {
        this.fachada = fachada;
        setTitle("Cafeteria - Sistema de Gestion de Pedidos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 760);
        setLocationRelativeTo(null);

        configurarReceptoresAlertas();
        construirUI();
    }

    private void configurarReceptoresAlertas() {
        Consumer<String> receptorUI = msg -> SwingUtilities.invokeLater(() -> mostrarAlerta(msg));
        fachada.configurarReceptoresAlertas(receptorUI, receptorUI, receptorUI, receptorUI);
    }

    /** Inicia la ventana solicitando autenticacion (RQ03-NF). */
    public void iniciar() {
        if (!mostrarDialogoLogin()) {
            dispose();
            return;
        }
        actualizarVistaPorRol();
        recargarTodo();
        setVisible(true);
    }

    // ---- Construccion UI ----

    private void construirUI() {
        setLayout(new BorderLayout(8, 8));

        // Barra superior
        JPanel barraSuperior = new JPanel(new BorderLayout(10, 10));
        barraSuperior.setBackground(COLOR_PRIMARIO);
        barraSuperior.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel titulo = new JLabel("Cafeteria - Punto de Venta y Administracion");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel derSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derSuperior.setOpaque(false);
        lblUsuario.setForeground(Color.WHITE);
        lblUsuario.setFont(FUENTE_TEXTO);
        JButton btnCambiarUsuario = crearBotonGrande("Cambiar Usuario", new Color(121, 85, 72), 160);
        btnCambiarUsuario.addActionListener(e -> {
            fachada.cerrarSesion();
            if (!mostrarDialogoLogin()) {
                dispose();
                return;
            }
            actualizarVistaPorRol();
            recargarTodo();
        });
        derSuperior.add(lblUsuario);
        derSuperior.add(btnCambiarUsuario);

        barraSuperior.add(titulo, BorderLayout.WEST);
        barraSuperior.add(derSuperior, BorderLayout.EAST);

        // Panel persistente de alertas (RQ05-NF)
        panelAlertas.setBackground(COLOR_ALERTA_BG);
        panelAlertas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ALERTA_BORDE, 2),
                new EmptyBorder(6, 10, 6, 10)
        ));
        lblContadorAlertas.setFont(FUENTE_BOTON);
        lblContadorAlertas.setForeground(COLOR_ALERTA_BORDE);

        listaAlertas.setVisibleRowCount(3);
        listaAlertas.setFont(FUENTE_TEXTO);
        listaAlertas.setBackground(new Color(255, 250, 230));

        JButton btnConfirmarAlerta = crearBotonGrande("Confirmar Alerta Vista", COLOR_ALERTA_BORDE, 210);
        btnConfirmarAlerta.addActionListener(e -> confirmarAlertasSeleccionadas());
        JButton btnConfirmarTodas = crearBotonGrande("Confirmar Todas", COLOR_PRIMARIO, 165);
        btnConfirmarTodas.addActionListener(e -> {
            if (!modeloAlertas.isEmpty()) {
                modeloAlertas.clear();
                actualizarBannerAlertas();
            }
        });

        JPanel botonesAlerta = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        botonesAlerta.setOpaque(false);
        botonesAlerta.add(btnConfirmarAlerta);
        botonesAlerta.add(btnConfirmarTodas);

        panelAlertas.add(lblContadorAlertas, BorderLayout.NORTH);
        panelAlertas.add(new JScrollPane(listaAlertas), BorderLayout.CENTER);
        panelAlertas.add(botonesAlerta, BorderLayout.EAST);
        panelAlertas.setVisible(false);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(barraSuperior, BorderLayout.NORTH);
        norte.add(panelAlertas, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        // Tabs principales
        tabs.setFont(FUENTE_BOTON);
        tabs.addTab("1. Menu y Nuevo Pedido", construirTabNuevoPedido());
        tabs.addTab("2. Pedidos y Estados", construirTabPedidos());
        tabs.addTab("3. Cierre de Caja", construirTabCaja());
        tabs.addTab("4. Inventario (Admin)", construirTabInventario());
        tabs.addTab("5. Promociones (Admin)", construirTabPromociones());
        tabs.addTab("6. Config. Menu (Admin)", construirTabAdminMenu());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel construirTabNuevoPedido() {
        JPanel root = new JPanel(new GridLayout(1, 2, 12, 12));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel Izquierdo: Menu + Adiciones
        JPanel izq = new JPanel(new BorderLayout(8, 8));
        JPanel barraFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        barraFiltro.add(new JLabel("Preferencia (RQ03):"));
        cmbFiltro.setFont(FUENTE_TEXTO);
        cmbFiltro.addActionListener(e -> cargarMenu());
        barraFiltro.add(cmbFiltro);

        JButton btnRefrescarMenu = crearBotonGrande("Refrescar Menu", COLOR_PRIMARIO, 150);
        btnRefrescarMenu.addActionListener(e -> cargarMenu());
        barraFiltro.add(btnRefrescarMenu);
        izq.add(barraFiltro, BorderLayout.NORTH);

        configurarTabla(tablaMenu);
        tablaMenu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarItemMenu();
        });
        izq.add(new JScrollPane(tablaMenu), BorderLayout.CENTER);

        // Constructor de producto con adiciones (RQ02, RQ07)
        JPanel panelDecorador = new JPanel(new GridLayout(3, 1, 6, 6));
        panelDecorador.setBorder(BorderFactory.createTitledBorder("Personalizacion con Adiciones (Decorator)"));
        lblProductoActual.setFont(FUENTE_BOTON);
        lblProductoActual.setForeground(COLOR_PRIMARIO);
        panelDecorador.add(lblProductoActual);

        JPanel filaAdic = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        cmbAdiciones.setPreferredSize(new Dimension(220, 36));
        JButton btnAgregarAdic = crearBotonGrande("+ Agregar Adicion", COLOR_PRIMARIO, 175);
        btnAgregarAdic.addActionListener(e -> aplicarAdicionAlProductoActual());
        filaAdic.add(new JLabel("Adicion:"));
        filaAdic.add(cmbAdiciones);
        filaAdic.add(btnAgregarAdic);
        panelDecorador.add(filaAdic);

        JPanel filaCant = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        spnCantidad.setPreferredSize(new Dimension(70, 36));
        JButton btnAnadirAlPedido = crearBotonGrande("Agregar al Pedido", COLOR_ACENTO, 195);
        btnAnadirAlPedido.addActionListener(e -> agregarProductoActualAlPedido());
        filaCant.add(new JLabel("Cantidad:"));
        filaCant.add(spnCantidad);
        filaCant.add(btnAnadirAlPedido);
        panelDecorador.add(filaCant);

        izq.add(panelDecorador, BorderLayout.SOUTH);

        // Panel Derecho: Pedido Actual
        JPanel der = new JPanel(new BorderLayout(8, 8));
        JPanel topPedido = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        topPedido.add(new JLabel("Mesa:"));
        spnMesa.setPreferredSize(new Dimension(70, 36));
        topPedido.add(spnMesa);
        JButton btnNuevoPedido = crearBotonGrande("Iniciar / Reiniciar Pedido", COLOR_PRIMARIO, 210);
        btnNuevoPedido.addActionListener(e -> iniciarNuevoPedido());
        topPedido.add(btnNuevoPedido);
        der.add(topPedido, BorderLayout.NORTH);

        configurarTabla(tablaLineasPedido);
        der.add(new JScrollPane(tablaLineasPedido), BorderLayout.CENTER);

        JPanel piePedido = new JPanel(new BorderLayout(8, 8));
        lblTotalesPedido.setFont(FUENTE_TITULO);
        piePedido.add(lblTotalesPedido, BorderLayout.NORTH);

        JPanel botonesPedido = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        JButton btnQuitarLinea = crearBotonGrande("Quitar Linea", COLOR_PELIGRO, 150);
        btnQuitarLinea.addActionListener(e -> quitarLineaSeleccionada());
        JButton btnConfirmarPedido = crearBotonGrande("Confirmar Pedido", COLOR_ACENTO, 200);
        btnConfirmarPedido.addActionListener(e -> confirmarPedidoActual());
        botonesPedido.add(btnQuitarLinea);
        botonesPedido.add(btnConfirmarPedido);
        piePedido.add(botonesPedido, BorderLayout.SOUTH);

        der.add(piePedido, BorderLayout.SOUTH);

        root.add(izq);
        root.add(der);
        return root;
    }

    private JPanel construirTabPedidos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        configurarTabla(tablaPedidos);
        panel.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new GridLayout(2, 1, 6, 6));
        JPanel filaEstados = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        JPanel filaCobro = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));

        JButton btnRefrescar = crearBotonGrande("Refrescar", COLOR_PRIMARIO, 140);
        JButton btnEnPrep = crearBotonGrande("En Preparacion", new Color(245, 124, 0), 170);
        JButton btnListo = crearBotonGrande("Marcar Listo", new Color(2, 136, 209), 160);
        JButton btnEntregado = crearBotonGrande("Marcar Entregado", COLOR_ACENTO, 185);
        JButton btnPagarEfectivo = crearBotonGrande("Pago Efectivo", COLOR_PRIMARIO, 165);
        JButton btnPagarTarjeta = crearBotonGrande("Pago Tarjeta", COLOR_PRIMARIO, 165);
        JButton btnCancelar = crearBotonGrande("Cancelar Pedido", COLOR_PELIGRO, 175);

        btnRefrescar.addActionListener(e -> cargarPedidos());
        btnEnPrep.addActionListener(e -> cambiarEstadoSeleccionado(EstadoPedido.EN_PREPARACION));
        btnListo.addActionListener(e -> cambiarEstadoSeleccionado(EstadoPedido.LISTO));
        btnEntregado.addActionListener(e -> cambiarEstadoSeleccionado(EstadoPedido.ENTREGADO));
        btnPagarEfectivo.addActionListener(e -> pagarPedidoSeleccionado(MetodoPago.EFECTIVO));
        btnPagarTarjeta.addActionListener(e -> pagarPedidoSeleccionado(MetodoPago.TARJETA));
        btnCancelar.addActionListener(e -> cancelarPedidoSeleccionado());

        filaEstados.add(btnRefrescar);
        filaEstados.add(btnEnPrep);
        filaEstados.add(btnListo);
        filaEstados.add(btnEntregado);
        filaCobro.add(btnPagarEfectivo);
        filaCobro.add(btnPagarTarjeta);
        filaCobro.add(btnCancelar);

        acciones.add(filaEstados);
        acciones.add(filaCobro);

        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirTabCaja() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        txtReporteCaja.setEditable(false);
        txtReporteCaja.setFont(new Font("Consolas", Font.PLAIN, 14));
        panel.add(new JScrollPane(txtReporteCaja), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        JButton btnCerrar = crearBotonGrande("Ejecutar Cierre de Caja Hoy", COLOR_ACENTO, 250);
        btnCerrar.addActionListener(e -> {
            if (pedirConfirmacion("Confirmar cierre de caja para el dia de hoy?")) {
                try {
                    CierreCaja c = fachada.cerrarCaja();
                    txtReporteCaja.setText(c.generarReporte());
                } catch (Exception ex) {
                    mostrarError(ex.getMessage());
                }
            }
        });
        JButton btnHistorial = crearBotonGrande("Ver Historial Cierres", COLOR_PRIMARIO, 210);
        btnHistorial.addActionListener(e -> {
            try {
                StringBuilder sb = new StringBuilder("=== HISTORIAL DE CIERRES ===\n\n");
                for (CierreCaja c : fachada.consultarHistorialCierres()) {
                    sb.append(c.getFecha()).append(" | Pedidos: ").append(c.getNumPedidos())
                            .append(" | Ventas: $").append(String.format("%.2f", c.getTotalVentas())).append("\n");
                }
                txtReporteCaja.setText(sb.toString());
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        });
        botones.add(btnCerrar);
        botones.add(btnHistorial);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirTabInventario() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        configurarTabla(tablaInventario);
        tablaInventario.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Object estado = table.getModel().getValueAt(row, 4);
                if (!isSelected && estado != null && estado.toString().startsWith("ALERTA")) {
                    c.setBackground(new Color(255, 224, 224));
                    c.setForeground(COLOR_PELIGRO);
                } else if (!isSelected) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
        panel.add(new JScrollPane(tablaInventario), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        JButton btnRefrescar = crearBotonGrande("Refrescar Stock", COLOR_PRIMARIO, 165);
        JButton btnActualizar = crearBotonGrande("Actualizar Stock", COLOR_ACENTO, 175);
        JButton btnReponer = crearBotonGrande("Reponer (+Cantidad)", new Color(2, 136, 209), 195);
        JButton btnNuevo = crearBotonGrande("Nuevo Ingrediente", COLOR_PRIMARIO, 185);

        btnRefrescar.addActionListener(e -> cargarInventario());
        btnActualizar.addActionListener(e -> actualizarStockUI(false));
        btnReponer.addActionListener(e -> actualizarStockUI(true));
        btnNuevo.addActionListener(e -> registrarIngredienteUI());

        acciones.add(btnRefrescar);
        acciones.add(btnActualizar);
        acciones.add(btnReponer);
        acciones.add(btnNuevo);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirTabPromociones() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        configurarTabla(tablaPromos);
        panel.add(new JScrollPane(tablaPromos), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        JButton btnNueva = crearBotonGrande("Crear Promocion", COLOR_ACENTO, 175);
        JButton btnToggle = crearBotonGrande("Activar / Desactivar", COLOR_PRIMARIO, 190);
        JButton btnEliminar = crearBotonGrande("Eliminar Promocion", COLOR_PELIGRO, 185);

        btnNueva.addActionListener(e -> crearPromocionUI());
        btnToggle.addActionListener(e -> togglePromocionUI());
        btnEliminar.addActionListener(e -> eliminarPromocionUI());

        acciones.add(btnNueva);
        acciones.add(btnToggle);
        acciones.add(btnEliminar);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirTabAdminMenu() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        configurarTabla(tablaAdminMenu);
        panel.add(new JScrollPane(tablaAdminMenu), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        JButton btnRefrescar = crearBotonGrande("Refrescar", COLOR_PRIMARIO, 150);
        JButton btnEliminar = crearBotonGrande("Eliminar Item Seleccionado", COLOR_PELIGRO, 230);
        btnRefrescar.addActionListener(e -> cargarAdminMenu());
        btnEliminar.addActionListener(e -> {
            int row = tablaAdminMenu.getSelectedRow();
            if (row < 0) {
                mostrarError("Seleccione un item de la tabla para eliminar.");
                return;
            }
            ItemMenu item = listaTodosItems.get(row);
            if (pedirConfirmacion("Eliminar del menu '" + item.getNombre() + "'?")) {
                try {
                    fachada.eliminarItemMenu(item);
                    recargarTodo();
                } catch (Exception ex) {
                    mostrarError(ex.getMessage());
                }
            }
        });
        acciones.add(btnRefrescar);
        acciones.add(btnEliminar);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    // ---- Acciones de Pedido y Menu ----

    private void seleccionarItemMenu() {
        int row = tablaMenu.getSelectedRow();
        if (row < 0 || row >= itemsVisibles.size()) return;
        productoEnConstruccion = itemsVisibles.get(row);
        actualizarEtiquetaProductoActual();
    }

    private void aplicarAdicionAlProductoActual() {
        if (productoEnConstruccion == null) {
            mostrarError("Primero seleccione un plato o bebida de la tabla del menu.");
            return;
        }
        int idx = cmbAdiciones.getSelectedIndex();
        if (idx < 0 || idx >= adicionesDisponibles.size()) {
            mostrarError("No hay adiciones disponibles para agregar.");
            return;
        }
        try {
            productoEnConstruccion = fachada.agregarAdicion(productoEnConstruccion, adicionesDisponibles.get(idx));
            actualizarEtiquetaProductoActual();
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
            cargarAdiciones();
        }
    }

    private void actualizarEtiquetaProductoActual() {
        if (productoEnConstruccion == null) {
            lblProductoActual.setText("Seleccione un producto del menu...");
        } else {
            lblProductoActual.setText("Seleccionado: " + productoEnConstruccion.getDescripcion()
                    + "  [$" + String.format("%.2f", productoEnConstruccion.getPrecio()) + "]");
        }
    }

    private void iniciarNuevoPedido() {
        try {
            int mesa = (Integer) spnMesa.getValue();
            pedidoActual = fachada.crearPedido(mesa);
            refrescarTablaLineas();
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void agregarProductoActualAlPedido() {
        if (productoEnConstruccion == null) {
            mostrarError("Seleccione primero un producto del menu.");
            return;
        }
        if (pedidoActual == null) {
            iniciarNuevoPedido();
        }
        try {
            int cant = (Integer) spnCantidad.getValue();
            fachada.agregarProductoAPedido(pedidoActual, productoEnConstruccion, cant);
            refrescarTablaLineas();
            // Resetear el producto seleccionado a la version base de la fila actual
            seleccionarItemMenu();
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void quitarLineaSeleccionada() {
        if (pedidoActual == null) return;
        int row = tablaLineasPedido.getSelectedRow();
        if (row < 0) {
            mostrarError("Seleccione una linea del pedido para quitarla.");
            return;
        }
        try {
            pedidoActual.eliminarLinea(row);
            refrescarTablaLineas();
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void confirmarPedidoActual() {
        if (pedidoActual == null || pedidoActual.getLineas().isEmpty()) {
            mostrarError("El pedido actual no tiene productos. Agregue al menos uno antes de confirmar.");
            return;
        }
        try {
            fachada.confirmarPedido(pedidoActual);
            JOptionPane.showMessageDialog(this,
                    "Pedido #" + pedidoActual.getIdPedido() + " confirmado! Total: $"
                            + String.format("%.2f", pedidoActual.getTotal()),
                    "Pedido Confirmado", JOptionPane.INFORMATION_MESSAGE);
            pedidoActual = null;
            refrescarTablaLineas();
            recargarTodo();
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void refrescarTablaLineas() {
        modeloLineasPedido.setRowCount(0);
        if (pedidoActual == null) {
            lblTotalesPedido.setText("Subtotal: $0.00 | Descuento: $0.00 | Total: $0.00");
            return;
        }
        for (LineaPedido l : pedidoActual.getLineas()) {
            modeloLineasPedido.addRow(new Object[]{
                    l.getCantidad(),
                    l.getProducto().getDescripcion(),
                    String.format("$%.2f", l.getProducto().getPrecio()),
                    String.format("$%.2f", l.getSubtotal())
            });
        }
        double sub = pedidoActual.calcularSubtotal();
        double desc = fachada.calcularDescuentoEstimado(pedidoActual);
        lblTotalesPedido.setText(String.format("Mesa %d | Subtotal: $%.2f | Descuento: $%.2f | Total: $%.2f",
                pedidoActual.getMesa(), sub, desc, sub - desc));
    }

    // ---- Acciones de Pedidos Activos ----

    private Pedido obtenerPedidoSeleccionado() {
        int row = tablaPedidos.getSelectedRow();
        if (row < 0 || row >= listaPedidos.size()) {
            mostrarError("Seleccione un pedido de la tabla primero.");
            return null;
        }
        return listaPedidos.get(row);
    }

    private void cambiarEstadoSeleccionado(EstadoPedido estado) {
        Pedido p = obtenerPedidoSeleccionado();
        if (p == null) return;
        try {
            fachada.actualizarEstadoPedido(p, estado);
            cargarPedidos();
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void pagarPedidoSeleccionado(MetodoPago metodo) {
        Pedido p = obtenerPedidoSeleccionado();
        if (p == null) return;
        try {
            fachada.asignarMetodoPago(p, metodo);
            cargarPedidos();
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cancelarPedidoSeleccionado() {
        Pedido p = obtenerPedidoSeleccionado();
        if (p == null) return;
        if (pedirConfirmacion("Esta seguro de cancelar el pedido #" + p.getIdPedido() + "?")) {
            try {
                fachada.cancelarPedido(p);
                recargarTodo();
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    // ---- Acciones Administrativas ----

    private void actualizarStockUI(boolean esReposicion) {
        int row = tablaInventario.getSelectedRow();
        if (row < 0 || row >= listaIngredientes.size()) {
            mostrarError("Seleccione un ingrediente de la tabla.");
            return;
        }
        Ingrediente ing = listaIngredientes.get(row);
        String msg = esReposicion
                ? "Cantidad adicional a reponer para '" + ing.getNombre() + "':"
                : "Nuevo stock total para '" + ing.getNombre() + "':";
        String input = JOptionPane.showInputDialog(this, msg);
        if (input == null) return;
        try {
            double valor = Double.parseDouble(input.trim());
            if (esReposicion) {
                fachada.reponerStock(ing, valor);
            } else {
                fachada.actualizarStock(ing, valor);
            }
            recargarTodo();
        } catch (NumberFormatException e) {
            mostrarError("Ingrese un numero valido (ej: 250).");
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void registrarIngredienteUI() {
        JTextField txtId = new JTextField();
        JTextField txtNom = new JTextField();
        JTextField txtAct = new JTextField();
        JTextField txtMin = new JTextField();
        Object[] campos = {"ID Ingrediente:", txtId, "Nombre:", txtNom, "Stock actual:", txtAct, "Stock minimo:", txtMin};
        if (JOptionPane.showConfirmDialog(this, campos, "Registrar Ingrediente", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Ingrediente nuevo = new Ingrediente(
                        Integer.parseInt(txtId.getText().trim()),
                        txtNom.getText().trim(),
                        Double.parseDouble(txtAct.getText().trim()),
                        Double.parseDouble(txtMin.getText().trim())
                );
                fachada.registrarIngrediente(nuevo);
                recargarTodo();
            } catch (Exception ex) {
                mostrarError("Verifique los campos ingresados: " + ex.getMessage());
            }
        }
    }

    private void crearPromocionUI() {
        JTextField txtId = new JTextField();
        JTextField txtNom = new JTextField();
        JTextField txtDias = new JTextField("MONDAY,FRIDAY");
        JTextField txtIni = new JTextField("14:00");
        JTextField txtFin = new JTextField("18:00");
        JTextField txtPct = new JTextField("15");
        Object[] campos = {
                "ID Promocion:", txtId,
                "Nombre:", txtNom,
                "Dias (MONDAY..SUNDAY separados por coma, o vacio):", txtDias,
                "Hora inicio (HH:MM o vacio):", txtIni,
                "Hora fin (HH:MM o vacio):", txtFin,
                "Porcentaje (0-100):", txtPct
        };
        if (JOptionPane.showConfirmDialog(this, campos, "Nueva Promocion", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                List<DayOfWeek> dias = new ArrayList<>();
                if (!txtDias.getText().isBlank()) {
                    for (String d : txtDias.getText().split(",")) {
                        dias.add(DayOfWeek.valueOf(d.trim().toUpperCase()));
                    }
                }
                LocalTime hi = txtIni.getText().isBlank() ? null : LocalTime.parse(txtIni.getText().trim());
                LocalTime hf = txtFin.getText().isBlank() ? null : LocalTime.parse(txtFin.getText().trim());
                Promocion p = new Promocion(
                        Integer.parseInt(txtId.getText().trim()),
                        txtNom.getText().trim(),
                        dias, hi, hf,
                        Double.parseDouble(txtPct.getText().trim())
                );
                fachada.crearPromocion(p);
                cargarPromociones();
            } catch (Exception ex) {
                mostrarError("No se pudo crear la promocion: " + ex.getMessage());
            }
        }
    }

    private void togglePromocionUI() {
        int row = tablaPromos.getSelectedRow();
        if (row < 0 || row >= listaPromos.size()) {
            mostrarError("Seleccione una promocion de la tabla.");
            return;
        }
        try {
            Promocion p = listaPromos.get(row);
            p.setActiva(!p.isActiva());
            fachada.modificarPromocion(p);
            cargarPromociones();
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void eliminarPromocionUI() {
        int row = tablaPromos.getSelectedRow();
        if (row < 0 || row >= listaPromos.size()) {
            mostrarError("Seleccione una promocion de la tabla.");
            return;
        }
        Promocion p = listaPromos.get(row);
        if (pedirConfirmacion("Eliminar la promocion '" + p.getNombre() + "'?")) {
            try {
                fachada.eliminarPromocion(p);
                cargarPromociones();
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    // ---- Carga de datos y control de roles ----

    private boolean mostrarDialogoLogin() {
        while (true) {
            JTextField txtUsuario = new JTextField();
            JPasswordField txtClave = new JPasswordField();
            Object[] panel = {"Usuario:", txtUsuario, "Clave:", txtClave};
            int res = JOptionPane.showConfirmDialog(this, panel, "Iniciar Sesion - Cafeteria",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) {
                return false;
            }
            try {
                Usuario u = fachada.iniciarSesion(txtUsuario.getText(), new String(txtClave.getPassword()));
                lblUsuario.setText("Usuario: " + u.getNombre() + " (" + u.getRol() + ")");
                return true;
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    private void actualizarVistaPorRol() {
        boolean esAdmin = fachada.tienePermiso("GESTIONAR_INVENTARIO");
        tabs.setEnabledAt(3, esAdmin);
        tabs.setEnabledAt(4, esAdmin);
        tabs.setEnabledAt(5, esAdmin);
        if (!esAdmin && tabs.getSelectedIndex() >= 3) {
            tabs.setSelectedIndex(0);
        }
    }

    private void recargarTodo() {
        cargarMenu();
        cargarAdiciones();
        cargarPedidos();
        if (fachada.tienePermiso("GESTIONAR_INVENTARIO")) {
            cargarInventario();
            cargarPromociones();
            cargarAdminMenu();
        }
    }

    private void cargarMenu() {
        try {
            int filtro = cmbFiltro.getSelectedIndex();
            itemsVisibles = switch (filtro) {
                case 1 -> fachada.presentarMenuFiltrado(true, null);
                case 2 -> fachada.presentarMenuFiltrado(false, true);
                case 3 -> fachada.presentarMenuFiltrado(false, false);
                default -> fachada.presentarMenu();
            };
            modeloMenu.setRowCount(0);
            for (ItemMenu item : itemsVisibles) {
                String tipo = item instanceof Plato ? "Plato" : "Bebida";
                modeloMenu.addRow(new Object[]{
                        item.getIdItem(), item.getDescripcion(), item.getNombre(),
                        String.format("$%.2f", item.getPrecio()), tipo
                });
            }
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cargarAdiciones() {
        try {
            adicionesDisponibles = fachada.getAdicionesDisponibles();
            cmbAdiciones.removeAllItems();
            for (AdicionDecorator a : adicionesDisponibles) {
                cmbAdiciones.addItem(a.getNombre() + " (+$" + String.format("%.2f", a.getCostoExtra()) + ")");
            }
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cargarPedidos() {
        try {
            listaPedidos = fachada.consultarPedidos();
            modeloPedidos.setRowCount(0);
            for (Pedido p : listaPedidos) {
                modeloPedidos.addRow(new Object[]{
                        p.getIdPedido(), p.getMesa(), p.getEstado(),
                        p.getMetodoPago() != null ? p.getMetodoPago() : "Pendiente",
                        String.format("$%.2f", p.getSubtotal()),
                        String.format("$%.2f", p.getDescuento()),
                        String.format("$%.2f", p.getTotal())
                });
            }
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cargarInventario() {
        try {
            listaIngredientes = fachada.consultarInventario();
            modeloInventario.setRowCount(0);
            for (Ingrediente i : listaIngredientes) {
                modeloInventario.addRow(new Object[]{
                        i.getIdIngrediente(), i.getNombre(), i.getStockActual(), i.getStockMinimo(),
                        i.estaBajoMinimo() ? "ALERTA: BAJO MINIMO" : "OK"
                });
            }
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cargarPromociones() {
        try {
            listaPromos = fachada.consultarPromociones();
            modeloPromos.setRowCount(0);
            for (Promocion p : listaPromos) {
                String franja = p.tieneFranja() ? p.getHoraInicio() + " - " + p.getHoraFin() : "Todo el dia";
                modeloPromos.addRow(new Object[]{
                        p.getIdPromocion(), p.getNombre(), p.getDiasSemana(), franja,
                        p.getPorcentaje() + "%", p.isActiva() ? "Si" : "No"
                });
            }
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cargarAdminMenu() {
        try {
            listaTodosItems = fachada.consultarTodosItemsMenu();
            modeloAdminMenu.setRowCount(0);
            for (ItemMenu it : listaTodosItems) {
                modeloAdminMenu.addRow(new Object[]{
                        it.getIdItem(), it.getDescripcion(),
                        String.format("$%.2f", it.getPrecio()),
                        it.isDisponible() ? "Si" : "Agotado"
                });
            }
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    // ---- Alertas y Utilidades del Diagrama UML ----

    public void mostrarAlerta(String msg) {
        if (msg == null || msg.isBlank()) return;
        modeloAlertas.addElement(msg);
        actualizarBannerAlertas();
    }

    private void confirmarAlertasSeleccionadas() {
        int[] indices = listaAlertas.getSelectedIndices();
        if (indices.length == 0 && !modeloAlertas.isEmpty()) {
            modeloAlertas.remove(0);
        } else {
            for (int i = indices.length - 1; i >= 0; i--) {
                modeloAlertas.remove(indices[i]);
            }
        }
        actualizarBannerAlertas();
    }

    private void actualizarBannerAlertas() {
        int total = modeloAlertas.size();
        lblContadorAlertas.setText("Alertas pendientes por confirmar: " + total);
        panelAlertas.setVisible(total > 0);
        revalidate();
        repaint();
    }

    public boolean pedirConfirmacion(String msg) {
        int res = JOptionPane.showConfirmDialog(this, msg, "Confirmar Accion",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return res == JOptionPane.YES_OPTION;
    }

    public void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this,
                msg != null ? msg : "Ocurrio un error inesperado.",
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JButton crearBotonGrande(String texto, Color fondo, int ancho) {
        JButton btn = new JButton(texto);
        btn.setPreferredSize(new Dimension(ancho, 42));
        btn.setFont(FUENTE_BOTON);
        btn.setBackground(fondo);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private void configurarTabla(JTable tabla) {
        tabla.setFont(FUENTE_TEXTO);
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(FUENTE_BOTON);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
}
