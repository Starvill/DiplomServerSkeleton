package com.example.user_service.model;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name="usersData")
public class UserDataDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String name;
    private int age;
    private int weight;

    @Enumerated(EnumType.STRING) // Хранит в БД как строку ("MALE" или "FEMALE")
    private Gender gender;
    private int height; // см

    private LocalDate registrationDate; // дата регистрации
    private String profilePhoto; // фото профиля
}
