package Operaciones;

import com.datos.Pedido;
import com.datos.Promocion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MotorPromociones {

    private final GenericDAO<Promocion> promocionDAO;
    private final EstrategiaDescuento estrategiaDia;
    private final EstrategiaDescuento estrategiaFranja;

    public MotorPromociones(GenericDAO<Promocion> promocionDAO) {
        this(promocionDAO, new DescuentoPorDia(), new DescuentoPorFranja());
    }

    public MotorPromociones(GenericDAO<Promocion> promocionDAO,
                            EstrategiaDescuento estrategiaDia,
                            EstrategiaDescuento estrategiaFranja) {
        if (promocionDAO == null || estrategiaDia == null || estrategiaFranja == null) {
            throw new IllegalArgumentException("El DAO y las estrategias de descuento son obligatorios");
        }
        this.promocionDAO = promocionDAO;
        this.estrategiaDia = estrategiaDia;
        this.estrategiaFranja = estrategiaFranja;
    }

    public void crear(Promocion p) {
        validar(p);
        if (p.getIdPromocion() != 0 && existeId(p.getIdPromocion())) {
            throw new IllegalArgumentException("Ya existe una promoción con id " + p.getIdPromocion());
        }
        promocionDAO.crear(p);
    }

    public void modificar(Promocion p) {
        validar(p);
        if (!existeId(p.getIdPromocion())) {
            throw new IllegalArgumentException("No existe la promoción con id " + p.getIdPromocion());
        }
        promocionDAO.actualizar(p);
    }

    public void eliminar(Promocion p) {
        if (p == null) {
            throw new IllegalArgumentException("La promoción es obligatoria");
        }
        promocionDAO.eliminar(String.valueOf(p.getIdPromocion()));
    }

    public List<Promocion> consultar() {
        return promocionDAO.consultar();
    }

    public double calcularDescuento(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido es obligatorio");
        }
        LocalDateTime momento = pedido.getFecha() != null ? pedido.getFecha() : LocalDateTime.now();
        return calcularDescuento(pedido.calcularSubtotal(), momento);
    }

    public double calcularDescuento(double subtotal, LocalDateTime momento) {
        if (subtotal <= 0) {
            return 0.0;
        }
        double mejor = 0.0;
        for (Promocion p : activas()) {
            mejor = Math.max(mejor, descuentoDe(p, subtotal, momento));
        }
        return Math.min(mejor, subtotal);
    }

    public Optional<Promocion> mejorPromocion(double subtotal, LocalDateTime momento) {
        if (subtotal <= 0) {
            return Optional.empty();
        }
        Promocion ganadora = null;
        double mejor = 0.0;
        for (Promocion p : activas()) {
            double d = Math.min(descuentoDe(p, subtotal, momento), subtotal);
            if (d > mejor) {
                mejor = d;
                ganadora = p;
            }
        }
        return Optional.ofNullable(ganadora);
    }

    private boolean existeId(int id) {
        return promocionDAO.consultar().stream().anyMatch(prom -> prom.getIdPromocion() == id);
    }

    private void validar(Promocion p) {
        if (p == null) {
            throw new IllegalArgumentException("La promoción es obligatoria");
        }
        if (p.getPorcentaje() <= 0 || p.getPorcentaje() > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 (exclusivo) y 100");
        }
        if (!tieneDias(p) && !tieneFranja(p)) {
            throw new IllegalArgumentException(
                    "La promoción debe tener días de la semana y/o una franja horaria");
        }
    }

    private List<Promocion> activas() {
        return promocionDAO.consultar().stream()
                .filter(Promocion::isActiva)
                .toList();
    }

    private double descuentoDe(Promocion p, double subtotal, LocalDateTime momento) {
        boolean usaDia = tieneDias(p);
        boolean usaFranja = tieneFranja(p);
        if (!usaDia && !usaFranja) {
            return 0.0;
        }
        double descuento = Double.MAX_VALUE;
        if (usaDia) {
            descuento = Math.min(descuento, estrategiaDia.calcularDescuento(subtotal, p, momento));
        }
        if (usaFranja) {
            descuento = Math.min(descuento, estrategiaFranja.calcularDescuento(subtotal, p, momento));
        }
        return descuento == Double.MAX_VALUE ? 0.0 : descuento;
    }

    private static boolean tieneDias(Promocion p) {
        return p.getDiasSemana() != null && !p.getDiasSemana().isEmpty();
    }

    private static boolean tieneFranja(Promocion p) {
        return p.getHoraInicio() != null && p.getHoraFin() != null;
    }
}