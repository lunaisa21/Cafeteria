package Operaciones;

import com.datos.CierreCaja;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para CierreCaja con persistencia en PostgreSQL (RF09).
 */
public class CierreCajaDAO implements GenericDAO<CierreCaja> {

    @Override
    public void crear(CierreCaja c) {
        String sql = "INSERT INTO cierre_caja (fecha, total_ventas, num_pedidos) VALUES (?, ?, ?)";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(c.getFecha()));
            ps.setDouble(2, c.getTotalVentas());
            ps.setInt(3, c.getNumPedidos());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear cierre de caja: " + e.getMessage(), e);
        }
    }

    @Override
    public List<CierreCaja> consultar() {
        List<CierreCaja> lista = new ArrayList<>();
        String sql = "SELECT * FROM cierre_caja ORDER BY fecha";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar cierres de caja: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void actualizar(CierreCaja c) {
        String sql = "UPDATE cierre_caja SET total_ventas=?, num_pedidos=? WHERE fecha=?";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, c.getTotalVentas());
            ps.setInt(2, c.getNumPedidos());
            ps.setDate(3, Date.valueOf(c.getFecha()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar cierre de caja: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(CierreCaja c) {
        String sql = "DELETE FROM cierre_caja WHERE fecha=?";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(c.getFecha()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar cierre de caja: " + e.getMessage(), e);
        }
    }

    /** Consulta cierre de caja por fecha especifica (RF09). */
    public CierreCaja consultarPorFecha(LocalDate fecha) {
        String sql = "SELECT * FROM cierre_caja WHERE fecha = ?";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar cierre por fecha: " + e.getMessage(), e);
        }
        return null;
    }

    private CierreCaja mapear(ResultSet rs) throws SQLException {
        return new CierreCaja(
                rs.getDate("fecha").toLocalDate(),
                rs.getDouble("total_ventas"),
                rs.getInt("num_pedidos")
        );
    }
}
