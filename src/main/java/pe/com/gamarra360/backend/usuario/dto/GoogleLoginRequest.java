package pe.com.gamarra360.backend.usuario.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
