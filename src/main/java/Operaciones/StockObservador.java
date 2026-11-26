package operaciones;
import com.datos.Ingrediente;

public interface StockObservador {
    void alertaStockMinimo(Ingrediente i);
}