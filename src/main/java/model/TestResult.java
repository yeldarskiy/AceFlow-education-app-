package kz.aceflow.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Stores the result of a user's completed test attempt.
 */
public class TestResult {

    private int resultId;
    private int userId;
    private int testId;
    private BigDecimal score;
    private int duration;
    private LocalDateTime completedAt;
    private String testTitle;

    public TestResult() {}

    public String getScoreLabel() {
        if (score == null) return "--";
        double val = score.doubleValue();
        if (val >= 80) return "good";
        if (val >= 60) return "mid";
        return "bad";
    }

    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getTestId() { return testId; }
    public void setTestId(int testId) { this.testId = testId; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getTestTitle() { return testTitle; }
    public void setTestTitle(String testTitle) { this.testTitle = testTitle; }
}
