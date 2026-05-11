package pe.com.gamarra360.backend.usuario.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.usuario.entity.Admin;
import pe.com.gamarra360.backend.usuario.repository.AdminRepository;
import pe.com.gamarra360.backend.usuario.service.AdminService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminServiceImpl extends AbstractCrudService<Admin, Integer> implements AdminService {

    public AdminServiceImpl(AdminRepository repository) {
        super(repository, "Admin");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Admin entidad, Integer id) {
        entidad.setUsuarioId(id);
    }
}
