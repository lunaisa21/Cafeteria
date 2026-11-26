package com.datos;
import lombok.Getter;
import lombok.Setter;
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
    /** Verifica si el stock actual está por debajo del mínimo (RQ05-NF). */
    public boolean estaBajoMinimo() {
        return stockActual <= stockMinimo;
    }
    /** Verifica si hay stock suficiente para la cantidad requerida. */
    public boolean hayStock(double cantidadRequerida) {
        return stockActual >= cantidadRequerida;
    }
    /** Descuenta stock (RF11). */
    public void descontar(double cantidad) {
        if (cantidad < 0) throw new IllegalArgumentException("La cantidad a descontar no puede ser negativa");
        if (stockActual < cantidad) throw new IllegalStateException("Stock insuficiente de " + nombre);
        this.stockActual -= cantidad;
    }
    /** Repone stock. */
    public void reponer(double cantidad) {
        if (cantidad < 0) throw new IllegalArgumentException("La cantidad a reponer no puede ser negativa");
        this.stockActual += cantidad;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ingrediente that)) return false;
        return idIngrediente == that.idIngrediente;
    }
    @Override
    public int hashCode() {
        return Integer.hashCode(idIngrediente);
    }
