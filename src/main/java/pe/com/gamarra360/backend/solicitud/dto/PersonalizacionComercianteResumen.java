package pe.com.gamarra360.backend.solicitud.dto;

/**
 * Fila del "Tablero de Personalización" del comerciante. Una por cada
 * {@code Personalizacion} recibida por el vendedor autenticado.
 */
public record PersonalizacionComercianteResumen(
        Long id,
        String estado,
        String fechaCreacion,
        Integer clienteId,
        String nombreCliente,
        String emailCliente,
        String pedidoEstado
) {}
