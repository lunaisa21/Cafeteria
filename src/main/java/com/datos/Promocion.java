package com.datos;

import Operaciones.PedidoObservador;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Promocion {
    private  String nombre;
    private List<DiaSemana> diaSemanas;
    private LocalDate horaFin;
    private LocalDateTime horaInicio;
    private double porcentaje;
}
