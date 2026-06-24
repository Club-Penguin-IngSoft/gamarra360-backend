package pe.com.gamarra360.backend.usuario.dto;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonAlias;
import pe.com.gamarra360.backend.enums.GaleriaEnum;
import pe.com.gamarra360.backend.enums.RolEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class RegistroUsuarioRequest {
    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String contrasenha;
    private String dni;
    private String telefono;
    @NotNull
    private RolEnum rol;
    private String nombre;
    private String apellido;
    private String ruc;
    private String razonSocial;
    private String tipoDocumento;
    private String nombreTienda;
    private String logoUrl;
    private String informacion;
    private String piso;
    private String stand;
    private GaleriaEnum galeria;
    @JsonAlias("ofreceEnvio")
    private Boolean ofreceEnvioDomicilio;
}
