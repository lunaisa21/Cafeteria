package com.datos;

public enum Rol {
    ADMINISTRADOR, MESERO_CAJERO;

    public boolean puedeAdministrar() {
        return this == ADMINISTRADOR;
    }
}