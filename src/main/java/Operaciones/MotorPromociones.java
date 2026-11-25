package Operaciones;


import com.datos.Pedido;
import com.datos.Promocion;

import java.time.LocalDateTime;
import java.util.Optional;


public class MotorPromociones {

    private final PromocionDAO promocionDAO;
    private final EstrategiaDescuento estrategiaDia;
    private final EstrategiaDescuento estrategiaFranja;

    public MotorPromociones(PromocionDAO promocionDAO) {
        this(promocionDAO, new DescuentoPorDia(), new DescuentoPorFranja());
    }

    public MotorPromociones(PromocionDAO promocionDAO,
                            EstrategiaDescuento estrategiaDia,
                            EstrategiaDescuento estrategiaFranja) {
        this.promocionDAO = promocionDAO;
        this.estrategiaDia = estrategiaDia;
        this.estrategiaFranja = estrategiaFranja;
    }


    public void crear(Promocion p) {
        validar(p);
        promocionDAO.crear(p);
    }

    public void modificar(Promocion p) {
        validar(p);
        promocionDAO.actualizar(p);
    }

    public void eliminar(Promocion p) {
        if (p == null) {
            throw new IllegalArgumentException("La promoción es obligatoria");
        }
        promocionDAO.eliminar(p);
    }

    private void validar(Promocion p) {
        if (p == null) {
            throw new IllegalArgumentException("La promoción es obligatoria");
        }
        if (!p.tieneRestriccionDia() && !p.tieneFranja()) {
            throw new IllegalArgumentException(
                    "La promoción debe tener días de la semana y/o una franja horaria");
        }
    }
    public double calcularDescuento(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido es obligatorio");
        }
        return calcularDescuento(pedido.calcularSubtotal(), pedido.getFecha());
    }

    public double calcularDescuento(double subtotal, LocalDateTime momento) {
        double mejor = 0;
        for (Promocion p : promocionDAO.consultarActivas()) {
            mejor = Math.max(mejor, descuentoDe(p, subtotal, momento));
        }
        return Math.clamp(subtotal, 0, mejor);
    }

    /** La promoción que se está aplicando (útil para mostrar su nombre en la factura / interfaz). */
    public Optional<Promocion> mejorPromocion(double subtotal, LocalDateTime momento) {
        Promocion ganadora = null;
        double mejor = 0;
        for (Promocion p : promocionDAO.consultarActivas()) {
            double d = descuentoDe(p, subtotal, momento);
            if (d > mejor) {
                mejor = d;
                ganadora = p;
            }
        }
        return Optional.ofNullable(ganadora);
    }

    private double descuentoDe(Promocion p, double subtotal, LocalDateTime momento) {
        double descuento = Double.MAX_VALUE;
        if (p.tieneRestriccionDia()) {
            descuento = Math.min(descuento, estrategiaDia.calcularDescuento(subtotal, p, momento));
        }
        if (p.tieneFranja()) {
            descuento = Math.min(descuento, estrategiaFranja.calcularDescuento(subtotal, p, momento));
        }
        return descuento == Double.MAX_VALUE ? 0 : descuento;
    }
}
