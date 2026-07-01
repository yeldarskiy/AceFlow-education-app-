package kz.aceflow.service.impl;

import kz.aceflow.dao.QuestionDao;
import kz.aceflow.dao.TestDao;
import kz.aceflow.dao.TestResultDao;
import kz.aceflow.exception.ResourceNotFoundException;
import kz.aceflow.model.Question;
import kz.aceflow.model.Test;
import kz.aceflow.model.TestResult;
import kz.aceflow.service.TestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link TestService}.
 * <p>
 * This is the only class allowed to talk to {@link TestDao}, {@link TestResultDao},
 * and {@link QuestionDao} for test-taking logic — controllers must depend only
 * on this service.
 */
@Service
public class TestServiceImpl implements TestService {

    private static final Logger log = LoggerFactory.getLogger(TestServiceImpl.class);

    private final TestDao testDao;
    private final TestResultDao testResultDao;
    private final QuestionDao questionDao;

    @Autowired
    public TestServiceImpl(TestDao testDao, TestResultDao testResultDao, QuestionDao questionDao) {
        this.testDao = testDao;
        this.testResultDao = testResultDao;
        this.questionDao = questionDao;
    }

    @Override
    public List<Test> getTestsPage(int userId, int page, int pageSize) {
        List<Test> tests = testDao.findAll(page, pageSize);
        for (Test test : tests) {
            Optional<TestResult> best = testResultDao.findBestScoreByUserAndTest(userId, test.getTestId());
            best.ifPresent(r -> test.setBestScore(r.getScore()));
        }
        return tests;
    }

    @Override
    public int countAllTests() {
        return testDao.countAll();
    }

    @Override
    public Test getTestWithQuestions(int testId) {
        Test test = testDao.findById(testId)
            .orElseThrow(() -> new ResourceNotFoundException("Test", testId));
        test.setQuestions(questionDao.findByTestId(testId));
        return test;
    }

    @Override
    public GradeResult gradeSubmission(int userId, int testId, Map<String, String> answers) {
        Test test = testDao.findById(testId)
            .orElseThrow(() -> new ResourceNotFoundException("Test", testId));
        List<Question> questions = questionDao.findByTestId(testId);

        int correct = 0;
        for (Question q : questions) {
            String answer = answers.get("q_" + q.getQuestionId());
            if (answer != null && answer.equalsIgnoreCase(q.getCorrectAnswer())) {
                correct++;
            }
        }

        double score = questions.isEmpty()
            ? 0
            : Math.round((double) correct / questions.size() * 100 * 100.0) / 100.0;

        TestResult result = new TestResult();
        result.setUserId(userId);
        result.setTestId(testId);
        result.setScore(BigDecimal.valueOf(score));
        result.setDuration(0);
        testResultDao.save(result);

        log.info("User {} completed test {} with score {}%", userId, testId, score);
        return new GradeResult(score, correct, questions.size(), test.getTitle());
    }
}
