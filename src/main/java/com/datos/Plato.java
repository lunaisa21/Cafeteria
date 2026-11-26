package com.datos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Plato extends ItemMenu {

    private int tiempoPreparacion; // minutos
    private boolean esVegetariano;

    public Plato(int idItem, String nombre, String descripcion, double precioBase,
                 int tiempoPreparacion, boolean esVegetariano) {
        super(idItem, nombre, descripcion, precioBase);
        if (tiempoPreparacion < 0) {
            throw new IllegalArgumentException("El tiempo de preparacion no puede ser negativo");
        }
        this.tiempoPreparacion = tiempoPreparacion;
        this.esVegetariano = esVegetariano;
    }

    @Override
    public String getDescripcion() {
        String base = getNombre();
        if (esVegetariano) base += " (vegetariano)";
        return base;
    }

    @Override
    public double getPrecio() {
        return getPrecioBase();
    }
}
