package pe.com.gamarra360.backend.service;

import java.util.List;

public interface CrudService<T, ID> {
    List<T> listar();
    T obtener(ID id);
    T crear(T entidad);
    T actualizar(ID id, T entidad);
    void eliminar(ID id);
}
