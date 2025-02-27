package com.example.user_service.repository;

import com.example.user_service.model.UserDataDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDataDetailsRepository extends JpaRepository<UserDataDetails, Long> {
    Optional<UserDataDetails> findByUserId(Long userId);
}

