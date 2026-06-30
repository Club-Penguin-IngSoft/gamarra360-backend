//package pe.com.gamarra360.backend.listener;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.event.TransactionPhase;
//import org.springframework.transaction.event.TransactionalEventListener;
//import pe.com.gamarra360.backend.admin.service.UsuarioDesactivadoEvent;
//import pe.com.gamarra360.backend.usuario.service.NotificacionService;
//
//@Component
//@RequiredArgsConstructor
//public class UsuarioDesactivadoListener {
//
//    private final NotificacionService notificacionService;
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handle(UsuarioDesactivadoEvent event) {
//
//        notificacionService.crearNotificacion(
//                event.getUsuario().getUsuarioId(),
//                "Tu cuenta ha sido DESACTIVADA",
//                "VERIFICACION",
//                null
//        );
//    }
//}