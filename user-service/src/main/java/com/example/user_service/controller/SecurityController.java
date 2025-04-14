package com.example.user_service.controller;

import com.example.user_service.dto.SigninRequest;
import com.example.user_service.dto.SignupRequest;
import com.example.user_service.model.User;
import com.example.user_service.service.UserDataDetailsService;
import com.example.user_service.service.EmailService;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.jwt.JwtCore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users/auth")
public class SecurityController {

    private UserRepository userRepository;
    private  PasswordEncoder passwordEncoder;
    private  AuthenticationManager authenticationManager;
    private JwtCore jwtCore;

    @Autowired
    private EmailService emailService;
    private final Map<String, String> verificationCodes = new HashMap<>();

    @Autowired
    private UserDataDetailsService userDataDetailsService;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    @Autowired
    public void setAuthenticationManager (AuthenticationManager authenticationManager) {
        this.authenticationManager =   authenticationManager;
    }
    @Autowired
    public void setJwtCore(JwtCore jwtCore) {
        this.jwtCore = jwtCore;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        if (userRepository.existsUserByUsername(signupRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different name");
        }
        if (userRepository.existsUserByEmail(signupRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different email");
        }
        if (verificationCodes.containsKey(signupRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different email");
        }
        String hashed = passwordEncoder.encode(signupRequest.getPassword());
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(hashed);
        User savedUser = userRepository.save(user);
        System.out.println(savedUser);
        userDataDetailsService.createUser(savedUser.getId());
        String code = emailService.sendVerificationCode(signupRequest.getEmail());
        verificationCodes.put(signupRequest.getEmail(), code);
        return ResponseEntity.ok("Success signup");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestParam String email, @RequestParam String code) {
        System.out.println(verificationCodes.containsKey(email));
        if (!verificationCodes.containsKey(email)) {
            return ResponseEntity.badRequest().body("Verification code not found. Request a new one.");
        }
        if (!verificationCodes.get(email).equals(code)) {
            return ResponseEntity.badRequest().body("Invalid verification code.");
        }
        verificationCodes.remove(email);
        return ResponseEntity.ok("Email verified successfully.");
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody SigninRequest signinRequest) {
        Optional<User> optionalUser = userRepository.findUserByUsername(signinRequest.getUsername());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        User user = optionalUser.get();
        if (verificationCodes.containsKey(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email not verified. Please check your email for the verification code.");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signinRequest.getUsername(), signinRequest.getPassword()
                    ));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtCore.generateToken(authentication);
        return ResponseEntity.ok(jwt);
    }
}
