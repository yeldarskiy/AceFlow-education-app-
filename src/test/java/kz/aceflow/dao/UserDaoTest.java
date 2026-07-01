package kz.aceflow.dao;

import kz.aceflow.dao.impl.UserDaoImpl;
import kz.aceflow.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserDao Integration Tests")
class UserDaoTest extends AbstractDaoTest {

    private final UserDaoImpl dao = new UserDaoImpl();

    @Test
    @DisplayName("findByEmail: returns seeded user")
    void findByEmail_shouldReturnUser() {
        Optional<User> user = dao.findByEmail("test@example.kz");
        assertTrue(user.isPresent());
        assertEquals("Test User", user.get().getName());
    }

    @Test
    @DisplayName("save: creates new user")
    void save_shouldCreateUser() {
        User user = new User();
        user.setName("New Student");
        user.setEmail("new" + System.nanoTime() + "@test.kz");
        user.setPasswordHash("$2a$10$hash");
        user.setXpPoints(0);
        user.setStreakDays(0);
        user.setRole("STUDENT");

        User saved = dao.save(user);
        assertTrue(saved.getUserId() > 0);
        assertTrue(dao.existsByEmail(saved.getEmail()));
    }

    @Test
    @DisplayName("addXp: increments XP total")
    void addXp_shouldIncrement() {
        Optional<User> user = dao.findByEmail("test@example.kz");
        assertTrue(user.isPresent());
        int before = user.get().getXpPoints();
        int after = dao.addXp(user.get().getUserId(), 50);
        assertEquals(before + 50, after);
    }

    @Test
    @DisplayName("findAll: returns users")
    void findAll_shouldReturnUsers() {
        List<User> users = dao.findAll();
        assertFalse(users.isEmpty());
    }
}
