package com.backtoyou.service;

import com.backtoyou.dto.AdminCreateUserRequest;
import com.backtoyou.dto.AdminStatsDto;
import com.backtoyou.dto.AdminUpdateUserRequest;
import com.backtoyou.dto.AdminUserDto;
import com.backtoyou.entity.Item;
import com.backtoyou.entity.User;
import com.backtoyou.repository.ItemRepository;
import com.backtoyou.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private User currentAdmin;
    private User studentUser;
    private User otherAdmin;

    @BeforeEach
    void setUp() {
        currentAdmin = new User(1, "Admin One", "admin1@viva-technology.org", "hash1",
                User.Role.ADMIN, User.UserStatus.ACTIVE, LocalDateTime.now());
        studentUser = new User(2, "Student One", "student1@viva-technology.org", "hash2",
                User.Role.STUDENT, User.UserStatus.ACTIVE, LocalDateTime.now());
        otherAdmin = new User(3, "Admin Two", "admin2@viva-technology.org", "hash3",
                User.Role.ADMIN, User.UserStatus.ACTIVE, LocalDateTime.now());
    }

    @Test
    @DisplayName("getAllUsers maps users safely to AdminUserDto")
    void testGetAllUsers() {
        when(userRepository.findAllByOrderByIdDesc()).thenReturn(List.of(currentAdmin, studentUser));

        List<AdminUserDto> users = adminService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals("Admin One", users.get(0).getName());
        assertEquals("ADMIN", users.get(0).getRole());
        assertEquals("ACTIVE", users.get(0).getStatus());
        assertNotNull(users.get(0).getCreatedAt());
    }

    @Test
    @DisplayName("getAdminStats calculates metrics accurately")
    void testGetAdminStats() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByStatus(User.UserStatus.ACTIVE)).thenReturn(8L);
        when(userRepository.countByStatus(User.UserStatus.INACTIVE)).thenReturn(2L);
        when(userRepository.countByRole(User.Role.STUDENT)).thenReturn(7L);
        when(userRepository.countByRole(User.Role.ADMIN)).thenReturn(3L);

        when(itemRepository.count()).thenReturn(15L);
        when(itemRepository.countByType(Item.ItemType.LOST)).thenReturn(9L);
        when(itemRepository.countByType(Item.ItemType.FOUND)).thenReturn(6L);
        when(itemRepository.countByStatus(Item.ItemStatus.RESOLVED)).thenReturn(5L);

        AdminStatsDto stats = adminService.getAdminStats();

        assertEquals(10L, stats.getTotalUsers());
        assertEquals(8L, stats.getActiveUsers());
        assertEquals(2L, stats.getInactiveUsers());
        assertEquals(7L, stats.getStudentsCount());
        assertEquals(3L, stats.getAdminsCount());
        assertEquals(15L, stats.getTotalItems());
        assertEquals(9L, stats.getTotalLost());
        assertEquals(6L, stats.getTotalFound());
        assertEquals(5L, stats.getTotalResolved());
    }

    @Test
    @DisplayName("createUser succeeds with valid data")
    void testCreateUserSuccess() {
        AdminCreateUserRequest req = new AdminCreateUserRequest(
                "New User", "new@viva-technology.org", "Password123!", "STUDENT", "ACTIVE");

        when(userRepository.existsByEmail("new@viva-technology.org")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(99);
            return u;
        });

        User created = adminService.createUser(req);

        assertNotNull(created);
        assertEquals(99, created.getId());
        assertEquals("New User", created.getName());
        assertEquals("hashed_pass", created.getPassword());
        assertEquals(User.Role.STUDENT, created.getRole());
        assertEquals(User.UserStatus.ACTIVE, created.getStatus());
    }

    @Test
    @DisplayName("createUser validates duplicate email")
    void testCreateUserDuplicateEmail() {
        AdminCreateUserRequest req = new AdminCreateUserRequest(
                "New User", "existing@viva-technology.org", "Password123!", "STUDENT", "ACTIVE");
        when(userRepository.existsByEmail("existing@viva-technology.org")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminService.createUser(req));
        assertEquals("Email address is already registered.", ex.getMessage());
    }

    @Test
    @DisplayName("createUser validates short password")
    void testCreateUserShortPassword() {
        AdminCreateUserRequest req = new AdminCreateUserRequest(
                "New User", "valid@viva-technology.org", "short", "STUDENT", "ACTIVE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminService.createUser(req));
        assertEquals("Password must be at least 8 characters long.", ex.getMessage());
    }

    @Test
    @DisplayName("createUser validates invalid role")
    void testCreateUserInvalidRole() {
        AdminCreateUserRequest req = new AdminCreateUserRequest(
                "New User", "valid@viva-technology.org", "Password123!", "MODERATOR", "ACTIVE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminService.createUser(req));
        assertEquals("Invalid user role selected.", ex.getMessage());
    }

    @Test
    @DisplayName("self-deactivation is rejected in updateUserStatus")
    void testSelfDeactivationProtectionStatus() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateUserStatus(1, "INACTIVE", currentAdmin));
        assertEquals("You cannot deactivate your own administrator account.", ex.getMessage());
    }

    @Test
    @DisplayName("self-deactivation is rejected in updateUser")
    void testSelfDeactivationProtectionUpdate() {
        AdminUpdateUserRequest req = new AdminUpdateUserRequest(
                "Admin One", "admin1@viva-technology.org", "ADMIN", "INACTIVE", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateUser(1, req, currentAdmin));
        assertEquals("You cannot deactivate your own administrator account.", ex.getMessage());
    }

    @Test
    @DisplayName("self-demotion is rejected in updateUser")
    void testSelfDemotionProtection() {
        AdminUpdateUserRequest req = new AdminUpdateUserRequest(
                "Admin One", "admin1@viva-technology.org", "STUDENT", "ACTIVE", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateUser(1, req, currentAdmin));
        assertEquals("You cannot remove your own administrator role.", ex.getMessage());
    }

    @Test
    @DisplayName("last active admin cannot be deactivated")
    void testLastActiveAdminProtection() {
        when(userRepository.findById(3)).thenReturn(Optional.of(otherAdmin));
        when(userRepository.countByRoleAndStatus(User.Role.ADMIN, User.UserStatus.ACTIVE)).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateUserStatus(3, "INACTIVE", currentAdmin));
        assertEquals("Cannot deactivate the only active administrator account.", ex.getMessage());
    }

    @Test
    @DisplayName("last active admin cannot be demoted")
    void testLastActiveAdminDemoteProtection() {
        when(userRepository.findById(3)).thenReturn(Optional.of(otherAdmin));
        when(userRepository.countByRoleAndStatus(User.Role.ADMIN, User.UserStatus.ACTIVE)).thenReturn(1L);

        AdminUpdateUserRequest req = new AdminUpdateUserRequest(
                "Admin Two", "admin2@viva-technology.org", "STUDENT", "ACTIVE", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateUser(3, req, currentAdmin));
        assertEquals("Cannot modify the role or status of the only active administrator account.", ex.getMessage());
    }

    @Test
    @DisplayName("self-deletion is rejected in deleteUser")
    void testSelfDeletionProtection() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                adminService.deleteUser(1, currentAdmin));
        assertEquals("You cannot delete your own administrator account.", ex.getMessage());
    }

    @Test
    @DisplayName("last remaining admin cannot be deleted")
    void testLastRemainingAdminDeleteProtection() {
        when(userRepository.findById(3)).thenReturn(Optional.of(otherAdmin));
        when(userRepository.countByRole(User.Role.ADMIN)).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                adminService.deleteUser(3, currentAdmin));
        assertEquals("Cannot delete the only administrator account.", ex.getMessage());
    }

    @Test
    @DisplayName("user with associated items cannot be deleted")
    void testUserWithItemsDeleteProtection() {
        when(userRepository.findById(2)).thenReturn(Optional.of(studentUser));
        when(itemRepository.countByUserId(2)).thenReturn(3L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                adminService.deleteUser(2, currentAdmin));
        assertEquals("Cannot delete user because they have associated lost/found item reports. Deactivate the user account instead.", ex.getMessage());
    }

    @Test
    @DisplayName("deleteUser succeeds when valid")
    void testDeleteUserSuccess() {
        when(userRepository.findById(2)).thenReturn(Optional.of(studentUser));
        when(itemRepository.countByUserId(2)).thenReturn(0L);

        assertDoesNotThrow(() -> adminService.deleteUser(2, currentAdmin));
        verify(userRepository, times(1)).delete(studentUser);
    }
}
