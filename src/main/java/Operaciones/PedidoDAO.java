package Operaciones;

import com.datos.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para Pedido con persistencia en PostgreSQL (RQ05, RF09).
 */
public class PedidoDAO implements GenericDAO<Pedido> {

    @Override
    public void crear(Pedido p) {
        String sqlPedido = "INSERT INTO pedido (mesa, fecha, estado, metodo_pago, subtotal, descuento, total, stock_descontado) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_pedido";
        String sqlLinea = "INSERT INTO linea_pedido (id_pedido, descripcion_producto, precio_unitario, cantidad) VALUES (?, ?, ?, ?)";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlPedido)) {
                    ps.setInt(1, p.getMesa());
                    ps.setTimestamp(2, Timestamp.valueOf(p.getFecha()));
                    ps.setString(3, p.getEstado().name());
                    ps.setString(4, p.getMetodoPago() != null ? p.getMetodoPago().name() : null);
                    ps.setDouble(5, p.getSubtotal());
                    ps.setDouble(6, p.getDescuento());
                    ps.setDouble(7, p.getTotal());
                    ps.setBoolean(8, p.isStockDescontado());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            p.setIdPedido(rs.getInt(1));
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(sqlLinea)) {
                    for (LineaPedido l : p.getLineas()) {
                        ps.setInt(1, p.getIdPedido());
                        ps.setString(2, l.getProducto().getDescripcion());
                        ps.setDouble(3, l.getProducto().getPrecio());
                        ps.setInt(4, l.getCantidad());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear pedido: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Pedido> consultar() {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT * FROM pedido ORDER BY id_pedido";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Pedido p = mapear(rs);
                cargarLineas(con, p);
                lista.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar pedidos: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void actualizar(Pedido p) {
        String sql = "UPDATE pedido SET estado=?, metodo_pago=?, subtotal=?, descuento=?, total=?, stock_descontado=? WHERE id_pedido=?";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getEstado().name());
            ps.setString(2, p.getMetodoPago() != null ? p.getMetodoPago().name() : null);
            ps.setDouble(3, p.getSubtotal());
            ps.setDouble(4, p.getDescuento());
            ps.setDouble(5, p.getTotal());
            ps.setBoolean(6, p.isStockDescontado());
            ps.setInt(7, p.getIdPedido());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar pedido: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Pedido p) {
        try (Connection con = GestorConexion.getInstancia().obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = con.prepareStatement("DELETE FROM linea_pedido WHERE id_pedido=?")) {
                    ps1.setInt(1, p.getIdPedido());
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM pedido WHERE id_pedido=?")) {
                    ps2.setInt(1, p.getIdPedido());
                    ps2.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar pedido: " + e.getMessage(), e);
        }
    }

    /** Consulta pedidos por fecha (RF09 - reporte del dia). */
    public List<Pedido> consultarPorFecha(LocalDate fecha) {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT * FROM pedido WHERE DATE(fecha) = ? ORDER BY id_pedido";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pedido p = mapear(rs);
                    cargarLineas(con, p);
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar pedidos por fecha: " + e.getMessage(), e);
        }
        return lista;
    }

    private void cargarLineas(Connection con, Pedido p) throws SQLException {
        String sql = "SELECT descripcion_producto, precio_unitario, cantidad FROM linea_pedido WHERE id_pedido = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getIdPedido());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final String desc = rs.getString("descripcion_producto");
                    final double precio = rs.getDouble("precio_unitario");
                    int cant = rs.getInt("cantidad");
                    Producto prodSnapshot = new Producto() {
                        @Override public String getDescripcion() { return desc; }
                        @Override public double getPrecio() { return precio; }
                    };
                    p.getLineas().add(new LineaPedido(prodSnapshot, cant));
                }
            }
        }
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setIdPedido(rs.getInt("id_pedido"));
        p.setMesa(rs.getInt("mesa"));
        p.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        p.setEstado(EstadoPedido.valueOf(rs.getString("estado")));
        String mp = rs.getString("metodo_pago");
        if (mp != null) p.setMetodoPago(MetodoPago.valueOf(mp));
        p.setSubtotal(rs.getDouble("subtotal"));
        p.setDescuento(rs.getDouble("descuento"));
        p.setTotal(rs.getDouble("total"));
        p.setStockDescontado(rs.getBoolean("stock_descontado"));
        return p;
    }
}
