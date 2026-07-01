package kz.aceflow.service.impl;

import kz.aceflow.dao.GoalDao;
import kz.aceflow.exception.ResourceNotFoundException;
import kz.aceflow.model.Goal;
import kz.aceflow.service.GoalService;
import kz.aceflow.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link GoalService}.
 */
@Service
public class GoalServiceImpl implements GoalService {

    private static final Logger log = LoggerFactory.getLogger(GoalServiceImpl.class);

    private final GoalDao goalDao;
    private final UserService userService;

    @Autowired
    public GoalServiceImpl(GoalDao goalDao, UserService userService) {
        this.goalDao = goalDao;
        this.userService = userService;
    }

    @Override
    public Goal createGoal(int userId, String title, LocalDate deadline, String priority) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTitle(title.trim());
        goal.setDeadline(deadline);
        goal.setPriority(priority != null ? priority : "MEDIUM");
        goal.setXpReward(calculateXpReward(priority));
        return goalDao.save(goal);
    }

    @Override
    public Goal createGoalFromForm(int userId, String title, String deadlineStr, String priority) {
        LocalDate deadline = (deadlineStr != null && !deadlineStr.isBlank())
                ? LocalDate.parse(deadlineStr) : null;
        return createGoal(userId, title, deadline, priority);
    }

    @Override
    public List<Goal> getGoalsByUserId(int userId) {
        return goalDao.findByUserId(userId);
    }

    @Override
    public void completeGoal(int goalId, int userId) {
        Goal goal = goalDao.findById(goalId)
            .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));

        if (goal.getUserId() != userId) {
            throw new SecurityException("User " + userId + " does not own goal " + goalId);
        }

        if (!goal.isCompleted()) {
            goalDao.markCompleted(goalId);
            userService.awardXp(userId, goal.getXpReward());
            log.info("Goal {} completed by user {}. XP awarded: {}", goalId, userId, goal.getXpReward());
        }
    }

    @Override
    public void deleteGoal(int goalId, int userId) {
        Goal goal = goalDao.findById(goalId)
            .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));

        if (goal.getUserId() != userId) {
            throw new SecurityException("User " + userId + " does not own goal " + goalId);
        }

        goalDao.deleteById(goalId);
        log.info("Goal {} deleted by user {}", goalId, userId);
    }

    private int calculateXpReward(String priority) {
        if (priority == null) return 100;
        return switch (priority.toUpperCase()) {
            case "HIGH"   -> 300;
            case "MEDIUM" -> 150;
            case "LOW"    -> 75;
            default       -> 100;
        };
    }
}
