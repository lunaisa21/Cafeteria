package com.datos;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class Bebida extends ItemMenu {
    private String tamano; // "Pequeno", "Mediano", "Grande"
    private boolean esFria;
    public Bebida(int idItem, String nombre, String descripcion, double precioBase,
                  String tamano, boolean esFria) {
        super(idItem, nombre, descripcion, precioBase);
        this.tamano = tamano;
        this.esFria = esFria;
    }
    @Override
    public String getDescripcion() {
        String base = getNombre();
        if (tamano != null && !tamano.isBlank()) {
            base += " " + tamano;
        }
        return esFria ? base + " (fria)" : base + " (caliente)";
    }
    @Override
    public double getPrecio() {
        return getPrecioBase();
    }
}