package com.datos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Usuario {
    String nombre;
    String clave;
    Rol rol;
}
