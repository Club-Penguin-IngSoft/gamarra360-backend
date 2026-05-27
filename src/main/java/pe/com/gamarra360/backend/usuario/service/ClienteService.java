package pe.com.gamarra360.backend.usuario.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import pe.com.gamarra360.backend.usuario.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public List<Cliente> listar() {
        log.info("Listando Cliente");
        return repository.findAll();
    }

    public Cliente obtener(Integer id) {
        log.info("Obteniendo Cliente con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id " + id));
    }

    public Cliente crear(Cliente entidad) {
        log.info("Creando Cliente");
        return repository.save(entidad);
    }

    public Cliente actualizar(Integer id, Cliente entidad) {
        log.info("Actualizando Cliente con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Cliente no encontrado con id " + id);
        }
        entidad.setUsuarioId(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando Cliente con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Cliente no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
