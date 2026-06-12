package pe.com.gamarra360.backend.storage.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import pe.com.gamarra360.backend.storage.service.StorageService;

@Service
@Slf4j
@ConditionalOnProperty(
    prefix = "storage",
    name = "provider",
    havingValue = "SUPABASE"
)
public class SupabaseStorageServiceImpl implements StorageService {

    @Override
    public String subirArchivo(MultipartFile archivo, String carpeta) {
        log.info("SupabaseStorageServiceImpl: Preparado para subir archivo a la carpeta {}", carpeta);
        // TODO: Implementar integración con Supabase Storage
        return "https://supabase-placeholder-url.com/" + carpeta + "/" + archivo.getOriginalFilename();
    }

    @Override
    public void eliminarArchivo(String url) {
        log.info("SupabaseStorageServiceImpl: Preparado para eliminar archivo con URL {}", url);
        // TODO: Implementar eliminación en Supabase Storage
    }
}
