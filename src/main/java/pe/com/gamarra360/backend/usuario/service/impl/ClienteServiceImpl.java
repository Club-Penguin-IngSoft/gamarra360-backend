package pe.com.gamarra360.backend.usuario.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import pe.com.gamarra360.backend.usuario.repository.ClienteRepository;
import pe.com.gamarra360.backend.usuario.service.ClienteService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClienteServiceImpl extends AbstractCrudService<Cliente, Integer> implements ClienteService {

    public ClienteServiceImpl(ClienteRepository repository) {
        super(repository, "Cliente");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Cliente entidad, Integer id) {
        entidad.setUsuarioId(id);
    }
}
