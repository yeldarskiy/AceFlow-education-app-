package kz.aceflow.dao;

import kz.aceflow.model.TestResult;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for {@link TestResult} entities.
 */
public interface TestResultDao {
    TestResult save(TestResult result);
    List<TestResult> findByUserId(int userId, int page, int pageSize);
    Optional<TestResult> findBestScoreByUserAndTest(int userId, int testId);
    double findAverageScoreByUserId(int userId);
    int countByUserId(int userId);
}
