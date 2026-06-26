package pe.com.gamarra360.backend.pedido.service;

import pe.com.gamarra360.backend.pedido.dto.DashboardResumenDTO;
import pe.com.gamarra360.backend.pedido.dto.PedidoComercianteDetalle;
import pe.com.gamarra360.backend.pedido.dto.PedidoComercianteResumen;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.service.CrudService;

import java.time.LocalDate;
import java.util.List;

public interface PedidoService extends CrudService<Pedido, Long> {
    void cancelar(Long id, Integer clienteId);

    List<PedidoComercianteResumen> listarPorVendedor(Integer vendedorId);

    PedidoComercianteDetalle obtenerDetalleComerciante(Long id, Integer vendedorId);

    Pedido avanzarEstado(Long id, Integer vendedorId);
    /** Resumen del dashboard del comerciante filtrado por rango de fechas. */
    DashboardResumenDTO obtenerDashboard(Integer vendedorId, LocalDate desde, LocalDate hasta);
}
