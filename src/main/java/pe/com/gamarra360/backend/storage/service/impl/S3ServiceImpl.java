package pe.com.gamarra360.backend.storage.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.com.gamarra360.backend.storage.service.S3Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(
    prefix = "storage",
    name = "provider",
    havingValue = "S3",
    matchIfMissing = true
)
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final String bucket;
    private final String region;

    public S3ServiceImpl(S3Client s3Client,
                         @Value("${aws.s3.bucket}") String bucket,
                         @Value("${aws.region.static}") String region) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.region = region;
    }

    @Override
    public String subirArchivo(MultipartFile archivo, String carpeta) {
        String extension = obtenerExtension(archivo.getOriginalFilename());
        String key = carpeta + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(archivo.getContentType())
                    .contentLength(archivo.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(archivo.getInputStream(), archivo.getSize()));

            String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
            log.info("Archivo subido a S3: {}", url);
            return url;

        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo antes de subirlo a S3", e);
        }
    }

    @Override
    public void eliminarArchivo(String url) {
        String prefix = "https://" + bucket + ".s3." + region + ".amazonaws.com/";
        if (!url.startsWith(prefix)) {
            log.warn("URL no pertenece al bucket configurado, se omite eliminación: {}", url);
            return;
        }
        String key = url.substring(prefix.length());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        log.info("Archivo eliminado de S3: {}", key);
    }

    private String obtenerExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}