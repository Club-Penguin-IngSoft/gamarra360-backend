package pe.com.gamarra360.backend.usuario.service.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.usuario.dto.OnboardingLinkResponse;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;
import pe.com.gamarra360.backend.usuario.service.ComercianteStripeService;
import com.stripe.model.Balance;
import com.stripe.net.RequestOptions;
import com.stripe.model.LoginLink;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ComercianteStripeServiceImpl implements ComercianteStripeService {

    private final ComercianteRepository comercianteRepository;

    @Override
    public Map<String, Object> obtenerBalance(Integer comercianteId) throws StripeException {
        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Comerciante no encontrado: " + comercianteId));

        RequestOptions options = RequestOptions.builder()
                .setStripeAccount(comerciante.getStripeAccountId())
                .build();

        Balance balance = Balance.retrieve(options);
        return Map.of(
                "disponible", balance.getAvailable().get(0).getAmount(),
                "pendiente", balance.getPending().get(0).getAmount(),
                "moneda", balance.getAvailable().get(0).getCurrency()
        );
    }

    @Override
    public Map<String, String> generarDashboardLink(Integer comercianteId) throws StripeException {
        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Comerciante no encontrado: " + comercianteId));

        LoginLink loginLink = LoginLink.createOnAccount(
                comerciante.getStripeAccountId(),
                new java.util.HashMap<>(),
                null
        );
        return Map.of("url", loginLink.getUrl());
    }

    @Override
    @Transactional
    public OnboardingLinkResponse generarLinkOnboarding(Integer comercianteId)
            throws StripeException {

        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Comerciante no encontrado: " + comercianteId));

        // Si ya tiene cuenta Stripe, no crear otra
        if (comerciante.getStripeAccountId() != null) {
            Account account = Account.retrieve(comerciante.getStripeAccountId());
            boolean completo = Boolean.TRUE.equals(account.getDetailsSubmitted());

            if (completo) {
                log.info("Comerciante {} ya completó el onboarding en Stripe.", comercianteId);
                return new OnboardingLinkResponse(
                        comerciante.getStripeAccountId(), null, true);
            }

            // Tiene cuenta pero no completó — genera nuevo link
            String url = crearAccountLink(comerciante.getStripeAccountId(), comercianteId);
            return new OnboardingLinkResponse(
                    comerciante.getStripeAccountId(), url, false);
        }

        // Crear nueva cuenta Express en Stripe
        AccountCreateParams params = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.EXPRESS)
                .setEmail(comerciante.getEmail())
                .setCountry("US")
                .setCapabilities(AccountCreateParams.Capabilities.builder().
                        setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder()
                                .setRequested(true).build())
                        .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                .setRequested(true).build())
                        .build())
                .setBusinessProfile(AccountCreateParams.BusinessProfile.builder()
                        .setName(comerciante.getNombreTienda())
                        .build())
                .build();

        Account account = Account.create(params);
        log.info("Cuenta Stripe creada para comerciante {}: {}",
                comercianteId, account.getId());

        comerciante.setStripeAccountId(account.getId());
        comercianteRepository.save(comerciante);

        String url = crearAccountLink(account.getId(), comercianteId);
        return new OnboardingLinkResponse(account.getId(), url, false);
    }

    @Override
    @Transactional
    public void completarOnboarding(String stripeAccountId) throws StripeException {
        Account account = Account.retrieve(stripeAccountId);

        if (Boolean.TRUE.equals(account.getDetailsSubmitted())) {
            comercianteRepository.findByStripeAccountId(stripeAccountId)
                    .ifPresent(comerciante -> {
                        log.info("Onboarding completado para comerciante con cuenta {}",
                                stripeAccountId);
                        comercianteRepository.save(comerciante);
                    });
        }
    }

    private String crearAccountLink(String stripeAccountId, Integer comercianteId)
            throws StripeException {
        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(stripeAccountId)
                .setRefreshUrl("http://localhost:5173/comerciante/stripe/refresh/"
                        + comercianteId)
                .setReturnUrl("http://localhost:5173/comerciante/stripe/completado")
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();

        return AccountLink.create(params).getUrl();
    }
}