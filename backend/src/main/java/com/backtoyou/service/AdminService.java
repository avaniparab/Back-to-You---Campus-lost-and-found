package com.backtoyou.service;

import com.backtoyou.dto.AdminCreateUserRequest;
import com.backtoyou.dto.AdminStatsDto;
import com.backtoyou.dto.AdminUpdateUserRequest;
import com.backtoyou.dto.AdminUserDto;
import com.backtoyou.entity.Item;
import com.backtoyou.entity.User;
import com.backtoyou.repository.ItemRepository;
import com.backtoyou.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, ItemRepository itemRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAllByOrderByIdDesc().stream()
            .map(AdminUserDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminStatsDto getAdminStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(User.UserStatus.ACTIVE);
        long inactiveUsers = userRepository.countByStatus(User.UserStatus.INACTIVE);
        long studentsCount = userRepository.countByRole(User.Role.STUDENT);
        long adminsCount = userRepository.countByRole(User.Role.ADMIN);

        long totalItems = itemRepository.count();
        long totalLost = itemRepository.countByType(Item.ItemType.LOST);
        long totalFound = itemRepository.countByType(Item.ItemType.FOUND);
        long totalResolved = itemRepository.countByStatus(Item.ItemStatus.RESOLVED);

        return new AdminStatsDto(
            totalUsers,
            activeUsers,
            inactiveUsers,
            studentsCount,
            adminsCount,
            totalItems,
            totalLost,
            totalFound,
            totalResolved
        );
    }

    @Transactional
    public User createUser(AdminCreateUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty.");
        }

        String name = request.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Full Name is required.");
        }

        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        if (email.isEmpty() || !isValidEmail(email)) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }

        String password = request.getPassword();
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }

        String roleStr = (request.getRole() != null && !request.getRole().trim().isEmpty())
            ? request.getRole().trim().toUpperCase() : "STUDENT";
        if (!"STUDENT".equals(roleStr) && !"ADMIN".equals(roleStr)) {
            throw new IllegalArgumentException("Invalid user role selected.");
        }

        String statusStr = (request.getStatus() != null && !request.getStatus().trim().isEmpty())
            ? request.getStatus().trim().toUpperCase() : "ACTIVE";
        if (!"ACTIVE".equals(statusStr) && !"INACTIVE".equals(statusStr)) {
            throw new IllegalArgumentException("Invalid account status selected.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email address is already registered.");
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(User.Role.valueOf(roleStr));
        user.setStatus(User.UserStatus.valueOf(statusStr));

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Integer targetUserId, AdminUpdateUserRequest request, User currentAdmin) {
        if (targetUserId == null || targetUserId <= 0) {
            throw new IllegalArgumentException("Invalid user ID.");
        }

        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty.");
        }

        String name = request.getName() != null ? request.getName().trim() : "";
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Full Name is required.");
        }

        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        if (email.isEmpty() || !isValidEmail(email)) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }

        String roleStr = request.getRole() != null ? request.getRole().trim().toUpperCase() : "";
        if (!"STUDENT".equals(roleStr) && !"ADMIN".equals(roleStr)) {
            throw new IllegalArgumentException("Invalid user role selected.");
        }

        String statusStr = request.getStatus() != null ? request.getStatus().trim().toUpperCase() : "";
        if (!"ACTIVE".equals(statusStr) && !"INACTIVE".equals(statusStr)) {
            throw new IllegalArgumentException("Invalid account status selected.");
        }

        // Protection 1: Self-editing protections
        if (currentAdmin != null && targetUserId.equals(currentAdmin.getId())) {
            if ("INACTIVE".equals(statusStr)) {
                throw new IllegalArgumentException("You cannot deactivate your own administrator account.");
            }
            if (!"ADMIN".equals(roleStr)) {
                throw new IllegalArgumentException("You cannot remove your own administrator role.");
            }
        }

        User targetUser = userRepository.findById(targetUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Protection 2: Prevent demoting or deactivating the last active administrator
        if (targetUser.getRole() == User.Role.ADMIN && (!"ADMIN".equals(roleStr) || "INACTIVE".equals(statusStr))) {
            long activeAdminCount = userRepository.countByRoleAndStatus(User.Role.ADMIN, User.UserStatus.ACTIVE);
            if (activeAdminCount <= 1) {
                throw new IllegalArgumentException("Cannot modify the role or status of the only active administrator account.");
            }
        }

        // Check duplicate email for other users
        if (userRepository.existsByEmailAndIdNot(email, targetUserId)) {
            throw new IllegalArgumentException("Email address is already in use by another account.");
        }

        // Optional password change
        String password = request.getPassword();
        if (password != null && !password.trim().isEmpty()) {
            if (password.trim().length() < 8) {
                throw new IllegalArgumentException("New password must be at least 8 characters long.");
            }
            targetUser.setPassword(passwordEncoder.encode(password.trim()));
        }

        targetUser.setName(name);
        targetUser.setEmail(email);
        targetUser.setRole(User.Role.valueOf(roleStr));
        targetUser.setStatus(User.UserStatus.valueOf(statusStr));

        return userRepository.save(targetUser);
    }

    @Transactional
    public User updateUserStatus(Integer targetUserId, String statusInput, User currentAdmin) {
        if (targetUserId == null || targetUserId <= 0) {
            throw new IllegalArgumentException("Invalid user ID.");
        }

        String status = statusInput != null ? statusInput.trim().toUpperCase() : "";
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new IllegalArgumentException("Invalid status option.");
        }

        // Protection 1: Prevent admin from deactivating their own account
        if (currentAdmin != null && targetUserId.equals(currentAdmin.getId()) && "INACTIVE".equals(status)) {
            throw new IllegalArgumentException("You cannot deactivate your own administrator account.");
        }

        User targetUser = userRepository.findById(targetUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Protection 2: Prevent deactivating the last active administrator
        if (targetUser.getRole() == User.Role.ADMIN && "INACTIVE".equals(status)) {
            long activeAdminCount = userRepository.countByRoleAndStatus(User.Role.ADMIN, User.UserStatus.ACTIVE);
            if (activeAdminCount <= 1) {
                throw new IllegalArgumentException("Cannot deactivate the only active administrator account.");
            }
        }

        targetUser.setStatus(User.UserStatus.valueOf(status));
        return userRepository.save(targetUser);
    }

    @Transactional
    public void deleteUser(Integer targetUserId, User currentAdmin) {
        if (targetUserId == null || targetUserId <= 0) {
            throw new IllegalArgumentException("Invalid user ID.");
        }

        // Protection 1: Prevent self-deletion
        if (currentAdmin != null && targetUserId.equals(currentAdmin.getId())) {
            throw new IllegalArgumentException("You cannot delete your own administrator account.");
        }

        User targetUser = userRepository.findById(targetUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Protection 2: Prevent deleting the last remaining administrator
        if (targetUser.getRole() == User.Role.ADMIN) {
            long adminCount = userRepository.countByRole(User.Role.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalArgumentException("Cannot delete the only administrator account.");
            }
        }

        // Protection 3: Check for associated lost/found item reports
        long itemCount = itemRepository.countByUserId(targetUserId);
        if (itemCount > 0) {
            throw new IllegalArgumentException("Cannot delete user because they have associated lost/found item reports. Deactivate the user account instead.");
        }

        userRepository.delete(targetUser);
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}
