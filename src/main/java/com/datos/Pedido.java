package com.datos;

import Operaciones.PedidoObservador;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Pedido {
    private int idPedido;
    private int mesa;
    private LocalDateTime fecha;
    private EstadoPedido estado;
    private MetodoPago metodoPago;
    private List<LineaPedido> lineas;
    private double subtotal;
    private double descuento;
    private double total;
    private boolean stockDescontado;

    @Getter(AccessLevel.NONE)
    private final List<PedidoObservador> observadores = new ArrayList<>();


    public Pedido(int mesa) {
        this.mesa = mesa;
        this.fecha = LocalDateTime.now();
        this.estado = EstadoPedido.PENDIENTE;
    }

    public void agregarLinea(LineaPedido l) {
        if (l == null) {
            throw new IllegalArgumentException("La línea del pedido no puede ser nula.");
        }
        if (l.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        if (estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden agregar productos a un pedido PENDIENTE.");
        }
        lineas.add(l);
    }

    public void cambiarEstado(EstadoPedido e) {
        this.estado = e;
        notificarObservadores();
    }

    public void agregarObservador(PedidoObservador o) {
        if (o != null && !observadores.contains(o)) {
            observadores.add(o);
        }
    }

    public void notificarObservadores() {
        // Copia de la lista, por si un observador se registra o se quita mientras se notifica
        for (PedidoObservador o : new ArrayList<>(observadores)) {
            o.actualizar(this);
        }
    }

    public double calcularSubtotal() {
        double suma = 0;
        for (LineaPedido l : lineas) {
            suma += l.getSubtotal();
        }
        return suma;
    }
}
