package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.catalogo.repository.VarianteProductoRepository;
import pe.com.gamarra360.backend.catalogo.service.VarianteProductoService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VarianteProductoServiceImpl extends AbstractCrudService<VarianteProducto, Integer> implements VarianteProductoService {

    public VarianteProductoServiceImpl(VarianteProductoRepository repository) {
        super(repository, "VarianteProducto");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(VarianteProducto entidad, Integer id) {
        entidad.setIdVariante(id);
    }
}
