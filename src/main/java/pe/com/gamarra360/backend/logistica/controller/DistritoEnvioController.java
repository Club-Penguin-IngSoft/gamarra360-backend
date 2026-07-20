package pe.com.gamarra360.backend.logistica.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.gamarra360.backend.logistica.dto.DistritoEnvioDto;
import pe.com.gamarra360.backend.logistica.repository.DistritoEnvioRepository;
import pe.com.gamarra360.backend.logistica.entity.DistritoEnvio;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.exception.DatosInvalidosException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/v1/distritos")
@RequiredArgsConstructor
@Slf4j
public class DistritoEnvioController {

    private final DistritoEnvioRepository repository;

    @GetMapping
    public ResponseEntity<List<DistritoEnvioDto>> listar() {
        log.info("GET /api/v1/distritos");
        List<DistritoEnvioDto> dtos = repository.findByActivoTrueOrderByCiudadAscNombreAsc()
                .stream()
                .map(d -> new DistritoEnvioDto(d.getIdDistrito(), d.getCiudad(), d.getNombre(), d.getCostoEnvio()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    public record DistritoEnvioRequest(String ciudad, String nombre, Double costoEnvio, Boolean activo) {}

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DistritoEnvio> listarAdmin() {
        return repository.findAll().stream()
                .sorted((a, b) -> (a.getCiudad() + a.getNombre()).compareToIgnoreCase(b.getCiudad() + b.getNombre()))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DistritoEnvio> crear(@RequestBody DistritoEnvioRequest request) {
        validar(request);
        DistritoEnvio distrito = new DistritoEnvio();
        aplicar(distrito, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(distrito));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DistritoEnvio actualizar(@PathVariable Integer id, @RequestBody DistritoEnvioRequest request) {
        validar(request);
        DistritoEnvio distrito = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Distrito", id));
        aplicar(distrito, request);
        return repository.save(distrito);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        DistritoEnvio distrito = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Distrito", id));
        distrito.setActivo(false);
        repository.save(distrito);
        return ResponseEntity.noContent().build();
    }

    private void validar(DistritoEnvioRequest request) {
        if (request.ciudad() == null || request.ciudad().isBlank() || request.nombre() == null || request.nombre().isBlank()) {
            throw new DatosInvalidosException("Ciudad y distrito son obligatorios.");
        }
        if (request.costoEnvio() == null || request.costoEnvio() < 0) {
            throw new DatosInvalidosException("El costo de envío debe ser mayor o igual a cero.");
        }
    }

    private void aplicar(DistritoEnvio distrito, DistritoEnvioRequest request) {
        distrito.setCiudad(request.ciudad().trim());
        distrito.setNombre(request.nombre().trim());
        distrito.setCostoEnvio(request.costoEnvio());
        distrito.setActivo(request.activo() == null || request.activo());
    }
}
