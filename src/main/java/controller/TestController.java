package kz.aceflow.controller;

import jakarta.servlet.http.HttpSession;
import kz.aceflow.model.User;
import kz.aceflow.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Lists available tests. All data access goes through {@link TestService} —
 * this controller never touches a DAO directly.
 */
@Controller
@RequestMapping("/tests")
public class TestController {

    private final TestService testService;

    @Autowired
    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping
    public String listTests(@RequestParam(defaultValue = "0") int page,
                            HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        var result = testService.getTestsPageResult(user.getUserId(), page, 10);

        model.addAttribute("user", user);
        model.addAttribute("tests", result.items());
        model.addAttribute("currentPage", result.currentPage());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("totalItems", result.totalItems());
        return "tests/index";
    }
}
