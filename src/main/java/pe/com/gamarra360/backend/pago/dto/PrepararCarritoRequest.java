package pe.com.gamarra360.backend.pago.dto;

import java.util.List;

/** Payload de POST /api/v1/pagos/preparar */
public class PrepararCarritoRequest {

    private Integer clienteId;
    private Double total;
    private List<GrupoTiendaDto> grupos;

    public PrepararCarritoRequest() {}

    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public List<GrupoTiendaDto> getGrupos() { return grupos; }
    public void setGrupos(List<GrupoTiendaDto> grupos) { this.grupos = grupos; }
}
