package pe.com.gamarra360.backend.usuario.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.com.gamarra360.backend.enums.GaleriaEnum;

@Getter
@Setter
@NoArgsConstructor
public class PerfilComercianteDto {

    // ── Titular (Usuario) ─────────────────────────────────────────────────────
    private String email;           // solo lectura — es el login
    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String tipoDocumento;
    private String dni;
    private String telefono;

    // ── Negocio (Comerciante) ─────────────────────────────────────────────────
    private String razonSocial;
    private String ruc;             // solo lectura — identificador fiscal
    private String logoUrl;
    private Boolean verificada;

    // ── Tienda ────────────────────────────────────────────────────────────────
    private String nombreTienda;
    private String informacion;
    private GaleriaEnum galeria;
    private String piso;
    private String stand;
    private String foto;
    private Boolean ofreceEnvioDomicilio;
}
