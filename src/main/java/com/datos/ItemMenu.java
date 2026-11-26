package com.datos;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor


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

}

