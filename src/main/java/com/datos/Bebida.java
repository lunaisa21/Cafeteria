package com.datos;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter

public class Bebida extends ItemMenu {

    private String tamano; // ej. "Pequeño", "Mediano", "Grande"
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
        return esFria ? base + " (fría)" : base + " (caliente)";
    }

    public double getPrecio() {

    }

    public void setTamano(String tamano) { this.tamano = tamano; }

    public void setEsFria(boolean esFria) { this.esFria = esFria; }
}

