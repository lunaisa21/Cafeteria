package Operaciones;
import com.datos.Bebida;
import com.datos.Ingrediente;
import com.datos.ItemMenu;
import com.datos.Plato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DAO para ItemMenu con persistencia en PostgreSQL (RQ01, RF13).
 */
public class ItemMenuDAO implements GenericDAO<ItemMenu> {

    @Override
    public void crear(ItemMenu item) {
        String sql = "INSERT INTO item_menu (id_item, nombre, descripcion, precio_base, disponible, tipo, tiempo_preparacion, es_vegetariano, tamano, es_fria) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlReceta = "INSERT INTO item_ingrediente (id_item, id_ingrediente, cantidad) VALUES (?, ?, ?)";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    setItemParams(ps, item);
                    ps.executeUpdate();
                }
                if (!item.getReceta().isEmpty()) {
                    try (PreparedStatement psRec = con.prepareStatement(sqlReceta)) {
                        for (Map.Entry<Ingrediente, Double> entry : item.getReceta().entrySet()) {
                            psRec.setInt(1, item.getIdItem());
                            psRec.setInt(2, entry.getKey().getIdIngrediente());
                            psRec.setDouble(3, entry.getValue());
                            psRec.addBatch();
                        }
                        psRec.executeBatch();
                    }
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear item del menu: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ItemMenu> consultar() {
        List<ItemMenu> items = new ArrayList<>();
        String sql = "SELECT * FROM item_menu ORDER BY id_item";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ItemMenu item = mapearItem(rs);
                cargarReceta(con, item);
                items.add(item);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar menu: " + e.getMessage(), e);
        }
        return items;
    }

    @Override
    public void actualizar(ItemMenu item) {
        String sql = "UPDATE item_menu SET nombre=?, descripcion=?, precio_base=?, disponible=?, tiempo_preparacion=?, es_vegetariano=?, tamano=?, es_fria=? WHERE id_item=?";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, item.getNombre());
            ps.setString(2, item.getDescripcion());
            ps.setDouble(3, item.getPrecioBase());
            ps.setBoolean(4, item.isDisponible());
            if (item instanceof Plato plato) {
                ps.setInt(5, plato.getTiempoPreparacion());
                ps.setBoolean(6, plato.isEsVegetariano());
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.BOOLEAN);
            } else if (item instanceof Bebida bebida) {
                ps.setNull(5, Types.INTEGER);
                ps.setNull(6, Types.BOOLEAN);
                ps.setString(7, bebida.getTamano());
                ps.setBoolean(8, bebida.isEsFria());
            } else {
                ps.setNull(5, Types.INTEGER);
                ps.setNull(6, Types.BOOLEAN);
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.BOOLEAN);
            }
            ps.setInt(9, item.getIdItem());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar item: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(ItemMenu item) {
        String sql = "DELETE FROM item_menu WHERE id_item=?";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, item.getIdItem());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar item: " + e.getMessage(), e);
        }
    }

    /** Consulta solo los items disponibles (RF13). */
    public List<ItemMenu> consultarDisponibles() {
        List<ItemMenu> items = new ArrayList<>();
        String sql = "SELECT * FROM item_menu WHERE disponible = true ORDER BY id_item";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ItemMenu item = mapearItem(rs);
                cargarReceta(con, item);
                items.add(item);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar items disponibles: " + e.getMessage(), e);
        }
        return items;
    }

    // ---- Helpers ----

    private void cargarReceta(Connection con, ItemMenu item) throws SQLException {
        String sql = "SELECT i.id_ingrediente, i.nombre, i.stock_actual, i.stock_minimo, ii.cantidad "
                + "FROM item_ingrediente ii "
                + "JOIN ingrediente i ON i.id_ingrediente = ii.id_ingrediente "
                + "WHERE ii.id_item = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, item.getIdItem());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingrediente ing = new Ingrediente(
                            rs.getInt("id_ingrediente"),
                            rs.getString("nombre"),
                            rs.getDouble("stock_actual"),
                            rs.getDouble("stock_minimo")
                    );
                    item.agregarIngrediente(ing, rs.getDouble("cantidad"));
                }
            }
        }
    }

    private void setItemParams(PreparedStatement ps, ItemMenu item) throws SQLException {
        ps.setInt(1, item.getIdItem());
        ps.setString(2, item.getNombre());
        ps.setString(3, item.getDescripcion());
        ps.setDouble(4, item.getPrecioBase());
        ps.setBoolean(5, item.isDisponible());
        if (item instanceof Plato plato) {
            ps.setString(6, "PLATO");
            ps.setInt(7, plato.getTiempoPreparacion());
            ps.setBoolean(8, plato.isEsVegetariano());
            ps.setNull(9, Types.VARCHAR);
            ps.setNull(10, Types.BOOLEAN);
        } else if (item instanceof Bebida bebida) {
            ps.setString(6, "BEBIDA");
            ps.setNull(7, Types.INTEGER);
            ps.setNull(8, Types.BOOLEAN);
            ps.setString(9, bebida.getTamano());
            ps.setBoolean(10, bebida.isEsFria());
        } else {
            ps.setString(6, "OTRO");
            ps.setNull(7, Types.INTEGER);
            ps.setNull(8, Types.BOOLEAN);
            ps.setNull(9, Types.VARCHAR);
            ps.setNull(10, Types.BOOLEAN);
        }
    }

    private ItemMenu mapearItem(ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo");
        if ("PLATO".equals(tipo)) {
            Plato p = new Plato(
                    rs.getInt("id_item"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getDouble("precio_base"),
                    rs.getInt("tiempo_preparacion"),
                    rs.getBoolean("es_vegetariano")
            );
            p.setDisponible(rs.getBoolean("disponible"));
            return p;
        } else {
            Bebida b = new Bebida(
                    rs.getInt("id_item"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getDouble("precio_base"),
                    rs.getString("tamano"),
                    rs.getBoolean("es_fria")
            );
            b.setDisponible(rs.getBoolean("disponible"));
            return b;
        }
    }
}