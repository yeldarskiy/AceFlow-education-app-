package kz.aceflow.model;

import java.time.LocalDate;

/**
 * A user-defined study goal with deadline and XP reward.
 */
public class Goal {

    private int goalId;
    private int userId;
    private String title;
    private LocalDate deadline;
    private boolean completed;
    private int xpReward;
    private String priority;

    public Goal() {}

    public int getGoalId() { return goalId; }
    public void setGoalId(int goalId) { this.goalId = goalId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
