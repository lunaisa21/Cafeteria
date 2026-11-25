package com.datos;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


public class CierreCaja {
    private LocalDate fecha;
    private double totalVentas;
    private int numPedidos;          // pedidos entregados (ventas)
    private int numCancelados;
    private double totalEfectivo;
    private double totalTarjeta;
    private final Map<String, Integer> productosVendidos = new LinkedHashMap<>();

    public CierreCaja() {
    }

    public CierreCaja(LocalDate fecha, double totalVentas, int numPedidos, int numCancelados,
                      double totalEfectivo, double totalTarjeta) {
        this.fecha = fecha;
        this.totalVentas = totalVentas;
        this.numPedidos = numPedidos;
        this.numCancelados = numCancelados;
        this.totalEfectivo = totalEfectivo;
        this.totalTarjeta = totalTarjeta;
    }

    public void agregarProductoVendido(String descripcion, int cantidad) {
        productosVendidos.merge(descripcion, cantidad, Integer::sum);
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public double getTotalVentas() { return totalVentas; }
    public void setTotalVentas(double totalVentas) { this.totalVentas = totalVentas; }

    public int getNumPedidos() { return numPedidos; }
    public void setNumPedidos(int numPedidos) { this.numPedidos = numPedidos; }

    public int getNumCancelados() { return numCancelados; }
    public void setNumCancelados(int numCancelados) { this.numCancelados = numCancelados; }

    public double getTotalEfectivo() { return totalEfectivo; }
    public void setTotalEfectivo(double totalEfectivo) { this.totalEfectivo = totalEfectivo; }

    public double getTotalTarjeta() { return totalTarjeta; }
    public void setTotalTarjeta(double totalTarjeta) { this.totalTarjeta = totalTarjeta; }

    public Map<String, Integer> getProductosVendidos() { return Collections.unmodifiableMap(productosVendidos); }
}