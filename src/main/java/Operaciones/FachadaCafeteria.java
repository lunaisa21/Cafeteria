package Operaciones;

import com.datos.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Fachada unica del sistema de cafeteria. Conecta las interfaces (consola y Swing)
 * con toda la logica de negocio, validando permisos por rol (RQ03-NF).
 */
public class FachadaCafeteria {

    private final GestorMenu menu;
    private final GestorPedidos pedidos;
    private final GestorInventario inventario;
    private final MotorPromociones promociones;
    private final GestorCaja caja;
    private final GestorAutenticacion auth;

    private final Operaciones.NotificadorCocina notificadorCocina;
    private final NotificadorCaja notificadorCaja;
    private final NotificadorMesero notificadorMesero;
    private final AlertaInventario alertaInventario;

    private Usuario usuarioActual;

    public FachadaCafeteria() {
        this(new ItemMenuDAO(), new IngredienteDAO(), new PedidoDAO(),
                new PromocionDAO(), new UsuarioDAO(), new CierreCajaDAO());
    }

    public FachadaCafeteria(ItemMenuDAO itemMenuDAO,
                            IngredienteDAO ingredienteDAO,
                            PedidoDAO pedidoDAO,
                            PromocionDAO promocionDAO,
                            UsuarioDAO usuarioDAO,
                            CierreCajaDAO cierreCajaDAO) {
        this.notificadorCocina = new NotificadorCocina();
        this.notificadorCaja = new NotificadorCaja();
        this.notificadorMesero = new NotificadorMesero();
        this.alertaInventario = new AlertaInventario();

        this.inventario = new GestorInventario(ingredienteDAO);
        this.inventario.agregarObservador(this.alertaInventario);

        this.menu = new GestorMenu(itemMenuDAO, this.inventario);
        this.promociones = new MotorPromociones(promocionDAO);
        this.pedidos = new GestorPedidos(
                this.inventario, this.promociones, pedidoDAO,
                this.notificadorCocina, this.notificadorCaja, this.notificadorMesero
        );
        this.caja = new GestorCaja(cierreCajaDAO, pedidoDAO);
        this.auth = new GestorAutenticacion(usuarioDAO);
    }

    // ---- Autenticacion y Permisos (RQ03-NF) ----

