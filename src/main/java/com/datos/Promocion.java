package com.datos;

import Operaciones.PedidoObservador;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;

@Getter
@Setter
public class Promocion {
    private  String nombre;
    private double precio;
    private List<DiaSemana> diaSemanas;
    private LocalDate horaFin;
    private LocalDateTime horaInicio;
    private double porcentaje;

}
