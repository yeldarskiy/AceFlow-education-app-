package kz.aceflow.dao.impl;

import kz.aceflow.dao.GoalDao;
import kz.aceflow.exception.DaoException;
import kz.aceflow.model.Goal;
import kz.aceflow.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class GoalDaoImpl implements GoalDao {

    private static final Logger log = LoggerFactory.getLogger(GoalDaoImpl.class);
    private final ConnectionPool pool = ConnectionPool.getInstance();

    private static final String SQL_SAVE =
            "INSERT INTO goals (user_id, title, deadline, is_completed, xp_reward, priority) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT goal_id, user_id, title, deadline, is_completed, xp_reward, priority " +
                    "FROM goals WHERE goal_id = ?";

    private static final String SQL_FIND_BY_USER =
            "SELECT goal_id, user_id, title, deadline, is_completed, xp_reward, priority " +
                    "FROM goals WHERE user_id = ? ORDER BY is_completed ASC, deadline IS NULL ASC, deadline ASC";

    private static final String SQL_UPDATE =
            "UPDATE goals SET title = ?, deadline = ?, priority = ? WHERE goal_id = ?";

    private static final String SQL_MARK_COMPLETED =
            "UPDATE goals SET is_completed = TRUE WHERE goal_id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM goals WHERE goal_id = ?";

    @Override
    public Goal save(Goal goal) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_SAVE, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, goal.getUserId());
            ps.setString(2, goal.getTitle());
            ps.setObject(3, goal.getDeadline());
            ps.setBoolean(4, goal.isCompleted());
            ps.setInt(5, goal.getXpReward());
            ps.setString(6, goal.getPriority() != null ? goal.getPriority() : "MEDIUM");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) goal.setGoalId(keys.getInt(1));
            }
            return goal;
        } catch (SQLException e) {
            log.error("Failed to save goal for user: {}", goal.getUserId(), e);
            throw new DaoException("Failed to save goal", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public Optional<Goal> findById(int goalId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, goalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Failed to find goal: {}", goalId, e);
            throw new DaoException("Failed to find goal", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public List<Goal> findByUserId(int userId) {
        List<Goal> goals = new ArrayList<>();
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USER)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) goals.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find goals for user: {}", userId, e);
            throw new DaoException("Failed to find goals", e);
        } finally {
            pool.releaseConnection(conn);
        }
        return goals;
    }

    @Override
    public boolean update(Goal goal) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, goal.getTitle());
            ps.setObject(2, goal.getDeadline());
            ps.setString(3, goal.getPriority());
            ps.setInt(4, goal.getGoalId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("Failed to update goal: {}", goal.getGoalId(), e);
            throw new DaoException("Failed to update goal", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public boolean markCompleted(int goalId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_MARK_COMPLETED)) {
            ps.setInt(1, goalId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("Failed to mark goal completed: {}", goalId, e);
            throw new DaoException("Failed to mark goal completed", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public boolean deleteById(int goalId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, goalId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("Failed to delete goal: {}", goalId, e);
            throw new DaoException("Failed to delete goal", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    private Goal mapRow(ResultSet rs) throws SQLException {
        Goal goal = new Goal();
        goal.setGoalId(rs.getInt("goal_id"));
        goal.setUserId(rs.getInt("user_id"));
        goal.setTitle(rs.getString("title"));
        Date deadline = rs.getDate("deadline");
        if (deadline != null) goal.setDeadline(deadline.toLocalDate());
        goal.setCompleted(rs.getBoolean("is_completed"));
        goal.setXpReward(rs.getInt("xp_reward"));
        goal.setPriority(rs.getString("priority"));
        return goal;
    }
}