package com.example.user_service.controller;

import com.example.user_service.model.Gender;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.user_service.model.UserDataDetails;
import com.example.user_service.service.UserDataDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/users/secured/details")
public class UserDataDetailsController {
    private final UserDataDetailsService service;

    public UserDataDetailsController(UserDataDetailsService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDataDetails> getUserById(@PathVariable Long id) {
        return service.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestPart("updatedUser") UserDataDetails updatedUser,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        if (updatedUser.getGender() != Gender.MALE && updatedUser.getGender() != Gender.FEMALE) {
            return ResponseEntity.badRequest().body("We do not accept combat helicopters");
        }
        try {
            // Логика сохранения файла, если он был предоставлен
            String imagePath = null;
            if (file != null && !file.isEmpty()) {
                // Сохранить файл, например, в локальную директорию
                imagePath = saveFile(file);
            }
            System.out.println(imagePath);
            updatedUser.setProfilePhoto(imagePath);
            return ResponseEntity.ok(service.updateUser(id, updatedUser));
        } catch (RuntimeException | IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
    // Метод для сохранения файла
    private String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get("C:\\vus\\diplomproject\\back\\user-service\\src\\main\\java\\com\\example\\user_service\\uploads");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "Файл не содержит имени!";
        }
        Path filePath = uploadPath.resolve(originalFilename);
        file.transferTo(filePath.toFile());
        return originalFilename;
    }

    private String deleteFile(String fileName) throws IOException {
        Path uploadPath = Paths.get("C:\\vus\\diplomproject\\back\\user-service\\src\\main\\java\\com\\example\\user_service\\uploads");
        if (!Files.exists(uploadPath)) {
            return "Папка uploads не найдена!";
        }
        Path filePath = uploadPath.resolve(fileName);
        if (!Files.exists(filePath)) {
            return "Файл с именем \"" + fileName + "\" не найден!";
        }
        Files.delete(filePath);
        return fileName;
    }
}
