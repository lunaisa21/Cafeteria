package com.datos;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class Ingrediente {
    private String nombre;
    private double stockActual;
    private double stockMinimo;
}
