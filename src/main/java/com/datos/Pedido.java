this.mesa = mesa;
        this.fecha = LocalDateTime.now();
        this.estado = EstadoPedido.PENDIENTE;
    }
/** Agrega una linea al pedido. Solo en estado PENDIENTE. */
public void agregarLinea(LineaPedido l) {
    if (l == null) {
        throw new IllegalArgumentException("La linea del pedido no puede ser nula.");
    }
    if (l.getCantidad() <= 0) {
        throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
    }
    if (estado != EstadoPedido.PENDIENTE) {
        throw new IllegalStateException("Solo se pueden agregar productos a un pedido PENDIENTE.");
    }
    lineas.add(l);
}
/** Elimina una linea del pedido en estado PENDIENTE. */
public void eliminarLinea(int indice) {
    if (estado != EstadoPedido.PENDIENTE) {
        throw new IllegalStateException("Solo se pueden modificar productos en un pedido PENDIENTE.");
    }
    if (indice < 0 || indice >= lineas.size()) {
        throw new IndexOutOfBoundsException("Indice de linea invalido: " + indice);
    }
    lineas.remove(indice);
}
/** Calcula el subtotal sumando las lineas. */
public double calcularSubtotal() {
    double suma = 0;
    for (LineaPedido l : lineas) {
        suma += l.getSubtotal();
    }
    return suma;
}
/** Cambia el estado y notifica a los observadores (RQ08). */
public void cambiarEstado(EstadoPedido e) {
    this.estado = e;
    notificarObservadores();
}
/** Registra un observador para recibir notificaciones de cambio de estado. */
public void agregarObservador(PedidoObservador o) {
    if (o != null && !observadores.contains(o)) {
        observadores.add(o);
    }
}
/** Notifica a todos los observadores del cambio actual (RQ08). */
public void notificarObservadores() {
    for (PedidoObservador o : new ArrayList<>(observadores)) {
        o.actualizar(this);
    }
}
}