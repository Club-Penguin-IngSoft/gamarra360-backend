package pe.com.gamarra360.backend.catalogo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.gamarra360.backend.catalogo.entity.MaterialFiltro;
import pe.com.gamarra360.backend.catalogo.repository.MaterialFiltroRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/materiales")
@Slf4j
public class MaterialFiltroController {

    private final MaterialFiltroRepository repository;

    public MaterialFiltroController(MaterialFiltroRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<MaterialFiltro>> listar() {
        log.info("GET /api/v1/materiales");
        return ResponseEntity.ok(repository.findAll());
    }
}
