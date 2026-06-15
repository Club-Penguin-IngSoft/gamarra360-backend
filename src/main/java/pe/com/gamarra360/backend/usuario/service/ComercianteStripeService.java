package pe.com.gamarra360.backend.usuario.service;

import com.stripe.exception.StripeException;
import pe.com.gamarra360.backend.usuario.dto.OnboardingLinkResponse;

import java.util.Map;

public interface ComercianteStripeService {
    OnboardingLinkResponse generarLinkOnboarding(Integer comercianteId) throws StripeException;
    void completarOnboarding(String stripeAccountId) throws StripeException;
    Map<String, Object> obtenerBalance(Integer comercianteId) throws StripeException;
    Map<String, String> generarDashboardLink(Integer comercianteId) throws StripeException;
}