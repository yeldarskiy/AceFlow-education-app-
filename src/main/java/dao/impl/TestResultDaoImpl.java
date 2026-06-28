package kz.aceflow.dao.impl;

import kz.aceflow.dao.TestResultDao;
import kz.aceflow.exception.DaoException;
import kz.aceflow.model.TestResult;
import kz.aceflow.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TestResultDaoImpl implements TestResultDao {

    private static final Logger log = LoggerFactory.getLogger(TestResultDaoImpl.class);
    private final ConnectionPool pool = ConnectionPool.getInstance();

    private static final String SQL_SAVE =
            "INSERT INTO test_results (user_id, test_id, score, duration) VALUES (?, ?, ?, ?)";

    private static final String SQL_FIND_BY_USER =
            "SELECT r.result_id, r.user_id, r.test_id, r.score, r.duration, r.completed_at, t.title " +
                    "FROM test_results r JOIN tests t ON r.test_id = t.test_id " +
                    "WHERE r.user_id = ? ORDER BY r.completed_at DESC LIMIT ? OFFSET ?";

    private static final String SQL_BEST_SCORE =
            "SELECT result_id, user_id, test_id, score, duration, completed_at " +
                    "FROM test_results WHERE user_id = ? AND test_id = ? ORDER BY score DESC LIMIT 1";

    private static final String SQL_AVG_SCORE =
            "SELECT AVG(score) FROM test_results WHERE user_id = ?";

    private static final String SQL_COUNT =
            "SELECT COUNT(*) FROM test_results WHERE user_id = ?";

    @Override
    public TestResult save(TestResult result) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_SAVE, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, result.getUserId());
            ps.setInt(2, result.getTestId());
            ps.setBigDecimal(3, result.getScore());
            ps.setInt(4, result.getDuration());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) result.setResultId(keys.getInt(1));
            }
            return result;
        } catch (SQLException e) {
            log.error("Failed to save test result", e);
            throw new DaoException("Failed to save test result", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public List<TestResult> findByUserId(int userId, int page, int pageSize) {
        List<TestResult> results = new ArrayList<>();
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USER)) {
            ps.setInt(1, userId);
            ps.setInt(2, pageSize);
            ps.setInt(3, page * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find results for user: {}", userId, e);
            throw new DaoException("Failed to find test results", e);
        } finally {
            pool.releaseConnection(conn);
        }
        return results;
    }

    @Override
    public Optional<TestResult> findBestScoreByUserAndTest(int userId, int testId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_BEST_SCORE)) {
            ps.setInt(1, userId);
            ps.setInt(2, testId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Failed to find best score", e);
            throw new DaoException("Failed to find best score", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public double findAverageScoreByUserId(int userId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_AVG_SCORE)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) {
            log.error("Failed to find avg score", e);
            throw new DaoException("Failed to find avg score", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public int countByUserId(int userId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_COUNT)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            log.error("Failed to count results", e);
            throw new DaoException("Failed to count results", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    private TestResult mapRow(ResultSet rs) throws SQLException {
        TestResult r = new TestResult();
        r.setResultId(rs.getInt("result_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setTestId(rs.getInt("test_id"));
        r.setScore(rs.getBigDecimal("score"));
        r.setDuration(rs.getInt("duration"));
        Timestamp ts = rs.getTimestamp("completed_at");
        if (ts != null) r.setCompletedAt(ts.toLocalDateTime());
        try { r.setTestTitle(rs.getString("title")); } catch (SQLException ignored) {}
        return r;
    }
}
