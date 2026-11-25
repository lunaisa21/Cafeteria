package Operaciones;

import com.datos.Promocion;


import java.time.LocalDateTime;


public interface EstrategiaDescuento {

    /** Calcula el descuento (en dinero) para un subtotal en un momento dado. */
    double calcularDescuento(double subtotal, Promocion p, LocalDateTime momento);

    /** Versión del diagrama: evalúa con la fecha y hora actuales. */
    default double calcularDescuento(double subtotal, Promocion p) {
        return calcularDescuento(subtotal, p, LocalDateTime.now());
    }

    /** Descuento = subtotal * porcentaje / 100, redondeado a 2 decimales. */
    static double aplicarPorcentaje(double subtotal, double porcentaje) {
        return Math.round(subtotal * porcentaje) / 100.0;
    }
}
