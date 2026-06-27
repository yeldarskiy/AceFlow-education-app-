package kz.aceflow.dao;

import kz.aceflow.model.Document;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for {@link Document} entities.
 */
public interface DocumentDao {
    Document save(Document document);
    Optional<Document> findById(int docId);
    List<Document> findByUserId(int userId, int page, int pageSize);
    int countByUserId(int userId);
    boolean deleteById(int docId);
}
