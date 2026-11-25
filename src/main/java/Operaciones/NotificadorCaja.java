package Operaciones;

import com.datos.EstadoPedido;
import com.datos.LineaPedido;
import com.datos.Pedido;

import java.util.function.Consumer;

public class NotificadorCaja implements PedidoObservador {
    private Consumer<String> receptor; // lo registra la interfaz (consola o GUI)

    public void setReceptor(Consumer<String> receptor) {
        this.receptor = receptor;
    }

    @Override
    public void actualizar(Pedido pedido) {
        EstadoPedido e = pedido.getEstado();
        if (e == EstadoPedido.PENDIENTE || e == EstadoPedido.ENTREGADO
                || e == EstadoPedido.CANCELADO) {
            mostrarAlertaCaja(pedido);
        }
    }

    public void mostrarAlertaCaja(Pedido pedido) {
        String mensaje;
        switch (pedido.getEstado()) {
            case ENTREGADO:
                mensaje = "[CAJA] Entregado, listo para cobrar $" + pedido.getTotal() + ". ";
                break;
            case CANCELADO:
                mensaje = "[CAJA] Pedido CANCELADO, no cobrar. ";
                break;
            default:
                mensaje = "[CAJA] Pedido registrado, total $" + pedido.getTotal() + ". ";
        }
        if (receptor != null) {
            receptor.accept(mensaje + resumen(pedido));
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
