package com.example.product_service.controller;

import com.example.product_service.model.Product;
import com.example.product_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Optional<Product> optionalProduct = productService.findById(id);
        if (optionalProduct.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }

        Product product = optionalProduct.get();

        // Если у продукта указан файл
        if (product.getImagePath() != null) {
            Path filePath = Paths.get("C:\\vus\\diplomproject\\back\\product-service\\src\\main\\java\\com\\example\\product_service\\uploads", product.getImagePath());
            if (Files.exists(filePath)) {
                // Создаем ресурс для файла
                Resource fileResource = new FileSystemResource(filePath.toFile());

                // Возвращаем файл как вложение
                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + product.getImagePath() + "\"")
                        .body(fileResource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
            }
        }

        // Если у продукта нет файла, вернуть только данные продукта
        return ResponseEntity.ok(product);
    }

    @GetMapping("/user/{userId}")
    public List<Product> getProductsByUserId(@PathVariable Long userId) {
        return productService.findByUserId(userId);
    }

    // Для создания продукта с поддержкой файла
    @PostMapping
    public Product createProduct(
            @RequestParam("createdAt") LocalDateTime createdAt,
            @RequestParam("description") String description,
            @RequestParam Long userId,
            @RequestParam(required = false) MultipartFile file) throws IOException {

        // Логика сохранения файла, если он был предоставлен
        String imagePath = null;
        if (file != null && !file.isEmpty()) {
            // Сохранить файл, например, в локальную директорию
            imagePath = saveFile(file);
        }

        Product product = new Product();
        product.setCreatedAt(createdAt);
        product.setDescription(description);
        double timeCal = 50;
        product.setCalories(timeCal);
        product.setUserId(userId);
        product.setImagePath(imagePath);

        return productService.save(product);
    }

    // Для обновления продукта с поддержкой файла
    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable Long id,
            @RequestParam("description") String description,
            @RequestParam Long userId,
            @RequestParam(required = false) MultipartFile file) throws IOException {

        Product product = productService.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

        String imagePath = product.getImagePath();
        if (file != null && file.getSize() > 0) {
            deleteFile(product.getImagePath());
            imagePath = saveFile(file);
        }

        product.setDescription(description);
        double timeCal = 50;
        product.setCalories(timeCal);
        product.setUserId(userId);
        product.setImagePath(imagePath);

        return productService.save(product);
    }

    // Удаление продукта
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) throws IOException {
        Product product = productService.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.getImagePath() != null) {
            deleteFile(product.getImagePath());
        }
        productService.deleteById(id);
    }

    // Метод для сохранения файла
    private String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get("C:\\vus\\diplomproject\\back\\product-service\\src\\main\\java\\com\\example\\product_service\\uploads");
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
        Path uploadPath = Paths.get("C:\\vus\\diplomproject\\back\\product-service\\src\\main\\java\\com\\example\\product_service\\uploads");
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
