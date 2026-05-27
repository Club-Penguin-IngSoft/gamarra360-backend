package pe.com.gamarra360.backend.usuario.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;
import pe.com.gamarra360.backend.usuario.service.ComercianteService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
public class ComercianteServiceImpl extends AbstractCrudService<Comerciante, Integer> implements ComercianteService {

    private final ComercianteRepository comercianteRepository;

    public ComercianteServiceImpl(ComercianteRepository repository) {
        super(repository, "Comerciante");
        this.comercianteRepository = repository;
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Comerciante entidad, Integer id) {
        entidad.setUsuarioId(id);
    }

    @Override
    public List<Comerciante> listarPendientes() {
        log.info("Listando comerciantes pendientes de aprobacion");
        return comercianteRepository.findByVerificadoFalse();
    }

    @Override
    @Transactional
    public Comerciante aprobar(Integer id) {
        log.info("Aprobando comerciante con id {}", id);
        Comerciante comerciante = obtener(id);
        comerciante.setVerificado(true);
        comerciante.setActivo(true);
        return comercianteRepository.save(comerciante);
    }

    @Override
    @Transactional
    public void rechazar(Integer id) {
        log.info("Rechazando/eliminando comerciante con id {}", id);
        eliminar(id);
    }
}
