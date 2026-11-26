package com.datos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 * Representa el cierre de caja diario y su reporte de ventas (RQ06, RF09).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CierreCaja {
    private LocalDate fecha;
    private double totalVentas;
    private int numPedidos;
    private List<Pedido> pedidos = new ArrayList<>();
    public CierreCaja(LocalDate fecha, double totalVentas, int numPedidos) {
        this.fecha = fecha;
        this.totalVentas = totalVentas;
        this.numPedidos = numPedidos;
        this.pedidos = new ArrayList<>();
    }
    /** Genera el reporte textual detallado de transacciones del dia (RQ06, RF09). */
    public String generarReporte() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CIERRE DE CAJA ===\n");
        sb.append("Fecha:        ").append(fecha).append("\n");
        sb.append("Num. pedidos: ").append(numPedidos).append("\n");
        sb.append("Total ventas: $").append(String.format("%.2f", totalVentas)).append("\n");
        if (pedidos != null && !pedidos.isEmpty()) {
            double efectivo = 0;
            double tarjeta = 0;
            double sinDefinir = 0;
            sb.append("\n--- Detalle de transacciones ---\n");
            for (Pedido p : pedidos) {
                sb.append("Pedido #").append(p.getIdPedido())
                        .append(" | Mesa ").append(p.getMesa())
                        .append(" | ").append(p.getEstado())
                        .append(" | Pago: ").append(p.getMetodoPago() != null ? p.getMetodoPago() : "N/A")
                        .append(" | Total: $").append(String.format("%.2f", p.getTotal()))
                        .append("\n");
                if (p.getMetodoPago() == MetodoPago.EFECTIVO) efectivo += p.getTotal();
                else if (p.getMetodoPago() == MetodoPago.TARJETA) tarjeta += p.getTotal();
                else sinDefinir += p.getTotal();
            }
            sb.append("\nResumen por metodo de pago:\n");
            sb.append("  Efectivo:    $").append(String.format("%.2f", efectivo)).append("\n");
            sb.append("  Tarjeta:     $").append(String.format("%.2f", tarjeta)).append("\n");
            if (sinDefinir > 0) {
                sb.append("  Por cobrar:  $").append(String.format("%.2f", sinDefinir)).append("\n");
            }
        }