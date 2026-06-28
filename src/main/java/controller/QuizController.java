package kz.aceflow.controller;

import jakarta.servlet.http.HttpSession;
import kz.aceflow.dao.QuestionDao;
import kz.aceflow.dao.TestDao;
import kz.aceflow.dao.TestResultDao;
import kz.aceflow.model.Question;
import kz.aceflow.model.Test;
import kz.aceflow.model.TestResult;
import kz.aceflow.model.User;
import kz.aceflow.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tests")
public class QuizController {

    private static final Logger log = LoggerFactory.getLogger(QuizController.class);

    private final TestDao testDao;
    private final TestResultDao testResultDao;
    private final QuestionDao questionDao;

    @Autowired
    public QuizController(TestDao testDao, TestResultDao testResultDao, QuestionDao questionDao) {
        this.testDao = testDao;
        this.testResultDao = testResultDao;
        this.questionDao = questionDao;
    }

    @GetMapping("/{testId}/start")
    public String startTest(@PathVariable int testId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        Test test = testDao.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test", testId));
        List<Question> questions = questionDao.findByTestId(testId);
        test.setQuestions(questions);

        model.addAttribute("user", user);
        model.addAttribute("test", test);
        model.addAttribute("questions", questions);
        return "tests/quiz";
    }

    @PostMapping("/{testId}/submit")
    public String submitTest(@PathVariable int testId,
                             @RequestParam Map<String, String> answers,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
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

        double score = questions.isEmpty() ? 0 :
                Math.round((double) correct / questions.size() * 100 * 100.0) / 100.0;

        TestResult result = new TestResult();
        result.setUserId(user.getUserId());
        result.setTestId(testId);
        result.setScore(BigDecimal.valueOf(score));
        result.setDuration(0);
        testResultDao.save(result);

        redirectAttributes.addFlashAttribute("score", score);
        redirectAttributes.addFlashAttribute("correct", correct);
        redirectAttributes.addFlashAttribute("total", questions.size());
        redirectAttributes.addFlashAttribute("testTitle", test.getTitle());

        log.info("User {} completed test {} with score {}%", user.getUserId(), testId, score);
        return "redirect:/tests/" + testId + "/result";
    }

    @GetMapping("/{testId}/result")
    public String showResult(@PathVariable int testId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        model.addAttribute("user", user);
        model.addAttribute("testId", testId);
        return "tests/result";
    }
}