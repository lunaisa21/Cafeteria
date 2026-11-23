package com.datos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CierreCaja {
    private LocalDate fecha;
    private double totalVentas;
    private int numPredidos;
}
