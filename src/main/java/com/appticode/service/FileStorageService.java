package com.appticode.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String directory) {
        try {
            Path targetDir = getTargetDirectory(directory);
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            Path targetPath = targetDir.resolve(fileName);
            
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return directory != null ? directory + "/" + fileName : fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file: " + e.getMessage());
        }
    }

    public String storeFile(InputStream inputStream, String fileName, String directory) {
        try {
            Path targetDir = getTargetDirectory(directory);
            String uniqueFileName = generateUniqueFileName(fileName);
            Path targetPath = targetDir.resolve(uniqueFileName);
            
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return directory != null ? directory + "/" + uniqueFileName : uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file: " + e.getMessage());
        }
    }

    public Resource loadFileAsResource(String filePath) {
        try {
            Path path = Paths.get(uploadDir).resolve(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not load file: " + e.getMessage());
        }
    }

    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadDir).resolve(filePath).normalize();
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean fileExists(String filePath) {
        Path path = Paths.get(uploadDir).resolve(filePath).normalize();
        return Files.exists(path);
    }

    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + extension;
    }

    public void createDirectory(String directoryPath) {
        try {
            Path path = Paths.get(uploadDir).resolve(directoryPath);
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("Could not create directory: " + e.getMessage());
        }
    }

    public List<String> listFiles() {
        try {
            Path root = Paths.get(uploadDir);
            if (Files.exists(root)) {
                try (Stream<Path> walk = Files.walk(root, 1)) {
                    return walk
                        .filter(path -> !path.equals(root))
                        .map(root::relativize)
                        .map(Path::toString)
                        .collect(Collectors.toList());
                }
            }
            return List.of();
        } catch (IOException e) {
            throw new RuntimeException("Could not list files: " + e.getMessage());
        }
    }

    private Path getTargetDirectory(String directory) {
        Path targetDir;
        if (directory != null && !directory.isEmpty()) {
            targetDir = Paths.get(uploadDir).resolve(directory).normalize();
        } else {
            targetDir = Paths.get(uploadDir);
        }
        
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create directory: " + e.getMessage());
        }
        
        return targetDir;
    }
}