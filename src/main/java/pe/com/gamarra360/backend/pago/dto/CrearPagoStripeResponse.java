package pe.com.gamarra360.backend.pago.dto;

public record CrearPagoStripeResponse(
        Long pagoId,
        String clientSecret,
        Long montoCentimos,
        String currency
) {}
