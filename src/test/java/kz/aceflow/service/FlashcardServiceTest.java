package kz.aceflow.service;

import kz.aceflow.dao.FlashcardDao;
import kz.aceflow.model.Flashcard;
import kz.aceflow.service.impl.FlashcardServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlashcardService Tests")
class FlashcardServiceTest {

    @Mock private FlashcardDao flashcardDao;
    @InjectMocks private FlashcardServiceImpl flashcardService;

    @Test
    @DisplayName("getCardsByUserId: delegates with pagination")
    void getCardsByUserId_shouldDelegate() {
        when(flashcardDao.findByUserId(1, 0, 20)).thenReturn(List.of(new Flashcard()));
        assertEquals(1, flashcardService.getCardsByUserId(1, 0, 20).size());
    }

    @Test
    @DisplayName("getCardsDueForReview: delegates to DAO")
    void getCardsDueForReview_shouldDelegate() {
        when(flashcardDao.findDueForReview(1)).thenReturn(List.of());
        assertTrue(flashcardService.getCardsDueForReview(1).isEmpty());
    }

    @Test
    @DisplayName("countCardsByUserId: returns count")
    void countCardsByUserId_shouldReturnCount() {
        when(flashcardDao.countByUserId(1)).thenReturn(10);
        assertEquals(10, flashcardService.countCardsByUserId(1));
    }

    @Test
    @DisplayName("countMasteredByUserId: returns count")
    void countMasteredByUserId_shouldReturnCount() {
        when(flashcardDao.countMasteredByUserId(1)).thenReturn(3);
        assertEquals(3, flashcardService.countMasteredByUserId(1));
    }

    @Test
    @DisplayName("createCard: saves new card")
    void createCard_shouldSave() {
        when(flashcardDao.save(any(Flashcard.class))).thenAnswer(inv -> {
            Flashcard c = inv.getArgument(0);
            c.setCardId(1);
            return c;
        });

        Flashcard card = flashcardService.createCard(1, "Front", "Back");

        assertEquals("Front", card.getFrontText());
        assertEquals("MEDIUM", card.getDifficulty());
        verify(flashcardDao).save(any());
    }

    @Test
    @DisplayName("rateCard: updates card on easy rating")
    void rateCard_shouldUpdate_onEasy() {
        Flashcard card = new Flashcard();
        card.setCardId(1);
        card.setTimesReviewed(0);
        when(flashcardDao.findById(1)).thenReturn(Optional.of(card));
        when(flashcardDao.update(any())).thenReturn(true);

        flashcardService.rateCard(1, "easy");

        verify(flashcardDao).update(argThat(c -> "EASY".equals(c.getDifficulty())));
    }

    @Test
    @DisplayName("rateCard: marks mastered on master rating")
    void rateCard_shouldMaster_onMaster() {
        Flashcard card = new Flashcard();
        card.setCardId(1);
        when(flashcardDao.findById(1)).thenReturn(Optional.of(card));
        when(flashcardDao.update(any())).thenReturn(true);

        flashcardService.rateCard(1, "master");

        verify(flashcardDao).update(argThat(Flashcard::isMastered));
    }

    @Test
    @DisplayName("rateCard: no-op when card not found")
    void rateCard_shouldNoOp_whenNotFound() {
        when(flashcardDao.findById(999)).thenReturn(Optional.empty());
        flashcardService.rateCard(999, "easy");
        verify(flashcardDao, never()).update(any());
    }
}
