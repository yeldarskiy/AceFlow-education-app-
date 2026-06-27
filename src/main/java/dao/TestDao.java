package kz.aceflow.dao;

import kz.aceflow.model.Test;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for {@link Test} entities.
 */
public interface TestDao {
    Test save(Test test);
    Optional<Test> findById(int testId);
    List<Test> findAll(int page, int pageSize);
    List<Test> findByDocId(int docId);
    int countAll();
    boolean deleteById(int testId);
}
