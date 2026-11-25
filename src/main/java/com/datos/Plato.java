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
    public String getDescripcion() {
        return esVegetariano ? getNombre() + " (vegetariano)" : getNombre();
    }

    public int getTiempoPreparacion() { return tiempoPreparacion; }
    public void setTiempoPreparacion(int tiempoPreparacion) {
        if (tiempoPreparacion < 0) {
            throw new IllegalArgumentException("El tiempo de preparación no puede ser negativo");
        }
        this.tiempoPreparacion = tiempoPreparacion;
    }

    public boolean isEsVegetariano() { return esVegetariano; }
    public void setEsVegetariano(boolean esVegetariano) { this.esVegetariano = esVegetariano; }
}
