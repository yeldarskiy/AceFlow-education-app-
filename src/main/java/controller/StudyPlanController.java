package kz.aceflow.controller;

import jakarta.servlet.http.HttpSession;
import kz.aceflow.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/studyplan")
public class StudyPlanController {

    @GetMapping
    public String showStudyPlan(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        model.addAttribute("user", user);
        return "studyplan/index";
    }
}