package com.datos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Promocion {

    private int idPromocion;
    private String nombre;
    private List<DayOfWeek> diasSemana = new ArrayList<>();
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private double porcentaje;
    private boolean activa = true;

    public Promocion(int idPromocion, String nombre, List<DayOfWeek> diasSemana,
                     LocalTime horaInicio, LocalTime horaFin, double porcentaje) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la promocion es obligatorio");
        }
        if ((horaInicio == null) != (horaFin == null)) {
            throw new IllegalArgumentException("La franja necesita hora de inicio y de fin");
        }
        if (porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }
        this.idPromocion = idPromocion;
        this.nombre = nombre;
        this.diasSemana = diasSemana == null ? new ArrayList<>() : new ArrayList<>(diasSemana);
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.porcentaje = porcentaje;
        this.activa = true;
    }

    /** Tiene restriccion de dias de la semana. */
    public boolean tieneRestriccionDia() {
        return diasSemana != null && !diasSemana.isEmpty();
    }

    /** Tiene franja horaria. */
    public boolean tieneFranja() {
        return horaInicio != null && horaFin != null;
    }

    /** Aplica para un dia especifico. */
    public boolean aplicaDia(DayOfWeek dia) {
        return tieneRestriccionDia() && diasSemana.contains(dia);
    }

    /** Aplica para una hora especifica. */
    public boolean aplicaFranja(LocalTime hora) {
        if (!tieneFranja() || hora == null) return false;
        return !hora.isBefore(horaInicio) && !hora.isAfter(horaFin);
    }
}

