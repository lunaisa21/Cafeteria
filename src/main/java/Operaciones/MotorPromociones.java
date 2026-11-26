package Operaciones;

import Operaciones.DescuentoPorDia;
import Operaciones.DescuentoPorFranja;
import Operaciones.EstrategiaDescuento;
import Operaciones.PromocionDAO;
import com.datos.Pedido;
import com.datos.Promocion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Motor de promociones que aplica estrategias de descuento por dia y franja horaria (RQ04).
 */
public class MotorPromociones {

    private final PromocionDAO promocionDAO;
    private final EstrategiaDescuento estrategiaDia;
    private final EstrategiaDescuento estrategiaFranja;
    private final List<Promocion> promociones = new ArrayList<>();

    public MotorPromociones(PromocionDAO promocionDAO) {
        this(promocionDAO, new DescuentoPorDia(), new DescuentoPorFranja());
    }

    public MotorPromociones(PromocionDAO promocionDAO,
                            EstrategiaDescuento estrategiaDia,
                            EstrategiaDescuento estrategiaFranja) {
        if (promocionDAO == null) {
            throw new IllegalArgumentException("PromocionDAO es obligatorio.");
        }
        this.promocionDAO = promocionDAO;
        this.estrategiaDia = estrategiaDia;
        this.estrategiaFranja = estrategiaFranja;
        refrescar();
    }

    private void refrescar() {
        promociones.clear();
        promociones.addAll(promocionDAO.consultar());
    }

    public void crear(Promocion p) {
        validar(p);
        promocionDAO.crear(p);
        refrescar();
    }

    public void modificar(Promocion p) {
        validar(p);
        promocionDAO.actualizar(p);
        refrescar();
    }

    public void eliminar(Promocion p) {
        if (p == null) {
            throw new IllegalArgumentException("Debe seleccionar una promocion para eliminar.");
        }
        promocionDAO.eliminar(p);
        refrescar();
    }

    public List<Promocion> consultarTodas() {
        refrescar();
        return Collections.unmodifiableList(promociones);
    }

    public List<Promocion> consultarActivas() {
        return promocionDAO.consultarActivas();
    }

    public double calcularDescuento(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido es obligatorio.");
        }
        LocalDateTime momento = pedido.getFecha() != null ? pedido.getFecha() : LocalDateTime.now();
        return calcularDescuento(pedido.calcularSubtotal(), momento);
    }

    public double calcularDescuento(double subtotal, LocalDateTime momento) {
        double mejor = 0;
        for (Promocion p : promocionDAO.consultarActivas()) {
            mejor = Math.max(mejor, descuentoDe(p, subtotal, momento));
        }
        return Math.min(subtotal, Math.max(0, mejor));
    }

    /** Devuelve la promocion mas conveniente aplicable en ese momento. */
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

    // ---- Helpers ----

    private void validar(Promocion p) {
        if (p == null) {
            throw new IllegalArgumentException("La promocion es obligatoria.");
        }
        if (!p.tieneRestriccionDia() && !p.tieneFranja()) {
            throw new IllegalArgumentException(
                    "La promocion debe definir al menos un dia de la semana y/o una franja horaria.");
        }
        if (p.tieneFranja() && p.getHoraInicio().isAfter(p.getHoraFin())) {
            throw new IllegalArgumentException(
                    "La hora de inicio no puede ser posterior a la hora de fin.");
        }
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
