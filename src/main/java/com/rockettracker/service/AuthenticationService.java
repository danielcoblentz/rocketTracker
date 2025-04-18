package com.rockettracker.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rockettracker.config.ApplicationConfiguration;
import com.rockettracker.dto.RegisterUserDto;
import com.rockettracker.dto.LoginUserDto;
import com.rockettracker.model.User;
import com.rockettracker.repository.UserRepository;

@Service
public class AuthenticationService {

    private final ApplicationConfiguration applicationConfiguration;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthenticationService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        EmailService emailService,
        ApplicationConfiguration applicationConfiguration
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.applicationConfiguration = applicationConfiguration;
    }

    public User signup(RegisterUserDto input) {
        User user = new User(
            input.getUsername(),
            input.getEmail(),
            passwordEncoder.encode(input.getPassword())
        );

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);

        sendVerificationEmail(user);

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input) {
        User user = userRepository.findByEmail(input.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified");
        }

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                input.getEmail(),
                input.getPassword()
            )
        );

        return user;
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString(); // generate random verification code
    }

    private void sendVerificationEmail(User user) {
        String subject = "Verify your account";
        String body = "Hello " + user.getUsername() + ",/n/n" +
                      "Please verify your account using the code: " + user.getVerificationCode() +
                      "/n/nThis code will expire in 15 minutes.";

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}
