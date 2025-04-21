package com.rockettracker.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rockettracker.config.ApplicationConfiguration;
import com.rockettracker.dto.RegisterUserDto;
import com.rockettracker.dto.VerifyUserDto;
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

    public void verifyUser(VerifyUserDto input) {
        Optional<User> optionalUser = user.Repository.findByEmail(input.getEmail());
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            if(user.getVerificationCodeExpireAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("verfiication code has expired");
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpireAt(null);
                userRepository.save(user);
            }
            else{
                throw new RuntimeException("invalid verification code");
            }
        } else{
            throw new RuntimeException("User not found");
        }
    }

    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled) {
                throw new RuntimeException("account is already verified");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpireAt(LocalDateTime.now().plusHours(0.5));
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            throw new RuntimeException("user not found in repository");
        }
    }
    public void sendVerificationEmail(User user) {
        String subject = "account Verification";
        String verificationCode = user.getVerificationCode();
        String htmlMessage = "<html>"
        + "<body style=\"font-family: Arial, sans-serif;\">"
        + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
        + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
        + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
        + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
        + "<h3 style=\"color: #333;\">Verification Code:</h3>"
        + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
        + "</div>"
        + "</div>"
        + "</body>"
        + "</html>";
    }
}
