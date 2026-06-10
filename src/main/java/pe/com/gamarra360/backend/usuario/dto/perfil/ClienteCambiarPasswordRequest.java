package pe.com.gamarra360.backend.usuario.dto.perfil;

public record ClienteCambiarPasswordRequest(
        String contrasenhaActual,
        String nuevaContrasenha
) {}
