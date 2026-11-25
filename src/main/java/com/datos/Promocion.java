package com.datos;


import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor



public class Promocion {

    @Setter
    private int idPromocion;
    @Setter
    private String nombre;
    private List<DayOfWeek> diasSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private double porcentaje;
    @Setter
    private boolean activa;

    public Promocion(int idPromocion, String nombre, List<DayOfWeek> diasSemana,
                     LocalTime horaInicio, LocalTime horaFin, double porcentaje) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la promoción es obligatorio");
        }
        if ((horaInicio == null) != (horaFin == null)) {
            throw new IllegalArgumentException("La franja necesita hora de inicio y de fin");
        }
        validarPorcentaje(porcentaje);
        this.idPromocion = idPromocion;
        this.nombre = nombre;
        this.diasSemana = diasSemana == null ? new ArrayList<>() : new ArrayList<>(diasSemana);
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.porcentaje = porcentaje;
        this.activa = true;
    }

    private static void validarPorcentaje(double porcentaje) {
        if (porcentaje <= 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 (exclusivo) y 100");
        }
    }


    public boolean tieneRestriccionDia() {
        return !diasSemana.isEmpty();
    }

    public boolean tieneFranja() {
        return horaInicio != null && horaFin != null;
    }

    public boolean aplicaDia(DayOfWeek dia) {
        return diasSemana.isEmpty() || diasSemana.contains(dia);
    }

    public boolean aplicaFranja(LocalTime hora) {
        if (!tieneFranja()) {
            return true;
        }
        if (horaInicio.equals(horaFin)) { // franja de 24 h
            return true;
        }
        if (horaInicio.isBefore(horaFin)) {
            return !hora.isBefore(horaInicio) && hora.isBefore(horaFin);
        }
        // cruza la medianoche
        return !hora.isBefore(horaInicio) || hora.isBefore(horaFin);
    }


    public boolean aplicaEn(LocalDateTime momento) {
        return activa && aplicaDia(momento.getDayOfWeek()) && aplicaFranja(momento.toLocalTime());
    }



    public List<DayOfWeek> getDiasSemana() { return new ArrayList<>(diasSemana); }
    public void setDiasSemana(List<DayOfWeek> diasSemana) {
        this.diasSemana = diasSemana == null ? new ArrayList<>() : new ArrayList<>(diasSemana);
    }

    public void setFranja(LocalTime horaInicio, LocalTime horaFin) {
        if ((horaInicio == null) != (horaFin == null)) {
            throw new IllegalArgumentException("La franja necesita hora de inicio y de fin");
        }
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public void setPorcentaje(double porcentaje) {
        validarPorcentaje(porcentaje);
        this.porcentaje = porcentaje;
    }

    @Override
    public String toString() {
        return nombre + " (" + porcentaje + "%)";
    }
}
