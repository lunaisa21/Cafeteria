package Operaciones;

import com.datos.Promocion;

import java.time.LocalDateTime;

/**
 * Estrategia de descuento por dia de la semana (RQ04).
 */
public class DescuentoPorDia implements EstrategiaDescuento {

    @Override
    public double calcularDescuento(double subtotal, Promocion p, LocalDateTime momento) {
        if (p == null || !p.isActiva() || subtotal <= 0) {
            return 0;
        }
        if (!p.aplicaDia(momento.getDayOfWeek())) {
            return 0;
        }
        return EstrategiaDescuento.aplicarPorcentaje(subtotal, p.getPorcentaje());
    }
}
