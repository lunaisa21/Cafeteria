package Operaciones;

import java.util.List;
import java.util.Optional;


public interface GenericDAO<T> {


    void guardar(T objeto);


    void actualizar(T objeto);

    Optional<T> buscarPorId(String id);

    List<T> listarTodos();


    boolean eliminar(String id);
}