package Operaciones;

import com.datos.CierreCaja;
import com.datos.EstadoPedido;
import com.datos.Pedido;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona el cierre de caja diario y la generacion del reporte de ventas (RQ06, RF09).
 */
public class GestorCaja {

    private final CierreCajaDAO cierreCajaDAO;
    private final PedidoDAO pedidoDAO;

    public GestorCaja(CierreCajaDAO cierreCajaDAO, PedidoDAO pedidoDAO) {
        if (cierreCajaDAO == null || pedidoDAO == null) {
            throw new IllegalArgumentException("CierreCajaDAO y PedidoDAO son obligatorios.");
        }
        this.cierreCajaDAO = cierreCajaDAO;
        this.pedidoDAO = pedidoDAO;
    }

    public GestorCaja(CierreCajaDAO cierreCajaDAO) {
        this(cierreCajaDAO, new PedidoDAO());
    }

    /** Realiza el cierre de caja para la fecha dada, calculando ventas y transacciones validas (RQ06, RF09). */
    public CierreCaja cerrarCaja(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("Debe especificar la fecha para el cierre de caja.");
        }

        List<Pedido> delDia = pedidoDAO.consultarPorFecha(fecha);
        List<Pedido> validos = new ArrayList<>();
        double totalVentas = 0.0;

        for (Pedido p : delDia) {
            if (p.getEstado() != EstadoPedido.CANCELADO) {
                validos.add(p);
                totalVentas += p.getTotal();
            }
        }

        CierreCaja cierre = new CierreCaja(fecha, totalVentas, validos.size(), validos);
        CierreCaja existente = cierreCajaDAO.consultarPorFecha(fecha);
        if (existente != null) {
            cierreCajaDAO.actualizar(cierre);
        } else {
            cierreCajaDAO.crear(cierre);
        }
        return cierre;
    }

    public CierreCaja consultarCierre(LocalDate fecha) {
        return cierreCajaDAO.consultarPorFecha(fecha);
    }

    public List<CierreCaja> consultarHistorial() {
        return cierreCajaDAO.consultar();
    }
}
