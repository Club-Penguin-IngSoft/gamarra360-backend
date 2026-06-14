package pe.com.gamarra360.backend.storage.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pe.com.gamarra360.backend.storage.service.StorageService;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(
    prefix = "storage",
    name = "provider",
    havingValue = "SUPABASE"
)
public class SupabaseStorageServiceImpl implements StorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.secret}")
    private String supabaseSecret;

    @Value("${supabase.bucket}")
    private String bucket;

    private final RestTemplate restTemplate;

    public SupabaseStorageServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String subirArchivo(MultipartFile archivo, String carpeta) {
        try {
            String extension = obtenerExtension(archivo.getOriginalFilename());
            String filename = UUID.randomUUID().toString() + extension;
            String path = carpeta + "/" + filename;

            // URL para subir el objeto: {url}/storage/v1/object/{bucket}/{path}
            String uploadUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucket, path);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseSecret);
            headers.set("apikey", supabaseSecret);
            headers.setContentType(MediaType.valueOf(archivo.getContentType()));

            HttpEntity<byte[]> entity = new HttpEntity<>(archivo.getBytes(), headers);

            // Supabase usa POST para subir nuevos objetos
            log.info("Subiendo archivo a Supabase Storage: {}", uploadUrl);
            restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, String.class);

            // La URL pública es: {url}/storage/v1/object/public/{bucket}/{path}
            String publicUrl = String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, path);
            log.info("Archivo subido con éxito. URL pública: {}", publicUrl);
            
            return publicUrl;

        } catch (IOException e) {
            log.error("Error al leer bytes del archivo: {}", e.getMessage());
            throw new RuntimeException("Error al procesar el archivo para Supabase", e);
        } catch (Exception e) {
            log.error("Error al subir archivo a Supabase: {}", e.getMessage());
            throw new RuntimeException("Error en la comunicación con Supabase Storage", e);
        }
    }

    @Override
    public void eliminarArchivo(String url) {
        try {
            // Extraer el path del objeto de la URL pública
            // Patrón: .../public/{bucket}/{path}
            String marker = "/public/" + bucket + "/";
            int index = url.indexOf(marker);
            if (index == -1) {
                log.warn("La URL no coincide con el bucket de Supabase configurado: {}", url);
                return;
            }
            String path = url.substring(index + marker.length());

            String deleteUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucket, path);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseSecret);
            headers.set("apikey", supabaseSecret);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("Eliminando archivo de Supabase Storage: {}", deleteUrl);
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);

        } catch (Exception e) {
            log.error("Error al eliminar archivo de Supabase: {}", e.getMessage());
            // No lanzamos excepción para no romper el flujo principal si falla la limpieza
        }
    }

    private String obtenerExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
