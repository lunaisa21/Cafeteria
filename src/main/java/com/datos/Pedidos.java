package com.datos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class Pedidos {
    private int mesa;
    private LocalDateTime fecha;
    private EstadoPedido estado;
    private MetodoPago metodoPago;
    private List<LineaPedido> lineas;
    private double total;
}
