package pe.com.gamarra360.backend.reporte.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.reporte.dto.*;
import pe.com.gamarra360.backend.reporte.service.ReporteService;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {
    private final ReporteService service;

    @GetMapping("/vendedor/ventas")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ReporteVentasResponse ventas(@RequestParam LocalDate desde, @RequestParam LocalDate hasta, Authentication auth) {
        return service.ventas(((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId(), desde, hasta);
    }

    @GetMapping("/vendedor/inventario")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ReporteInventarioResponse inventario(Authentication auth) {
        return service.inventario(((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ReporteAdminResponse admin(@RequestParam LocalDate desde, @RequestParam LocalDate hasta) {
        return service.admin(desde, hasta);
    }
}
