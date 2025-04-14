package com.example.user_service.service;

import com.example.user_service.model.Gender;
import com.example.user_service.model.UserDataDetails;
import com.example.user_service.repository.UserDataDetailsRepository;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserDataDetailsService {

    private final UserDataDetailsRepository repository;

    public UserDataDetailsService(UserDataDetailsRepository repository) {
        this.repository = repository;
    }

    public Optional<UserDataDetails> getUserByUserId(Long id) {
        return repository.findByUserId(id);
    }

    public UserDataDetails createUser(Long id){
        UserDataDetails user = new UserDataDetails();
        user.setUserId(id);
        user.setRegistrationDate(LocalDate.now());
        return repository.save(user);
    }

    public UserDataDetails updateUser(Long id, UserDataDetails updatedUser) {
        return repository.findByUserId(id)
                .map(existingUser -> {
                    existingUser.setName(updatedUser.getName());
                    existingUser.setAge(updatedUser.getAge());
                    existingUser.setGender(updatedUser.getGender());
                    existingUser.setHeight(updatedUser.getHeight());
                    existingUser.setProfilePhoto(updatedUser.getProfilePhoto());
                    return repository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}

