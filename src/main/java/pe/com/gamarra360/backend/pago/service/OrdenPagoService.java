package pe.com.gamarra360.backend.pago.service;

import pe.com.gamarra360.backend.service.CrudService;
import pe.com.gamarra360.backend.pago.entity.OrdenPago;
import pe.com.gamarra360.backend.pago.entity.OrdenPagoDetalleResponse;

import java.util.List;

public interface OrdenPagoService extends CrudService<OrdenPago, Long> {
    List<OrdenPago> listarPorCliente(Integer clienteId);
    OrdenPagoDetalleResponse obtenerDetalle(Long ordenPagoId);
    //void marcarComoPagado(Long ordenId);
}
