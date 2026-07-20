package pe.com.gamarra360.backend.logistica.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.logistica.dto.*;
import pe.com.gamarra360.backend.logistica.service.TarifaEnvioService;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;
import java.util.List;

@RestController @RequestMapping("/api/v1/tarifas-envio") @RequiredArgsConstructor
public class TarifaEnvioController {
    private final TarifaEnvioService service;
    @GetMapping("/vendedor/{vendedorId}") public List<TarifaEnvioResponse> publicas(@PathVariable Integer vendedorId){return service.listarVendedor(vendedorId);}
    @GetMapping("/mis-tarifas") @PreAuthorize("hasRole('VENDEDOR')") public List<TarifaEnvioResponse> propias(Authentication a){return service.listarVendedor(id(a));}
    @PutMapping("/mis-tarifas") @PreAuthorize("hasRole('VENDEDOR')") public TarifaEnvioResponse guardar(@Valid @RequestBody TarifaEnvioRequest r,Authentication a){return service.guardar(id(a),r);}
    private Integer id(Authentication a){return ((UsuarioPrincipal)a.getPrincipal()).getUsuarioId();}
}
