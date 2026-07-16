package pe.com.gamarra360.backend.catalogo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.catalogo.dto.OfertaRequestDto;
import pe.com.gamarra360.backend.catalogo.dto.OfertaResponseDto;
import pe.com.gamarra360.backend.catalogo.service.OfertaService;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ofertas")
@PreAuthorize("hasRole('VENDEDOR')")
@RequiredArgsConstructor
@Slf4j
public class OfertaController {

    private final OfertaService ofertaService;

    @GetMapping
    public ResponseEntity<List<OfertaResponseDto>> listar(Authentication auth) {
        Integer comercianteId = comercianteId(auth);
        log.info("GET /api/v1/ofertas - comercianteId={}", comercianteId);
        return ResponseEntity.ok(ofertaService.listar(comercianteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfertaResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer comercianteId = comercianteId(auth);
        log.info("GET /api/v1/ofertas/{} - comercianteId={}", id, comercianteId);
        return ResponseEntity.ok(ofertaService.obtener(id, comercianteId));
    }

    @PostMapping
    public ResponseEntity<OfertaResponseDto> crear(
            @Valid @RequestBody OfertaRequestDto request,
            Authentication auth) {
        Integer comercianteId = comercianteId(auth);
        log.info("POST /api/v1/ofertas - comercianteId={}", comercianteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ofertaService.crear(request, comercianteId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfertaResponseDto> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody OfertaRequestDto request,
            Authentication auth) {
        Integer comercianteId = comercianteId(auth);
        log.info("PUT /api/v1/ofertas/{} - comercianteId={}", id, comercianteId);
        return ResponseEntity.ok(ofertaService.actualizar(id, request, comercianteId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id, Authentication auth) {
        Integer comercianteId = comercianteId(auth);
        log.info("DELETE /api/v1/ofertas/{} - comercianteId={}", id, comercianteId);
        ofertaService.eliminar(id, comercianteId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<OfertaResponseDto> toggleActiva(@PathVariable Integer id, Authentication auth) {
        Integer comercianteId = comercianteId(auth);
        log.info("PATCH /api/v1/ofertas/{}/toggle - comercianteId={}", id, comercianteId);
        return ResponseEntity.ok(ofertaService.toggleActiva(id, comercianteId));
    }

    private Integer comercianteId(Authentication auth) {
        return ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
    }
}
