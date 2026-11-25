package Operaciones;

import com.datos.AdicionDecorator;
import com.datos.Ingrediente;
import com.datos.ItemMenu;
import com.datos.LineaPedido;
import com.datos.Pedido;
import com.datos.Producto;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controla el stock: valida existencia (RF13), descuenta al confirmar un pedido (RF11),
 * permite consultar/actualizar el stock (RF12) y avisa a los StockObservador cuando un
 * ingrediente llega al minimo (PATRON OBSERVER).
 */
public class GestorInventario {
    private final Map<Integer, Ingrediente> ingredientes = new LinkedHashMap<>();
    private final List<StockObservador> observadores = new ArrayList<>();
    private final GenericDAO<Ingrediente> dao; // puede ser null: entonces solo memoria

    public GestorInventario() {
        this(null);
    }

    public GestorInventario(GenericDAO<Ingrediente> dao) {
        this.dao = dao;
        if (dao != null) {
            for (Ingrediente i : dao.listarTodos()) {
                ingredientes.put(i.getIdIngrediente(), i);
            }
        }
    }

    /** DAO de archivo para ingredientes: id|nombre|stockActual|stockMinimo */
    public static ArchivoDAO<Ingrediente> crearDAO(Path archivo) {
        return new ArchivoDAO<>(archivo,
                i -> String.valueOf(i.getIdIngrediente()),
                i -> i.getIdIngrediente() + "|" + i.getNombre().replace("|", " ") + "|"
                        + i.getStockActual() + "|" + i.getStockMinimo(),
                linea -> {
                    String[] c = linea.split("\\|");
                    return new Ingrediente(Integer.parseInt(c[0]), c[1],
                            Double.parseDouble(c[2]), Double.parseDouble(c[3]));
                });
    }

    // ---------------- Observer ----------------
    public void agregarObservador(StockObservador o) { observadores.add(o); }

    public void eliminarObservador(StockObservador o) { observadores.remove(o); }

    private void notificarStockMinimo(Ingrediente i) {
        for (StockObservador o : new ArrayList<>(observadores)) {
            o.alertaStockMinimo(i);
        }
    }

    // ---------------- administracion del stock (RF12) ----------------
    public void registrarIngrediente(Ingrediente i) {
        if (i == null) throw new IllegalArgumentException("El ingrediente es obligatorio.");
        if (ingredientes.containsKey(i.getIdIngrediente())) {
            throw new IllegalArgumentException("Ya existe un ingrediente con id " + i.getIdIngrediente() + ".");
        }
        ingredientes.put(i.getIdIngrediente(), i);
        persistir(i);
        if (i.estaBajoMinimo()) notificarStockMinimo(i);
    }

    public void actualizarStock(int idIngrediente, double nuevoStock) {
        Ingrediente i = obtener(idIngrediente);
        i.setStockActual(nuevoStock);
        persistir(i);
        if (i.estaBajoMinimo()) notificarStockMinimo(i);
    }

    public void actualizarStockMinimo(int idIngrediente, double nuevoMinimo) {
        Ingrediente i = obtener(idIngrediente);
        i.setStockMinimo(nuevoMinimo);
        persistir(i);
    }

    public void reponer(int idIngrediente, double cantidad) {
        Ingrediente i = obtener(idIngrediente);
        i.reponer(cantidad);
        persistir(i);
    }

    public Ingrediente obtener(int idIngrediente) {
        Ingrediente i = ingredientes.get(idIngrediente);
        if (i == null) {
            throw new IllegalArgumentException("No existe el ingrediente con id " + idIngrediente + ".");
        }
        return i;
    }

    public List<Ingrediente> listar() {
        return List.copyOf(ingredientes.values());
    }

    public List<Ingrediente> listarBajoMinimo() {
        return ingredientes.values().stream().filter(Ingrediente::estaBajoMinimo).toList();
    }

    // ---------------- validacion de existencia (RF13) ----------------
    /** Hay stock para preparar {@code cantidad} unidades del producto (con o sin adiciones). */
    public boolean hayStock(Producto producto, int cantidad) {
        Map<Ingrediente, Double> requeridos = new LinkedHashMap<>();
        acumular(requeridos, producto, cantidad);
        return faltante(requeridos) == null;
    }

    /** Hay stock para todo el pedido. */
    public boolean hayStock(Pedido pedido) {
        return faltante(requeridosDe(pedido)) == null;
    }

    /** Marca como no disponibles los items del menu cuyos ingredientes se agotaron (RF13). */
    public void actualizarDisponibilidad(Collection<? extends ItemMenu> items) {
        for (ItemMenu item : items) {
            item.setDisponible(hayStock(item, 1));
        }
    }

    // ---------------- descuento automatico (RF11) ----------------
    /**
     * Descuenta del inventario todos los ingredientes del pedido. Es "todo o nada":
     * si falta algo lanza IllegalStateException y no se modifica nada.
     * Despues de descontar avisa a los observadores si algun ingrediente quedo en el minimo.
     */
    public void descontarPedido(Pedido pedido) {
        Map<Ingrediente, Double> requeridos = requeridosDe(pedido);
        String problema = faltante(requeridos);
        if (problema != null) {
            throw new IllegalStateException(problema);
        }
        List<Ingrediente> tocados = new ArrayList<>();
        for (Map.Entry<Ingrediente, Double> e : requeridos.entrySet()) {
            Ingrediente real = ingredientes.get(e.getKey().getIdIngrediente());
            real.descontar(e.getValue());
            persistir(real);
            tocados.add(real);
        }
        for (Ingrediente i : tocados) {
            if (i.estaBajoMinimo()) notificarStockMinimo(i);
        }
    }


    public void devolverPedido(Pedido pedido) {
        for (Map.Entry<Ingrediente, Double> e : requeridosDe(pedido).entrySet()) {
            Ingrediente real = ingredientes.get(e.getKey().getIdIngrediente());
            if (real != null) {
                real.reponer(e.getValue());
                persistir(real);
            }
        }
    }


    private Map<Ingrediente, Double> requeridosDe(Pedido pedido) {
        if (pedido == null) throw new IllegalArgumentException("El pedido es obligatorio.");
        Map<Ingrediente, Double> requeridos = new LinkedHashMap<>();
        if (pedido.getLineas() != null) {
            for (LineaPedido l : pedido.getLineas()) {
                acumular(requeridos, l.getProducto(), l.getCantidad());
            }
        }
        return requeridos;
    }


    private void acumular(Map<Ingrediente, Double> destino, Producto producto, int cantidad) {
        Producto base = AdicionDecorator.productoBase(producto);
        if (base instanceof ItemMenu item) {
            for (Map.Entry<Ingrediente, Double> r : item.getReceta().entrySet()) {
                destino.merge(r.getKey(), r.getValue() * cantidad, Double::sum);
            }
        }
    }


    private String faltante(Map<Ingrediente, Double> requeridos) {
        for (Map.Entry<Ingrediente, Double> e : requeridos.entrySet()) {
            Ingrediente real = ingredientes.get(e.getKey().getIdIngrediente());
            if (real == null) {
                return "El ingrediente '" + e.getKey().getNombre() + "' no esta registrado en el inventario.";
            }
            if (!real.hayStock(e.getValue())) {
                return "Stock insuficiente de " + real.getNombre() + " (hay " + real.getStockActual()
                        + ", se necesitan " + e.getValue() + ").";
            }
        }
        return null;
    }

    private void persistir(Ingrediente i) {
        if (dao != null) dao.guardar(i);
    }
}