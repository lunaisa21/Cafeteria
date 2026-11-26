package com.datos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LineaPedido {
    private int cantidad;
    private Producto producto;

    public double getSubtotal() {
        return producto.getPrecio() * cantidad;
    }
}
