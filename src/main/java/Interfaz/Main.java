package Interfaz;

import Interfaz.InterfazConsola;
import Interfaz.VentanaPrincipal;
import com.datos.*;
import Operaciones.*;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Punto de entrada de la aplicacion.
 * Soporta conexion PostgreSQL (Neon) o modo demostracion local en memoria,
 * y permite iniciar la interfaz grafica (Swing) o la interfaz de consola.
 */
public class Main {

    public static void main(String[] args) {
        FachadaCafeteria fachada = inicializarFachada();

        // Si se pasa argumento --consola, lanza modo consola directamente
        if (args.length > 0 && args[0].equalsIgnoreCase("--consola")) {
            new Interfaz.InterfazConsola(fachada).iniciar();
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("--gui")) {
            SwingUtilities.invokeLater(() -> new VentanaPrincipal(fachada).iniciar());
            return;
        }

        // Si hay entorno grafico disponible, muestra selector visual inmediato
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> {
                Object[] opciones = {"Interfaz Grafica (Swing)", "Interfaz de Consola"};
                int eleccion = JOptionPane.showOptionDialog(
                        null,
                        "Seleccione como desea iniciar el Sistema de Cafeteria:",
                        "Cafeteria - Inicio",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        opciones,
                        opciones[0]
                );
                if (eleccion == 1) {
                    new Thread(() -> new InterfazConsola(fachada).iniciar(), "consola-thread").start();
                } else if (eleccion == 0) {
                    new VentanaPrincipal(fachada).iniciar();
                }
            });
            return;
        }

        // Respaldo si el entorno es headless
        new InterfazConsola(fachada).iniciar();
    }

    private static FachadaCafeteria inicializarFachada() {
        if (configurarConexionDesdeEntornoOArchivo()) {
            try {
                FachadaCafeteria fachada = new FachadaCafeteria();
                cargarCatalogoAdicionesInicial(fachada);
                return fachada;
            } catch (RuntimeException e) {
                System.err.println("[AVISO] No se pudo conectar a PostgreSQL (" + e.getMessage()
                        + "). Iniciando en modo demo en memoria.");
            }
        } else {
            System.out.println("[INFO] No se detecto db.properties ni variables DB_URL. "
                    + "Iniciando en modo demo en memoria (admin/1234, mesero/1234).");
        }
        return crearFachadaEnMemoria();
    }

    private static boolean configurarConexionDesdeEntornoOArchivo() {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASSWORD");
        if (url != null && !url.isBlank() && user != null) {
            GestorConexion.getInstancia(url, user, pass != null ? pass : "");
            return true;
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            props.load(fis);
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            pass = props.getProperty("db.password", "");
            if (url != null && !url.isBlank() && user != null) {
                GestorConexion.getInstancia(url, user, pass);
                return true;
            }
        } catch (IOException ignored) {
            // No existe db.properties; se usara modo demo en memoria
        }
        return false;
    }

    private static void cargarCatalogoAdicionesInicial(FachadaCafeteria fachada) {
        try {
            AdicionDecorator shotCafe = new AdicionDecorator("Shot extra de espresso", 2000);
            AdicionDecorator lecheAlmendra = new AdicionDecorator("Leche de almendras", 2500);
            AdicionDecorator quesoExtra = new AdicionDecorator("Extra queso mozzarella", 3000);
            AdicionDecorator crema = new AdicionDecorator("Crema batida", 1800);

            fachada.registrarAdicionInicial(shotCafe);
            fachada.registrarAdicionInicial(lecheAlmendra);
            fachada.registrarAdicionInicial(quesoExtra);
            fachada.registrarAdicionInicial(crema);
        } catch (Exception ignored) {
        }
    }

    /** Construye una Fachada con DAOs en memoria precargados para pruebas/demos sin BD externa. */
    public static FachadaCafeteria crearFachadaEnMemoria() {
        List<Usuario> usuarios = new ArrayList<>(List.of(
                new Usuario(1, "admin", "1234", Rol.ADMINISTRADOR),
                new Usuario(2, "mesero", "1234", Rol.MESERO_CAJERO)
        ));

        Ingrediente cafe = new Ingrediente(1, "Cafe en grano (g)", 500, 100);
        Ingrediente leche = new Ingrediente(2, "Leche entera (ml)", 2000, 400);
        Ingrediente pan = new Ingrediente(3, "Pan artesanal (und)", 20, 5);
        Ingrediente queso = new Ingrediente(4, "Queso mozzarella (g)", 600, 150);
        Ingrediente jamon = new Ingrediente(5, "Jamon de pavo (g)", 400, 100);
        Ingrediente choco = new Ingrediente(6, "Chocolate en polvo (g)", 300, 60);
        List<Ingrediente> ingredientes = new ArrayList<>(List.of(cafe, leche, pan, queso, jamon, choco));

        Bebida espresso = new Bebida(1, "Espresso", "Cafe espresso concentrado", 4500, "Pequeno", false);
        espresso.agregarIngrediente(cafe, 18);

        Bebida capuccino = new Bebida(2, "Capuccino", "Espresso con leche vaporizada", 7500, "Mediano", false);
        capuccino.agregarIngrediente(cafe, 18);
        capuccino.agregarIngrediente(leche, 180);

        Bebida mocaccino = new Bebida(3, "Mocaccino Frio", "Cafe frio con chocolate", 9000, "Grande", true);
        mocaccino.agregarIngrediente(cafe, 18);
        mocaccino.agregarIngrediente(leche, 200);
        mocaccino.agregarIngrediente(choco, 20);

        Plato caprese = new Plato(4, "Sandwich Caprese", "Pan artesanal con mozzarella y tomate", 13500, 8, true);
        caprese.agregarIngrediente(pan, 1);
        caprese.agregarIngrediente(queso, 80);

        Plato jamonQueso = new Plato(5, "Sandwich Jamon y Queso", "Pan artesanal con jamon y queso", 14500, 10, false);
        jamonQueso.agregarIngrediente(pan, 1);
        jamonQueso.agregarIngrediente(queso, 60);
        jamonQueso.agregarIngrediente(jamon, 70);

        List<ItemMenu> items = new ArrayList<>(List.of(espresso, capuccino, mocaccino, caprese, jamonQueso));

        List<Promocion> promos = new ArrayList<>(List.of(
                new Promocion(1, "Happy Hour Tarde",
                        List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                        LocalTime.of(14, 0), LocalTime.of(18, 0), 15.0),
                new Promocion(2, "Fin de Semana Cafetero",
                        List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), null, null, 10.0)
        ));

        List<Pedido> pedidos = new ArrayList<>();
        List<CierreCaja> cierres = new ArrayList<>();

        ItemMenuDAO itemDAO = new ItemMenuDAO() {
            @Override public void crear(ItemMenu t) { items.add(t); }
            @Override public List<ItemMenu> consultar() { return new ArrayList<>(items); }
            @Override public void actualizar(ItemMenu t) {
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).getIdItem() == t.getIdItem()) { items.set(i, t); return; }
                }
            }
            @Override public void eliminar(ItemMenu t) { items.removeIf(x -> x.getIdItem() == t.getIdItem()); }
            @Override public List<ItemMenu> consultarDisponibles() {
                return items.stream().filter(ItemMenu::isDisponible).toList();
            }
        };

        IngredienteDAO ingDAO = new IngredienteDAO() {
            @Override public void crear(Ingrediente t) { ingredientes.add(t); }
            @Override public List<Ingrediente> consultar() { return new ArrayList<>(ingredientes); }
            @Override public void actualizar(Ingrediente t) {
                for (int i = 0; i < ingredientes.size(); i++) {
                    if (ingredientes.get(i).getIdIngrediente() == t.getIdIngrediente()) {
                        ingredientes.set(i, t); return;
                    }
                }
            }
            @Override public void eliminar(Ingrediente t) { ingredientes.removeIf(x -> x.getIdIngrediente() == t.getIdIngrediente()); }
            @Override public List<Ingrediente> consultarBajoMinimo() {
                return ingredientes.stream().filter(Ingrediente::estaBajoMinimo).toList();
            }
        };

        PedidoDAO pedDAO = new PedidoDAO() {
            private int seq = 1;
            @Override public void crear(Pedido t) { t.setIdPedido(seq++); pedidos.add(t); }
            @Override public List<Pedido> consultar() { return new ArrayList<>(pedidos); }
            @Override public void actualizar(Pedido t) {
                for (int i = 0; i < pedidos.size(); i++) {
                    if (pedidos.get(i).getIdPedido() == t.getIdPedido()) { pedidos.set(i, t); return; }
                }
            }
            @Override public void eliminar(Pedido t) { pedidos.removeIf(x -> x.getIdPedido() == t.getIdPedido()); }
            @Override public List<Pedido> consultarPorFecha(LocalDate f) {
                return pedidos.stream().filter(p -> p.getFecha() != null && p.getFecha().toLocalDate().equals(f)).toList();
            }
        };

        PromocionDAO promoDAO = new PromocionDAO() {
            @Override public void crear(Promocion t) { promos.add(t); }
            @Override public List<Promocion> consultar() { return new ArrayList<>(promos); }
            @Override public void actualizar(Promocion t) {
                for (int i = 0; i < promos.size(); i++) {
                    if (promos.get(i).getIdPromocion() == t.getIdPromocion()) { promos.set(i, t); return; }
                }
            }
            @Override public void eliminar(Promocion t) { promos.removeIf(x -> x.getIdPromocion() == t.getIdPromocion()); }
            @Override public List<Promocion> consultarActivas() {
                return promos.stream().filter(Promocion::isActiva).toList();
            }
        };

        UsuarioDAO userDAO = new UsuarioDAO() {
            @Override public void crear(Usuario t) { usuarios.add(t); }
            @Override public List<Usuario> consultar() { return new ArrayList<>(usuarios); }
            @Override public void actualizar(Usuario t) {
                for (int i = 0; i < usuarios.size(); i++) {
                    if (usuarios.get(i).getIdUsuario() == t.getIdUsuario()) { usuarios.set(i, t); return; }
                }
            }
            @Override public void eliminar(Usuario t) { usuarios.removeIf(x -> x.getIdUsuario() == t.getIdUsuario()); }
            @Override public Usuario buscarPorNombre(String n) {
                return usuarios.stream().filter(u -> u.getNombre().equals(n)).findFirst().orElse(null);
            }
        };

        CierreCajaDAO cajaDAO = new CierreCajaDAO() {
            @Override public void crear(CierreCaja t) { cierres.add(t); }
            @Override public List<CierreCaja> consultar() { return new ArrayList<>(cierres); }
            @Override public void actualizar(CierreCaja t) {
                for (int i = 0; i < cierres.size(); i++) {
                    if (cierres.get(i).getFecha().equals(t.getFecha())) { cierres.set(i, t); return; }
                }
            }
            @Override public void eliminar(CierreCaja t) { cierres.removeIf(x -> x.getFecha().equals(t.getFecha())); }
            @Override public CierreCaja consultarPorFecha(LocalDate f) {
                return cierres.stream().filter(c -> c.getFecha().equals(f)).findFirst().orElse(null);
            }
        };

        FachadaCafeteria fachada = new FachadaCafeteria(itemDAO, ingDAO, pedDAO, promoDAO, userDAO, cajaDAO);
        // Enlazar adiciones con receta real de ingredientes en modo demo
        AdicionDecorator shotCafe = new AdicionDecorator("Shot extra de espresso", 2000);
        shotCafe.agregarIngrediente(cafe, 10);
        AdicionDecorator quesoExtra = new AdicionDecorator("Extra queso mozzarella", 3000);
        quesoExtra.agregarIngrediente(queso, 40);
        fachada.registrarAdicionInicial(shotCafe);
        fachada.registrarAdicionInicial(quesoExtra);
        return fachada;
    }
}
