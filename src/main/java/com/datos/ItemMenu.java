package com.datos;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public abstract class ItemMenu implements Producto {
    private String nombre;
    private String descripcion;
    private double precioBase;
    private boolean disponible;
    private Map<Ingrediente, Double> receta;
     @Override
    public double getPrecio(){
         return this.precioBase;
     }

}
