package com.backtoyou.service;

import com.backtoyou.dto.LoginRequest;
import com.backtoyou.dto.RegisterRequest;
import com.backtoyou.dto.UserDto;
import com.backtoyou.entity.User;
import com.backtoyou.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern VIVA_EMAIL_PATTERN = Pattern.compile("^.+@viva-technology\\.org$", Pattern.CASE_INSENSITIVE);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full Name is required.");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email address is required.");
        }

        String email = request.getEmail().trim();
        if (!VIVA_EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email address must end with @viva-technology.org domain.");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }

        if (request.getConfirmPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and Confirm Password do not match.");
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Email address is already registered.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.STUDENT); // Public registration strictly assigns STUDENT
        user.setStatus(User.UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Please enter your password.");
        }

        String email = request.getEmail().trim();
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOptional.get().getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        User user = userOptional.get();

        if (user.getStatus() == User.UserStatus.INACTIVE) {
            throw new IllegalStateException("Your account has been deactivated. Please contact the administrator.");
        }

        return user;
    }
}
