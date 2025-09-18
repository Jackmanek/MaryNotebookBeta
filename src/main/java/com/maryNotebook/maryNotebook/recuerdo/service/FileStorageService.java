package com.maryNotebook.maryNotebook.recuerdo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileStorageService {

    @Value("${marynotebook.upload-dir}")
    private String uploadDir;

    public String guardarArchivo(MultipartFile file) throws IOException {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        Path targetLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(targetLocation); // crear carpeta si no existe
        Path filePath = targetLocation.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + filename; // o devolver ruta completa si quieres
    }
}
