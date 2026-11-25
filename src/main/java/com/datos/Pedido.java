package com.datos;

import Operaciones.PedidoObservador;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class Pedido {
    private int idPedido;
    private int mesa;
    private LocalDateTime fecha;
    private EstadoPedido estado;
    private MetodoPago metodoPago;
    private List<LineaPedido> lineas;
    private double total;

    public void cambiarEstadoPedido(EstadoPedido e) {
    }
    public void agregarObservador(PedidoObservador o){

    }
    public void notificarObservadores(){
        
    }

    public void calcularSubtotal() {
        return;
    }
}
