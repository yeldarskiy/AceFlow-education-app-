package kz.aceflow.dao;

import kz.aceflow.model.StudyPlan;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for {@link StudyPlan} entities.
 */
public interface StudyPlanDao {
    StudyPlan save(StudyPlan plan);
    Optional<StudyPlan> findById(int planId);
    Optional<StudyPlan> findActiveByUserId(int userId);
    List<StudyPlan> findByUserId(int userId);
    boolean update(StudyPlan plan);
    boolean deleteById(int planId);
}
