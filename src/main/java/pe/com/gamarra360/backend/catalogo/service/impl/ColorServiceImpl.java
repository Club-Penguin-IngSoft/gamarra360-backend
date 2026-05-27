package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.catalogo.entity.Color;
import pe.com.gamarra360.backend.catalogo.repository.ColorRepository;
import pe.com.gamarra360.backend.catalogo.service.ColorService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ColorServiceImpl extends AbstractCrudService<Color, Integer> implements ColorService {

    public ColorServiceImpl(ColorRepository repository) {
        super(repository, "Color");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Color entidad, Integer id) {
        entidad.setIdColor(id);
    }
}
