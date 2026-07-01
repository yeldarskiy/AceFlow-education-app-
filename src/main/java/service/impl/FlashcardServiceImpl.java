package kz.aceflow.service.impl;

import kz.aceflow.dao.FlashcardDao;
import kz.aceflow.model.Flashcard;
import kz.aceflow.service.FlashcardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of {@link FlashcardService}. Only this class talks to {@link FlashcardDao}.
 */
@Service
public class FlashcardServiceImpl implements FlashcardService {

    private static final Logger log = LoggerFactory.getLogger(FlashcardServiceImpl.class);

    private final FlashcardDao flashcardDao;

    @Autowired
    public FlashcardServiceImpl(FlashcardDao flashcardDao) {
        this.flashcardDao = flashcardDao;
    }

    @Override
    public List<Flashcard> getCardsByUserId(int userId, int page, int pageSize) {
        return flashcardDao.findByUserId(userId, page, pageSize);
    }

    @Override
    public List<Flashcard> getCardsDueForReview(int userId) {
        return flashcardDao.findDueForReview(userId);
    }

    @Override
    public int countCardsByUserId(int userId) {
        return flashcardDao.countByUserId(userId);
    }

    @Override
    public int countMasteredByUserId(int userId) {
        return flashcardDao.countMasteredByUserId(userId);
    }

    @Override
    public Flashcard createCard(int userId, String frontText, String backText) {
        Flashcard card = new Flashcard();
        card.setUserId(userId);
        card.setFrontText(frontText);
        card.setBackText(backText);
        card.setDifficulty("MEDIUM");
        Flashcard saved = flashcardDao.save(card);
        log.info("Flashcard {} created by user {}", saved.getCardId(), userId);
        return saved;
    }

    @Override
    public void rateCard(int cardId, String rating) {
        flashcardDao.findById(cardId).ifPresent(card -> {
            card.setTimesReviewed(card.getTimesReviewed() + 1);
            switch (rating) {
                case "easy"   -> { card.setNextReview(LocalDate.now().plusDays(7)); card.setDifficulty("EASY"); }
                case "ok"     -> { card.setNextReview(LocalDate.now().plusDays(3)); card.setDifficulty("MEDIUM"); }
                case "hard"   -> { card.setNextReview(LocalDate.now().plusDays(1)); card.setDifficulty("HARD"); }
                case "master" -> { card.setMastered(true); card.setNextReview(LocalDate.now().plusDays(30)); }
                default       -> log.warn("Unknown rating '{}' for card {}", rating, cardId);
            }
            flashcardDao.update(card);
        });
    }
}
