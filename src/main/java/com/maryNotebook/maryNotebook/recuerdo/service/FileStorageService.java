package com.maryNotebook.maryNotebook.recuerdo.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(
            @Value("${marynotebook.upload-dir:uploads}") String uploadDir
    ) {
        this.rootLocation = Paths.get(uploadDir);
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(rootLocation);
    }

    public String guardarArchivo(MultipartFile archivo) throws IOException {
        if (archivo.isEmpty()) {
            throw new IOException("El archivo está vacío");
        }

        String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
        Path destino = rootLocation.resolve(nombreArchivo);

        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        // Retornar la ruta relativa para guardar en BD
        return "/images/" + nombreArchivo;
    }

    public void eliminarArchivo(String imagenPath) {
        if (imagenPath == null) return;
        try {
            String nombreArchivo = imagenPath.replace("/images/","");
            Files.deleteIfExists(rootLocation.resolve(nombreArchivo));
        } catch (IOException ignored) {}
    }
}
