package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.catalogo.repository.ProductoRepository;
import pe.com.gamarra360.backend.catalogo.service.ProductoService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProductoServiceImpl extends AbstractCrudService<Producto, Integer> implements ProductoService {

    public ProductoServiceImpl(ProductoRepository repository) {
        super(repository, "Producto");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Producto entidad, Integer id) {
        entidad.setIdProducto(id);
    }
}
