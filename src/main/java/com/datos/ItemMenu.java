package com.datos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public abstract class ItemMenu implements Producto {

    private int idItem;
    private String nombre;
    private String descripcion;
    private double precioBase;
    private boolean disponible;
    private final Map<Ingrediente, Double> receta = new LinkedHashMap<>();

    protected ItemMenu(int idItem, String nombre, String descripcion, double precioBase) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del item es obligatorio");
        }
        if (precioBase < 0) {
            throw new IllegalArgumentException("El precio base no puede ser negativo");
        }
        this.idItem = idItem;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioBase = precioBase;
        this.disponible = true;
    }

    @Override
    public String getDescripcion() {
        return nombre;
    }

    @Override
    public double getPrecio() {
        return precioBase;
    }

    /** Agrega un ingrediente requerido a la receta del item (RF11). */
    public void agregarIngrediente(Ingrediente ingrediente, double cantidad) {
        if (ingrediente == null || cantidad <= 0) {
            throw new IllegalArgumentException("El ingrediente y la cantidad (>0) son obligatorios");
        }
        receta.put(ingrediente, cantidad);
    }
}

