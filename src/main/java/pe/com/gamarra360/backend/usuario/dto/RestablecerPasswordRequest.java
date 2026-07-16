package pe.com.gamarra360.backend.usuario.dto;

public record RestablecerPasswordRequest(String email, String codigo, String nuevaContrasenha) {}
