package Operaciones;

import com.datos.Ingrediente;

import java.util.function.Consumer;

/**
 * Implementacion concreta del StockObservador.
 * Emite alertas cuando el stock alcanza niveles minimos (RQ05-NF).
 */
public class AlertaInventario implements StockObservador {
    private Consumer<String> receptor;

    public void setReceptor(Consumer<String> receptor) {
        this.receptor = receptor;
    }

    @Override
    public void alertaStockMinimo(Ingrediente i) {
        String mensaje = "[ALERTA INVENTARIO] Stock minimo alcanzado: "
                + i.getNombre() + " (actual: " + i.getStockActual()
                + ", minimo: " + i.getStockMinimo() + ")";
        if (receptor != null) {
            receptor.accept(mensaje);
        } else {
            System.out.println(mensaje);
        }
    }
}
