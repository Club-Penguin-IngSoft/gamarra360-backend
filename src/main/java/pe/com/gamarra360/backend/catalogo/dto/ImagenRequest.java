package pe.com.gamarra360.backend.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagenRequest {
    @NotBlank(message = "La URL de la imagen es obligatoria")
    private String url;
    private Boolean esPrincipal = false;
}