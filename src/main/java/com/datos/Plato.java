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
        setTiempoPreparacion(tiempoPreparacion);
        this.esVegetariano = esVegetariano;
    }

    @Override
    public double getPrecio() {
        return getPrecioBase();    }
}
