package Operaciones;

import com.datos.AdicionDecorator;
import com.datos.ItemMenu;
import com.datos.ItemMenuDAO;

import java.util.ArrayList;
import java.util.List;

public class GestorMenu {

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
            boolean hayStock = inventario.hayStock(item, 1);
            if (item.isDisponible() != hayStock) {
                item.setDisponible(hayStock);
                itemMenuDAO.actualizar(item);
            }
        }
    }
}