package kz.aceflow.controller;

import jakarta.servlet.http.HttpSession;
import kz.aceflow.model.Goal;
import kz.aceflow.model.User;
import kz.aceflow.service.GoalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Manages CRUD operations for user goals. Uses POST-Redirect-GET.
 */
@Controller
@RequestMapping("/goals")
public class GoalController {

    private static final Logger log = LoggerFactory.getLogger(GoalController.class);
    private final GoalService goalService;

    @Autowired
    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public String listGoals(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        List<Goal> goals = goalService.getGoalsByUserId(user.getUserId());
        model.addAttribute("goals", goals);
        model.addAttribute("user", user);
        return "goals/index";
    }

    @PostMapping("/create")
    public String createGoal(@RequestParam String title,
                             @RequestParam(required = false) String deadline,
                             @RequestParam(defaultValue = "MEDIUM") String priority,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        LocalDate deadlineDate = (deadline != null && !deadline.isBlank())
            ? LocalDate.parse(deadline) : null;
        goalService.createGoal(user.getUserId(), title, deadlineDate, priority);
        redirectAttributes.addFlashAttribute("successKey", "goals.added");
        return "redirect:/goals";
    }

    @PostMapping("/{goalId}/complete")
    public String completeGoal(@PathVariable("goalId") int goalId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        goalService.completeGoal(goalId, user.getUserId());
        redirectAttributes.addFlashAttribute("successKey", "goals.completed");
        return "redirect:/goals";
    }

    @PostMapping("/{goalId}/delete")
    public String deleteGoal(@PathVariable("goalId") int goalId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        goalService.deleteGoal(goalId, user.getUserId());
        redirectAttributes.addFlashAttribute("successKey", "common.delete");
        return "redirect:/goals";
    }
}
