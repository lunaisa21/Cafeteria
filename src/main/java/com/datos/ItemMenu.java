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


    @Override
    public String getDescripcion() {
        return nombre;
    }

    @Override
    public double getPrecio() {
        return precioBase;
    }



    public void agregarIngrediente(Ingrediente ingrediente, double cantidad) {
        if (ingrediente == null) {
            throw new IllegalArgumentException("El ingrediente es obligatorio");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad en la receta debe ser mayor a 0");
        }
        receta.put(ingrediente, cantidad);
    }

    public void quitarIngrediente(Ingrediente ingrediente) {
        receta.remove(ingrediente);
    }


    public Map<Ingrediente, Double> getReceta() {
        return Collections.unmodifiableMap(receta);
    }

    public void setReceta(Map<Ingrediente, Double> nueva) {
        receta.clear();
        if (nueva != null) {
            nueva.forEach(this::agregarIngrediente);
        }
    }


    public String getDetalle() { return descripcion; }
    public void setDetalle(String descripcion) { this.descripcion = descripcion; }

    public void setPrecioBase(double precioBase) {
        if (precioBase < 0) throw new IllegalArgumentException("El precio base no puede ser negativo");
        this.precioBase = precioBase;
    }

    @Override
    public String toString() {
        return getDescripcion() + " - $" + getPrecio();
    }
}

