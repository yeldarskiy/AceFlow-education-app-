package kz.aceflow.service.impl;

import kz.aceflow.dao.UserDao;
import kz.aceflow.exception.AuthException;
import kz.aceflow.exception.ResourceNotFoundException;
import kz.aceflow.model.User;
import kz.aceflow.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link UserService} with BCrypt password hashing
 * and business rule enforcement.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final int BCRYPT_ROUNDS = 12;

    private final UserDao userDao;

    @Autowired
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User register(String name, String email, String password) {
        if (userDao.existsByEmail(email)) {
            log.warn("Registration attempt with existing email: {}", email);
            throw new AuthException("auth.email.taken");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.toLowerCase().trim());
        user.setPasswordHash(hashedPassword);
        user.setXpPoints(0);
        user.setStreakDays(0);
        user.setRole("STUDENT");

        User saved = userDao.save(user);
        log.info("New user registered: {}", saved.getEmail());
        return saved;
    }

    @Override
    public User registerWithConfirmation(String name, String email, String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            throw new AuthException("validation.password.mismatch");
        }
        return register(name, email, password);
    }

    @Override
    public User login(String email, String password) {
        Optional<User> userOpt = userDao.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("Login attempt for non-existent email: {}", email);
            throw new AuthException("auth.login.error");
        }

        User user = userOpt.get();
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            log.warn("Invalid password attempt for email: {}", email);
            throw new AuthException("auth.login.error");
        }

        log.info("User logged in: {}", email);
        return user;
    }

    @Override
    public Optional<User> findById(int userId) {
        return userDao.findById(userId);
    }

    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }

    @Override
    public User updateProfile(int userId, String newName) {
        User user = userDao.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setName(newName.trim());
        userDao.update(user);
        log.info("Updated profile for user: {}", userId);
        return user;
    }

    @Override
    public int awardXp(int userId, int xpAmount) {
        if (xpAmount <= 0) {
            throw new IllegalArgumentException("XP amount must be positive, got: " + xpAmount);
        }
        int newTotal = userDao.addXp(userId, xpAmount);
        log.debug("Awarded {} XP to user {}. New total: {}", xpAmount, userId, newTotal);
        return newTotal;
    }

    @Override
    public void changePassword(int userId, String currentPassword, String newPassword) {
        User user = userDao.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!BCrypt.checkpw(currentPassword, user.getPasswordHash())) {
            throw new AuthException("Current password is incorrect");
        }

        String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
        userDao.updatePassword(userId, newHash);
        log.info("Password changed for user: {}", userId);
    }
}
