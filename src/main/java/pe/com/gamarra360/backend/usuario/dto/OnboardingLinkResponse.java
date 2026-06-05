package pe.com.gamarra360.backend.usuario.dto;

public record OnboardingLinkResponse(
        String stripeAccountId,
        String onboardingUrl,
        Boolean yaCompletado
) {}