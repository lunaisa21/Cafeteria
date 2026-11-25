package Operaciones;


import com.datos.Promocion;

import java.time.LocalDateTime;

public class DescuentoPorFranja implements EstrategiaDescuento {

    @Override
    public double calcularDescuento(double subtotal, Promocion p, LocalDateTime momento) {
        if (p == null || !p.isActiva() || subtotal <= 0 || momento == null) {
            return 0;
        }
        if (!p.aplicaFranja(momento.toLocalTime())) {
            return 0;
        }
        return EstrategiaDescuento.aplicarPorcentaje(subtotal, p.getPorcentaje());
    }
}

