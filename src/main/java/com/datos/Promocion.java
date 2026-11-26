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
        this.idPromocion = idPromocion;
        this.nombre = nombre;
        this.diasSemana = diasSemana == null ? new ArrayList<>() : new ArrayList<>(diasSemana);
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.porcentaje = porcentaje;
        this.activa = true;
    }

}
