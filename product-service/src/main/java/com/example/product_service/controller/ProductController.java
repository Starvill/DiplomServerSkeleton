package com.example.product_service.controller;

import com.example.product_service.model.NutritionInfo;
import com.example.product_service.model.Product;
import com.example.product_service.repository.NutritionInfoRepository;
import com.example.product_service.service.NutritionInfoService;
import com.example.product_service.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.util.*;
import org.springframework.core.io.ByteArrayResource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private NutritionInfoRepository nutritionInfoRepository;

    @Autowired
    private NutritionInfoService nutritionInfoService;

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
            Path filePath = Paths.get("uploads", product.getImagePath());
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

        // Сохранение изображения (если есть)
        String imagePath = null;
        if (file != null && !file.isEmpty()) {
            imagePath = saveFile(file);
        }

        Product product = new Product();
        product.setCreatedAt(createdAt);
        product.setDescription(description);
        product.setUserId(userId);
        product.setImagePath(imagePath);

        // Сохраняем продукт до получения ID
        product = productService.save(product);

        // Обрабатываем продукты из FastAPI
        List<NutritionInfo> nutritionList = getNutritionListFromFastApi(file, product.getId());
        double totalCalories = nutritionList.stream()
                .mapToDouble(n -> n.getCalories())
                .sum();

        product.setCalories(totalCalories);
        productService.save(product);

        // Сохраняем NutritionInfo
        for (NutritionInfo info : nutritionList) {
            nutritionInfoRepository.save(info);
        }
        return product;
    }

    private List<NutritionInfo> getNutritionListFromFastApi(MultipartFile file, Long productId) throws IOException {
        String url = "http://127.0.0.1:8000/analyze";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        String outerJson = response.getBody()
                .replaceAll("(?s)```json\\s*", "")  // удаляем открытие блока
                .replaceAll("(?s)```", "")          // удаляем закрытие блока
                .trim();                            // обрезаем пробелы и переносы


        // Десериализуем строку-обёртку, внутри которой JSON (если FastAPI вернул JSON как строку)
        String innerJson = objectMapper.readValue(outerJson, String.class);

        // Теперь парсим настоящий JSON
        Map<String, Object> parsed = objectMapper.readValue(innerJson, new TypeReference<>() {});

        List<Map<String, Object>> products = (List<Map<String, Object>>) parsed.get("products");

        List<NutritionInfo> nutritionInfos = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            Map<String, Object> productMap = products.get(i);
            NutritionInfo info = new NutritionInfo();
            info.setProductDetailJsonId((long) i);// можно использовать индекс или hash продукта
            info.setName(productMap.get("name").toString());
            info.setCalories(toDouble(productMap.get("calories")));
            info.setFat(toDouble(productMap.get("fat")));
            info.setCarbohydrates(toDouble(productMap.get("carbohydrates")));
            info.setProtein(toDouble(productMap.get("proteins")));
            info.setGrams(toDouble(productMap.get("weight")));
            info.setProductId(productId);
            nutritionInfos.add(info);
        }

        return nutritionInfos;
    }

    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0;
        }
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
    @Transactional
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) throws IOException {
        Product product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Удаление NutritionInfo
        nutritionInfoService.deleteByProductId(product.getId());

        // Удаление изображения, если есть
        if (product.getImagePath() != null) {
            deleteFile(product.getImagePath());
        }

        // Удаление самого продукта
        productService.deleteById(id);
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
