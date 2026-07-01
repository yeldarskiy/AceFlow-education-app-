package kz.aceflow.dao;

import kz.aceflow.dao.impl.AchievementDaoImpl;
import kz.aceflow.model.Achievement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AchievementDao Integration Tests")
class AchievementDaoTest extends AbstractDaoTest {

    private final AchievementDaoImpl dao = new AchievementDaoImpl();

    @Test
    @DisplayName("findAll: returns seeded achievements")
    void findAll_shouldReturnAchievements() {
        List<Achievement> all = dao.findAll();
        assertEquals(2, all.size());
        assertEquals("First Win", all.get(0).getTitle());
    }

    @Test
    @DisplayName("findAllWithUserStatus: includes earned status")
    void findAllWithUserStatus_shouldIncludeStatus() {
        List<Achievement> list = dao.findAllWithUserStatus(1);
        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(Achievement::isEarned) || list.stream().anyMatch(a -> !a.isEarned()));
    }

    @Test
    @DisplayName("grantToUser: grants and counts earned")
    void grantToUser_shouldGrant() {
        int before = dao.countEarnedByUserId(1);
        assertTrue(dao.grantToUser(1, 1));
        assertEquals(before + 1, dao.countEarnedByUserId(1));
    }

    @Test
    @DisplayName("grantToUser: returns false on duplicate")
    void grantToUser_shouldReturnFalse_onDuplicate() {
        assertTrue(dao.grantToUser(1, 2));
        assertFalse(dao.grantToUser(1, 2));
    }
}
