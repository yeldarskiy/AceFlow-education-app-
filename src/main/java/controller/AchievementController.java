package kz.aceflow.controller;

import jakarta.servlet.http.HttpSession;
import kz.aceflow.model.User;
import kz.aceflow.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    @Autowired
    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @GetMapping
    public String showAchievements(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        int userId = user.getUserId();
        model.addAttribute("user", user);
        model.addAttribute("achievements", achievementService.getAchievementsForUser(userId));
        model.addAttribute("earnedCount", achievementService.countEarned(userId));
        model.addAttribute("totalCount", achievementService.countTotal());
        return "achievements/index";
    }
}
