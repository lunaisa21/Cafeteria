package Operaciones;

import Operaciones.PedidoObservador;
import com.datos.EstadoPedido;
import com.datos.LineaPedido;
import com.datos.Pedido;

import java.util.function.Consumer;

/**
 * Notifica al mesero cuando un pedido cambia de estado (RQ08).
 */
public class NotificadorMesero implements PedidoObservador {
    private Consumer<String> receptor;

    public void setReceptor(Consumer<String> receptor) {
        this.receptor = receptor;
    }

    @Override
    public void actualizar(Pedido pedido) {
        EstadoPedido e = pedido.getEstado();
        if (e == EstadoPedido.EN_PREPARACION || e == EstadoPedido.LISTO
                || e == EstadoPedido.CANCELADO) {
            mostrarAlertaMesero(pedido);
        }
    }

    public void mostrarAlertaMesero(Pedido pedido) {
        String mensaje;
        switch (pedido.getEstado()) {
            case LISTO:
                mensaje = "[MESERO] LISTO para recoger y llevar a la mesa. ";
                break;
            case CANCELADO:
                mensaje = "[MESERO] Pedido CANCELADO. ";
                break;
            default:
                mensaje = "[MESERO] Pedido en preparacion. ";
        }
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

