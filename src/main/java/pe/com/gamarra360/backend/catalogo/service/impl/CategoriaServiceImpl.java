package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.catalogo.entity.Categoria;
import pe.com.gamarra360.backend.catalogo.repository.CategoriaRepository;
import pe.com.gamarra360.backend.catalogo.service.CategoriaService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CategoriaServiceImpl extends AbstractCrudService<Categoria, Integer> implements CategoriaService {

    public CategoriaServiceImpl(CategoriaRepository repository) {
        super(repository, "Categoria");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Categoria entidad, Integer id) {
        entidad.setIdCategoria(id);
    }
}
