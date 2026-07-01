package kz.aceflow.service;

import kz.aceflow.dao.AchievementDao;
import kz.aceflow.model.Achievement;
import kz.aceflow.service.impl.AchievementServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AchievementService Tests")
class AchievementServiceTest {

    @Mock private AchievementDao achievementDao;
    @InjectMocks private AchievementServiceImpl achievementService;

    @Test
    @DisplayName("getAchievementsForUser: delegates to DAO")
    void getAchievementsForUser_shouldDelegate() {
        Achievement a = new Achievement();
        a.setTitle("First Win");
        when(achievementDao.findAllWithUserStatus(1)).thenReturn(List.of(a));

        List<Achievement> result = achievementService.getAchievementsForUser(1);

        assertEquals(1, result.size());
        verify(achievementDao).findAllWithUserStatus(1);
    }

    @Test
    @DisplayName("countEarned: returns DAO count")
    void countEarned_shouldReturnCount() {
        when(achievementDao.countEarnedByUserId(1)).thenReturn(3);
        assertEquals(3, achievementService.countEarned(1));
    }

    @Test
    @DisplayName("countTotal: returns size of all achievements")
    void countTotal_shouldReturnSize() {
        when(achievementDao.findAll()).thenReturn(List.of(new Achievement(), new Achievement()));
        assertEquals(2, achievementService.countTotal());
    }

    @Test
    @DisplayName("grantAchievement: returns true when granted")
    void grantAchievement_shouldReturnTrue() {
        when(achievementDao.grantToUser(1, 5)).thenReturn(true);
        assertTrue(achievementService.grantAchievement(1, 5));
    }

    @Test
    @DisplayName("grantAchievement: returns false when already earned")
    void grantAchievement_shouldReturnFalse() {
        when(achievementDao.grantToUser(1, 5)).thenReturn(false);
        assertFalse(achievementService.grantAchievement(1, 5));
    }
}
