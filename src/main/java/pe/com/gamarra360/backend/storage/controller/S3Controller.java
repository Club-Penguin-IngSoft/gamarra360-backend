package pe.com.gamarra360.backend.storage.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.com.gamarra360.backend.storage.service.S3Service;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/s3")
@Slf4j
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    /**
     * Sube un archivo a S3 y retorna su URL pública.
     *
     * @param archivo  archivo a subir (imagen, pdf, etc.)
     * @param carpeta  prefijo de carpeta en el bucket (ej: "productos", "tiendas")
     * @return         JSON { "url": "https://..." }
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> subir(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "carpeta", defaultValue = "general") String carpeta) {

        log.info("POST /api/v1/s3/upload - carpeta={}, archivo={}", carpeta, archivo.getOriginalFilename());
        String url = s3Service.subirArchivo(archivo, carpeta);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * Elimina un archivo de S3 dada su URL pública.
     *
     * @param url  URL pública del archivo a eliminar
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> eliminar(@RequestParam("url") String url) {
        log.info("DELETE /api/v1/s3/delete - url={}", url);
        s3Service.eliminarArchivo(url);
        return ResponseEntity.noContent().build();
    }
}