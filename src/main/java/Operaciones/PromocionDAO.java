package Operaciones;

import com.datos.Promocion;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DAO para Promocion con persistencia en PostgreSQL (RQ04).
 */
public class PromocionDAO implements GenericDAO<Promocion> {

    @Override
    public void crear(Promocion p) {
        String sql = "INSERT INTO promocion (id_promocion, nombre, dias_semana, hora_inicio, hora_fin, porcentaje, activa) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setParams(ps, p);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear promocion: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Promocion> consultar() {
        List<Promocion> lista = new ArrayList<>();
        String sql = "SELECT * FROM promocion ORDER BY id_promocion";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar promociones: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void actualizar(Promocion p) {
        String sql = "UPDATE promocion SET nombre=?, dias_semana=?, hora_inicio=?, hora_fin=?, porcentaje=?, activa=? WHERE id_promocion=?";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, diasAString(p.getDiasSemana()));
            ps.setTime(3, p.getHoraInicio() != null ? Time.valueOf(p.getHoraInicio()) : null);
            ps.setTime(4, p.getHoraFin() != null ? Time.valueOf(p.getHoraFin()) : null);
            ps.setDouble(5, p.getPorcentaje());
            ps.setBoolean(6, p.isActiva());
            ps.setInt(7, p.getIdPromocion());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar promocion: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Promocion p) {
        String sql = "DELETE FROM promocion WHERE id_promocion=?";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getIdPromocion());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar promocion: " + e.getMessage(), e);
        }
    }

    /** Consulta las promociones activas. */
    public List<Promocion> consultarActivas() {
        List<Promocion> lista = new ArrayList<>();
        String sql = "SELECT * FROM promocion WHERE activa = true";
        try (Connection con = GestorConexion.getInstancia().obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar promociones activas: " + e.getMessage(), e);
        }
        return lista;
    }

    // ---- Helpers ----

    private void setParams(PreparedStatement ps, Promocion p) throws SQLException {
        ps.setInt(1, p.getIdPromocion());
        ps.setString(2, p.getNombre());
        ps.setString(3, diasAString(p.getDiasSemana()));
        ps.setTime(4, p.getHoraInicio() != null ? Time.valueOf(p.getHoraInicio()) : null);
        ps.setTime(5, p.getHoraFin() != null ? Time.valueOf(p.getHoraFin()) : null);
        ps.setDouble(6, p.getPorcentaje());
        ps.setBoolean(7, p.isActiva());
    }

    private String diasAString(List<DayOfWeek> dias) {
        if (dias == null || dias.isEmpty()) return "";
        return dias.stream().map(DayOfWeek::name).collect(Collectors.joining(","));
    }

    private List<DayOfWeek> stringADias(String s) {
        if (s == null || s.isBlank()) return new ArrayList<>();
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toList());
    }

    private Promocion mapear(ResultSet rs) throws SQLException {
        Promocion p = new Promocion(
                rs.getInt("id_promocion"),
                rs.getString("nombre"),
                stringADias(rs.getString("dias_semana")),
                rs.getTime("hora_inicio") != null ? rs.getTime("hora_inicio").toLocalTime() : null,
                rs.getTime("hora_fin") != null ? rs.getTime("hora_fin").toLocalTime() : null,
                rs.getDouble("porcentaje")
        );
        p.setActiva(rs.getBoolean("activa"));
        return p;
    }
}
