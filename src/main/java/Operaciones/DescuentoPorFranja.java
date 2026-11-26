package Operaciones;

import com.datos.Promocion;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class  DescuentoPorFranja implements Operaciones.EstrategiaDescuento {

    @Override
    public double calcularDescuento(double subtotal, Promocion p, LocalDateTime momento) {
        if (p == null || !p.isActiva() || subtotal <= 0 || momento == null) {
            return 0.0;
        }
        if (!dentroDeFranja(momento.toLocalTime(), p.getHoraInicio(), p.getHoraFin())) {
            return 0.0;
        }
        return Operaciones.EstrategiaDescuento.aplicarPorcentaje(subtotal, p.getPorcentaje());
    }

    private static boolean dentroDeFranja(LocalTime hora, LocalTime inicio, LocalTime fin) {
        if (hora == null || inicio == null || fin == null) {
            return false;
        }
        if (inicio.equals(fin)) {
            return true; // franja de 24 horas
        }
        if (inicio.isBefore(fin)) {
            return !hora.isBefore(inicio) && hora.isBefore(fin);
        }
        return !hora.isBefore(inicio) || hora.isBefore(fin); // cruza medianoche
    }
}