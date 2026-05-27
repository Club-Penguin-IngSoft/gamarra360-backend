package pe.com.gamarra360.backend.catalogo.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TiendaService {

    private final TiendaRepository repository;

    public TiendaService(TiendaRepository repository) {
        this.repository = repository;
    }

    public List<Tienda> listar() {
        log.info("Listando Tienda");
        return repository.findAll();
    }

    public Tienda obtener(Integer id) {
        log.info("Obteniendo Tienda con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tienda no encontrado con id " + id));
    }

    public Tienda crear(Tienda entidad) {
        log.info("Creando Tienda");
        return repository.save(entidad);
    }

    public Tienda actualizar(Integer id, Tienda entidad) {
        log.info("Actualizando Tienda con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Tienda no encontrado con id " + id);
        }
        entidad.setIdTienda(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando Tienda con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Tienda no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
