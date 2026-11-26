package com.datos;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter



public class Ingrediente {

    private int idIngrediente;
    private String nombre;
    private double stockActual;
    private double stockMinimo;

    public Ingrediente(int idIngrediente, String nombre, double stockActual, double stockMinimo) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del ingrediente es obligatorio");
        }
        if (stockActual < 0 || stockMinimo < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        this.idIngrediente = idIngrediente;
        this.nombre = nombre;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
    }

}

