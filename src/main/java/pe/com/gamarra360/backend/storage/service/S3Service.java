package pe.com.gamarra360.backend.storage.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String subirArchivo(MultipartFile archivo, String carpeta);
    void eliminarArchivo(String url);
}