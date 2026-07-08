package com.brasfi.webapp.service;

import com.brasfi.webapp.dto.ApiDtos.FileUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;

    private final Path uploadPath;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadPath = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public FileUploadResponse storeLearningMaterial(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Envie um arquivo para anexar.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("O arquivo deve ter no maximo 10 MB.");
        }

        try {
            Files.createDirectories(uploadPath);
            String originalName = sanitizeFileName(file.getOriginalFilename());
            String storedName = UUID.randomUUID() + "-" + originalName;
            Path target = uploadPath.resolve(storedName).normalize();
            if (!target.startsWith(uploadPath)) {
                throw new IllegalArgumentException("Nome de arquivo invalido.");
            }

            file.transferTo(target);
            return new FileUploadResponse(
                    originalName,
                    "/uploads/" + storedName,
                    file.getContentType(),
                    file.getSize()
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel salvar o arquivo.");
        }
    }

    private static String sanitizeFileName(String fileName) {
        String fallback = fileName == null || fileName.isBlank() ? "material" : fileName.trim();
        String normalized = Normalizer.normalize(fallback, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "material" : normalized;
    }
}
