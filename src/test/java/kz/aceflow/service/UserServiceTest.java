package kz.aceflow.service;

import kz.aceflow.dao.UserDao;
import kz.aceflow.exception.AuthException;
import kz.aceflow.exception.ResourceNotFoundException;
import kz.aceflow.model.User;
import kz.aceflow.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserServiceImpl}.
 * All DAO dependencies are mocked with Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId(1);
        sampleUser.setName("Yeldar Bazarbay");
        sampleUser.setEmail("yeldar@example.kz");
        // BCrypt hash of "password123"
        sampleUser.setPasswordHash("$2a$12$KIXjD4f2N3M8T5X2vR9oOuWbA1cD4gHjIlMnPqRsTuVwXyZ012345");
        sampleUser.setXpPoints(0);
        sampleUser.setRole("STUDENT");
    }

    // ── Registration tests ─────────────────────────────────────────────

    @Test
    @DisplayName("register: should create user when email is unique")
    void register_shouldCreateUser_whenEmailIsUnique() {
        when(userDao.existsByEmail(anyString())).thenReturn(false);
        when(userDao.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUserId(1);
            return u;
        });

        User result = userService.register("Yeldar", "yeldar@example.kz", "password123");

        assertNotNull(result);
        assertEquals("Yeldar", result.getName());
        assertEquals("yeldar@example.kz", result.getEmail());
        assertNotEquals("password123", result.getPasswordHash(), "Password must be hashed");
        verify(userDao).save(any(User.class));
    }

    @Test
    @DisplayName("register: should throw AuthException when email is already taken")
    void register_shouldThrowAuthException_whenEmailTaken() {
        when(userDao.existsByEmail("yeldar@example.kz")).thenReturn(true);

        AuthException ex = assertThrows(AuthException.class,
            () -> userService.register("Yeldar", "yeldar@example.kz", "password123"));

        assertEquals("auth.email.taken", ex.getMessage());
        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("register: should assign STUDENT role by default")
    void register_shouldAssignStudentRole() {
        when(userDao.existsByEmail(anyString())).thenReturn(false);
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register("Test", "test@test.kz", "pass1234");
        assertEquals("STUDENT", result.getRole());
    }

    @Test
    @DisplayName("registerWithConfirmation: should delegate when passwords match")
    void registerWithConfirmation_shouldRegister_whenPasswordsMatch() {
        when(userDao.existsByEmail(anyString())).thenReturn(false);
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.registerWithConfirmation(
                "Yeldar", "yeldar@example.kz", "password123", "password123");

        assertEquals("yeldar@example.kz", result.getEmail());
    }

    @Test
    @DisplayName("registerWithConfirmation: should throw when passwords mismatch")
    void registerWithConfirmation_shouldThrow_whenPasswordsMismatch() {
        AuthException ex = assertThrows(AuthException.class,
                () -> userService.registerWithConfirmation(
                        "Yeldar", "yeldar@example.kz", "password123", "different"));

        assertEquals("validation.password.mismatch", ex.getMessage());
        verify(userDao, never()).save(any());
    }

    // ── Login tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("login: should throw AuthException for non-existent email")
    void login_shouldThrowAuthException_whenEmailNotFound() {
        when(userDao.findByEmail("unknown@example.kz")).thenReturn(Optional.empty());

        assertThrows(AuthException.class,
            () -> userService.login("unknown@example.kz", "any"));
    }

    @Test
    @DisplayName("login: should throw AuthException for wrong password")
    void login_shouldThrowAuthException_whenPasswordWrong() {
        when(userDao.findByEmail("yeldar@example.kz")).thenReturn(Optional.of(sampleUser));

        assertThrows(AuthException.class,
            () -> userService.login("yeldar@example.kz", "wrongpassword"));
    }

    // ── findById tests ─────────────────────────────────────────────────

    @Test
    @DisplayName("findById: should return user when exists")
    void findById_shouldReturnUser_whenExists() {
        when(userDao.findById(1)).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getUserId());
    }

    @Test
    @DisplayName("findById: should return empty when user not found")
    void findById_shouldReturnEmpty_whenNotFound() {
        when(userDao.findById(999)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(999);

        assertFalse(result.isPresent());
    }

    // ── findAll tests ──────────────────────────────────────────────────

    @Test
    @DisplayName("findAll: should return all users from DAO")
    void findAll_shouldReturnAllUsers() {
        List<User> users = List.of(sampleUser, new User());
        when(userDao.findAll()).thenReturn(users);

        List<User> result = userService.findAll();

        assertEquals(2, result.size());
        verify(userDao).findAll();
    }

    // ── awardXp tests ──────────────────────────────────────────────────

    @Test
    @DisplayName("awardXp: should return new XP total")
    void awardXp_shouldReturnNewTotal() {
        when(userDao.addXp(1, 100)).thenReturn(1100);

        int result = userService.awardXp(1, 100);

        assertEquals(1100, result);
        verify(userDao).addXp(1, 100);
    }

    @Test
    @DisplayName("awardXp: should throw IllegalArgumentException for non-positive XP")
    void awardXp_shouldThrow_whenXpNotPositive() {
        assertThrows(IllegalArgumentException.class,
            () -> userService.awardXp(1, 0));
        assertThrows(IllegalArgumentException.class,
            () -> userService.awardXp(1, -50));
        verify(userDao, never()).addXp(anyInt(), anyInt());
    }

    // ── updateProfile tests ────────────────────────────────────────────

    @Test
    @DisplayName("updateProfile: should update user name successfully")
    void updateProfile_shouldUpdateName() {
        when(userDao.findById(1)).thenReturn(Optional.of(sampleUser));
        when(userDao.update(any())).thenReturn(true);

        User result = userService.updateProfile(1, "New Name");

        assertEquals("New Name", result.getName());
        verify(userDao).update(any());
    }

    @Test
    @DisplayName("updateProfile: should throw ResourceNotFoundException when user not found")
    void updateProfile_shouldThrow_whenUserNotFound() {
        when(userDao.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> userService.updateProfile(999, "Name"));
    }

    // ── User model tests ───────────────────────────────────────────────

    @Test
    @DisplayName("User.getLevel: should compute level from XP")
    void getLevel_shouldComputeCorrectly() {
        User user = new User();
        user.setXpPoints(0);
        assertEquals(1, user.getLevel());
        user.setXpPoints(999);
        assertEquals(1, user.getLevel());
        user.setXpPoints(1000);
        assertEquals(2, user.getLevel());
        user.setXpPoints(5500);
        assertEquals(6, user.getLevel());
    }

    @Test
    @DisplayName("User.getInitials: should return correct initials")
    void getInitials_shouldReturnCorrectly() {
        User user = new User();
        user.setName("Yeldar Bazarbay");
        assertEquals("YB", user.getInitials());

        user.setName("Aizat");
        assertEquals("A", user.getInitials());
    }
}
