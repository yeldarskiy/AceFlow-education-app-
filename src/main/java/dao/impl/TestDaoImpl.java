package kz.aceflow.dao.impl;

import kz.aceflow.dao.TestDao;
import kz.aceflow.exception.DaoException;
import kz.aceflow.model.Test;
import kz.aceflow.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TestDaoImpl implements TestDao {

    private static final Logger log = LoggerFactory.getLogger(TestDaoImpl.class);
    private final ConnectionPool pool = ConnectionPool.getInstance();

    private static final String SQL_FIND_ALL =
            "SELECT test_id, doc_id, title, test_type, time_limit, difficulty, created_at " +
                    "FROM tests ORDER BY created_at DESC LIMIT ? OFFSET ?";

    private static final String SQL_COUNT = "SELECT COUNT(*) FROM tests";

    private static final String SQL_FIND_BY_ID =
            "SELECT test_id, doc_id, title, test_type, time_limit, difficulty, created_at " +
                    "FROM tests WHERE test_id = ?";

    private static final String SQL_FIND_BY_DOC =
            "SELECT test_id, doc_id, title, test_type, time_limit, difficulty, created_at " +
                    "FROM tests WHERE doc_id = ?";

    private static final String SQL_SAVE =
            "INSERT INTO tests (doc_id, title, test_type, time_limit, difficulty) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_DELETE = "DELETE FROM tests WHERE test_id = ?";

    @Override
    public Test save(Test test) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_SAVE, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, test.getDocId());
            ps.setString(2, test.getTitle());
            ps.setString(3, test.getTestType());
            ps.setInt(4, test.getTimeLimit());
            ps.setString(5, test.getDifficulty());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) test.setTestId(keys.getInt(1));
            }
            return test;
        } catch (SQLException e) {
            log.error("Failed to save test", e);
            throw new DaoException("Failed to save test", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public Optional<Test> findById(int testId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, testId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Failed to find test: {}", testId, e);
            throw new DaoException("Failed to find test", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public List<Test> findAll(int page, int pageSize) {
        List<Test> tests = new ArrayList<>();
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, page * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tests.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find all tests", e);
            throw new DaoException("Failed to find tests", e);
        } finally {
            pool.releaseConnection(conn);
        }
        return tests;
    }

    @Override
    public List<Test> findByDocId(int docId) {
        List<Test> tests = new ArrayList<>();
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_DOC)) {
            ps.setInt(1, docId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tests.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find tests by doc: {}", docId, e);
            throw new DaoException("Failed to find tests by doc", e);
        } finally {
            pool.releaseConnection(conn);
        }
        return tests;
    }

    @Override
    public int countAll() {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_COUNT);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            log.error("Failed to count tests", e);
            throw new DaoException("Failed to count tests", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public boolean deleteById(int testId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, testId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("Failed to delete test: {}", testId, e);
            throw new DaoException("Failed to delete test", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    private Test mapRow(ResultSet rs) throws SQLException {
        Test test = new Test();
        test.setTestId(rs.getInt("test_id"));
        test.setDocId((Integer) rs.getObject("doc_id"));
        test.setTitle(rs.getString("title"));
        test.setTestType(rs.getString("test_type"));
        test.setTimeLimit(rs.getInt("time_limit"));
        test.setDifficulty(rs.getString("difficulty"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) test.setCreatedAt(ts.toLocalDateTime());
        return test;
    }
}