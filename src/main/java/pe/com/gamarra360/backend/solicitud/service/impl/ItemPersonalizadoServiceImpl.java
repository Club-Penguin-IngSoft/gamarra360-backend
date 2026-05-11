package pe.com.gamarra360.backend.solicitud.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.entity.ItemPersonalizado;
import pe.com.gamarra360.backend.solicitud.repository.ItemPersonalizadoRepository;
import pe.com.gamarra360.backend.solicitud.service.ItemPersonalizadoService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ItemPersonalizadoServiceImpl extends AbstractCrudService<ItemPersonalizado, Long> implements ItemPersonalizadoService {

    public ItemPersonalizadoServiceImpl(ItemPersonalizadoRepository repository) {
        super(repository, "ItemPersonalizado");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(ItemPersonalizado entidad, Long id) {
        entidad.setId(id);
    }
}
