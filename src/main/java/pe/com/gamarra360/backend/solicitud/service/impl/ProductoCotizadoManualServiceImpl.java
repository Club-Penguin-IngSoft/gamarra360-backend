package pe.com.gamarra360.backend.solicitud.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.entity.ProductoCotizadoManual;
import pe.com.gamarra360.backend.solicitud.repository.ProductoCotizadoManualRepository;
import pe.com.gamarra360.backend.solicitud.service.ProductoCotizadoManualService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProductoCotizadoManualServiceImpl extends AbstractCrudService<ProductoCotizadoManual, Long> implements ProductoCotizadoManualService {

    public ProductoCotizadoManualServiceImpl(ProductoCotizadoManualRepository repository) {
        super(repository, "ProductoCotizadoManual");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(ProductoCotizadoManual entidad, Long id) {
        entidad.setId(id);
    }
}
