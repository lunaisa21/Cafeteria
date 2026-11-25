package com.datos;
import  lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public abstract class AdicionDecorador implements Producto {
    private Producto producto;
    private String nombre;
    private double costoExtra;

    @Override
    public String getDescripcion() {
        return producto.getDescripcion() + "+" +  nombre;
    }
    @Override
    public double getPrecio() {
        return producto.getPrecio() + costoExtra;
    }
}
