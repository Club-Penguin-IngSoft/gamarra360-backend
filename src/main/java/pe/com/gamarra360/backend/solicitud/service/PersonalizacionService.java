package pe.com.gamarra360.backend.solicitud.service;

import pe.com.gamarra360.backend.service.CrudService;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionRequest;
import pe.com.gamarra360.backend.solicitud.entity.Personalizacion;

public interface PersonalizacionService extends CrudService<Personalizacion, Long> {

    /**
     * Crea una solicitud de personalización a partir del DTO del frontend.
     * El {@code clienteId} se extrae del JWT en el controller — nunca del body.
     */
    Personalizacion crearSolicitud(PersonalizacionRequest request, Integer clienteId);
}
