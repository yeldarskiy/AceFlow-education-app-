package kz.aceflow.service;

import kz.aceflow.model.Flashcard;

import java.util.List;

/**
 * Business logic for flashcard creation, study sessions, and spaced repetition.
 */
public interface FlashcardService {

    /**
     * Returns a page of a user's flashcards.
     *
     * @param userId   the owner
     * @param page     zero-based page index
     * @param pageSize number of cards per page
     * @return list of flashcards for the requested page
     */
    List<Flashcard> getCardsByUserId(int userId, int page, int pageSize);

    /**
     * Returns the cards currently due for spaced-repetition review.
     *
     * @param userId the owner
     * @return list of due flashcards (max 20)
     */
    List<Flashcard> getCardsDueForReview(int userId);

    /**
     * Total number of flashcards owned by a user.
     *
     * @param userId the owner
     * @return total card count
     */
    int countCardsByUserId(int userId);

    /**
     * Number of mastered flashcards owned by a user.
     *
     * @param userId the owner
     * @return mastered card count
     */
    int countMasteredByUserId(int userId);

    /**
     * Creates a new manually-authored flashcard.
     *
     * @param userId    the owner
     * @param frontText the question/front side
     * @param backText  the answer/back side
     * @return the persisted {@link Flashcard}
     */
    Flashcard createCard(int userId, String frontText, String backText);

    /**
     * Applies a spaced-repetition rating to a card, scheduling its next review
     * and marking it mastered when appropriate.
     *
     * @param cardId the card being rated
     * @param rating one of "hard", "ok", "easy", "master"
     */
    void rateCard(int cardId, String rating);
}
