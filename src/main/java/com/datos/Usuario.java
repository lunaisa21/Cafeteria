package com.datos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Usuario {
    private String nombre;
    private int idUsuario;
    private String clave;
    private Rol rol;

}
