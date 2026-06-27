package kz.aceflow.service;

import kz.aceflow.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Business logic interface for user management.
 * Handles registration, authentication, XP, and profile operations.
 */
public interface UserService {

    /**
     * Registers a new user. Validates that the email is unique,
     * hashes the password with BCrypt, and saves the user.
     *
     * @param name     full name of the user
     * @param email    unique email address
     * @param password plain-text password (will be hashed)
     * @return the created {@link User} with populated ID
     * @throws kz.aceflow.exception.AuthException if the email is already taken
     */
    User register(String name, String email, String password);

    /**
     * Authenticates a user by verifying their password against the stored hash.
     *
     * @param email    the user's email address
     * @param password the plain-text password to verify
     * @return the authenticated {@link User}
     * @throws kz.aceflow.exception.AuthException if credentials are invalid
     */
    User login(String email, String password);

    /**
     * Retrieves a user by ID.
     *
     * @param userId the user's primary key
     * @return an {@link Optional} containing the user, or empty if not found
     */
    Optional<User> findById(int userId);

    /**
     * Returns all users in the system (admin use only).
     *
     * @return list of all users
     */
    List<User> findAll();

    /**
     * Updates user profile information (name only — email changes not allowed).
     *
     * @param userId  the user to update
     * @param newName the new display name
     * @return the updated {@link User}
     */
    User updateProfile(int userId, String newName);

    /**
     * Awards XP to a user and checks for newly unlocked achievements.
     *
     * @param userId   the target user's ID
     * @param xpAmount the amount of XP to award (must be positive)
     * @return the new XP total after the award
     */
    int awardXp(int userId, int xpAmount);

    /**
     * Changes the password for a user after verifying the current password.
     *
     * @param userId          the user's ID
     * @param currentPassword the user's current plain-text password
     * @param newPassword     the new plain-text password to set
     * @throws kz.aceflow.exception.AuthException if currentPassword is incorrect
     */
    void changePassword(int userId, String currentPassword, String newPassword);
}
