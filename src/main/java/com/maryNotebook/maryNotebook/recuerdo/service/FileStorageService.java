package com.maryNotebook.maryNotebook.recuerdo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageService {

    private final Path rootLocation = Paths.get("uploads");

    public String guardarArchivo(MultipartFile archivo) throws IOException {
        if (archivo.isEmpty()) {
            throw new IOException("El archivo está vacío");
        }

        // Asegurarse de que la carpeta exista
        Files.createDirectories(rootLocation);

        String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
        Path destino = rootLocation.resolve(nombreArchivo);

        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        // Retornar la ruta relativa para guardar en BD
        return "/images/" + nombreArchivo;
    }

    public void eliminarArchivo(String nombreArchivo) {
        if (nombreArchivo == null) return;
        try {
            Path archivo = rootLocation.resolve(
                    nombreArchivo.replace("/images/", "")
            );
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
