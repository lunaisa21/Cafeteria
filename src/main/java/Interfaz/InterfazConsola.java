package Interfaz;

import com.datos.*;
import Operaciones.FachadaCafeteria;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Interfaz de consola del sistema de cafeteria.
 * Cumple con todos los requerimientos funcionales y no funcionales mediante la Fachada.
 */
public class InterfazConsola {

    private final FachadaCafeteria fachada;
    private final Scanner sc = new Scanner(System.in);
    private final List<String> alertasPendientes = new ArrayList<>();
    private final List<Pedido> pedidosSesion = new ArrayList<>();

    public InterfazConsola(FachadaCafeteria fachada) {
        this.fachada = fachada;
        Consumer<String> receptor = msg -> alertasPendientes.add(msg);
        fachada.configurarReceptoresAlertas(receptor, receptor, receptor, receptor);
    }

    public void iniciar() {
        System.out.println("===========================================");
        System.out.println("   SISTEMA DE CAFETERIA - MODO CONSOLA");
        System.out.println("===========================================");

        while (true) {
            if (fachada.getUsuarioActual() == null) {
                if (!pantallaLogin()) {
                    System.out.println("Hasta luego.");
                    return;
                }
            }
            ejecutarMenuPrincipal();
        }
    }

    private boolean pantallaLogin() {
        System.out.println("\n--- INICIO DE SESION --- (escriba 'salir' en usuario para terminar)");
        System.out.print("Usuario: ");
        String nombre = sc.nextLine().trim();
        if (nombre.equalsIgnoreCase("salir")) {
            return false;
        }
        System.out.print("Clave: ");
        String clave = sc.nextLine().trim();
        try {
            Usuario u = fachada.iniciarSesion(nombre, clave);
            System.out.println("Bienvenido, " + u.getNombre() + " [" + u.getRol() + "]");
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
        return true;
    }

    private void ejecutarMenuPrincipal() {
        mostrarAlertasPendientes();
        Usuario actual = fachada.getUsuarioActual();
        boolean esAdmin = actual.getRol().puedeAdministrar();

        System.out.println("\n=== MENU PRINCIPAL (" + actual.getNombre() + " - " + actual.getRol() + ") ===");
        System.out.println("1. Ver menu disponible (con filtros)");
        System.out.println("2. Crear y confirmar nuevo pedido (con adiciones)");
        System.out.println("3. Gestionar estado / cancelar / cobrar pedido");
        System.out.println("4. Realizar cierre de caja del dia");
        if (esAdmin) {
            System.out.println("5. [ADMIN] Inventario de ingredientes");
            System.out.println("6. [ADMIN] Gestion de promociones");
            System.out.println("7. [ADMIN] Gestion del menu");
        }
        System.out.println("8. Confirmar / revisar alertas pendientes (" + alertasPendientes.size() + ")");
        System.out.println("9. Cerrar sesion");
        System.out.print("Seleccione una opcion: ");

        String op = sc.nextLine().trim();
        try {
            switch (op) {
                case "1" -> verMenuConFiltros();
                case "2" -> flujoCrearPedido();
                case "3" -> flujoGestionPedidos();
                case "4" -> flujoCierreCaja();
                case "5" -> { if (esAdmin) flujoInventario(); else mostrarError("Opcion exclusiva para ADMINISTRADOR."); }
                case "6" -> { if (esAdmin) flujoPromociones(); else mostrarError("Opcion exclusiva para ADMINISTRADOR."); }
                case "7" -> { if (esAdmin) flujoAdminMenu(); else mostrarError("Opcion exclusiva para ADMINISTRADOR."); }
                case "8" -> confirmarAlertas();
                case "9" -> fachada.cerrarSesion();
                default -> mostrarError("Opcion invalida. Ingrese el numero de una opcion del menu.");
            }
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    /** RQ05-NF: Las alertas permanecen visibles hasta que el usuario las confirme. */
    private void mostrarAlertasPendientes() {
        if (alertasPendientes.isEmpty()) return;
        System.out.println("\n*************************************************");
        System.out.println("  ALERTAS PENDIENTES DE CONFIRMACION (" + alertasPendientes.size() + "):");
        for (int i = 0; i < alertasPendientes.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + alertasPendientes.get(i));
        }
        System.out.println("  (Use la opcion 8 para marcarlas como vistas)");
        System.out.println("*************************************************");
    }

    private void confirmarAlertas() {
        if (alertasPendientes.isEmpty()) {
            System.out.println("No hay alertas pendientes.");
            return;
        }
        mostrarAlertasPendientes();
        if (pedirConfirmacion("Desea marcar todas estas alertas como confirmadas/vistas?")) {
            alertasPendientes.clear();
            System.out.println("Alertas confirmadas.");
        }
    }

    private void verMenuConFiltros() {
        System.out.println("\n--- FILTRAR MENU (RQ01, RQ03) ---");
        System.out.println("1. Todo el menu disponible");
        System.out.println("2. Solo platos vegetarianos");
        System.out.println("3. Solo bebidas frias");
        System.out.println("4. Solo bebidas calientes");
        System.out.print("Elija filtro: ");
        String f = sc.nextLine().trim();

        List<ItemMenu> items = switch (f) {
            case "2" -> fachada.presentarMenuFiltrado(true, null);
            case "3" -> fachada.presentarMenuFiltrado(false, true);
            case "4" -> fachada.presentarMenuFiltrado(false, false);
            default -> fachada.presentarMenu();
        };

        if (items.isEmpty()) {
            System.out.println("No hay productos disponibles para ese criterio.");
            return;
        }
        System.out.println("\nID | Nombre / Descripcion | Precio");
        System.out.println("--------------------------------------------");
        for (ItemMenu item : items) {
            System.out.printf("%2d | %-28s | $%.2f (%s)%n",
                    item.getIdItem(), item.getDescripcion(), item.getPrecio(), item.getNombre());
        }
    }

    private void flujoCrearPedido() {
        int mesa = leerEntero("Numero de mesa (>0): ");
        Pedido pedido = fachada.crearPedido(mesa);

        boolean agregando = true;
        while (agregando) {
            List<ItemMenu> disponibles = fachada.presentarMenu();
            if (disponibles.isEmpty()) {
                mostrarError("No hay productos disponibles en el menu en este momento.");
                return;
            }
            System.out.println("\n--- PRODUCTOS DISPONIBLES ---");
            for (int i = 0; i < disponibles.size(); i++) {
                ItemMenu it = disponibles.get(i);
                System.out.printf("%d. %s - $%.2f%n", i + 1, it.getDescripcion(), it.getPrecio());
            }
            int idx = leerEntero("Seleccione producto (0 para terminar de agregar): ");
            if (idx == 0) break;
            if (idx < 1 || idx > disponibles.size()) {
                mostrarError("Numero fuera de rango. Elija entre 1 y " + disponibles.size() + ".");
                continue;
            }

            Producto seleccionado = disponibles.get(idx - 1);
            seleccionado = aplicarAdicionesInteractivo(seleccionado);
            int cant = leerEntero("Cantidad: ");
            try {
                fachada.agregarProductoAPedido(pedido, seleccionado, cant);
                System.out.printf("Agregado: %dx %s -> Subtotal linea: $%.2f%n",
                        cant, seleccionado.getDescripcion(), seleccionado.getPrecio() * cant);
            } catch (Exception e) {
                mostrarError(e.getMessage());
            }

            System.out.print("Agregar otro producto? (s/n): ");
            agregando = sc.nextLine().trim().equalsIgnoreCase("s");
        }

        if (pedido.getLineas().isEmpty()) {
            System.out.println("Pedido vacio descartado.");
            return;
        }

        double sub = pedido.calcularSubtotal();
        double desc = fachada.calcularDescuentoEstimado(pedido);
        System.out.printf("\nResumen Pedido Mesa %d -> Subtotal: $%.2f | Descuento promo: $%.2f | Total: $%.2f%n",
                pedido.getMesa(), sub, desc, (sub - desc));

        if (pedirConfirmacion("Desea confirmar el pedido ahora (descuenta stock y envia a cocina)?")) {
            fachada.confirmarPedido(pedido);
            pedidosSesion.add(pedido);
            System.out.println("Pedido #" + pedido.getIdPedido() + " confirmado exitosamente.");
            mostrarAlertasPendientes();
        }
    }

    private Producto aplicarAdicionesInteractivo(Producto base) {
        Producto actual = base;
        while (true) {
            List<AdicionDecorator> adiciones = fachada.getAdicionesDisponibles();
            if (adiciones.isEmpty()) break;

            System.out.printf("Producto actual: %s | Precio unitario: $%.2f%n",
                    actual.getDescripcion(), actual.getPrecio());
            System.out.print("Desea agregar una adicion? (s/n): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("s")) break;

            for (int i = 0; i < adiciones.size(); i++) {
                AdicionDecorator a = adiciones.get(i);
                System.out.printf("%d. %s (+$%.2f)%n", i + 1, a.getNombre(), a.getCostoExtra());
            }
            int idx = leerEntero("Seleccione adicion: ");
            if (idx < 1 || idx > adiciones.size()) {
                mostrarError("Opcion de adicion invalida.");
                continue;
            }
            try {
                actual = fachada.agregarAdicion(actual, adiciones.get(idx - 1));
                System.out.printf("Actualizado -> %s | Nuevo precio: $%.2f%n",
                        actual.getDescripcion(), actual.getPrecio());
            } catch (Exception e) {
                mostrarError(e.getMessage());
            }
        }
        return actual;
    }

    private void flujoGestionPedidos() {
        List<Pedido> todos = fachada.consultarPedidos();
        if (todos.isEmpty()) {
            System.out.println("No hay pedidos registrados.");
            return;
        }
        System.out.println("\n--- PEDIDOS REGISTRADOS ---");
        for (int i = 0; i < todos.size(); i++) {
            Pedido p = todos.get(i);
            System.out.printf("%d. Pedido #%d | Mesa %d | Estado: %s | Pago: %s | Total: $%.2f%n",
                    i + 1, p.getIdPedido(), p.getMesa(), p.getEstado(),
                    p.getMetodoPago() != null ? p.getMetodoPago() : "Pendiente", p.getTotal());
        }
        int idx = leerEntero("Seleccione pedido (0 para volver): ");
        if (idx == 0) return;
        if (idx < 1 || idx > todos.size()) {
            mostrarError("Pedido invalido.");
            return;
        }
        Pedido p = todos.get(idx - 1);

        System.out.println("1. Pasar a EN_PREPARACION");
        System.out.println("2. Pasar a LISTO");
        System.out.println("3. Pasar a ENTREGADO");
        System.out.println("4. Registrar metodo de pago (EFECTIVO / TARJETA)");
        System.out.println("5. Cancelar pedido");
        System.out.print("Accion: ");
        String act = sc.nextLine().trim();
        switch (act) {
            case "1" -> fachada.actualizarEstadoPedido(p, EstadoPedido.EN_PREPARACION);
            case "2" -> fachada.actualizarEstadoPedido(p, EstadoPedido.LISTO);
            case "3" -> fachada.actualizarEstadoPedido(p, EstadoPedido.ENTREGADO);
            case "4" -> {
                System.out.print("Metodo (1=EFECTIVO, 2=TARJETA): ");
                String mp = sc.nextLine().trim();
                fachada.asignarMetodoPago(p, mp.equals("2") ? MetodoPago.TARJETA : MetodoPago.EFECTIVO);
                System.out.println("Metodo de pago registrado.");
            }
            case "5" -> {
                if (pedirConfirmacion("Seguro que desea CANCELAR el pedido #" + p.getIdPedido() + "?")) {
                    fachada.cancelarPedido(p);
                    System.out.println("Pedido cancelado.");
                }
            }
            default -> mostrarError("Accion no reconocida.");
        }
        mostrarAlertasPendientes();
    }

    private void flujoCierreCaja() {
        if (!pedirConfirmacion("Desea efectuar el cierre de caja del dia de hoy?")) {
            return;
        }
        CierreCaja cierre = fachada.cerrarCaja();
        System.out.println("\n" + cierre.generarReporte());
    }

    private void flujoInventario() {
        System.out.println("\n--- INVENTARIO EN TIEMPO REAL (RF12) ---");
        List<Ingrediente> lista = fachada.consultarInventario();
        for (Ingrediente i : lista) {
            String marca = i.estaBajoMinimo() ? " [BAJO MINIMO!]" : "";
            System.out.printf("ID %d | %-25s | Stock: %.2f | Min: %.2f%s%n",
                    i.getIdIngrediente(), i.getNombre(), i.getStockActual(), i.getStockMinimo(), marca);
        }
        System.out.println("\n1. Actualizar stock de un ingrediente");
        System.out.println("2. Registrar nuevo ingrediente");
        System.out.println("0. Volver");
        System.out.print("Opcion: ");
        String op = sc.nextLine().trim();
        if (op.equals("1")) {
            int id = leerEntero("ID del ingrediente: ");
            Ingrediente encontrado = lista.stream()
                    .filter(x -> x.getIdIngrediente() == id)
                    .findFirst()
                    .orElse(null);
            if (encontrado == null) {
                mostrarError("No existe un ingrediente con ID " + id + ".");
                return;
            }
            double nuevo = leerDouble("Nuevo stock actual: ");
            fachada.actualizarStock(encontrado, nuevo);
            System.out.println("Stock actualizado.");
        } else if (op.equals("2")) {
            int id = leerEntero("ID nuevo: ");
            System.out.print("Nombre: ");
            String nom = sc.nextLine().trim();
            double act = leerDouble("Stock inicial: ");
            double min = leerDouble("Stock minimo: ");
            fachada.registrarIngrediente(new Ingrediente(id, nom, act, min));
            System.out.println("Ingrediente registrado.");
        }
    }

    private void flujoPromociones() {
        System.out.println("\n--- GESTION DE PROMOCIONES (RQ04) ---");
        List<Promocion> promos = fachada.consultarPromociones();
        for (Promocion p : promos) {
            System.out.printf("ID %d | %s | %.1f%% | Dias: %s | Franja: %s-%s | Activa: %s%n",
                    p.getIdPromocion(), p.getNombre(), p.getPorcentaje(),
                    p.getDiasSemana(), p.getHoraInicio(), p.getHoraFin(), p.isActiva());
        }
        System.out.println("\n1. Crear promocion\n2. Activar/Desactivar promocion\n3. Eliminar promocion\n0. Volver");
        System.out.print("Opcion: ");
        String op = sc.nextLine().trim();
        switch (op) {
            case "1" -> {
                int id = leerEntero("ID promocion: ");
                System.out.print("Nombre: ");
                String nom = sc.nextLine().trim();
                System.out.print("Dias (ej: MONDAY,FRIDAY o vacio): ");
                List<DayOfWeek> dias = parsearDias(sc.nextLine().trim());
                System.out.print("Hora inicio (HH:MM o vacio): ");
                String hi = sc.nextLine().trim();
                LocalTime tIni = hi.isEmpty() ? null : parsearHora(hi);
                LocalTime tFin = null;
                if (tIni != null) {
                    System.out.print("Hora fin (HH:MM): ");
                    tFin = parsearHora(sc.nextLine().trim());
                }
                double pct = leerDouble("Porcentaje descuento (0-100): ");
                fachada.crearPromocion(new Promocion(id, nom, dias, tIni, tFin, pct));
                System.out.println("Promocion creada.");
            }
            case "2" -> {
                int id = leerEntero("ID de la promocion a cambiar estado: ");
                Promocion target = promos.stream().filter(x -> x.getIdPromocion() == id).findFirst().orElse(null);
                if (target == null) {
                    mostrarError("Promocion no encontrada.");
                    return;
                }
                target.setActiva(!target.isActiva());
                fachada.modificarPromocion(target);
                System.out.println("Promocion actualizada (activa=" + target.isActiva() + ").");
            }
            case "3" -> {
                int id = leerEntero("ID de la promocion a eliminar: ");
                Promocion target = promos.stream().filter(x -> x.getIdPromocion() == id).findFirst().orElse(null);
                if (target == null) {
                    mostrarError("Promocion no encontrada.");
                    return;
                }
                if (pedirConfirmacion("Eliminar permanentemente la promocion '" + target.getNombre() + "'?")) {
                    fachada.eliminarPromocion(target);
                    System.out.println("Promocion eliminada.");
                }
            }
            default -> {}
        }
    }

    private void flujoAdminMenu() {
        System.out.println("\n--- ADMINISTRACION DEL MENU ---");
        List<ItemMenu> items = fachada.consultarTodosItemsMenu();
        for (ItemMenu it : items) {
            System.out.printf("ID %d | %s | $%.2f | Disponible: %s%n",
                    it.getIdItem(), it.getDescripcion(), it.getPrecio(), it.isDisponible());
        }
        System.out.println("1. Eliminar item del menu\n0. Volver");
        System.out.print("Opcion: ");
        if (sc.nextLine().trim().equals("1")) {
            int id = leerEntero("ID del item a eliminar: ");
            ItemMenu target = items.stream().filter(x -> x.getIdItem() == id).findFirst().orElse(null);
            if (target == null) {
                mostrarError("Item no encontrado.");
                return;
            }
            if (pedirConfirmacion("Eliminar item '" + target.getNombre() + "'?")) {
                fachada.eliminarItemMenu(target);
                System.out.println("Item eliminado.");
            }
        }
    }

    // ---- Metodos del diagrama UML ----

    public boolean pedirConfirmacion(String msg) {
        System.out.print("\n[CONFIRMACION] " + msg + " (s/n): ");
        String resp = sc.nextLine().trim();
        return resp.equalsIgnoreCase("s") || resp.equalsIgnoreCase("si");
    }

    public void mostrarError(String msg) {
        System.err.println("\n[ERROR] " + (msg != null ? msg : "Ocurrio un error inesperado. Intente nuevamente."));
    }

    // ---- Helpers de entrada ----

    private int leerEntero(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                mostrarError("Debe ingresar un numero entero valido (ej: 1, 2, 3).");
            }
        }
    }

    private double leerDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                mostrarError("Debe ingresar un numero decimal valido (ej: 15.5).");
            }
        }
    }

    private List<DayOfWeek> parsearDias(String entrada) {
        List<DayOfWeek> dias = new ArrayList<>();
        if (entrada.isEmpty()) return dias;
        for (String parte : entrada.split(",")) {
            dias.add(DayOfWeek.valueOf(parte.trim().toUpperCase()));
        }
        return dias;
    }

    private LocalTime parsearHora(String texto) {
        try {
            return LocalTime.parse(texto);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de hora invalido. Use HH:MM (ej: 14:30).");
        }
    }
}
