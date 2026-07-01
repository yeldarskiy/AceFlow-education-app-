package kz.aceflow.service;

import kz.aceflow.model.PageResult;
import kz.aceflow.model.Test;

import java.util.List;
import java.util.Map;

/**
 * Business logic for listing tests, running a quiz, and grading submissions.
 */
public interface TestService {

    /**
     * Returns a paginated list of tests with best-score metadata for the view.
     */
    PageResult<Test> getTestsPageResult(int userId, int page, int pageSize);

    List<Test> getTestsPage(int userId, int page, int pageSize);

    /**
     * Total number of tests in the system, used to compute pagination.
     *
     * @return total test count
     */
    int countAllTests();

    /**
     * Loads a single test together with its questions, ready to render a quiz.
     *
     * @param testId the test to load
     * @return the test with its questions populated
     * @throws kz.aceflow.exception.ResourceNotFoundException if no such test exists
     */
    Test getTestWithQuestions(int testId);

    /**
     * Grades a submitted quiz against the correct answers, persists the
     * result for the user, and returns the outcome.
     *
     * @param userId  the user who took the test
     * @param testId  the test being graded
     * @param answers map of "q_{questionId}" -> submitted answer letter
     * @return the grading outcome (score, correct count, total count, test title)
     */
    GradeResult gradeSubmission(int userId, int testId, Map<String, String> answers);

    /**
     * Outcome of grading a quiz submission.
     */
    record GradeResult(double score, int correct, int total, String testTitle) {}
}
