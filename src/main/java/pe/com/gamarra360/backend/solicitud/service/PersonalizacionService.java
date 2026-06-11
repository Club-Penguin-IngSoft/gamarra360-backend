package pe.com.gamarra360.backend.solicitud.service;

import pe.com.gamarra360.backend.service.CrudService;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionDetalleResponse;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionRequest;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionResumen;
import pe.com.gamarra360.backend.solicitud.entity.Personalizacion;

import java.util.List;

public interface PersonalizacionService extends CrudService<Personalizacion, Long> {

    /**
     * Crea una solicitud de personalización a partir del DTO del frontend.
     * El {@code clienteId} se extrae del JWT en el controller — nunca del body.
     */
    Personalizacion crearSolicitud(PersonalizacionRequest request, Integer clienteId);

    /** Lista las personalizaciones del cliente autenticado, más recientes primero. */
    List<PersonalizacionResumen> listarPorCliente(Integer clienteId);

    /** Detalle completo de una personalización, validando que pertenezca al cliente. */
    PersonalizacionDetalleResponse obtenerDetalle(Long id, Integer clienteId);

    /** Acepta la propuesta del vendedor: pasa a ACEPTADA y crea el ItemPersonalizado. */
    PersonalizacionDetalleResponse aceptar(Long id, Integer clienteId);

    /** Rechaza la propuesta del vendedor: pasa a RECHAZADA. */
    void rechazar(Long id, Integer clienteId);
}
