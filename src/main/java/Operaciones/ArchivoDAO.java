package Operaciones;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


public class ArchivoDAO<T> implements GenericDAO<T> {
    private final Path archivo;
    private final Function<T, String> obtenerId;
    private final Function<T, String> aLinea;
    private final Function<String, T> desdeLinea;
    private final Map<String, T> datos = new LinkedHashMap<>();

    public ArchivoDAO(Path archivo, Function<T, String> obtenerId,
                      Function<T, String> aLinea, Function<String, T> desdeLinea) {
        this.archivo = archivo;
        this.obtenerId = obtenerId;
        this.aLinea = aLinea;
        this.desdeLinea = desdeLinea;
        cargar();
    }

    @Override
    public synchronized void guardar(T objeto) {
        datos.put(idDe(objeto), objeto);
        persistir();
    }

    @Override
    public synchronized void actualizar(T objeto) {
        String id = idDe(objeto);
        if (!datos.containsKey(id)) {
            throw new IllegalArgumentException("No existe el registro '" + id + "' para actualizar.");
        }
        datos.put(id, objeto);
        persistir();
    }

    @Override
    public synchronized Optional<T> buscarPorId(String id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public synchronized List<T> listarTodos() {
        return new ArrayList<>(datos.values());
    }

    @Override
    public synchronized boolean eliminar(String id) {
        boolean existia = datos.remove(id) != null;
        if (existia) persistir();
        return existia;
    }


    private String idDe(T objeto) {
        if (objeto == null) throw new IllegalArgumentException("No se puede guardar un objeto nulo.");
        return obtenerId.apply(objeto);
    }

    private void cargar() {
        if (!Files.exists(archivo)) return;
        try {
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                if (linea.isBlank()) continue;
                T objeto;
                try {
                    objeto = desdeLinea.apply(linea);
                } catch (RuntimeException e) {
                    throw new IllegalStateException("Linea corrupta en " + archivo + ": " + linea, e);
                }
                datos.put(obtenerId.apply(objeto), objeto);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo " + archivo, e);
        }
    }


    private void persistir() {
        try {
            Path carpeta = archivo.toAbsolutePath().getParent();
            if (carpeta != null) Files.createDirectories(carpeta);
            List<String> lineas = new ArrayList<>();
            for (T o : datos.values()) lineas.add(aLinea.apply(o));
            Path temporal = archivo.resolveSibling(archivo.getFileName() + ".tmp");
            Files.write(temporal, lineas, StandardCharsets.UTF_8);
            try {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo " + archivo, e);
        }
    }
}