package kz.aceflow.dao.impl;

import kz.aceflow.dao.AchievementDao;
import kz.aceflow.exception.DaoException;
import kz.aceflow.model.Achievement;
import kz.aceflow.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AchievementDaoImpl implements AchievementDao {

    private static final Logger log = LoggerFactory.getLogger(AchievementDaoImpl.class);
    private final ConnectionPool pool = ConnectionPool.getInstance();

    private static final String SQL_FIND_ALL =
            "SELECT achievement_id, title, description, xp_reward, category, condition_type, condition_value " +
                    "FROM achievements ORDER BY achievement_id";

    private static final String SQL_FIND_ALL_WITH_STATUS =
            "SELECT a.achievement_id, a.title, a.description, a.xp_reward, a.category, " +
                    "a.condition_type, a.condition_value, " +
                    "CASE WHEN ua.user_id IS NOT NULL THEN TRUE ELSE FALSE END AS earned, " +
                    "COALESCE(ua.is_seen, FALSE) AS is_seen, ua.earned_at " +
                    "FROM achievements a " +
                    "LEFT JOIN user_achievements ua ON a.achievement_id = ua.achievement_id AND ua.user_id = ? " +
                    "ORDER BY a.achievement_id";

    private static final String SQL_GRANT =
            "INSERT INTO user_achievements (user_id, achievement_id) VALUES (?, ?)";

    private static final String SQL_COUNT_EARNED =
            "SELECT COUNT(*) FROM user_achievements WHERE user_id = ?";

    @Override
    public List<Achievement> findAll() {
        List<Achievement> list = new ArrayList<>();
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapBaseRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find all achievements", e);
            throw new DaoException("Failed to find achievements", e);
        } finally {
            pool.releaseConnection(conn);
        }
        return list;
    }

    @Override
    public List<Achievement> findAllWithUserStatus(int userId) {
        List<Achievement> list = new ArrayList<>();
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL_WITH_STATUS)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Achievement a = mapBaseRow(rs);
                    a.setEarned(rs.getBoolean("earned"));
                    a.setSeen(rs.getBoolean("is_seen"));
                    Timestamp earnedAt = rs.getTimestamp("earned_at");
                    if (earnedAt != null) {
                        a.setEarnedAt(earnedAt.toLocalDateTime());
                    }
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find achievements for user: {}", userId, e);
            throw new DaoException("Failed to find achievements for user", e);
        } finally {
            pool.releaseConnection(conn);
        }
        return list;
    }

    @Override
    public boolean grantToUser(int userId, int achievementId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_GRANT)) {
            ps.setInt(1, userId);
            ps.setInt(2, achievementId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.warn("Achievement {} already granted or failed for user {}: {}", achievementId, userId, e.getMessage());
            return false;
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public int countEarnedByUserId(int userId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_COUNT_EARNED)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            log.error("Failed to count earned achievements for user: {}", userId, e);
            throw new DaoException("Failed to count earned achievements", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    private Achievement mapBaseRow(ResultSet rs) throws SQLException {
        Achievement a = new Achievement();
        a.setAchievementId(rs.getInt("achievement_id"));
        a.setTitle(rs.getString("title"));
        a.setDescription(rs.getString("description"));
        a.setXpReward(rs.getInt("xp_reward"));
        a.setCategory(rs.getString("category"));
        a.setConditionType(rs.getString("condition_type"));
        a.setConditionValue(rs.getInt("condition_value"));
        return a;
    }
}
