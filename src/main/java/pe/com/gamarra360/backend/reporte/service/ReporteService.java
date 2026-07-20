package pe.com.gamarra360.backend.reporte.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.catalogo.repository.ProductoRepository;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
import pe.com.gamarra360.backend.catalogo.repository.VarianteProductoRepository;
import pe.com.gamarra360.backend.configuracion.repository.ParametroSistemaRepository;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.pedido.repository.PedidoRepository;
import pe.com.gamarra360.backend.reporte.dto.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService {
    private final PedidoRepository pedidoRepository;
    private final TiendaRepository tiendaRepository;
    private final ProductoRepository productoRepository;
    private final VarianteProductoRepository varianteRepository;
    private final ParametroSistemaRepository parametroSistemaRepository;

    @Transactional(readOnly = true)
    public ReporteVentasResponse ventas(Integer vendedorId, LocalDate desde, LocalDate hasta) {
        List<Pedido> pedidos = pedidoRepository.findByVendedorIdAndFechaBetweenOrderByFechaDesc(
                vendedorId, desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay().minusNanos(1));
        return ventasResponse(pedidos);
    }

    @Transactional(readOnly = true)
    public ReporteInventarioResponse inventario(Integer vendedorId) {
        Tienda tienda = tiendaRepository.findByIdComerciante(vendedorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tienda", vendedorId));
        List<ReporteInventarioResponse.InventarioFila> filas = new ArrayList<>();
        for (Producto p : productoRepository.findByIdTiendaAndActivoTrue(tienda.getIdTienda())) {
            for (VarianteProducto v : varianteRepository.findByIdProducto(p.getIdProducto())) {
                filas.add(new ReporteInventarioResponse.InventarioFila(
                        p.getIdProducto(), v.getIdVariante(), p.getNombre(), v.getSku(),
                        v.getTalla() != null ? v.getTalla().getTalla() : null,
                        v.getColor() != null ? v.getColor().getNombre() : null,
                        v.getMaterial(), v.getCalidad(), v.getStock() != null ? v.getStock() : 0,
                        v.getMinimoStock() != null ? v.getMinimoStock() : 0, Boolean.TRUE.equals(v.getDisponible())));
            }
        }
        long unidades = filas.stream().mapToLong(ReporteInventarioResponse.InventarioFila::stock).sum();
        long bajos = filas.stream().filter(f -> f.stock() <= f.stockMinimo()).count();
        return new ReporteInventarioResponse(unidades, bajos, filas);
    }

    @Transactional(readOnly = true)
    public ReporteAdminResponse admin(LocalDate desde, LocalDate hasta) {
        List<Pedido> pedidos = pedidoRepository.findByFechaBetweenOrderByFechaDesc(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay().minusNanos(1));
        ReporteVentasResponse r = ventasResponse(pedidos);
        double tasa = parametroSistemaRepository.findById("COMISION_PLATAFORMA")
                .map(p -> { try { return Double.parseDouble(p.getValor()); } catch (NumberFormatException e) { return 0.10; } })
                .orElse(0.10);
        return new ReporteAdminResponse(r.totalVentas(), r.totalVentas() * tasa, r.devoluciones(), r.pedidos(), r.cancelados());
    }

    private ReporteVentasResponse ventasResponse(List<Pedido> pedidos) {
        List<ReporteVentasResponse.VentaFila> filas = pedidos.stream().map(p -> new ReporteVentasResponse.VentaFila(
                p.getId(), p.getFecha() != null ? p.getFecha().toString() : null,
                p.getEstado() != null ? p.getEstado().name() : null,
                p.getTipoEntrega() != null ? p.getTipoEntrega().name() : null,
                p.getTotal() != null ? p.getTotal() : 0.0)).toList();
        double devoluciones = pedidos.stream().filter(p -> p.getEstado() != null && p.getEstado().name().equals("CANCELADO")).mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0).sum();
        double ventas = pedidos.stream().filter(p -> p.getEstado() == null || !p.getEstado().name().equals("CANCELADO")).mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0).sum();
        long cancelados = pedidos.stream().filter(p -> p.getEstado() != null && p.getEstado().name().equals("CANCELADO")).count();
        return new ReporteVentasResponse(ventas, pedidos.size() - cancelados, cancelados, devoluciones, filas);
    }
}
