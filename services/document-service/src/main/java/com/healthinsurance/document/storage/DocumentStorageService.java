package com.healthinsurance.document.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface DocumentStorageService {

    String storeFile(MultipartFile file, UUID documentId);

    Resource loadFileAsResource(String storageReference);

    void deleteFile(String storageReference);
}
