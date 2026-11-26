package Operaciones;

import com.datos.Rol;
import com.datos.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para Usuario con persistencia en PostgreSQL (RQ03-NF).
 */
public class UsuarioDAO implements GenericDAO<Usuario> {

    @Override
    public void crear(Usuario u) {
        String sql = "INSERT INTO usuario (id_usuario, nombre, clave, rol) VALUES (?, ?, ?, ?)";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, u.getIdUsuario());
            ps.setString(2, u.getNombre());
            ps.setString(3, u.getClave());
            ps.setString(4, u.getRol().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Usuario> consultar() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY id_usuario";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar usuarios: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void actualizar(Usuario u) {
        String sql = "UPDATE usuario SET nombre=?, clave=?, rol=? WHERE id_usuario=?";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getClave());
            ps.setString(3, u.getRol().name());
            ps.setInt(4, u.getIdUsuario());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Usuario u) {
        String sql = "DELETE FROM usuario WHERE id_usuario=?";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, u.getIdUsuario());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar usuario: " + e.getMessage(), e);
        }
    }

    /** Busca un usuario por nombre (login). */
    public Usuario buscarPorNombre(String nombre) {
        String sql = "SELECT * FROM usuario WHERE nombre = ?";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario: " + e.getMessage(), e);
        }
        return null;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id_usuario"),
                rs.getString("nombre"),
                rs.getString("clave"),
                Rol.valueOf(rs.getString("rol"))
        );
    }
}