package kz.aceflow.controller;

import jakarta.servlet.http.HttpSession;
import kz.aceflow.dao.FlashcardDao;
import kz.aceflow.model.Flashcard;
import kz.aceflow.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/flashcards")
public class FlashcardController {

    private static final Logger log = LoggerFactory.getLogger(FlashcardController.class);
    private final FlashcardDao flashcardDao;

    @Autowired
    public FlashcardController(FlashcardDao flashcardDao) {
        this.flashcardDao = flashcardDao;
    }

    @GetMapping
    public String listFlashcards(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        List<Flashcard> cards = flashcardDao.findByUserId(user.getUserId(), 0, 50);
        int total = flashcardDao.countByUserId(user.getUserId());
        int mastered = flashcardDao.countMasteredByUserId(user.getUserId());
        model.addAttribute("user", user);
        model.addAttribute("cards", cards);
        model.addAttribute("total", total);
        model.addAttribute("mastered", mastered);
        return "flashcards/index";
    }

    @GetMapping("/study")
    public String studyFlashcards(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        List<Flashcard> cards = flashcardDao.findDueForReview(user.getUserId());
        model.addAttribute("user", user);
        model.addAttribute("cards", cards);
        return "flashcards/study";
    }

    @PostMapping("/{cardId}/rate")
    public String rateCard(@PathVariable int cardId,
                           @RequestParam String rating,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        flashcardDao.findById(cardId).ifPresent(card -> {
            card.setTimesReviewed(card.getTimesReviewed() + 1);
            switch (rating) {
                case "easy" -> { card.setNextReview(LocalDate.now().plusDays(7)); card.setDifficulty("EASY"); }
                case "ok"   -> { card.setNextReview(LocalDate.now().plusDays(3)); card.setDifficulty("MEDIUM"); }
                case "hard" -> { card.setNextReview(LocalDate.now().plusDays(1)); card.setDifficulty("HARD"); }
                case "master" -> { card.setMastered(true); card.setNextReview(LocalDate.now().plusDays(30)); }
            }
            flashcardDao.update(card);
        });
        return "redirect:/flashcards/study";
    }

    @PostMapping("/create")
    public String createCard(@RequestParam String frontText,
                             @RequestParam String backText,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        Flashcard card = new Flashcard();
        card.setUserId(user.getUserId());
        card.setFrontText(frontText);
        card.setBackText(backText);
        card.setDifficulty("MEDIUM");
        flashcardDao.save(card);
        redirectAttributes.addFlashAttribute("successMessage", "Flashcard created!");
        return "redirect:/flashcards";
    }
}