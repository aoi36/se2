package com.example.demo.Service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    @PostConstruct
    public void init() {
        try {
            // Create the directory if it doesn't exist
            Files.createDirectories(fileStorageLocation);
            System.out.println("Upload directory created/exists at: " + fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }
}
