package Operaciones;
import com.datos.AdicionDecorador;
import com.datos.ItemMenu;
import com.datos.Producto;

import java.util.ArrayList;
import java.util.List;

public class GestorMenu {
    private List<ItemMenu> items = new ArrayList<>();

    public List<ItemMenu> presentarMenu() {
        List<ItemMenu> disponibles = new ArrayList<>();
        for (ItemMenu item : items) {
            if (item.isDisponible()) {
                disponibles.add(item);
            }
        }
        return disponibles;
    }
    public Producto agregarAdicion(Producto p , AdicionDecorador a){
        a.setProducto(p);
        return a;
    }
    public void ocultarAgotadas(){
        for (ItemMenu item : items) {
            if(item.isDisponible()){
                item.setDisponible(false);
            }
        }
    }
}
