package Operaciones;

import com.datos.Usuario;

import java.util.List;
import java.util.Set;

/**
 * Gestiona la autenticacion de usuarios y el control de permisos por rol (RQ03-NF).
 */
public class GestorAutenticacion {

    private static final Set<String> ACCIONES_OPERATIVAS = Set.of(
            "VER_MENU",
            "CREAR_PEDIDO",
            "CONFIRMAR_PEDIDO",
            "ACTUALIZAR_ESTADO_PEDIDO",
            "CANCELAR_PEDIDO",
            "COBRAR_PEDIDO",
            "CERRAR_CAJA",
            "VER_ALERTAS"
    );

    private final UsuarioDAO usuarioDAO;

    public GestorAutenticacion(UsuarioDAO usuarioDAO) {
        if (usuarioDAO == null) {
            throw new IllegalArgumentException("UsuarioDAO es obligatorio.");
        }
        this.usuarioDAO = usuarioDAO;
    }

    /** Autentica un usuario por nombre y clave (RQ03-NF). */
    public Usuario login(String nombre, String clave) {
        if (nombre == null || nombre.isBlank() || clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("Debe ingresar usuario y clave.");
        }
        Usuario u = usuarioDAO.buscarPorNombre(nombre.trim());
        if (u == null || !u.getClave().equals(clave)) {
            throw new SecurityException("Credenciales invalidas. Verifique el usuario y la clave.");
        }
        return u;
    }

    /** Verifica si el usuario tiene permiso para ejecutar la accion indicada (RQ03-NF). */
    public boolean tienePermiso(Usuario u, String accion) {
        if (u == null || u.getRol() == null || accion == null) {
            return false;
        }
        if (u.getRol().puedeAdministrar()) {
            return true;
        }
        return ACCIONES_OPERATIVAS.contains(accion.toUpperCase());
    }

    public void registrarUsuario(Usuario u) {
        if (u == null || u.getNombre() == null || u.getNombre().isBlank()) {
            throw new IllegalArgumentException("Los datos del usuario son obligatorios.");
        }
        usuarioDAO.crear(u);
    }

    public List<Usuario> consultarUsuarios() {
        return usuarioDAO.consultar();
    }
}
