package kz.aceflow.dao.impl;

import kz.aceflow.dao.FlashcardDao;
import kz.aceflow.exception.DaoException;
import kz.aceflow.model.Flashcard;
import kz.aceflow.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FlashcardDaoImpl implements FlashcardDao {

    private static final Logger log = LoggerFactory.getLogger(FlashcardDaoImpl.class);
    private final ConnectionPool pool = ConnectionPool.getInstance();

    private static final String SQL_SAVE =
            "INSERT INTO flashcards (doc_id, user_id, front_text, back_text, difficulty) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT card_id, doc_id, user_id, front_text, back_text, difficulty, times_reviewed, next_review, is_mastered " +
                    "FROM flashcards WHERE card_id = ?";

    private static final String SQL_FIND_BY_USER =
            "SELECT card_id, doc_id, user_id, front_text, back_text, difficulty, times_reviewed, next_review, is_mastered " +
                    "FROM flashcards WHERE user_id = ? ORDER BY is_mastered ASC, times_reviewed ASC LIMIT ? OFFSET ?";

    private static final String SQL_DUE_FOR_REVIEW =
            "SELECT card_id, doc_id, user_id, front_text, back_text, difficulty, times_reviewed, next_review, is_mastered " +
                    "FROM flashcards WHERE user_id = ? AND (next_review IS NULL OR next_review <= CURDATE()) AND is_mastered = FALSE LIMIT 20";

    private static final String SQL_UPDATE =
            "UPDATE flashcards SET difficulty = ?, times_reviewed = ?, next_review = ?, is_mastered = ? WHERE card_id = ?";

    private static final String SQL_DELETE = "DELETE FROM flashcards WHERE card_id = ?";

    private static final String SQL_COUNT = "SELECT COUNT(*) FROM flashcards WHERE user_id = ?";

    private static final String SQL_COUNT_MASTERED =
            "SELECT COUNT(*) FROM flashcards WHERE user_id = ? AND is_mastered = TRUE";

    @Override
    public Flashcard save(Flashcard card) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_SAVE, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, card.getDocId());
            ps.setInt(2, card.getUserId());
            ps.setString(3, card.getFrontText());
            ps.setString(4, card.getBackText());
            ps.setString(5, card.getDifficulty() != null ? card.getDifficulty() : "MEDIUM");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) card.setCardId(keys.getInt(1));
            }
            return card;
        } catch (SQLException e) {
            log.error("Failed to save flashcard", e);
            throw new DaoException("Failed to save flashcard", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public Optional<Flashcard> findById(int cardId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, cardId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Failed to find flashcard: {}", cardId, e);
            throw new DaoException("Failed to find flashcard", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public List<Flashcard> findByUserId(int userId, int page, int pageSize) {
        List<Flashcard> cards = new ArrayList<>();
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USER)) {
            ps.setInt(1, userId);
            ps.setInt(2, pageSize);
            ps.setInt(3, page * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) cards.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find flashcards for user: {}", userId, e);
            throw new DaoException("Failed to find flashcards", e);
        } finally {
            pool.releaseConnection(conn);
        }
        return cards;
    }

    @Override
    public List<Flashcard> findDueForReview(int userId) {
        List<Flashcard> cards = new ArrayList<>();
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_DUE_FOR_REVIEW)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) cards.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find due flashcards for user: {}", userId, e);
            throw new DaoException("Failed to find due flashcards", e);
        } finally {
            pool.releaseConnection(conn);
        }
        return cards;
    }

    @Override
    public boolean update(Flashcard card) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, card.getDifficulty());
            ps.setInt(2, card.getTimesReviewed());
            ps.setObject(3, card.getNextReview());
            ps.setBoolean(4, card.isMastered());
            ps.setInt(5, card.getCardId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("Failed to update flashcard: {}", card.getCardId(), e);
            throw new DaoException("Failed to update flashcard", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public boolean deleteById(int cardId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, cardId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("Failed to delete flashcard: {}", cardId, e);
            throw new DaoException("Failed to delete flashcard", e);
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
            log.error("Failed to count flashcards", e);
            throw new DaoException("Failed to count flashcards", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public int countMasteredByUserId(int userId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_COUNT_MASTERED)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            log.error("Failed to count mastered flashcards", e);
            throw new DaoException("Failed to count mastered flashcards", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    private Flashcard mapRow(ResultSet rs) throws SQLException {
        Flashcard card = new Flashcard();
        card.setCardId(rs.getInt("card_id"));
        card.setDocId((Integer) rs.getObject("doc_id"));
        card.setUserId(rs.getInt("user_id"));
        card.setFrontText(rs.getString("front_text"));
        card.setBackText(rs.getString("back_text"));
        card.setDifficulty(rs.getString("difficulty"));
        card.setTimesReviewed(rs.getInt("times_reviewed"));
        Date nextReview = rs.getDate("next_review");
        if (nextReview != null) card.setNextReview(nextReview.toLocalDate());
        card.setMastered(rs.getBoolean("is_mastered"));
        return card;
    }
}