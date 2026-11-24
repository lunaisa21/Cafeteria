package Operaciones;

import com.datos.EstadoPedido;
import com.datos.Pedido;

public interface PedidoObservador {
    void actualizar(Pedido p);
}
