package pe.com.gamarra360.backend.reclamo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.reclamo.dto.*;
import pe.com.gamarra360.backend.reclamo.service.ReclamoService;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reclamos")
@RequiredArgsConstructor
public class ReclamoController {
    private final ReclamoService service;
    private Integer id(Authentication auth) { return ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId(); }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ReclamoResponse> crear(@Valid @RequestBody ReclamoCrearRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request, id(auth)));
    }
    @GetMapping("/mis-reclamos")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<ReclamoResponse> cliente(Authentication auth) { return service.listarCliente(id(auth)); }
    @GetMapping("/mis-reclamos/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ReclamoResponse detalleCliente(@PathVariable Long id, Authentication auth) { return service.detalleCliente(id, id(auth)); }

    @GetMapping("/vendedor")
    @PreAuthorize("hasRole('VENDEDOR')")
    public List<ReclamoResponse> vendedor(Authentication auth) { return service.listarVendedor(id(auth)); }
    @GetMapping("/vendedor/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ReclamoResponse detalleVendedor(@PathVariable Long id, Authentication auth) { return service.detalleVendedor(id, id(auth)); }
    @PatchMapping("/vendedor/{id}/responder")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ReclamoResponse responderVendedor(@PathVariable Long id, @Valid @RequestBody ReclamoResponderRequest request, Authentication auth) {
        return service.responderVendedor(id, request.respuesta(), id(auth));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReclamoResponse> admin() { return service.listarAdmin(); }
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ReclamoResponse detalleAdmin(@PathVariable Long id) { return service.detalleAdmin(id); }
    @PatchMapping("/admin/{id}/responder")
    @PreAuthorize("hasRole('ADMIN')")
    public ReclamoResponse responderAdmin(@PathVariable Long id, @Valid @RequestBody ReclamoResponderRequest request) {
        return service.responderAdmin(id, request.respuesta());
    }
}
