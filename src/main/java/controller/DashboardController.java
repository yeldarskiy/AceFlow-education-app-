package kz.aceflow.controller;

import jakarta.servlet.http.HttpSession;
import kz.aceflow.model.User;
import kz.aceflow.service.GoalService;
import kz.aceflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Serves the main dashboard page with user stats summary.
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final GoalService goalService;

    @Autowired
    public DashboardController(UserService userService, GoalService goalService) {
        this.userService = userService;
        this.goalService = goalService;
    }

    @GetMapping
    public String showDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        model.addAttribute("user", user);
        model.addAttribute("goals", goalService.getGoalsByUserId(user.getUserId()));
        return "dashboard/index";
    }
}
