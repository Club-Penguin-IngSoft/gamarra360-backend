package pe.com.gamarra360.backend.pago.dto;

import java.util.List;

/**
 * Representa un grupo de items de un mismo vendedor, antes de que el pago
 * sea confirmado. Se serializa a JSON y se guarda en CarritoPendiente.
 *
 * Equivalente en el frontend: IGrupoTienda (pedidoService.ts).
 */
public class GrupoTiendaDto {

    private Integer vendedorId;
    private String tipoEntrega; // "DELIVERY" | "RECOJO_TIENDA"
    private String direccionEntrega;
    private Double total;
    private List<ItemCarritoDto> items;

    public GrupoTiendaDto() {}

    public Integer getVendedorId() { return vendedorId; }
    public void setVendedorId(Integer vendedorId) { this.vendedorId = vendedorId; }

    public String getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(String tipoEntrega) { this.tipoEntrega = tipoEntrega; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public List<ItemCarritoDto> getItems() { return items; }
    public void setItems(List<ItemCarritoDto> items) { this.items = items; }

    public static class ItemCarritoDto {
        private Integer idVarianteProducto;
        private Integer cantidad;
        private Double precio;
        private Long personalizacionId;
        private Long cotizacionId;

        public ItemCarritoDto() {}

        public Integer getIdVarianteProducto() { return idVarianteProducto; }
        public void setIdVarianteProducto(Integer idVarianteProducto) { this.idVarianteProducto = idVarianteProducto; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

        public Double getPrecio() { return precio; }
        public void setPrecio(Double precio) { this.precio = precio; }

        public Long getPersonalizacionId() { return personalizacionId; }
        public void setPersonalizacionId(Long personalizacionId) { this.personalizacionId = personalizacionId; }

        public Long getCotizacionId() { return cotizacionId; }
        public void setCotizacionId(Long cotizacionId) { this.cotizacionId = cotizacionId; }
    }
}
