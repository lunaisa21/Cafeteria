package Operaciones;
import com.datos.AdicionDecorator;
import com.datos.ItemMenu;
import com.datos.Producto;

import java.util.ArrayList;
import java.util.List;


import java.util.Collections;


public class GestorMenu<GestorInventario> {

    private final ItemMenuDAO itemMenuDAO;
    private final GestorInventario inventario;
    private final List<AdicionDecorator> catalogoAdiciones = new ArrayList<>();

    public GestorMenu(ItemMenuDAO itemMenuDAO, GestorInventario inventario) {
        this.itemMenuDAO = itemMenuDAO;
        this.inventario = inventario;
    }


    public List<ItemMenu> presentarMenu() {
        ocultarAgotados();
        return itemMenuDAO.consultarDisponibles();
    }

    public void ocultarAgotados() {
        for (ItemMenu item : itemMenuDAO.consultar()) {
            boolean hayStock = inventario.hayStock(item);
            if (item.isDisponible() != hayStock) {
                item.setDisponible(hayStock);
                itemMenuDAO.actualizar(item);
            }
        }
    }


    public Producto agregarAdicion(Producto p, AdicionDecorator a) {
        if (p == null || a == null) {
            throw new IllegalArgumentException("El producto y la adición son obligatorios");
        }
        return a.aplicarA(p);
    }


    public Producto agregarAdicion(Producto p, String nombreAdicion) {
        AdicionDecorator modelo = catalogoAdiciones.stream()
                .filter(a -> a.getNombre().equalsIgnoreCase(nombreAdicion))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "La adición '" + nombreAdicion + "' no existe en el catálogo"));
        return agregarAdicion(p, modelo);
    }

    public void registrarAdicion(AdicionDecorator plantilla) {
        if (plantilla == null) {
            throw new IllegalArgumentException("La adición es obligatoria");
        }
        catalogoAdiciones.removeIf(a -> a.getNombre().equalsIgnoreCase(plantilla.getNombre()));
        catalogoAdiciones.add(plantilla);
    }

    public List<AdicionDecorator> getCatalogoAdiciones() {
        return Collections.unmodifiableList(catalogoAdiciones);
    }
}
