package com.datos;
import  lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@Setter


public class AdicionDecorator implements Producto {

    private Producto producto;
    private String nombre;
    private double costoExtra;

    private AdicionDecorator(String nombre, double costoExtra) {
        this.producto = null;
        this.nombre = nombre;
        this.costoExtra = costoExtra;
    }


    @Override
    public String getDescripcion() {
        return producto.getDescripcion() + " + " + nombre;
    }

    @Override
    public double getPrecio() {
        return producto.getPrecio() + costoExtra;
    }


}

