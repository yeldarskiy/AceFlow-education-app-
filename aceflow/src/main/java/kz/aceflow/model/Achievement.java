package kz.aceflow.model;

import java.time.LocalDateTime;

/**
 * An achievement definition with optional earned-by-user metadata.
 */
public class Achievement {

    private int achievementId;
    private String title;
    private String description;
    private int xpReward;
    private String category;
    private String conditionType;
    private int conditionValue;

    // Populated when loaded for a specific user
    private boolean earned;
    private boolean seen;
    private LocalDateTime earnedAt;

    public Achievement() {}

    public int getAchievementId() { return achievementId; }
    public void setAchievementId(int achievementId) { this.achievementId = achievementId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }
    public int getConditionValue() { return conditionValue; }
    public void setConditionValue(int conditionValue) { this.conditionValue = conditionValue; }
    public boolean isEarned() { return earned; }
    public void setEarned(boolean earned) { this.earned = earned; }
    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }
    public LocalDateTime getEarnedAt() { return earnedAt; }
    public void setEarnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; }
}
