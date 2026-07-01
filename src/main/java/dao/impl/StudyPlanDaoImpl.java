package kz.aceflow.dao.impl;

import kz.aceflow.dao.StudyPlanDao;
import kz.aceflow.exception.DaoException;
import kz.aceflow.model.StudyPlan;
import kz.aceflow.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class StudyPlanDaoImpl implements StudyPlanDao {

    private static final Logger log = LoggerFactory.getLogger(StudyPlanDaoImpl.class);
    private final ConnectionPool pool = ConnectionPool.getInstance();

    private static final String SQL_SAVE =
            "INSERT INTO study_plans (user_id, exam_name, exam_type, exam_date, start_date, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT plan_id, user_id, exam_name, exam_type, exam_date, start_date, status " +
                    "FROM study_plans WHERE plan_id = ?";

    private static final String SQL_FIND_ACTIVE_BY_USER =
            "SELECT plan_id, user_id, exam_name, exam_type, exam_date, start_date, status " +
                    "FROM study_plans WHERE user_id = ? AND status = 'ACTIVE' " +
                    "ORDER BY exam_date ASC LIMIT 1";

    private static final String SQL_FIND_BY_USER =
            "SELECT plan_id, user_id, exam_name, exam_type, exam_date, start_date, status " +
                    "FROM study_plans WHERE user_id = ? ORDER BY exam_date ASC";

    private static final String SQL_UPDATE =
            "UPDATE study_plans SET exam_name = ?, exam_type = ?, exam_date = ?, status = ? WHERE plan_id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM study_plans WHERE plan_id = ?";

    @Override
    public StudyPlan save(StudyPlan plan) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_SAVE, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, plan.getUserId());
            ps.setString(2, plan.getExamName());
            ps.setString(3, plan.getExamType());
            ps.setObject(4, plan.getExamDate());
            ps.setObject(5, plan.getStartDate());
            ps.setString(6, plan.getStatus() != null ? plan.getStatus() : "ACTIVE");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    plan.setPlanId(keys.getInt(1));
                }
            }
            return plan;
        } catch (SQLException e) {
            log.error("Failed to save study plan for user: {}", plan.getUserId(), e);
            throw new DaoException("Failed to save study plan", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public Optional<StudyPlan> findById(int planId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Failed to find study plan: {}", planId, e);
            throw new DaoException("Failed to find study plan", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public Optional<StudyPlan> findActiveByUserId(int userId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ACTIVE_BY_USER)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Failed to find active study plan for user: {}", userId, e);
            throw new DaoException("Failed to find active study plan", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public List<StudyPlan> findByUserId(int userId) {
        List<StudyPlan> plans = new ArrayList<>();
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USER)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    plans.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find study plans for user: {}", userId, e);
            throw new DaoException("Failed to find study plans", e);
        } finally {
            pool.releaseConnection(conn);
        }
        return plans;
    }

    @Override
    public boolean update(StudyPlan plan) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, plan.getExamName());
            ps.setString(2, plan.getExamType());
            ps.setObject(3, plan.getExamDate());
            ps.setString(4, plan.getStatus());
            ps.setInt(5, plan.getPlanId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("Failed to update study plan: {}", plan.getPlanId(), e);
            throw new DaoException("Failed to update study plan", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public boolean deleteById(int planId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, planId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("Failed to delete study plan: {}", planId, e);
            throw new DaoException("Failed to delete study plan", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    private StudyPlan mapRow(ResultSet rs) throws SQLException {
        StudyPlan plan = new StudyPlan();
        plan.setPlanId(rs.getInt("plan_id"));
        plan.setUserId(rs.getInt("user_id"));
        plan.setExamName(rs.getString("exam_name"));
        plan.setExamType(rs.getString("exam_type"));
        Date examDate = rs.getDate("exam_date");
        if (examDate != null) {
            plan.setExamDate(examDate.toLocalDate());
        }
        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            plan.setStartDate(startDate.toLocalDate());
        }
        plan.setStatus(rs.getString("status"));
        return plan;
    }
}
