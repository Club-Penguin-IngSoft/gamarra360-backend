package pe.com.gamarra360.backend.usuario.dto;

import lombok.Getter;
import lombok.Setter;
import pe.com.gamarra360.backend.enums.RolEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class GoogleRegistroRequest {
    @NotBlank(message = "El token de Google es obligatorio")
    private String idToken;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombres;
    
    @NotBlank(message = "El apellido es obligatorio")
    private String primerApellido;
    
    private String segundoApellido;
    
    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento; // DNI, PASAPORTE, etc.
    
    @NotBlank(message = "El número de documento es obligatorio")
    private String numeroDocumento; // Mapeado a 'dni'
    
    @NotBlank(message = "El celular es obligatorio")
    private String celular; // Mapeado a 'telefono'

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasenha; // Para permitir login local

    @NotNull(message = "El rol es obligatorio")
    private RolEnum rol; // CLIENTE o VENDEDOR

    // Campos adicionales para vendedor
    private Long idTienda; // Número de la tienda
    private String ruc;
    private String razonSocial;
}
