package Operaciones;

import java.util.List;
import java.util.Optional;

public interface GenericDAO<T> {

    void guardar(T objeto);

    List<T> consultar();

    void actualizar(T objeto);

    Optional<T> buscarPorId(String id);

    List<T> listarTodos();

    void eliminar(String id);

}