package pe.com.gamarra360.backend.usuario.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.usuario.entity.Admin;
import pe.com.gamarra360.backend.usuario.repository.AdminRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AdminService {

    private final AdminRepository repository;

    public AdminService(AdminRepository repository) {
        this.repository = repository;
    }

    public List<Admin> listar() {
        log.info("Listando Admin");
        return repository.findAll();
    }

    public Admin obtener(Integer id) {
        log.info("Obteniendo Admin con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Admin no encontrado con id " + id));
    }

    public Admin crear(Admin entidad) {
        log.info("Creando Admin");
        return repository.save(entidad);
    }

    public Admin actualizar(Integer id, Admin entidad) {
        log.info("Actualizando Admin con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Admin no encontrado con id " + id);
        }
        entidad.setUsuarioId(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando Admin con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Admin no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
