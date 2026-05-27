package pe.com.gamarra360.backend.usuario.service;

import pe.com.gamarra360.backend.service.CrudService;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import java.util.List;

public interface ComercianteService extends CrudService<Comerciante, Integer> {
    List<Comerciante> listarPendientes();
    Comerciante aprobar(Integer id);
    void rechazar(Integer id);
}
