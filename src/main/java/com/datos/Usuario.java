package com.datos;

public class Usuario {
    private String nombre;
    private String clave;
    private Rol rol;

    public Usuario() {
    }

    public Usuario(String nombre, String clave, Rol rol) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (nombre.contains("|")) {
            throw new IllegalArgumentException("El nombre de usuario no puede contener el caracter '|'");
        }
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("La clave es obligatoria");
        }
        if (rol == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        this.nombre = nombre.trim();
        this.clave = clave;
        this.rol = rol;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public boolean esAdministrador() {
        return rol != null && rol.puedeAdministrar();
    }

    @Override
    public String toString() {
        return nombre + " (" + rol + ")"; // la clave nunca se imprime
    }
}
