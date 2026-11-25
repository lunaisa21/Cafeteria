package Operaciones;

import com.datos.EstadoPedido;
import com.datos.LineaPedido;
import com.datos.Pedido;

import java.sql.SQLException;
import java.util.List;

public class GestorPedidos {
    private final GestorInventario inventario;
    private final MotorPromociones promociones;
    private final GenericDAO<Pedido> pedidoDAO;
    private final PedidoObservador notificadorCocina;
    private final PedidoObservador notificadorCaja;
    private final PedidoObservador notificadorMesero;

    public GestorPedidos(GestorInventario inventario, MotorPromociones promociones, GenericDAO<Pedido> pedidoDAO, PedidoObservador notificadorCocina, PedidoObservador notificadorCaja, PedidoObservador notificadorMesero) {
        this.inventario = inventario;
        this.promociones = promociones;
        this.pedidoDAO = pedidoDAO;
        this.notificadorCocina = notificadorCocina;
        this.notificadorCaja = notificadorCaja;
        this.notificadorMesero = notificadorMesero;
    }

    public  Pedido crearPedido(int mesa){
        if (mesa <= 0){
            throw  new IllegalArgumentException("El numero de mesa debe ser mayor a cero");
        }
        Pedido pedido = new Pedido();
        pedido.agregarObservador(notificadorCocina);
        pedido.agregarObservador(notificadorCaja);
        pedido.agregarObservador(notificadorMesero);
        return pedido;
    }
    public void confirmarPedido(Pedido pedido) {
        validarNoNulo(pedido);

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden confirmar pedidos en estado PENDIENTE.");
        }

        List<LineaPedido> lineas = pedido.getLineas();
        if (lineas.isEmpty()) {
            throw new IllegalStateException("El pedido no tiene productos. Agregue al menos uno.");
        }

        // Verificacion previa para dar un mensaje claro (el descuento real es atomico en SQL)
        for (LineaPedido linea : lineas) {
            if (!inventario.hayStock(linea.getProducto())) {
                throw new IllegalStateException("No hay stock suficiente de: "
                        + linea.getProducto().getDescripcion()
                        + ". Quite el producto o elija otro.");
            }
        }

        double subtotal = pedido.calcularSubtotal();
        double descuento = promociones.calcularDescuento(pedido);
        pedido.setSubtotal(subtotal);
        pedido.setDescuento(descuento);
        pedido.setTotal(subtotal - descuento);

        try (Connection con = conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                // UPDATE ingrediente SET stock = stock - ? WHERE id = ? AND stock >= ?
                // Si no actualiza ninguna fila, lanza IllegalStateException (sin stock).
                inventario.descontarPorPedido(pedido, con);
                pedido.setStockDescontado(true);
                pedidoDAO.crear(pedido, con);   // asigna el idPedido generado
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                pedido.setStockDescontado(false);
                throw e;
            }
        } catch (SQLException e) {
            throw errorBD("No se pudo confirmar el pedido. Intente de nuevo.", e);
        }

        // Solo se notifica cuando todo quedo guardado
        pedido.notificarObservadores();
    }

    /** Cambia el estado validando la transicion; guarda y luego notifica (RQ05, RQ08). */
    public void actualizarEstado(Pedido pedido, EstadoPedido nuevoEstado) {
        validarNoNulo(pedido);
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("Debe indicar el nuevo estado del pedido.");
        }
        validarTransicion(pedido.getEstado(), nuevoEstado);

        try (Connection con = conexion.obtenerConexion()) {
            pedidoDAO.actualizarEstado(pedido.getIdPedido(), nuevoEstado,
                    pedido.isStockDescontado(), con);
        } catch (SQLException e) {
            throw errorBD("No se pudo actualizar el estado del pedido.", e);
        }

        pedido.cambiarEstado(nuevoEstado); // Pedido notifica a cocina, caja y mesero
    }

    /**
     * Cancela el pedido. Si estaba PENDIENTE y ya se habia descontado el stock,
     * lo repone en la misma transaccion; en otros estados no se reintegra.
     */
    public void cancelarPedido(Pedido pedido) {
        validarNoNulo(pedido);
        validarTransicion(pedido.getEstado(), EstadoPedido.CANCELADO);

        boolean debeReponer = pedido.getEstado() == EstadoPedido.PENDIENTE
                && pedido.isStockDescontado();

        if (!debeReponer) {
            actualizarEstado(pedido, EstadoPedido.CANCELADO);
            return;
        }

        try (Connection con = conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                inventario.reponerPorPedido(pedido, con);
                pedidoDAO.actualizarEstado(pedido.getIdPedido(),
                        EstadoPedido.CANCELADO, false, con);
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw errorBD("No se pudo cancelar el pedido. Intente de nuevo.", e);
        }

        // Memoria actualizada solo despues del commit
        pedido.setStockDescontado(false);
        pedido.cambiarEstado(EstadoPedido.CANCELADO);
    }

    // ---------- Auxiliares ----------

    /** PENDIENTE -> EN_PREPARACION -> LISTO -> ENTREGADO; cancelar solo antes de LISTO. */
    private void validarTransicion(EstadoPedido actual, EstadoPedido nuevo) {
        boolean valida;
        switch (actual) {
            case PENDIENTE:
                valida = nuevo == EstadoPedido.EN_PREPARACION || nuevo == EstadoPedido.CANCELADO;
                break;
            case EN_PREPARACION:
                valida = nuevo == EstadoPedido.LISTO || nuevo == EstadoPedido.CANCELADO;
                break;
            case LISTO:
                valida = nuevo == EstadoPedido.ENTREGADO;
                break;
            default: // ENTREGADO y CANCELADO son estados finales
                valida = false;
        }
        if (!valida) {
            throw new IllegalStateException("No se puede pasar de " + actual + " a " + nuevo + ".");
        }
    }

    private void validarNoNulo(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no existe.");
        }
    }

    private IllegalStateException errorBD(String mensaje, SQLException causa) {
        return new IllegalStateException(mensaje, causa);
    }

}
