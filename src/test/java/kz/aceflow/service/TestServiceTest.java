package kz.aceflow.service;

import kz.aceflow.dao.QuestionDao;
import kz.aceflow.dao.TestDao;
import kz.aceflow.dao.TestResultDao;
import kz.aceflow.exception.ResourceNotFoundException;
import kz.aceflow.model.Question;
import kz.aceflow.model.TestResult;
import kz.aceflow.service.impl.TestServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestService Tests")
class TestServiceTest {

    @Mock private TestDao testDao;
    @Mock private TestResultDao testResultDao;
    @Mock private QuestionDao questionDao;
    @InjectMocks private TestServiceImpl testService;

    @Test
    @DisplayName("getTestsPage: attaches best scores")
    void getTestsPage_shouldAttachBestScores() {
        kz.aceflow.model.Test test = new kz.aceflow.model.Test();
        test.setTestId(1);
        when(testDao.findAll(0, 10)).thenReturn(List.of(test));
        TestResult result = new TestResult();
        result.setScore(BigDecimal.valueOf(85));
        when(testResultDao.findBestScoreByUserAndTest(2, 1)).thenReturn(Optional.of(result));

        List<kz.aceflow.model.Test> tests = testService.getTestsPage(2, 0, 10);

        assertEquals(1, tests.size());
        assertEquals(BigDecimal.valueOf(85), tests.get(0).getBestScore());
    }

    @Test
    @DisplayName("countAllTests: returns DAO count")
    void countAllTests_shouldReturnCount() {
        when(testDao.countAll()).thenReturn(7);
        assertEquals(7, testService.countAllTests());
    }

    @Test
    @DisplayName("getTestWithQuestions: loads test and questions")
    void getTestWithQuestions_shouldLoad() {
        kz.aceflow.model.Test test = new kz.aceflow.model.Test();
        test.setTestId(1);
        test.setTitle("Math");
        when(testDao.findById(1)).thenReturn(Optional.of(test));
        when(questionDao.findByTestId(1)).thenReturn(List.of(new Question()));

        kz.aceflow.model.Test result = testService.getTestWithQuestions(1);

        assertEquals("Math", result.getTitle());
        assertEquals(1, result.getQuestions().size());
    }

    @Test
    @DisplayName("getTestWithQuestions: throws when not found")
    void getTestWithQuestions_shouldThrow_whenNotFound() {
        when(testDao.findById(999)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> testService.getTestWithQuestions(999));
    }

    @Test
    @DisplayName("gradeSubmission: calculates score and saves result")
    void gradeSubmission_shouldGrade() {
        kz.aceflow.model.Test test = new kz.aceflow.model.Test();
        test.setTestId(1);
        test.setTitle("Quiz");
        when(testDao.findById(1)).thenReturn(Optional.of(test));

        Question q = new Question();
        q.setQuestionId(10);
        q.setCorrectAnswer("A");
        when(questionDao.findByTestId(1)).thenReturn(List.of(q));
        when(testResultDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TestService.GradeResult grade = testService.gradeSubmission(2, 1, Map.of("q_10", "A"));

        assertEquals(100.0, grade.score());
        assertEquals(1, grade.correct());
        assertEquals(1, grade.total());
        verify(testResultDao).save(any());
    }

    @Test
    @DisplayName("gradeSubmission: returns zero when no questions")
    void gradeSubmission_shouldReturnZero_whenNoQuestions() {
        kz.aceflow.model.Test test = new kz.aceflow.model.Test();
        test.setTestId(1);
        test.setTitle("Empty");
        when(testDao.findById(1)).thenReturn(Optional.of(test));
        when(questionDao.findByTestId(1)).thenReturn(List.of());
        when(testResultDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TestService.GradeResult grade = testService.gradeSubmission(2, 1, Map.of());

        assertEquals(0.0, grade.score());
        assertEquals(0, grade.correct());
    }
}
