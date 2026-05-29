package pe.com.gamarra360.backend.catalogo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import pe.com.gamarra360.backend.catalogo.dto.PerfilTiendaPublicaDto;
import pe.com.gamarra360.backend.catalogo.dto.TiendaInfoResponse;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.service.TiendaService;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tiendas")
@Slf4j
public class TiendaController {
    private final TiendaService service;

    public TiendaController(TiendaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Tienda>> listar() {
        log.info("GET /api/v1/tiendas");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tienda> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/tiendas/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping("/publico/{id}")
    public ResponseEntity<PerfilTiendaPublicaDto> obtenerPerfilPublico(@PathVariable Integer id) {
        log.info("GET /api/v1/tiendas/publico/{}", id);
        return ResponseEntity.ok(service.obtenerPerfilPublico(id));
    }

    @GetMapping("/mi-tienda")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<TiendaInfoResponse> obtenerMiTienda(Authentication auth) {
        Integer comercianteId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("GET /api/v1/tiendas/mi-tienda - comerciante {}", comercianteId);
        return ResponseEntity.ok(service.obtenerInfoComerciante(comercianteId));
    }

    @PostMapping
    public ResponseEntity<Tienda> crear(@RequestBody Tienda request) {
        log.info("POST /api/v1/tiendas");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tienda> actualizar(@PathVariable Integer id, @RequestBody Tienda request) {
        log.info("PUT /api/v1/tiendas/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/tiendas/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
