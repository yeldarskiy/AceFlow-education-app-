package kz.aceflow.dao.impl;

import kz.aceflow.dao.QuestionDao;
import kz.aceflow.exception.DaoException;
import kz.aceflow.model.Question;
import kz.aceflow.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class QuestionDaoImpl implements QuestionDao {

    private static final Logger log = LoggerFactory.getLogger(QuestionDaoImpl.class);
    private final ConnectionPool pool = ConnectionPool.getInstance();

    private static final String SQL_FIND_BY_TEST =
            "SELECT question_id, test_id, question_text, correct_answer, " +
                    "option_a, option_b, option_c, option_d, question_type, difficulty, points, explanation " +
                    "FROM questions WHERE test_id = ? ORDER BY question_id";

    private static final String SQL_FIND_BY_ID =
            "SELECT question_id, test_id, question_text, correct_answer, " +
                    "option_a, option_b, option_c, option_d, question_type, difficulty, points, explanation " +
                    "FROM questions WHERE question_id = ?";

    private static final String SQL_SAVE =
            "INSERT INTO questions (test_id, question_text, correct_answer, " +
                    "option_a, option_b, option_c, option_d, question_type, difficulty, points, explanation) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_DELETE = "DELETE FROM questions WHERE question_id = ?";

    @Override
    public List<Question> findByTestId(int testId) {
        List<Question> questions = new ArrayList<>();
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_TEST)) {
            ps.setInt(1, testId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) questions.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find questions for test: {}", testId, e);
            throw new DaoException("Failed to find questions", e);
        } finally {
            pool.releaseConnection(conn);
        }
        return questions;
    }

    @Override
    public Optional<Question> findById(int questionId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Failed to find question: {}", questionId, e);
            throw new DaoException("Failed to find question", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public Question save(Question q) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_SAVE, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, q.getTestId());
            ps.setString(2, q.getQuestionText());
            ps.setString(3, q.getCorrectAnswer());
            ps.setString(4, q.getOptionA());
            ps.setString(5, q.getOptionB());
            ps.setString(6, q.getOptionC());
            ps.setString(7, q.getOptionD());
            ps.setString(8, q.getQuestionType() != null ? q.getQuestionType() : "MULTIPLE_CHOICE");
            ps.setString(9, q.getDifficulty() != null ? q.getDifficulty() : "MEDIUM");
            ps.setInt(10, q.getPoints() > 0 ? q.getPoints() : 10);
            ps.setString(11, q.getExplanation());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) q.setQuestionId(keys.getInt(1));
            }
            return q;
        } catch (SQLException e) {
            log.error("Failed to save question", e);
            throw new DaoException("Failed to save question", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public boolean deleteById(int questionId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, questionId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("Failed to delete question: {}", questionId, e);
            throw new DaoException("Failed to delete question", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    private Question mapRow(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setQuestionId(rs.getInt("question_id"));
        q.setTestId(rs.getInt("test_id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setCorrectAnswer(rs.getString("correct_answer"));
        q.setOptionA(rs.getString("option_a"));
        q.setOptionB(rs.getString("option_b"));
        q.setOptionC(rs.getString("option_c"));
        q.setOptionD(rs.getString("option_d"));
        q.setQuestionType(rs.getString("question_type"));
        q.setDifficulty(rs.getString("difficulty"));
        q.setPoints(rs.getInt("points"));
        q.setExplanation(rs.getString("explanation"));
        return q;
    }
}