package Operaciones;

import com.datos.EstadoPedido;
import com.datos.MetodoPago;
import com.datos.Pedido;

import java.time.LocalDate;
import java.util.List;

/**
 * Orquesta el ciclo de vida de los pedidos: creacion, confirmacion,
 * descuento de inventario, promociones, cambios de estado y notificaciones (RQ05, RQ08, RF11).
 */
public class GestorPedidos {

    private final GestorInventario inventario;
    private final MotorPromociones promociones;
    private final PedidoDAO pedidoDAO;
    private final PedidoObservador notificadorCocina;
    private final PedidoObservador notificadorCaja;
    private final PedidoObservador notificadorMesero;

    public GestorPedidos(GestorInventario inventario,
                         MotorPromociones promociones,
                         PedidoDAO pedidoDAO,
                         PedidoObservador notificadorCocina,
                         PedidoObservador notificadorCaja,
                         PedidoObservador notificadorMesero) {
        if (inventario == null || promociones == null || pedidoDAO == null) {
            throw new IllegalArgumentException("Inventario, promociones y PedidoDAO son obligatorios.");
        }
        this.inventario = inventario;
        this.promociones = promociones;
        this.pedidoDAO = pedidoDAO;
        this.notificadorCocina = notificadorCocina;
        this.notificadorCaja = notificadorCaja;
        this.notificadorMesero = notificadorMesero;
    }

    /** Crea un nuevo pedido en estado PENDIENTE con sus observadores conectados. */
    public Pedido crearPedido(int mesa) {
        if (mesa <= 0) {
            throw new IllegalArgumentException("El numero de mesa debe ser mayor a 0.");
        }
        Pedido pedido = new Pedido(mesa);
        registrarObservadores(pedido);
        return pedido;
    }

    /** Confirma el pedido: valida stock, calcula promociones, descuenta ingredientes, persiste y notifica. */
    public void confirmarPedido(Pedido p) {
        validarNoNulo(p);
        if (p.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden confirmar pedidos en estado PENDIENTE.");
        }
        if (p.getLineas() == null || p.getLineas().isEmpty()) {
            throw new IllegalStateException("El pedido esta vacio. Agregue al menos un plato o bebida antes de confirmar.");
        }
        if (!inventario.hayStock(p)) {
            throw new IllegalStateException("No hay stock suficiente para completar este pedido. "
                    + "Revise los productos seleccionados o reponga inventario.");
        }

        double subtotal = p.calcularSubtotal();
        double descuento = promociones.calcularDescuento(p);
        p.setSubtotal(subtotal);
        p.setDescuento(descuento);
        p.setTotal(subtotal - descuento);

        inventario.descontarPorPedido(p);
        try {
            if (p.getIdPedido() <= 0) {
                pedidoDAO.crear(p);
            } else {
                pedidoDAO.actualizar(p);
            }
        } catch (RuntimeException ex) {
            inventario.reponerPorPedido(p);
            throw ex;
        }

        registrarObservadores(p);
        p.notificarObservadores();
    }

    /** Actualiza el estado del pedido validando la transicion, persiste y notifica (RQ05, RQ08). */
    public void actualizarEstado(Pedido p, EstadoPedido nuevoEstado) {
        validarNoNulo(p);
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("Debe indicar el nuevo estado del pedido.");
        }
        if (nuevoEstado == EstadoPedido.CANCELADO) {
            cancelarPedido(p);
            return;
        }

        EstadoPedido estadoAnterior = p.getEstado();
        validarTransicion(estadoAnterior, nuevoEstado);

        p.setEstado(nuevoEstado);
        try {
            pedidoDAO.actualizar(p);
        } catch (RuntimeException ex) {
            p.setEstado(estadoAnterior);
            throw ex;
        }

        registrarObservadores(p);
        p.notificarObservadores();
    }

    /** Cancela un pedido y repone el stock si aun estaba PENDIENTE con stock descontado. */
    public void cancelarPedido(Pedido p) {
        validarNoNulo(p);
        EstadoPedido estadoAnterior = p.getEstado();
        validarTransicion(estadoAnterior, EstadoPedido.CANCELADO);

        boolean repusoStock = false;
        if (estadoAnterior == EstadoPedido.PENDIENTE && p.isStockDescontado()) {
            inventario.reponerPorPedido(p);
            repusoStock = true;
        }

        p.setEstado(EstadoPedido.CANCELADO);
        try {
            if (p.getIdPedido() > 0) {
                pedidoDAO.actualizar(p);
            }
        } catch (RuntimeException ex) {
            p.setEstado(estadoAnterior);
            if (repusoStock) {
                inventario.descontarPorPedido(p);
            }
            throw ex;
        }

        registrarObservadores(p);
        p.notificarObservadores();
    }

    /** Registra o actualiza el metodo de pago del pedido. */
    public void asignarMetodoPago(Pedido p, MetodoPago metodo) {
        validarNoNulo(p);
        if (metodo == null) {
            throw new IllegalArgumentException("Debe elegir un metodo de pago valido (EFECTIVO o TARJETA).");
        }
        p.setMetodoPago(metodo);
        if (p.getIdPedido() > 0) {
            pedidoDAO.actualizar(p);
        }
    }

    public List<Pedido> consultarTodos() {
        List<Pedido> lista = pedidoDAO.consultar();
        for (Pedido p : lista) {
            registrarObservadores(p);
        }
        return lista;
    }

    public List<Pedido> consultarPorFecha(LocalDate fecha) {
        List<Pedido> lista = pedidoDAO.consultarPorFecha(fecha);
        for (Pedido p : lista) {
            registrarObservadores(p);
        }
        return lista;
    }

    // ---- Auxiliares ----

    private void registrarObservadores(Pedido p) {
        p.agregarObservador(notificadorCocina);
        p.agregarObservador(notificadorCaja);
        p.agregarObservador(notificadorMesero);
    }

    private void validarTransicion(EstadoPedido actual, EstadoPedido nuevo) {
        boolean valida = switch (actual) {
            case PENDIENTE -> nuevo == EstadoPedido.EN_PREPARACION || nuevo == EstadoPedido.CANCELADO;
            case EN_PREPARACION -> nuevo == EstadoPedido.LISTO || nuevo == EstadoPedido.CANCELADO;
            case LISTO -> nuevo == EstadoPedido.ENTREGADO;
            case ENTREGADO, CANCELADO -> false;
        };
        if (!valida) {
            throw new IllegalStateException("Transicion invalida de " + actual + " a " + nuevo
                    + ". Flujo permitido: PENDIENTE -> EN_PREPARACION -> LISTO -> ENTREGADO (o CANCELADO antes de LISTO).");
        }
    }

    private void validarNoNulo(Pedido p) {
        if (p == null) {
            throw new IllegalArgumentException("El pedido seleccionado no existe.");
        }
    }
}

