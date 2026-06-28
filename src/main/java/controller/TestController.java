package kz.aceflow.controller;

import jakarta.servlet.http.HttpSession;
import kz.aceflow.dao.TestDao;
import kz.aceflow.dao.TestResultDao;
import kz.aceflow.model.Test;
import kz.aceflow.model.TestResult;
import kz.aceflow.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tests")
public class TestController {

    private final TestDao testDao;
    private final TestResultDao testResultDao;

    @Autowired
    public TestController(TestDao testDao, TestResultDao testResultDao) {
        this.testDao = testDao;
        this.testResultDao = testResultDao;
    }

    @GetMapping
    public String listTests(@RequestParam(defaultValue = "0") int page,
                            HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        int pageSize = 10;
        List<Test> tests = testDao.findAll(page, pageSize);
        int total = testDao.countAll();
        int totalPages = (int) Math.ceil((double) total / pageSize);

        for (Test test : tests) {
            Optional<TestResult> best = testResultDao.findBestScoreByUserAndTest(
                    user.getUserId(), test.getTestId());
            best.ifPresent(r -> test.setBestScore(r.getScore()));
        }

        model.addAttribute("user", user);
        model.addAttribute("tests", tests);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        return "tests/index";
    }
}
