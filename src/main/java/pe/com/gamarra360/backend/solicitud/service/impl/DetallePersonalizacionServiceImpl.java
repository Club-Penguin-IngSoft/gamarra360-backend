package pe.com.gamarra360.backend.solicitud.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.entity.DetallePersonalizacion;
import pe.com.gamarra360.backend.solicitud.repository.DetallePersonalizacionRepository;
import pe.com.gamarra360.backend.solicitud.service.DetallePersonalizacionService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DetallePersonalizacionServiceImpl extends AbstractCrudService<DetallePersonalizacion, Integer> implements DetallePersonalizacionService {

    public DetallePersonalizacionServiceImpl(DetallePersonalizacionRepository repository) {
        super(repository, "DetallePersonalizacion");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(DetallePersonalizacion entidad, Integer id) {
        entidad.setIdDetallePersonalizacion(id);
    }
}
