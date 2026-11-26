package Operaciones;

import com.datos.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestiona la presentacion del menu, personalizacion con adiciones
 * y ocultamiento automatico de productos agotados (RQ01, RQ02, RQ03, RQ07, RF13).
 */
public class GestorMenu {

    private final ItemMenuDAO itemMenuDAO;
    private final GestorInventario inventario;
    private final List<AdicionDecorator> catalogoAdiciones = new ArrayList<>();

    public GestorMenu(ItemMenuDAO itemMenuDAO, GestorInventario inventario) {
        if (itemMenuDAO == null || inventario == null) {
            throw new IllegalArgumentException("ItemMenuDAO y GestorInventario son obligatorios.");
        }
        this.itemMenuDAO = itemMenuDAO;
        this.inventario = inventario;
    }

    /** Oculta items sin stock y devuelve los disponibles (RQ01, RF13). */
    public List<ItemMenu> presentarMenu() {
        ocultarAgotados();
        return itemMenuDAO.consultarDisponibles();
    }

    /** Devuelve todos los items del menu (incluidos agotados, para administracion). */
    public List<ItemMenu> consultarTodos() {
        ocultarAgotados();
        return itemMenuDAO.consultar();
    }

    /** Configura/filtra el menu segun preferencias del usuario (RQ03). */
    public List<ItemMenu> presentarMenuFiltrado(boolean soloVegetarianos, Boolean soloBebidasFrias) {
        List<ItemMenu> disponibles = presentarMenu();
        List<ItemMenu> filtrados = new ArrayList<>();
        for (ItemMenu item : disponibles) {
            if (soloVegetarianos) {
                if (item instanceof Plato plato && plato.isEsVegetariano()) {
                    filtrados.add(item);
                }
                continue;
            }
            if (soloBebidasFrias != null) {
                if (item instanceof Bebida bebida && bebida.isEsFria() == soloBebidasFrias) {
                    filtrados.add(item);
                }
                continue;
            }
            filtrados.add(item);
        }
        return filtrados;
    }

    /** Agrega una adicion (Decorator) validando disponibilidad de ingredientes (RQ02, RQ07, RF13). */
    public Producto agregarAdicion(Producto p, AdicionDecorator a) {
        if (p == null || a == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto base y una adicion valida.");
        }
        if (!inventario.hayStock(a)) {
            a.setDisponible(false);
            throw new IllegalStateException("La adicion '" + a.getNombre()
                    + "' esta agotada. Seleccione otra adicion o continue sin ella.");
        }
        return a.aplicarA(p);
    }

    /** Bloquea/oculta platos, bebidas y adiciones cuyos ingredientes no tengan stock (RF13). */
    public void ocultarAgotados() {
        for (ItemMenu item : itemMenuDAO.consultar()) {
            boolean hayStock = inventario.hayStock(item);
            if (item.isDisponible() != hayStock) {
                item.setDisponible(hayStock);
                itemMenuDAO.actualizar(item);
            }
        }
        for (AdicionDecorator adicion : catalogoAdiciones) {
            adicion.setDisponible(inventario.hayStock(adicion));
        }
    }

    // ---- Gestion de catalogo (Admin) ----

    public void registrarItem(ItemMenu item) {
        if (item == null) {
            throw new IllegalArgumentException("El item del menu es obligatorio.");
        }
        item.setDisponible(inventario.hayStock(item));
        itemMenuDAO.crear(item);
    }

    public void actualizarItem(ItemMenu item) {
        if (item == null) {
            throw new IllegalArgumentException("El item del menu es obligatorio.");
        }
        item.setDisponible(inventario.hayStock(item));
        itemMenuDAO.actualizar(item);
    }

    public void eliminarItem(ItemMenu item) {
        if (item == null) {
            throw new IllegalArgumentException("Debe seleccionar el item a eliminar.");
        }
        itemMenuDAO.eliminar(item);
    }

    public void registrarAdicion(AdicionDecorator plantilla) {
        if (plantilla == null) {
            throw new IllegalArgumentException("La adicion es obligatoria.");
        }
        catalogoAdiciones.removeIf(a -> a.getNombre().equalsIgnoreCase(plantilla.getNombre()));
        plantilla.setDisponible(inventario.hayStock(plantilla));
        catalogoAdiciones.add(plantilla);
    }

    public List<AdicionDecorator> getCatalogoAdiciones() {
        return Collections.unmodifiableList(catalogoAdiciones);
    }

    public List<AdicionDecorator> getAdicionesDisponibles() {
        ocultarAgotados();
        List<AdicionDecorator> disponibles = new ArrayList<>();
        for (AdicionDecorator a : catalogoAdiciones) {
            if (a.isDisponible()) {
                disponibles.add(a);
            }
        }
        return disponibles;
    }
}
