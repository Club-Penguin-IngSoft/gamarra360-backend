package pe.com.gamarra360.backend.reclamo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.exception.ConflictoNegocioException;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.pedido.repository.PedidoRepository;
import pe.com.gamarra360.backend.reclamo.dto.ReclamoCrearRequest;
import pe.com.gamarra360.backend.reclamo.dto.ReclamoResponse;
import pe.com.gamarra360.backend.reclamo.entity.Reclamo;
import pe.com.gamarra360.backend.reclamo.repository.ReclamoRepository;
import pe.com.gamarra360.backend.usuario.entity.Usuario;
import pe.com.gamarra360.backend.usuario.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReclamoService {
    private final ReclamoRepository repository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ReclamoResponse crear(ReclamoCrearRequest request, Integer clienteId) {
        Reclamo r = new Reclamo();
        r.setClienteId(clienteId);
        r.setTipo(request.tipo());
        r.setAsunto(request.asunto().trim());
        r.setDescripcion(request.descripcion().trim());
        if ("RECLAMO_PEDIDO".equals(request.tipo())) {
            if (request.pedidoId() == null) throw new ConflictoNegocioException("Selecciona el pedido relacionado.");
            Pedido pedido = pedidoRepository.findById(request.pedidoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", request.pedidoId()));
            if (!clienteId.equals(pedido.getClienteId())) throw new AccessDeniedException("El pedido no pertenece al cliente.");
            r.setPedidoId(pedido.getId());
            r.setVendedorId(pedido.getVendedorId());
        }
        return map(repository.save(r));
    }

    @Transactional(readOnly = true)
    public List<ReclamoResponse> listarCliente(Integer clienteId) {
        return repository.findByClienteIdOrderByFechaCreacionDesc(clienteId).stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<ReclamoResponse> listarVendedor(Integer vendedorId) {
        return repository.findByVendedorIdOrderByFechaCreacionDesc(vendedorId).stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<ReclamoResponse> listarAdmin() {
        return repository.findAllByOrderByFechaCreacionDesc().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public ReclamoResponse detalleCliente(Long id, Integer clienteId) {
        Reclamo r = obtener(id);
        if (!clienteId.equals(r.getClienteId())) throw new AccessDeniedException("El reclamo no pertenece al cliente.");
        return map(r);
    }

    @Transactional(readOnly = true)
    public ReclamoResponse detalleVendedor(Long id, Integer vendedorId) {
        Reclamo r = obtener(id);
        if (!vendedorId.equals(r.getVendedorId())) throw new AccessDeniedException("El reclamo no pertenece al vendedor.");
        return map(r);
    }

    @Transactional(readOnly = true)
    public ReclamoResponse detalleAdmin(Long id) { return map(obtener(id)); }

    @Transactional
    public ReclamoResponse responderVendedor(Long id, String respuesta, Integer vendedorId) {
        Reclamo r = obtener(id);
        if (!vendedorId.equals(r.getVendedorId())) throw new AccessDeniedException("El reclamo no pertenece al vendedor.");
        return responder(r, respuesta);
    }

    @Transactional
    public ReclamoResponse responderAdmin(Long id, String respuesta) {
        Reclamo r = obtener(id);
        if (!"LIBRO_PLATAFORMA".equals(r.getTipo())) throw new ConflictoNegocioException("Los reclamos de pedido los responde el vendedor.");
        return responder(r, respuesta);
    }

    private ReclamoResponse responder(Reclamo r, String respuesta) {
        r.setRespuesta(respuesta.trim());
        r.setEstado("RESPONDIDO");
        r.setFechaRespuesta(LocalDateTime.now());
        return map(repository.save(r));
    }

    private Reclamo obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Reclamo", id));
    }

    private ReclamoResponse map(Reclamo r) {
        Usuario cliente = usuarioRepository.findById(r.getClienteId()).orElse(null);
        String nombre = cliente == null ? null : ((cliente.getNombres() == null ? "" : cliente.getNombres()) + " " +
                (cliente.getPrimerApellido() == null ? "" : cliente.getPrimerApellido())).trim();
        return new ReclamoResponse(r.getId(), r.getClienteId(), nombre, r.getVendedorId(), r.getPedidoId(),
                r.getTipo(), r.getAsunto(), r.getDescripcion(), r.getEstado(), r.getRespuesta(),
                r.getFechaCreacion().toString(), r.getFechaRespuesta() != null ? r.getFechaRespuesta().toString() : null);
    }
}
