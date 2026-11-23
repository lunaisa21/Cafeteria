package com.datos;

import java.time.LocalDateTime;
import java.util.List;

public class Pedidos {
    private int mesa;
    private LocalDateTime fecha;
    private EstadoPedido estado;
    private MetodoPago metodoPago;
    private List<LineaPedido> lineas;
    private double total;
}
