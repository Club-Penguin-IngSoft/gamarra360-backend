package pe.com.gamarra360.backend.logistica.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.gamarra360.backend.logistica.dto.DistritoEnvioDto;
import pe.com.gamarra360.backend.logistica.repository.DistritoEnvioRepository;

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
}
