package com.example.user_service.controller;

import com.example.user_service.service.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users/secured")
public class MainController {

    @GetMapping("/user")
    public Map<String, String> userAccess(Principal principal) {
        if (principal == null) {
            return null;
        }

        Map<String, String> response = new HashMap<>();
        response.put("username", principal.getName());

        // Получение ID пользователя через SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // Предполагаем, что ID пользователя содержится в userDetails.getUsername() или в другой части
            String userId = extractUserId(userDetails);
            response.put("id", userId);
        } else {
            response.put("id", "unknown"); // Если ID не найден
        }

        return response;
    }

    private String extractUserId(UserDetails userDetails) {
        if (userDetails instanceof UserDetailsImpl) {
            Long id = ((UserDetailsImpl) userDetails).getId();
            return id != null ? id.toString() : "unknown";
        }
        return "unknown";
    }
}
