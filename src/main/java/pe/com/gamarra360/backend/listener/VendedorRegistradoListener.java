//package pe.com.gamarra360.backend.listener;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.event.TransactionalEventListener;
//import org.springframework.transaction.event.TransactionPhase;
//import pe.com.gamarra360.backend.admin.service.VendedorRegistradoEvent;
//import pe.com.gamarra360.backend.usuario.service.NotificacionService;
//
//@Component
//@RequiredArgsConstructor
//public class VendedorRegistradoListener {
//    @Autowired
//    private final NotificacionService notificacionService;
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handle(VendedorRegistradoEvent event) {
//
//        notificacionService.crearNotificacion(
//                event.getUsuario().getUsuarioId(),
//                "Nuevo comerciante registrado: " + event.getUsuario().getNombres(),
//                "VERIFICACION",
//                3//solo es referencial
//        );
//    }
//}
