package Operaciones;

import com.datos.Promocion;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;


public class DescuentoPorDia implements Operaciones.EstrategiaDescuento {

    @Override
    public double calcularDescuento(double subtotal, Promocion p, LocalDateTime momento) {
        if (p == null || !p.isActiva() || subtotal <= 0 || momento == null) {
            return 0.0;
        }
        List<DayOfWeek> dias = p.getDiasSemana();
        if (dias == null || dias.isEmpty() || !dias.contains(momento.getDayOfWeek())) {
            return 0.0;
        }
        return Operaciones.EstrategiaDescuento.aplicarPorcentaje(subtotal, p.getPorcentaje());
    }
}