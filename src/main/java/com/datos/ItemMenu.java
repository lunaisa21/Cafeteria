package com.datos;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;


@Getter
@Setter
public abstract class ItemMenu implements Producto {
    private String nombre;
    private String descripcion;
    private double precioBase;
    private boolean disponible;
    private Map<Ingrediente, Double> items;

}
