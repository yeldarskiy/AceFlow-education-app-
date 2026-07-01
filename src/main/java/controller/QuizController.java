package kz.aceflow.controller;

import jakarta.servlet.http.HttpSession;
import kz.aceflow.model.Test;
import kz.aceflow.model.User;
import kz.aceflow.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Runs the quiz-taking flow (start, submit, view result).
 * All grading and persistence logic lives in {@link TestService} —
 * this controller never touches a DAO directly.
 */
@Controller
@RequestMapping("/tests")
public class QuizController {

    private final TestService testService;

    @Autowired
    public QuizController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/{testId}/start")
    public String startTest(@PathVariable int testId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        Test test = testService.getTestWithQuestions(testId);

        model.addAttribute("user", user);
        model.addAttribute("test", test);
        model.addAttribute("questions", test.getQuestions());
        return "tests/quiz";
    }

    @PostMapping("/{testId}/submit")
    public String submitTest(@PathVariable int testId,
                             @RequestParam Map<String, String> answers,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");

        TestService.GradeResult result = testService.gradeSubmission(user.getUserId(), testId, answers);

        redirectAttributes.addFlashAttribute("score", result.score());
        redirectAttributes.addFlashAttribute("correct", result.correct());
        redirectAttributes.addFlashAttribute("total", result.total());
        redirectAttributes.addFlashAttribute("testTitle", result.testTitle());

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
