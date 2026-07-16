//package pe.com.gamarra360.backend.listener;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.event.TransactionPhase;
//import org.springframework.transaction.event.TransactionalEventListener;
//import pe.com.gamarra360.backend.admin.service.UsuarioReactivadoEvent;
//import pe.com.gamarra360.backend.usuario.service.NotificacionService;
//
//@Component
//@RequiredArgsConstructor
//public class UsuarioReactivadoListener {
//
//    private final NotificacionService notificacionService;
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handle(UsuarioReactivadoEvent event) {
//
//        notificacionService.crearNotificacion(
//                event.getUsuario().getUsuarioId(),
//                "Tu cuenta ha sido REACTIVADA",
//                "VERIFICACION",
//                null
//        );
//    }
//}