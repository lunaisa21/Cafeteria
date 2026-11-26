package Operaciones;

import com.datos.AdicionDecorator;
import com.datos.ItemMenu;
import com.datos.Producto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GestorMenu {

    private final GenericDAO<ItemMenu> itemMenuDAO;
    private final GestorInventario inventario;
    private final List<AdicionDecorator> catalogoAdiciones = new ArrayList<>();

    public GestorMenu(GenericDAO<ItemMenu> itemMenuDAO, GestorInventario inventario) {
        if (itemMenuDAO == null || inventario == null) {
            throw new IllegalArgumentException("El DAO del menú y el inventario son obligatorios");
        }
        this.itemMenuDAO = itemMenuDAO;
        this.inventario = inventario;
    }

    /** Retorna los ítems del menú disponibles tras actualizar el estado según inventario. */
    public List<ItemMenu> presentarMenu() {
        ocultarAgotados();
        return itemMenuDAO.consultar().stream()
                .filter(ItemMenu::isDisponible)
                .toList();
    }

    /**
     * Marca como no disponibles los ítems sin stock suficiente y vuelve a habilitar
     * los que ya tienen ingredientes disponibles; actualiza solo los que cambian.
     */
    public void ocultarAgotados() {
        for (ItemMenu item : itemMenuDAO.consultar()) {
            boolean hayStock = inventario.hayStock(item, 1);
            if (item.isDisponible() != hayStock) {
                item.setDisponible(hayStock);
                itemMenuDAO.actualizar(item);
            }
        }
    }

    /** Firma del diagrama: envuelve un Producto en un nuevo AdicionDecorator sin mutar el original. */
    public Producto agregarAdicion(Producto p, AdicionDecorator a) {
        if (p == null || a == null) {
            throw new IllegalArgumentException("El producto y la adición son obligatorios");
        }
        return new AdicionDecorator(p, a.getNombre(), a.getCostoExtra());
    }

    /** Sobrecarga auxiliar para buscar una adición registrada por nombre en el catálogo. */
    public Producto agregarAdicion(Producto p, String nombreAdicion) {
        if (nombreAdicion == null || nombreAdicion.isBlank()) {
            throw new IllegalArgumentException("El nombre de la adición es obligatorio");
        }
        AdicionDecorator modelo = catalogoAdiciones.stream()
                .filter(a -> a.getNombre().equalsIgnoreCase(nombreAdicion.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "La adición '" + nombreAdicion + "' no existe en el catálogo"));
        return agregarAdicion(p, modelo);
    }

    public void registrarAdicion(AdicionDecorator modelo) {
        if (modelo == null) {
            throw new IllegalArgumentException("La adición es obligatoria");
        }
        catalogoAdiciones.removeIf(a -> a.getNombre().equalsIgnoreCase(modelo.getNombre()));
        catalogoAdiciones.add(modelo);
    }

    public List<AdicionDecorator> getCatalogoAdiciones() {
        return Collections.unmodifiableList(catalogoAdiciones);
    }
}