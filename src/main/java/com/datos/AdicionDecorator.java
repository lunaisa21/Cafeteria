private String nombre;
private double costoExtra;
private boolean disponible = true;
private final Map<Ingrediente, Double> receta = new LinkedHashMap<>();
/** Constructor completo: decora un producto existente. */
public AdicionDecorator(Producto producto, String nombre, double costoExtra) {
    if (nombre == null || nombre.isBlank()) {
        throw new IllegalArgumentException("El nombre de la adicion es obligatorio");
    }
    if (costoExtra < 0) {
        throw new IllegalArgumentException("El costo extra no puede ser negativo");
    }
    this.producto = producto;
    this.nombre = nombre;
    this.costoExtra = costoExtra;
}
/** Constructor plantilla: sin producto (se usa como catalogo de adiciones). */
public AdicionDecorator(String nombre, double costoExtra) {
    this(null, nombre, costoExtra);
}
@Override
public String getDescripcion() {
    if (producto == null) return nombre;
    return producto.getDescripcion() + " + " + nombre;
}
@Override
public double getPrecio() {
    if (producto == null) return costoExtra;
    return producto.getPrecio() + costoExtra;
}
/** Agrega un ingrediente necesario para esta adicion (RF11, RF13). */
public void agregarIngrediente(Ingrediente ingrediente, double cantidad) {
    if (ingrediente == null || cantidad <= 0) {
        throw new IllegalArgumentException("El ingrediente y la cantidad (>0) son obligatorios");
    }
    receta.put(ingrediente, cantidad);
}
/** Crea una nueva adicion aplicada al producto dado (factory para aplicar plantilla). */
public AdicionDecorator aplicarA(Producto p) {
    AdicionDecorator copia = new AdicionDecorator(p, this.nombre, this.costoExtra);
    copia.setDisponible(this.disponible);
    copia.getReceta().putAll(this.receta);
    return copia;
}
/** Extrae el producto base sin adiciones. */
public static Producto productoBase(Producto p) {
    while (p instanceof AdicionDecorator a) {
        p = a.getProducto();
    }
    return p;
}
}
