package com.example.user_service.controller;

import com.example.user_service.model.Gender;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
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
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/users/secured/details")
public class UserDataDetailsController {
    private final UserDataDetailsService service;

    public UserDataDetailsController(UserDataDetailsService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<UserDataDetails> optionalUser = service.getUserByUserId(id);
        UserDataDetails userDataDetails = optionalUser.get();
        return ResponseEntity.ok(userDataDetails);
    }

    @GetMapping("/photo/{id}")
    public ResponseEntity<?> getPhotoUserById(@PathVariable Long id) {
        Optional<UserDataDetails> optionalUser = service.getUserByUserId(id);
        UserDataDetails userDataDetails = optionalUser.get();
        if (userDataDetails.getProfilePhoto()!= null) {
            Path filePath = Paths.get("uploads", userDataDetails.getProfilePhoto());
            if (Files.exists(filePath)) {
                Resource fileResource = new FileSystemResource(filePath.toFile());
                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + userDataDetails.getProfilePhoto() + "\"")
                        .body(fileResource);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
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
            String imagePath = null;
            if (file != null && !file.isEmpty()) {
                imagePath = saveFile(file);
                if (imagePath == null) {
                    return ResponseEntity.badRequest().body("Failed to save file");
                }
            }

            updatedUser.setProfilePhoto(imagePath);
            return ResponseEntity.ok(service.updateUser(id, updatedUser));
        } catch (RuntimeException | IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
    // Метод для сохранения файла
    private String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get("uploads"); // Папка для загрузки
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "Файл не содержит имени!";
        }
        String name = originalFilename;
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            name = originalFilename.substring(0, dotIndex);
            extension = originalFilename.substring(dotIndex);
        }
        Path filePath = uploadPath.resolve(originalFilename);
        while (Files.exists(filePath)) {
            String randomCode = String.format("_%06d", new Random().nextInt(1_000_000));
            String newFilename = name + randomCode + extension;
            filePath = uploadPath.resolve(newFilename);
            originalFilename = newFilename;
        }
        System.out.println(filePath);
        file.transferTo(filePath);
        return originalFilename;
    }

    private String deleteFile(String fileName) throws IOException {
        Path uploadPath = Paths.get("uploads");
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
