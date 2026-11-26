package Operaciones;

import com.datos.Promocion;

import java.time.LocalDateTime;

/**
 * Patrón Strategy: cada implementación decide si una promoción aplica
 * (por día o por franja horaria) y cuánto dinero descuenta. Devuelve 0 cuando no aplica.
 */
public interface EstrategiaDescuento {

    /** Calcula el descuento (en dinero) para un subtotal en un momento dado. */
    double calcularDescuento(double subtotal, Promocion p, LocalDateTime momento);

    /** Firma del diagrama: evalúa con la fecha y hora actuales. */
    default double calcularDescuento(double subtotal, Promocion p) {
        return calcularDescuento(subtotal, p, LocalDateTime.now());
    }

    /** Descuento = subtotal * (porcentaje / 100.0), redondeado a 2 decimales. */
    static double aplicarPorcentaje(double subtotal, double porcentaje) {
        if (subtotal <= 0 || porcentaje <= 0) {
            return 0.0;
        }
        double monto = subtotal * (porcentaje / 100.0);
        return Math.round(monto * 100.0) / 100.0;
    }
}
