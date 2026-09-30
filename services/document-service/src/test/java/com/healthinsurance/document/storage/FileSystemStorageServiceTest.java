package com.healthinsurance.document.storage;

import com.healthinsurance.document.exception.DocumentStorageException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileSystemStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new FileSystemStorageService(tempDir.toString());
        storageService.init();
    }

    @Test
    void testStoreFile_Success() {
        UUID docId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "invoice.pdf", "application/pdf", "Dummy PDF content".getBytes()
        );

        String storageRef = storageService.storeFile(file, docId);

        assertNotNull(storageRef);
        assertEquals(docId.toString() + ".pdf", storageRef);

        Resource resource = storageService.loadFileAsResource(storageRef);
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void testStoreFile_PathTraversal_ThrowsException() {
        UUID docId = UUID.randomUUID();
        MockMultipartFile maliciousFile = new MockMultipartFile(
                "file", "../evil.exe", "application/octet-stream", "hacked".getBytes()
        );

        assertThrows(DocumentStorageException.class, () ->
                storageService.storeFile(maliciousFile, docId));
    }

    @Test
    void testStoreFile_EmptyFile_ThrowsException() {
        UUID docId = UUID.randomUUID();
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]
        );

        assertThrows(DocumentStorageException.class, () ->
                storageService.storeFile(emptyFile, docId));
    }

    @Test
    void testDeleteFile_Success() {
        UUID docId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "receipt.png", "image/png", "Image content".getBytes()
        );

        String storageRef = storageService.storeFile(file, docId);
        assertTrue(storageService.loadFileAsResource(storageRef).exists());

        storageService.deleteFile(storageRef);
        assertThrows(DocumentStorageException.class, () ->
                storageService.loadFileAsResource(storageRef));
    }
}
