package kz.aceflow.controller;

import jakarta.servlet.http.HttpSession;
import kz.aceflow.model.User;
import kz.aceflow.service.FlashcardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles flashcard listing, study sessions, and rating.
 * All persistence and spaced-repetition logic lives in {@link FlashcardService} —
 * this controller never touches a DAO directly.
 */
@Controller
@RequestMapping("/flashcards")
public class FlashcardController {

    private final FlashcardService flashcardService;

    @Autowired
    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @GetMapping
    public String listFlashcards(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        model.addAttribute("user", user);
        model.addAttribute("cards", flashcardService.getCardsByUserId(user.getUserId(), 0, 50));
        model.addAttribute("total", flashcardService.countCardsByUserId(user.getUserId()));
        model.addAttribute("mastered", flashcardService.countMasteredByUserId(user.getUserId()));
        return "flashcards/index";
    }

    @GetMapping("/study")
    public String studyFlashcards(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        model.addAttribute("user", user);
        model.addAttribute("cards", flashcardService.getCardsDueForReview(user.getUserId()));
        return "flashcards/study";
    }

    @PostMapping("/{cardId}/rate")
    public String rateCard(@PathVariable int cardId,
                           @RequestParam String rating,
                           HttpSession session) {
        flashcardService.rateCard(cardId, rating);
        return "redirect:/flashcards/study";
    }

    @PostMapping("/create")
    public String createCard(@RequestParam String frontText,
                             @RequestParam String backText,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        flashcardService.createCard(user.getUserId(), frontText, backText);
        redirectAttributes.addFlashAttribute("successKey", "flashcards.created.success");
        return "redirect:/flashcards";
    }
}
