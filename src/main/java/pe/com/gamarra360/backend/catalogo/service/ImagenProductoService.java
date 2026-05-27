package pe.com.gamarra360.backend.catalogo.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.catalogo.entity.ImagenProducto;
import pe.com.gamarra360.backend.catalogo.repository.ImagenProductoRepository;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ImagenProductoService {

    private final ImagenProductoRepository repository;

    public ImagenProductoService(ImagenProductoRepository repository) {
        this.repository = repository;
    }

    public List<ImagenProducto> listar() {
        log.info("Listando ImagenProducto");
        return repository.findAll();
    }

    public ImagenProducto obtener(Integer id) {
        log.info("Obteniendo ImagenProducto con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("ImagenProducto no encontrado con id " + id));
    }

    public ImagenProducto crear(ImagenProducto entidad) {
        log.info("Creando ImagenProducto");
        return repository.save(entidad);
    }

    public ImagenProducto actualizar(Integer id, ImagenProducto entidad) {
        log.info("Actualizando ImagenProducto con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("ImagenProducto no encontrado con id " + id);
        }
        entidad.setIdImagen(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando ImagenProducto con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("ImagenProducto no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
