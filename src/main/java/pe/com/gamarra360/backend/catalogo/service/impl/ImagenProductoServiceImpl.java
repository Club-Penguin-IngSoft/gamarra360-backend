package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.catalogo.entity.ImagenProducto;
import pe.com.gamarra360.backend.catalogo.repository.ImagenProductoRepository;
import pe.com.gamarra360.backend.catalogo.service.ImagenProductoService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ImagenProductoServiceImpl extends AbstractCrudService<ImagenProducto, Integer> implements ImagenProductoService {

    public ImagenProductoServiceImpl(ImagenProductoRepository repository) {
        super(repository, "ImagenProducto");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(ImagenProducto entidad, Integer id) {
        entidad.setIdImagen(id);
    }
}
