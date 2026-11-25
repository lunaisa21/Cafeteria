package com.datos;
import  lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@Setter


public class AdicionDecorator implements Producto {

    private final Producto producto; // null solo si es plantilla
    private final String nombre;
    private final double costoExtra;

    private AdicionDecorator(String nombre, double costoExtra) {
        validar(nombre, costoExtra);
        this.producto = null;
        this.nombre = nombre;
        this.costoExtra = costoExtra;
    }

    private static void validar(String nombre, double costoExtra) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la adición es obligatorio");
        }
        if (costoExtra < 0) {
            throw new IllegalArgumentException("El costo extra no puede ser negativo");
        }
    }


    public static AdicionDecorator plantilla(String nombre, double costoExtra) {
        return new AdicionDecorator(nombre, costoExtra);
    }

    public boolean esPlantilla() {
        return producto == null;
    }


    public AdicionDecorator aplicarA(Producto base) {
        return new AdicionDecorator(base, nombre, costoExtra);
    }


    public static Producto productoBase(Producto p) {
        while (p instanceof AdicionDecorator adicion && !adicion.esPlantilla()) {
            p = adicion.producto;
        }
        return p;
    }

    @Override
    public String getDescripcion() {
        verificarNoPlantilla();
        return producto.getDescripcion() + " + " + nombre;
    }

    @Override
    public double getPrecio() {
        verificarNoPlantilla();
        return producto.getPrecio() + costoExtra;
    }

    private void verificarNoPlantilla() {
        if (producto == null) {
            throw new IllegalStateException("La adición '" + nombre
                    + "' es una plantilla; aplíquela a un producto con aplicarA()");
        }
    }

    @Override
    public String toString() {
        return esPlantilla() ? nombre + " (+$" + costoExtra + ")" : getDescripcion() + " - $" + getPrecio();
    }
}

