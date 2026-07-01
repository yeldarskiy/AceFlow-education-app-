package kz.aceflow.service.impl;

import kz.aceflow.dao.AchievementDao;
import kz.aceflow.model.Achievement;
import kz.aceflow.service.AchievementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AchievementServiceImpl implements AchievementService {

    private static final Logger log = LoggerFactory.getLogger(AchievementServiceImpl.class);

    private final AchievementDao achievementDao;

    @Autowired
    public AchievementServiceImpl(AchievementDao achievementDao) {
        this.achievementDao = achievementDao;
    }

    @Override
    public List<Achievement> getAchievementsForUser(int userId) {
        return achievementDao.findAllWithUserStatus(userId);
    }

    @Override
    public int countEarned(int userId) {
        return achievementDao.countEarnedByUserId(userId);
    }

    @Override
    public int countTotal() {
        return achievementDao.findAll().size();
    }

    @Override
    public boolean grantAchievement(int userId, int achievementId) {
        boolean granted = achievementDao.grantToUser(userId, achievementId);
        if (granted) {
            log.info("Achievement {} granted to user {}", achievementId, userId);
        }
        return granted;
    }
}
