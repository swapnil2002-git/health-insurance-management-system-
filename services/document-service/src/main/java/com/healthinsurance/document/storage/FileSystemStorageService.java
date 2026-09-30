package com.healthinsurance.document.storage;

import com.healthinsurance.document.exception.DocumentStorageException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class FileSystemStorageService implements DocumentStorageService {

    private final Path rootLocation;

    public FileSystemStorageService(@Value("${document.storage.location:./uploads/documents}") String location) {
        this.rootLocation = Paths.get(location).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            log.info("Document storage directory initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new DocumentStorageException("Could not initialize storage directory at " + rootLocation, e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, UUID documentId) {
        if (file == null || file.isEmpty()) {
            throw new DocumentStorageException("Cannot store empty file.");
        }

        String rawFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (rawFilename.contains("..") || rawFilename.contains("/") || rawFilename.contains("\\")) {
            throw new DocumentStorageException("Path traversal attempt detected in filename: " + rawFilename);
        }

        // Determine file extension safely
        String extension = "";
        int dotIndex = rawFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = rawFilename.substring(dotIndex).toLowerCase();
        }

        // Generate safe internal filename using UUID
        String safeStorageName = documentId.toString() + extension;
        Path destinationFile = this.rootLocation.resolve(safeStorageName).normalize().toAbsolutePath();

        // Enforce boundary check against root directory
        if (!destinationFile.getParent().equals(this.rootLocation)) {
            throw new DocumentStorageException("Cannot store file outside current storage directory.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file {} with safe reference: {}", rawFilename, safeStorageName);
            return safeStorageName;
        } catch (IOException e) {
            throw new DocumentStorageException("Failed to store file " + safeStorageName, e);
        }
    }

    @Override
    public Resource loadFileAsResource(String storageReference) {
        try {
            Path file = rootLocation.resolve(storageReference).normalize();
            if (!file.startsWith(rootLocation)) {
                throw new DocumentStorageException("Path traversal attempt detected: " + storageReference);
            }

            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new DocumentStorageException("Could not read file: " + storageReference);
            }
        } catch (MalformedURLException e) {
            throw new DocumentStorageException("Could not read file: " + storageReference, e);
        }
    }

    @Override
    public void deleteFile(String storageReference) {
        try {
            Path file = rootLocation.resolve(storageReference).normalize();
            if (!file.startsWith(rootLocation)) {
                throw new DocumentStorageException("Path traversal attempt detected: " + storageReference);
            }
            Files.deleteIfExists(file);
            log.info("Deleted physical file: {}", storageReference);
        } catch (IOException e) {
            log.warn("Failed to delete physical file {}: {}", storageReference, e.getMessage());
        }
    }
}
