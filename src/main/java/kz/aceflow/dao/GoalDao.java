package kz.aceflow.dao;

import kz.aceflow.model.Goal;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for {@link Goal} entities.
 */
public interface GoalDao {
    Goal save(Goal goal);
    Optional<Goal> findById(int goalId);
    List<Goal> findByUserId(int userId);
    boolean update(Goal goal);
    boolean markCompleted(int goalId);
    boolean deleteById(int goalId);
}
