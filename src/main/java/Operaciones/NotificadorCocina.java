package com.operaciones;

import Operaciones.PedidoObservador;
import com.datos.EstadoPedido;
import com.datos.LineaPedido;
import com.datos.Pedido;

import java.util.function.Consumer;

/**
 * Notifica a cocina cuando un pedido es nuevo o se cancela (RQ08).
 */
public class NotificadorCocina implements PedidoObservador {
    private Consumer<String> receptor;

    public void setReceptor(Consumer<String> receptor) {
        this.receptor = receptor;
    }

    @Override
    public void actualizar(Pedido pedido) {
        if (pedido.getEstado() == EstadoPedido.PENDIENTE
                || pedido.getEstado() == EstadoPedido.CANCELADO) {
            mostrarAlertaCocina(pedido);
        }
    }

    public void mostrarAlertaCocina(Pedido pedido) {
        String mensaje = (pedido.getEstado() == EstadoPedido.CANCELADO)
                ? "[COCINA] CANCELADO, detener preparacion. "
                : "[COCINA] Nuevo pedido por preparar. ";
        if (receptor != null) {
            receptor.accept(mensaje + resumen(pedido));
        } else {
            System.out.println(mensaje + resumen(pedido));
        }
    }

    private String resumen(Pedido pedido) {
        StringBuilder sb = new StringBuilder("Pedido #" + pedido.getIdPedido()
                + " - Mesa " + pedido.getMesa() + ": ");
        for (int i = 0; i < pedido.getLineas().size(); i++) {
            LineaPedido l = pedido.getLineas().get(i);
            sb.append(l.getCantidad()).append("x ").append(l.getProducto().getDescripcion());
            if (i < pedido.getLineas().size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
