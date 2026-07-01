package kz.aceflow.service;

import kz.aceflow.model.StudyPlan;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for exam study plans.
 */
public interface StudyPlanService {

    Optional<StudyPlan> getActivePlan(int userId);

    List<StudyPlan> getPlansByUserId(int userId);

    StudyPlan createPlan(int userId, String examName, String examType, LocalDate examDate);

    void deletePlan(int planId, int userId);
}
