package pe.com.gamarra360.backend.solicitud.service;

import pe.com.gamarra360.backend.service.CrudService;
import pe.com.gamarra360.backend.solicitud.dto.CotizacionDetalleResponse;
import pe.com.gamarra360.backend.solicitud.dto.CotizacionRequest;
import pe.com.gamarra360.backend.solicitud.dto.CotizacionResumen;
import pe.com.gamarra360.backend.solicitud.dto.ContraPropuestaRequest;
import pe.com.gamarra360.backend.solicitud.dto.RespuestaCotizacionRequest;
import pe.com.gamarra360.backend.solicitud.entity.Cotizacion;

import java.util.List;

public interface CotizacionService extends CrudService<Cotizacion, Long> {

    CotizacionDetalleResponse crearSolicitud(CotizacionRequest request, Integer clienteId);

    List<CotizacionResumen> listarPorCliente(Integer clienteId);

    List<CotizacionResumen> listarPorVendedor(Integer vendedorId);

    CotizacionDetalleResponse obtenerDetalle(Long id);

    CotizacionDetalleResponse aceptar(Long id, Integer clienteId);

    void rechazar(Long id, Integer clienteId);

    CotizacionDetalleResponse responder(Long id, RespuestaCotizacionRequest request, Integer vendedorId);

    CotizacionDetalleResponse contraProponerCotizacion(Long id, ContraPropuestaRequest request, Integer clienteId);

    void cancelarPorVendedor(Long id, Integer vendedorId);
}
