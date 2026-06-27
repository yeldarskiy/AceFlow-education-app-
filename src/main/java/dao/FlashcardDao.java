package kz.aceflow.dao;

import kz.aceflow.model.Flashcard;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for {@link Flashcard} entities.
 */
public interface FlashcardDao {
    Flashcard save(Flashcard card);
    Optional<Flashcard> findById(int cardId);
    List<Flashcard> findByUserId(int userId, int page, int pageSize);
    List<Flashcard> findDueForReview(int userId);
    boolean update(Flashcard card);
    boolean deleteById(int cardId);
    int countByUserId(int userId);
    int countMasteredByUserId(int userId);
}
