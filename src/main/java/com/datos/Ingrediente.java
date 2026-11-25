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


    public boolean hayStock(double cantidad) {
        return stockActual >= cantidad;
    }

    public void descontar(double cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad a descontar no puede ser negativa");
        }
        if (!hayStock(cantidad)) {
            throw new IllegalStateException("Stock insuficiente de " + nombre
                    + " (hay " + stockActual + ", se piden " + cantidad + ")");
        }
        stockActual -= cantidad;
    }


    public void reponer(double cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad a reponer no puede ser negativa");
        }
        stockActual += cantidad;
    }


    public boolean estaBajoMinimo() {
        return stockActual <= stockMinimo;
    }

    public void setStockActual(double stockActual) {
        if (stockActual < 0) throw new IllegalArgumentException("El stock no puede ser negativo");
        this.stockActual = stockActual;
    }

    public void setStockMinimo(double stockMinimo) {
        if (stockMinimo < 0) throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        this.stockMinimo = stockMinimo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ingrediente other)) return false;
        return idIngrediente == other.idIngrediente;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idIngrediente);
    }

    @Override
    public String toString() {
        return nombre + " (stock " + stockActual + ", mín " + stockMinimo + ")";
    }
}

