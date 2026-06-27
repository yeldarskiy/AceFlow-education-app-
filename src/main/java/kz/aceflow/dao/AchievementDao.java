package kz.aceflow.dao;

import kz.aceflow.model.Achievement;
import java.util.List;

/**
 * DAO interface for {@link Achievement} entities.
 */
public interface AchievementDao {
    List<Achievement> findAll();
    List<Achievement> findAllWithUserStatus(int userId);
    boolean grantToUser(int userId, int achievementId);
    int countEarnedByUserId(int userId);
}
