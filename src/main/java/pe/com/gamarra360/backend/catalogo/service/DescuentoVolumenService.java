package pe.com.gamarra360.backend.catalogo.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.catalogo.entity.DescuentoVolumen;
import pe.com.gamarra360.backend.catalogo.repository.DescuentoVolumenRepository;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DescuentoVolumenService {

    private final DescuentoVolumenRepository repository;

    public DescuentoVolumenService(DescuentoVolumenRepository repository) {
        this.repository = repository;
    }

    public List<DescuentoVolumen> listar() {
        log.info("Listando DescuentoVolumen");
        return repository.findAll();
    }

    public DescuentoVolumen obtener(Integer id) {
        log.info("Obteniendo DescuentoVolumen con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("DescuentoVolumen no encontrado con id " + id));
    }

    public DescuentoVolumen crear(DescuentoVolumen entidad) {
        log.info("Creando DescuentoVolumen");
        return repository.save(entidad);
    }

    public DescuentoVolumen actualizar(Integer id, DescuentoVolumen entidad) {
        log.info("Actualizando DescuentoVolumen con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("DescuentoVolumen no encontrado con id " + id);
        }
        entidad.setIdDescuento(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando DescuentoVolumen con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("DescuentoVolumen no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
