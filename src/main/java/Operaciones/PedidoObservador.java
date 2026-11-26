package Operaciones;

import com.datos.Pedido;

/**
 * Observer para cambios de estado de pedidos (RQ08).
 */
public interface PedidoObservador {
    void actualizar(Pedido p);
}
