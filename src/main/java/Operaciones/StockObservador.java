package Operaciones;

import com.datos.Ingrediente;

/**
 * Observer para alertas de stock minimo (RQ05-NF).
 */
public interface StockObservador {
    void alertaStockMinimo(Ingrediente i);
}
