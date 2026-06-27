package kz.aceflow.model;

import java.time.LocalDate;

/**
 * A spaced-repetition flashcard belonging to a user.
 */
public class Flashcard {

    private int cardId;
    private Integer docId;
    private int userId;
    private String frontText;
    private String backText;
    private String difficulty;
    private int timesReviewed;
    private LocalDate nextReview;
    private boolean mastered;

    public Flashcard() {}

    public int getCardId() { return cardId; }
    public void setCardId(int cardId) { this.cardId = cardId; }
    public Integer getDocId() { return docId; }
    public void setDocId(Integer docId) { this.docId = docId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getFrontText() { return frontText; }
    public void setFrontText(String frontText) { this.frontText = frontText; }
    public String getBackText() { return backText; }
    public void setBackText(String backText) { this.backText = backText; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public int getTimesReviewed() { return timesReviewed; }
    public void setTimesReviewed(int timesReviewed) { this.timesReviewed = timesReviewed; }
    public LocalDate getNextReview() { return nextReview; }
    public void setNextReview(LocalDate nextReview) { this.nextReview = nextReview; }
    public boolean isMastered() { return mastered; }
    public void setMastered(boolean mastered) { this.mastered = mastered; }
}
