package kz.aceflow.dao.impl;

import kz.aceflow.dao.UserDao;
import kz.aceflow.exception.DaoException;
import kz.aceflow.model.User;
import kz.aceflow.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link UserDao}.
 * All SQL uses PreparedStatements — no string concatenation is used in queries.
 */
@Repository
public class UserDaoImpl implements UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDaoImpl.class);

    private static final String SQL_SAVE =
        "INSERT INTO users (name, email, password_hash, xp_points, streak_days, role) " +
        "VALUES (?, ?, ?, ?, ?, ?) RETURNING user_id, created_at";

    private static final String SQL_FIND_BY_ID =
        "SELECT user_id, name, email, password_hash, xp_points, streak_days, role, created_at " +
        "FROM users WHERE user_id = ?";

    private static final String SQL_FIND_BY_EMAIL =
        "SELECT user_id, name, email, password_hash, xp_points, streak_days, role, created_at " +
        "FROM users WHERE LOWER(email) = LOWER(?)";

    private static final String SQL_FIND_ALL =
        "SELECT user_id, name, email, password_hash, xp_points, streak_days, role, created_at " +
        "FROM users ORDER BY created_at DESC";

    private static final String SQL_UPDATE =
        "UPDATE users SET name = ?, xp_points = ?, streak_days = ? WHERE user_id = ?";

    private static final String SQL_UPDATE_PASSWORD =
        "UPDATE users SET password_hash = ? WHERE user_id = ?";

    private static final String SQL_ADD_XP =
        "UPDATE users SET xp_points = xp_points + ? WHERE user_id = ? RETURNING xp_points";

    private static final String SQL_INCREMENT_STREAK =
        "UPDATE users SET streak_days = streak_days + 1 WHERE user_id = ?";

    private static final String SQL_RESET_STREAK =
        "UPDATE users SET streak_days = 0 WHERE user_id = ?";

    private static final String SQL_DELETE =
        "DELETE FROM users WHERE user_id = ?";

    private static final String SQL_EXISTS_EMAIL =
        "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";

    private final ConnectionPool connectionPool = ConnectionPool.getInstance();

    @Override
    public User save(User user) {
        Connection conn = connectionPool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_SAVE)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setInt(4, user.getXpPoints());
            ps.setInt(5, user.getStreakDays());
            ps.setString(6, user.getRole() != null ? user.getRole() : "STUDENT");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setUserId(rs.getInt("user_id"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
            log.info("Saved new user: {}", user.getEmail());
            return user;
        } catch (SQLException e) {
            log.error("Failed to save user: {}", user.getEmail(), e);
            throw new DaoException("Failed to save user", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    @Override
    public Optional<User> findById(int userId) {
        Connection conn = connectionPool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find user by id: {}", userId, e);
            throw new DaoException("Failed to find user by id", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Connection conn = connectionPool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find user by email: {}", email, e);
            throw new DaoException("Failed to find user by email", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        Connection conn = connectionPool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to fetch all users", e);
            throw new DaoException("Failed to fetch all users", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
        return users;
    }

    @Override
    public boolean update(User user) {
        Connection conn = connectionPool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, user.getName());
            ps.setInt(2, user.getXpPoints());
            ps.setInt(3, user.getStreakDays());
            ps.setInt(4, user.getUserId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("Failed to update user: {}", user.getUserId(), e);
            throw new DaoException("Failed to update user", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    @Override
    public boolean updatePassword(int userId, String newHashedPw) {
        Connection conn = connectionPool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_PASSWORD)) {
            ps.setString(1, newHashedPw);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("Failed to update password for user: {}", userId, e);
            throw new DaoException("Failed to update password", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    @Override
    public int addXp(int userId, int xpAmount) {
        Connection conn = connectionPool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_ADD_XP)) {
            ps.setInt(1, xpAmount);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("xp_points");
                }
            }
        } catch (SQLException e) {
            log.error("Failed to add XP for user: {}", userId, e);
            throw new DaoException("Failed to add XP", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
        return 0;
    }

    @Override
    public void incrementStreak(int userId) {
        Connection conn = connectionPool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_INCREMENT_STREAK)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to increment streak for user: {}", userId, e);
            throw new DaoException("Failed to increment streak", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    @Override
    public void resetStreak(int userId) {
        Connection conn = connectionPool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_RESET_STREAK)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to reset streak for user: {}", userId, e);
            throw new DaoException("Failed to reset streak", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    @Override
    public boolean deleteById(int userId) {
        Connection conn = connectionPool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("Failed to delete user: {}", userId, e);
            throw new DaoException("Failed to delete user", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        Connection conn = connectionPool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("Failed to check email existence: {}", email, e);
            throw new DaoException("Failed to check email", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** Maps a single ResultSet row to a {@link User} object. */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setXpPoints(rs.getInt("xp_points"));
        user.setStreakDays(rs.getInt("streak_days"));
        user.setRole(rs.getString("role"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) user.setCreatedAt(ts.toLocalDateTime());
        return user;
    }
}
