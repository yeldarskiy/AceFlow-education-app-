package kz.aceflow.service.impl;

import kz.aceflow.dao.StudyPlanDao;
import kz.aceflow.exception.ResourceNotFoundException;
import kz.aceflow.model.StudyPlan;
import kz.aceflow.service.StudyPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StudyPlanServiceImpl implements StudyPlanService {

    private static final Logger log = LoggerFactory.getLogger(StudyPlanServiceImpl.class);

    private final StudyPlanDao studyPlanDao;

    @Autowired
    public StudyPlanServiceImpl(StudyPlanDao studyPlanDao) {
        this.studyPlanDao = studyPlanDao;
    }

    @Override
    public Optional<StudyPlan> getActivePlan(int userId) {
        return studyPlanDao.findActiveByUserId(userId);
    }

    @Override
    public List<StudyPlan> getPlansByUserId(int userId) {
        return studyPlanDao.findByUserId(userId);
    }

    @Override
    public StudyPlan createPlan(int userId, String examName, String examType, LocalDate examDate) {
        if (examName == null || examName.isBlank()) {
            throw new IllegalArgumentException("validation.exam.name.required");
        }
        if (examDate == null) {
            throw new IllegalArgumentException("validation.exam.date.required");
        }

        StudyPlan plan = new StudyPlan();
        plan.setUserId(userId);
        plan.setExamName(examName.trim());
        plan.setExamType(examType != null ? examType : "GENERAL");
        plan.setExamDate(examDate);
        plan.setStartDate(LocalDate.now());
        plan.setStatus("ACTIVE");

        StudyPlan saved = studyPlanDao.save(plan);
        log.info("Study plan created for user {}: {}", userId, examName);
        return saved;
    }

    @Override
    public StudyPlan createPlanFromForm(int userId, String examName, String examType, String examDateStr) {
        if (examDateStr == null || examDateStr.isBlank()) {
            throw new IllegalArgumentException("validation.exam.date.required");
        }
        return createPlan(userId, examName, examType, LocalDate.parse(examDateStr));
    }

    @Override
    public void deletePlan(int planId, int userId) {
        StudyPlan plan = studyPlanDao.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("StudyPlan", planId));

        if (plan.getUserId() != userId) {
            throw new SecurityException("User " + userId + " does not own plan " + planId);
        }

        studyPlanDao.deleteById(planId);
        log.info("Study plan {} deleted by user {}", planId, userId);
    }
}
