package kz.aceflow.controller;

import jakarta.servlet.http.HttpSession;
import kz.aceflow.model.User;
import kz.aceflow.service.StudyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/studyplan")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @Autowired
    public StudyPlanController(StudyPlanService studyPlanService) {
        this.studyPlanService = studyPlanService;
    }

    @GetMapping
    public String showStudyPlan(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        model.addAttribute("user", user);
        studyPlanService.getActivePlan(user.getUserId())
                .ifPresent(plan -> model.addAttribute("plan", plan));
        model.addAttribute("plans", studyPlanService.getPlansByUserId(user.getUserId()));
        return "studyplan/index";
    }

    @PostMapping("/create")
    public String createPlan(@RequestParam String examName,
                             @RequestParam(required = false) String examType,
                             @RequestParam String examDate,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        try {
            studyPlanService.createPlanFromForm(user.getUserId(), examName, examType, examDate);
            redirectAttributes.addFlashAttribute("successKey", "plan.created.success");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorKey", e.getMessage());
        }
        return "redirect:/studyplan";
    }

    @PostMapping("/{planId}/delete")
    public String deletePlan(@PathVariable int planId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        studyPlanService.deletePlan(planId, user.getUserId());
        redirectAttributes.addFlashAttribute("successKey", "common.delete");
        return "redirect:/studyplan";
    }
}