    public Usuario iniciarSesion(String nombre, String clave) {
        this.usuarioActual = auth.login(nombre, clave);
        return this.usuarioActual;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean tienePermiso(String accion) {
        return auth.tienePermiso(usuarioActual, accion);
    }

    private void exigirPermiso(String accion) {
        if (usuarioActual == null) {
            throw new SecurityException("Debe iniciar sesion primero para realizar esta operacion.");
        }
        if (!auth.tienePermiso(usuarioActual, accion)) {
            throw new SecurityException("Acceso denegado: la accion '" + accion
                    + "' requiere rol ADMINISTRADOR. Usuario actual: " + usuarioActual.getRol());
        }
    }

    // ---- Configuracion de alertas para la interfaz (RQ08, RQ05-NF) ----

    public void configurarReceptoresAlertas(Consumer<String> cocina,
                                            Consumer<String> cajaReceptor,
                                            Consumer<String> mesero,
                                            Consumer<String> stockMinimo) {
        notificadorCocina.setReceptor(cocina);
        notificadorCaja.setReceptor(cajaReceptor);
        notificadorMesero.setReceptor(mesero);
        alertaInventario.setReceptor(stockMinimo);
    }

    // ---- Menu y Adiciones (RQ01, RQ02, RQ03, RQ07, RF13) ----

    public List<ItemMenu> presentarMenu() {
        exigirPermiso("VER_MENU");
        return menu.presentarMenu();
    }

    public List<ItemMenu> presentarMenuFiltrado(boolean soloVegetarianos, Boolean soloBebidasFrias) {
        exigirPermiso("VER_MENU");
        return menu.presentarMenuFiltrado(soloVegetarianos, soloBebidasFrias);
    }

    public List<ItemMenu> consultarTodosItemsMenu() {
        exigirPermiso("GESTIONAR_MENU");
        return menu.consultarTodos();
    }

    public void registrarItemMenu(ItemMenu item) {
        exigirPermiso("GESTIONAR_MENU");
        menu.registrarItem(item);
    }

    public void actualizarItemMenu(ItemMenu item) {
        exigirPermiso("GESTIONAR_MENU");
        menu.actualizarItem(item);
    }

    public void eliminarItemMenu(ItemMenu item) {
        exigirPermiso("GESTIONAR_MENU");
        menu.eliminarItem(item);
    }

    public void registrarAdicion(AdicionDecorator plantilla) {
        exigirPermiso("GESTIONAR_MENU");
        menu.registrarAdicion(plantilla);
    }

    public void registrarAdicionInicial(AdicionDecorator plantilla) {
        menu.registrarAdicion(plantilla);
    }

    public List<AdicionDecorator> getAdicionesDisponibles() {
        exigirPermiso("VER_MENU");
        return menu.getAdicionesDisponibles();
    }

    public Producto agregarAdicion(Producto p, AdicionDecorator a) {
        exigirPermiso("CREAR_PEDIDO");
        return menu.agregarAdicion(p, a);
    }

    // ---- Pedidos (RQ05, RQ08, RF11, RF13) ----

    public Pedido crearPedido(int mesa) {
        exigirPermiso("CREAR_PEDIDO");
        return pedidos.crearPedido(mesa);
    }

    public void agregarProductoAPedido(Pedido pedido, Producto producto, int cantidad) {
        exigirPermiso("CREAR_PEDIDO");
        if (pedido == null) {
            throw new IllegalArgumentException("Primero debe crear o seleccionar un pedido.");
        }
        if (producto == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto del menu.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        }
        if (!inventario.hayStock(producto, cantidad)) {
            throw new IllegalStateException("No hay stock suficiente para " + cantidad
                    + " unidad(es) de '" + producto.getDescripcion()
                    + "'. Reduzca la cantidad o elija otro producto.");
        }
        pedido.agregarLinea(new LineaPedido(producto, cantidad));
    }

    public void confirmarPedido(Pedido pedido) {
        exigirPermiso("CONFIRMAR_PEDIDO");
        pedidos.confirmarPedido(pedido);
        menu.ocultarAgotados();
    }

    public void actualizarEstadoPedido(Pedido pedido, EstadoPedido nuevoEstado) {
        exigirPermiso("ACTUALIZAR_ESTADO_PEDIDO");
        pedidos.actualizarEstado(pedido, nuevoEstado);
        if (nuevoEstado == EstadoPedido.CANCELADO) {
            menu.ocultarAgotados();
        }
    }

    public void cancelarPedido(Pedido pedido) {
        exigirPermiso("CANCELAR_PEDIDO");
        pedidos.cancelarPedido(pedido);
        menu.ocultarAgotados();
    }

    public void asignarMetodoPago(Pedido pedido, MetodoPago metodo) {
        exigirPermiso("COBRAR_PEDIDO");
        pedidos.asignarMetodoPago(pedido, metodo);
    }

    public List<Pedido> consultarPedidos() {
        exigirPermiso("VER_MENU");
        return pedidos.consultarTodos();
    }

    public List<Pedido> consultarPedidosDelDia() {
        exigirPermiso("VER_MENU");
        return pedidos.consultarPorFecha(LocalDate.now());
    }

    public double calcularDescuentoEstimado(Pedido pedido) {
        if (pedido == null) return 0;
        return promociones.calcularDescuento(pedido);
    }

    public Optional<Promocion> mejorPromocionActual(Pedido pedido) {
        if (pedido == null) return Optional.empty();
        LocalDateTime momento = pedido.getFecha() != null ? pedido.getFecha() : LocalDateTime.now();
        return promociones.mejorPromocion(pedido.calcularSubtotal(), momento);
    }

    // ---- Inventario (RF12, RF13) ----

    public List<Ingrediente> consultarInventario() {
        exigirPermiso("GESTIONAR_INVENTARIO");
        return inventario.consultarStock();
    }

    public List<Ingrediente> consultarIngredientesBajoMinimo() {
        exigirPermiso("GESTIONAR_INVENTARIO");
        return inventario.consultarBajoMinimo();
    }

    public void registrarIngrediente(Ingrediente i) {
        exigirPermiso("GESTIONAR_INVENTARIO");
        inventario.registrarIngrediente(i);
        menu.ocultarAgotados();
    }

    public void actualizarStock(Ingrediente i, double nuevaCantidad) {
        exigirPermiso("GESTIONAR_INVENTARIO");
        inventario.actualizarStock(i, nuevaCantidad);
        menu.ocultarAgotados();
    }

    public void reponerStock(Ingrediente i, double cantidadAdicional) {
        exigirPermiso("GESTIONAR_INVENTARIO");
        inventario.reponerStock(i, cantidadAdicional);
        menu.ocultarAgotados();
    }

    // ---- Promociones (RQ04) ----

    public List<Promocion> consultarPromociones() {
        exigirPermiso("GESTIONAR_PROMOCIONES");
        return promociones.consultarTodas();
    }

    public List<Promocion> consultarPromocionesActivas() {
        exigirPermiso("VER_MENU");
        return promociones.consultarActivas();
    }

    public void crearPromocion(Promocion p) {
        exigirPermiso("GESTIONAR_PROMOCIONES");
        promociones.crear(p);
    }

    public void modificarPromocion(Promocion p) {
        exigirPermiso("GESTIONAR_PROMOCIONES");
        promociones.modificar(p);
    }

    public void eliminarPromocion(Promocion p) {
        exigirPermiso("GESTIONAR_PROMOCIONES");
        promociones.eliminar(p);
    }

    // ---- Caja (RQ06, RF09) ----

    public CierreCaja cerrarCaja() {
        return cerrarCaja(LocalDate.now());
    }

    public CierreCaja cerrarCaja(LocalDate fecha) {
        exigirPermiso("CERRAR_CAJA");
        return caja.cerrarCaja(fecha);
    }

    public List<CierreCaja> consultarHistorialCierres() {
        exigirPermiso("CERRAR_CAJA");
        return caja.consultarHistorial();
    }
}
