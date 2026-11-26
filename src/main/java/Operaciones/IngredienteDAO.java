package Operaciones;

import com.datos.Ingrediente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para Ingrediente con persistencia en PostgreSQL (RF11, RF12).
 */
public class IngredienteDAO implements GenericDAO<Ingrediente> {

    @Override
    public void crear(Ingrediente i) {
        String sql = "INSERT INTO ingrediente (id_ingrediente, nombre, stock_actual, stock_minimo) VALUES (?, ?, ?, ?)";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, i.getIdIngrediente());
            ps.setString(2, i.getNombre());
            ps.setDouble(3, i.getStockActual());
            ps.setDouble(4, i.getStockMinimo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear ingrediente: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Ingrediente> consultar() {
        List<Ingrediente> lista = new ArrayList<>();
        String sql = "SELECT * FROM ingrediente ORDER BY id_ingrediente";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar ingredientes: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void actualizar(Ingrediente i) {
        String sql = "UPDATE ingrediente SET nombre=?, stock_actual=?, stock_minimo=? WHERE id_ingrediente=?";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, i.getNombre());
            ps.setDouble(2, i.getStockActual());
            ps.setDouble(3, i.getStockMinimo());
            ps.setInt(4, i.getIdIngrediente());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar ingrediente: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Ingrediente i) {
        String sql = "DELETE FROM ingrediente WHERE id_ingrediente=?";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, i.getIdIngrediente());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar ingrediente: " + e.getMessage(), e);
        }
    }

    /** Consulta ingredientes bajo el stock minimo (RQ05-NF). */
    public List<Ingrediente> consultarBajoMinimo() {
        List<Ingrediente> lista = new ArrayList<>();
        String sql = "SELECT * FROM ingrediente WHERE stock_actual <= stock_minimo";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar ingredientes bajo minimo: " + e.getMessage(), e);
        }
        return lista;
    }

    private Ingrediente mapear(ResultSet rs) throws SQLException {
        return new Ingrediente(
                rs.getInt("id_ingrediente"),
                rs.getString("nombre"),
                rs.getDouble("stock_actual"),
                rs.getDouble("stock_minimo")
        );
    }
}
