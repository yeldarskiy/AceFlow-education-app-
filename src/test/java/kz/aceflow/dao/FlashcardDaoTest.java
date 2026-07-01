package kz.aceflow.dao;

import kz.aceflow.dao.impl.FlashcardDaoImpl;
import kz.aceflow.model.Flashcard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FlashcardDao Integration Tests")
class FlashcardDaoTest extends AbstractDaoTest {

    private final FlashcardDaoImpl dao = new FlashcardDaoImpl();

    @Test
    @DisplayName("save and findByUserId: paginated cards")
    void save_shouldPersistAndFind() {
        Flashcard card = new Flashcard();
        card.setUserId(1);
        card.setFrontText("Capital of France?");
        card.setBackText("Paris");
        card.setDifficulty("MEDIUM");

        Flashcard saved = dao.save(card);
        assertTrue(saved.getCardId() > 0);

        List<Flashcard> cards = dao.findByUserId(1, 0, 10);
        assertTrue(cards.stream().anyMatch(c -> "Paris".equals(c.getBackText())));
        assertEquals(1, dao.countByUserId(1));
    }

    @Test
    @DisplayName("update: modifies card")
    void update_shouldModify() {
        Flashcard card = new Flashcard();
        card.setUserId(1);
        card.setFrontText("Q");
        card.setBackText("A");
        dao.save(card);
        card.setDifficulty("MEDIUM");
        card.setMastered(true);

        assertTrue(dao.update(card));
        Optional<Flashcard> found = dao.findById(card.getCardId());
        assertTrue(found.get().isMastered());
        assertEquals(1, dao.countMasteredByUserId(1));
    }

    @Test
    @DisplayName("deleteById: removes card")
    void deleteById_shouldRemove() {
        Flashcard card = new Flashcard();
        card.setUserId(1);
        card.setFrontText("Temp");
        card.setBackText("Temp");
        dao.save(card);
        int id = card.getCardId();

        assertTrue(dao.deleteById(id));
        assertTrue(dao.findById(id).isEmpty());
    }
}
