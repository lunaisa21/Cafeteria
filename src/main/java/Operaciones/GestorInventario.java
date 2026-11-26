package Operaciones;

import com.datos.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestiona el inventario de ingredientes, valida existencias, descuenta por pedido
 * y emite alertas de stock minimo (RF11, RF12, RF13, RQ05-NF).
 */
public class GestorInventario {

    private final IngredienteDAO ingredienteDAO;
    private final List<StockObservador> observadores = new ArrayList<>();
    private final Map<Integer, Ingrediente> ingredientes = new LinkedHashMap<>();

    public GestorInventario(IngredienteDAO ingredienteDAO) {
        if (ingredienteDAO == null) {
            throw new IllegalArgumentException("El IngredienteDAO es obligatorio.");
        }
        this.ingredienteDAO = ingredienteDAO;
        cargarDesdeDAO();
    }

    private void cargarDesdeDAO() {
        ingredientes.clear();
        for (Ingrediente i : ingredienteDAO.consultar()) {
            ingredientes.put(i.getIdIngrediente(), i);
        }
    }

    // ---- Patron Observer (RQ05-NF) ----

    public void agregarObservador(StockObservador o) {
        if (o != null && !observadores.contains(o)) {
            observadores.add(o);
        }
    }

    public void eliminarObservador(StockObservador o) {
        observadores.remove(o);
    }

    private void notificarStockMinimo(Ingrediente i) {
        for (StockObservador o : new ArrayList<>(observadores)) {
            o.alertaStockMinimo(i);
        }
    }

    // ---- Administracion de Stock (RF12) ----

    public void registrarIngrediente(Ingrediente i) {
        if (i == null) {
            throw new IllegalArgumentException("El ingrediente es obligatorio.");
        }
        cargarDesdeDAO();
        if (ingredientes.containsKey(i.getIdIngrediente())) {
            throw new IllegalArgumentException("Ya existe un ingrediente con ID " + i.getIdIngrediente()
                    + ". Use un ID distinto o actualice el stock del existente.");
        }
        ingredienteDAO.crear(i);
        ingredientes.put(i.getIdIngrediente(), i);
        if (i.estaBajoMinimo()) {
            notificarStockMinimo(i);
        }
    }

