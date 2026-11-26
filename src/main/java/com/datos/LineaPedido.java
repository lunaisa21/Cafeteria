package com.datos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LineaPedido {
    private Producto producto;
    private int cantidad;

    public double getSubtotal() {
        return producto.getPrecio() * cantidad;
    }
}
