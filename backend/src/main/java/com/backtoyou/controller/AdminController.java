package com.backtoyou.controller;

import com.backtoyou.dto.*;
import com.backtoyou.entity.User;
import com.backtoyou.repository.UserRepository;
import com.backtoyou.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;

    public AdminController(AdminService adminService, UserRepository userRepository) {
        this.adminService = adminService;
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<AdminUsersResponse> getUsers(Authentication authentication) {
        User currentAdmin = getCurrentAdmin(authentication);
        if (currentAdmin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(AdminUsersResponse.error("Forbidden. Admin access required."));
        }

        try {
            List<AdminUserDto> users = adminService.getAllUsers();
            return ResponseEntity.ok(AdminUsersResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.ok(AdminUsersResponse.error("Database error while fetching users."));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats(Authentication authentication) {
        User currentAdmin = getCurrentAdmin(authentication);
        if (currentAdmin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(AdminStatsResponse.error("Forbidden. Admin access required."));
        }

        try {
            AdminStatsDto stats = adminService.getAdminStats();
            return ResponseEntity.ok(AdminStatsResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.ok(AdminStatsResponse.error("Database error while calculating stats."));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<AdminActionResponse> createUser(
            @RequestBody AdminCreateUserRequest request,
            Authentication authentication) {
        User currentAdmin = getCurrentAdmin(authentication);
        if (currentAdmin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(AdminActionResponse.error("Forbidden. Admin access required."));
        }

        try {
            User created = adminService.createUser(request);
            return ResponseEntity.ok(AdminActionResponse.createSuccess("User created successfully.", created.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(AdminActionResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(AdminActionResponse.error("Database error while creating user."));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<AdminActionResponse> updateUser(
            @PathVariable Integer id,
            @RequestBody AdminUpdateUserRequest request,
            Authentication authentication) {
        User currentAdmin = getCurrentAdmin(authentication);
        if (currentAdmin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(AdminActionResponse.error("Forbidden. Admin access required."));
        }

        try {
            adminService.updateUser(id, request, currentAdmin);
            return ResponseEntity.ok(AdminActionResponse.success("User details updated successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(AdminActionResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(AdminActionResponse.error("Database error while updating user details."));
        }
    }

    @RequestMapping(value = "/users/{id}/status", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<AdminActionResponse> updateUserStatus(
            @PathVariable Integer id,
            @RequestBody AdminUpdateStatusRequest request,
            Authentication authentication) {
        User currentAdmin = getCurrentAdmin(authentication);
        if (currentAdmin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(AdminActionResponse.error("Forbidden. Admin access required."));
        }

        try {
            String status = request != null ? request.getStatus() : null;
            User updated = adminService.updateUserStatus(id, status, currentAdmin);
            return ResponseEntity.ok(AdminActionResponse.success("User status updated to " + updated.getStatus().name() + " successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(AdminActionResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(AdminActionResponse.error("Database error while updating status."));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<AdminActionResponse> deleteUser(
            @PathVariable Integer id,
            Authentication authentication) {
        User currentAdmin = getCurrentAdmin(authentication);
        if (currentAdmin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(AdminActionResponse.error("Forbidden. Admin access required."));
        }

        try {
            adminService.deleteUser(id, currentAdmin);
            return ResponseEntity.ok(AdminActionResponse.success("User deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(AdminActionResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(AdminActionResponse.error("Database error while deleting user."));
        }
    }

    private User getCurrentAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.ADMIN) {
            return null;
        }
        return user;
    }
}
