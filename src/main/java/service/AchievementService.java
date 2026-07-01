package kz.aceflow.service;

import kz.aceflow.model.Achievement;

import java.util.List;

/**
 * Business logic for achievements and user progress tracking.
 */
public interface AchievementService {

    /**
     * Returns all achievements with earned/unlocked status for a user.
     */
    List<Achievement> getAchievementsForUser(int userId);

    /**
     * Number of achievements earned by the user.
     */
    int countEarned(int userId);

    /**
     * Total number of achievement definitions in the system.
     */
    int countTotal();

    /**
     * Grants an achievement to a user if not already earned.
     *
     * @return true if newly granted
     */
    boolean grantAchievement(int userId, int achievementId);
}
