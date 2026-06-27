package kz.aceflow.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a quiz/test with associated questions.
 */
public class Test {

    private int testId;
    private Integer docId;
    private String title;
    private String testType;
    private int timeLimit;
    private String difficulty;
    private LocalDateTime createdAt;
    private List<Question> questions;

    public Test() {}

    public int getTotalQuestions() {
        return questions != null ? questions.size() : 0;
    }

    public int getTestId() { return testId; }
    public void setTestId(int testId) { this.testId = testId; }
    public Integer getDocId() { return docId; }
    public void setDocId(Integer docId) { this.docId = docId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTestType() { return testType; }
    public void setTestType(String testType) { this.testType = testType; }
    public int getTimeLimit() { return timeLimit; }
    public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}