    /** Actualiza el stock actual de un ingrediente y verifica el minimo (RF12, RQ05-NF). */
    public void actualizarStock(Ingrediente i, double nuevaCantidad) {
        if (i == null) {
            throw new IllegalArgumentException("Debe seleccionar un ingrediente valido.");
        }
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo. Ingrese un valor mayor o igual a 0.");
        }
        cargarDesdeDAO();
        Ingrediente real = ingredientes.getOrDefault(i.getIdIngrediente(), i);
        real.setStockActual(nuevaCantidad);
        i.setStockActual(nuevaCantidad);
        ingredienteDAO.actualizar(real);
        ingredientes.put(real.getIdIngrediente(), real);
        if (real.estaBajoMinimo()) {
            notificarStockMinimo(real);
        }
    }

    /** Repone una cantidad adicional al stock actual de un ingrediente. */
    public void reponerStock(Ingrediente i, double cantidadAdicional) {
        if (i == null) {
            throw new IllegalArgumentException("Debe seleccionar un ingrediente valido.");
        }
        if (cantidadAdicional <= 0) {
            throw new IllegalArgumentException("La cantidad a reponer debe ser mayor a 0.");
        }
        cargarDesdeDAO();
        Ingrediente real = ingredientes.getOrDefault(i.getIdIngrediente(), i);
        real.reponer(cantidadAdicional);
        i.setStockActual(real.getStockActual());
        ingredienteDAO.actualizar(real);
        ingredientes.put(real.getIdIngrediente(), real);
    }

    public List<Ingrediente> consultarStock() {
        cargarDesdeDAO();
        return new ArrayList<>(ingredientes.values());
    }

    public List<Ingrediente> consultarBajoMinimo() {
        return ingredienteDAO.consultarBajoMinimo();
    }

    // ---- Validacion de stock (RF13) ----

    public boolean hayStock(Producto p) {
        return hayStock(p, 1);
    }

    public boolean hayStock(Producto p, int cantidad) {
        if (p == null || cantidad <= 0) return false;
        cargarDesdeDAO();
        Map<Ingrediente, Double> requeridos = new LinkedHashMap<>();
        acumularIngredientes(requeridos, p, cantidad);
        return buscarFaltante(requeridos) == null;
    }

    public boolean hayStock(Pedido pedido) {
        if (pedido == null) return false;
        cargarDesdeDAO();
        return buscarFaltante(requeridosDePedido(pedido)) == null;
    }

    // ---- Descuento y reposicion por pedido (RF11) ----

    public void descontarPorPedido(Pedido p) {
        if (p == null) {
            throw new IllegalArgumentException("El pedido es obligatorio.");
        }
        cargarDesdeDAO();
        Map<Ingrediente, Double> requeridos = requeridosDePedido(p);
        String faltante = buscarFaltante(requeridos);
        if (faltante != null) {
            throw new IllegalStateException(faltante);
        }

        List<Ingrediente> afectados = new ArrayList<>();
        for (Map.Entry<Ingrediente, Double> entry : requeridos.entrySet()) {
            Ingrediente real = ingredientes.get(entry.getKey().getIdIngrediente());
            real.descontar(entry.getValue());
            entry.getKey().setStockActual(real.getStockActual());
            ingredienteDAO.actualizar(real);
            afectados.add(real);
        }
        p.setStockDescontado(true);

        for (Ingrediente ing : afectados) {
            if (ing.estaBajoMinimo()) {
                notificarStockMinimo(ing);
            }
        }
    }

    public void reponerPorPedido(Pedido p) {
        if (p == null || !p.isStockDescontado()) {
            return;
        }
        cargarDesdeDAO();
        Map<Ingrediente, Double> requeridos = requeridosDePedido(p);
        for (Map.Entry<Ingrediente, Double> entry : requeridos.entrySet()) {
            Ingrediente real = ingredientes.get(entry.getKey().getIdIngrediente());
            if (real != null) {
                real.reponer(entry.getValue());
                entry.getKey().setStockActual(real.getStockActual());
                ingredienteDAO.actualizar(real);
            }
        }
        p.setStockDescontado(false);
    }

    // ---- Helpers internos ----

    private Map<Ingrediente, Double> requeridosDePedido(Pedido pedido) {
        Map<Ingrediente, Double> requeridos = new LinkedHashMap<>();
        if (pedido.getLineas() != null) {
            for (LineaPedido linea : pedido.getLineas()) {
                acumularIngredientes(requeridos, linea.getProducto(), linea.getCantidad());
            }
        }
        return requeridos;
    }

    private void acumularIngredientes(Map<Ingrediente, Double> destino, Producto producto, int cantidad) {
        Producto actual = producto;
        while (actual instanceof AdicionDecorator adicion) {
            for (Map.Entry<Ingrediente, Double> e : adicion.getReceta().entrySet()) {
                destino.merge(e.getKey(), e.getValue() * cantidad, Double::sum);
            }
            actual = adicion.getProducto();
        }
        if (actual instanceof ItemMenu item) {
            for (Map.Entry<Ingrediente, Double> e : item.getReceta().entrySet()) {
                destino.merge(e.getKey(), e.getValue() * cantidad, Double::sum);
            }
        }
    }

    private String buscarFaltante(Map<Ingrediente, Double> requeridos) {
        for (Map.Entry<Ingrediente, Double> entry : requeridos.entrySet()) {
            Ingrediente real = ingredientes.get(entry.getKey().getIdIngrediente());
            if (real == null) {
                return "El ingrediente '" + entry.getKey().getNombre()
                        + "' no esta registrado en el inventario. Registrelo primero.";
            }
            if (!real.hayStock(entry.getValue())) {
                return "Stock insuficiente de '" + real.getNombre() + "' (disponible: "
                        + real.getStockActual() + ", requerido: " + entry.getValue()
                        + "). Reponer stock o reducir la cantidad.";
            }
        }
        return null;
    }
}
