package kz.aceflow.service;

import kz.aceflow.model.Goal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for managing user goals.
 */
public interface GoalService {

    /**
     * Creates a new goal for a user.
     *
     * @param userId   the owner's user ID
     * @param title    the goal description
     * @param deadline optional target date
     * @param priority "LOW", "MEDIUM", or "HIGH"
     * @return the persisted {@link Goal}
     */
    Goal createGoal(int userId, String title, LocalDate deadline, String priority);

    /**
     * Parses optional deadline from form input and delegates to {@link #createGoal}.
     */
    Goal createGoalFromForm(int userId, String title, String deadlineStr, String priority);

    /**
     * Retrieves all goals for a user, ordered by completion status then deadline.
     *
     * @param userId the user's ID
     * @return list of goals
     */
    List<Goal> getGoalsByUserId(int userId);

    /**
     * Marks a goal as completed and awards XP to the user.
     *
     * @param goalId the goal to complete
     * @param userId the user completing the goal (ownership check)
     */
    void completeGoal(int goalId, int userId);

    /**
     * Deletes a goal after verifying ownership.
     *
     * @param goalId the goal to delete
     * @param userId the requesting user's ID
     */
    void deleteGoal(int goalId, int userId);
}
