package pe.com.gamarra360.backend.solicitud.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CotizacionDetalleResponse {
    private Long id;
    private String estado;
    private String fechaCreacion;
    private Integer clienteId;
    private String nombreCliente;
    private Integer vendedorId;
    private String nombreTienda;
    private String fotoTienda;
    private List<ProductoDetalleInfo> productos;
    private RespuestaInfo respuesta;
    private Double precioDeseado;
    private Long pedidoId;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductoDetalleInfo {
        private Integer id;
        private String tipo;
        private String nombre;
        private String imagenUrl;
        private Double precio;
        private String especificacion;
        private Integer cantidad;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RespuestaInfo {
        private Long idRespuesta;
        private Double precioPropuesto;
        private String comentario;
        private String condiciones;
        private String anotaciones;
        private String imagen;
        private String fecha;
    }
}
