package pe.com.gamarra360.backend.solicitud.service;

import pe.com.gamarra360.backend.service.CrudService;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionComercianteDetalle;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionComercianteResumen;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionDetalleResponse;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionRequest;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionResumen;
import pe.com.gamarra360.backend.solicitud.dto.ContraPropuestaRequest;
import pe.com.gamarra360.backend.solicitud.dto.RespuestaPersonalizacionRequest;
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

    /** Lista las personalizaciones recibidas por el comerciante autenticado, más recientes primero. */
    List<PersonalizacionComercianteResumen> listarPorVendedor(Integer vendedorId);

    /** Detalle completo de una personalización para el comerciante, validando que le pertenezca. */
    PersonalizacionComercianteDetalle obtenerDetalleComerciante(Long id, Integer vendedorId);

    /** El comerciante acepta (cotiza) o rechaza una solicitud en estado PENDIENTE. */
    PersonalizacionComercianteDetalle responder(Long id, RespuestaPersonalizacionRequest request, Integer vendedorId);

    /** El cliente cancela su propia solicitud en estado PENDIENTE o RESPONDIDA. */
    void cancelarPorCliente(Long id, Integer clienteId);

    /** El comerciante cancela una solicitud que le pertenece, en estado PENDIENTE o RESPONDIDA. */
    void cancelarPorVendedor(Long id, Integer vendedorId);

    /** El cliente envía una contrapropuesta cuando la solicitud está en estado RESPONDIDA: vuelve a PENDIENTE. */
    PersonalizacionDetalleResponse contraProponerCliente(Long id, ContraPropuestaRequest request, Integer clienteId);
}
