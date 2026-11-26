package com.datos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {
    private int idUsuario;
    private String nombre;
    private String clave;
    private Rol rol;
}