package kz.aceflow.dao;

import kz.aceflow.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for {@link User} entities.
 * All implementations must use PreparedStatements to prevent SQL injection.
 */
public interface UserDao {

    /**
     * Persists a new user to the database.
     *
     * @param user the user to save; password must already be hashed
     * @return the saved user with the generated {@code userId} populated
     */
    User save(User user);

    /**
     * Finds a user by their primary key.
     *
     * @param userId the user's ID
     * @return an {@link Optional} containing the user, or empty if not found
     */
    Optional<User> findById(int userId);

    /**
     * Finds a user by their unique email address.
     *
     * @param email the user's email (case-insensitive lookup)
     * @return an {@link Optional} containing the user, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Returns all registered users.
     *
     * @return list of all users, never null
     */
    List<User> findAll();

    /**
     * Updates mutable fields of an existing user (name, xpPoints, streakDays).
     *
     * @param user the user with updated values; {@code userId} must match an existing record
     * @return {@code true} if exactly one row was updated
     */
    boolean update(User user);

    /**
     * Updates the stored password hash for a user.
     *
     * @param userId      the target user's ID
     * @param newHashedPw the new BCrypt-hashed password
     * @return {@code true} if the password was updated successfully
     */
    boolean updatePassword(int userId, String newHashedPw);

    /**
     * Adds XP points to a user and returns the new total.
     *
     * @param userId   the target user's ID
     * @param xpAmount the positive amount of XP to add
     * @return the updated total XP for the user
     */
    int addXp(int userId, int xpAmount);

    /**
     * Increments the user's streak counter by 1.
     *
     * @param userId the target user's ID
     */
    void incrementStreak(int userId);

    /**
     * Resets the user's streak counter to 0.
     *
     * @param userId the target user's ID
     */
    void resetStreak(int userId);

    /**
     * Permanently deletes a user and all cascade-dependent records.
     *
     * @param userId the ID of the user to delete
     * @return {@code true} if the user was deleted
     */
    boolean deleteById(int userId);

    /**
     * Checks whether an email address is already registered.
     *
     * @param email the email to check
     * @return {@code true} if the email exists in the database
     */
    boolean existsByEmail(String email);
}
