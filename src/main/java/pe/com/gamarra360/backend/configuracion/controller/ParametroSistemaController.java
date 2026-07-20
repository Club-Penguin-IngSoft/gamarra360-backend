package pe.com.gamarra360.backend.configuracion.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.configuracion.entity.ParametroSistema;
import pe.com.gamarra360.backend.configuracion.repository.ParametroSistemaRepository;
import pe.com.gamarra360.backend.exception.ConflictoNegocioException;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/parametros")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ParametroSistemaController {
    private final ParametroSistemaRepository repository;

    public record ActualizarParametroRequest(@NotBlank String valor) {}

    @GetMapping
    public List<ParametroSistema> listar() {
        return repository.findAll().stream().sorted((a, b) -> a.getClave().compareTo(b.getClave())).toList();
    }

    @PutMapping("/{clave}")
    public ResponseEntity<ParametroSistema> actualizar(@PathVariable String clave,
                                                        @Valid @RequestBody ActualizarParametroRequest request) {
        ParametroSistema parametro = repository.findById(clave)
                .orElseThrow(() -> new RecursoNoEncontradoException("Parámetro", clave));
        if (!Boolean.TRUE.equals(parametro.getEditable())) {
            throw new ConflictoNegocioException("El parámetro no es editable.");
        }
        validarValor(parametro.getTipo(), request.valor());
        parametro.setValor(request.valor().trim());
        return ResponseEntity.ok(repository.save(parametro));
    }

    private void validarValor(String tipo, String valor) {
        try {
            if ("NUMERO".equals(tipo)) Double.parseDouble(valor);
            if ("ENTERO".equals(tipo)) Integer.parseInt(valor);
            if ("BOOLEANO".equals(tipo) && !"true".equalsIgnoreCase(valor) && !"false".equalsIgnoreCase(valor)) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException ex) {
            throw new ConflictoNegocioException("El valor no corresponde al tipo " + tipo + ".");
        }
    }
}
