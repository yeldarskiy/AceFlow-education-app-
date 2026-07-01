package kz.aceflow.dao;

import kz.aceflow.dao.impl.StudyPlanDaoImpl;
import kz.aceflow.model.StudyPlan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StudyPlanDao Integration Tests")
class StudyPlanDaoTest extends AbstractDaoTest {

    private final StudyPlanDaoImpl dao = new StudyPlanDaoImpl();

    @Test
    @DisplayName("save and findById: persists plan")
    void save_shouldPersist() {
        StudyPlan plan = new StudyPlan();
        plan.setUserId(1);
        plan.setExamName("IELTS");
        plan.setExamType("LANGUAGE");
        plan.setExamDate(LocalDate.now().plusMonths(3));
        plan.setStartDate(LocalDate.now());
        plan.setStatus("ACTIVE");

        StudyPlan saved = dao.save(plan);

        assertTrue(saved.getPlanId() > 0);
        Optional<StudyPlan> found = dao.findById(saved.getPlanId());
        assertTrue(found.isPresent());
        assertEquals("IELTS", found.get().getExamName());
    }

    @Test
    @DisplayName("findActiveByUserId: returns active plan")
    void findActiveByUserId_shouldReturnActive() {
        StudyPlan plan = new StudyPlan();
        plan.setUserId(1);
        plan.setExamName("SAT");
        plan.setExamDate(LocalDate.now().plusMonths(2));
        plan.setStartDate(LocalDate.now());
        plan.setStatus("ACTIVE");
        dao.save(plan);

        Optional<StudyPlan> active = dao.findActiveByUserId(1);
        assertTrue(active.isPresent());
        assertEquals("SAT", active.get().getExamName());
    }

    @Test
    @DisplayName("findByUserId: returns all plans for user")
    void findByUserId_shouldReturnAll() {
        StudyPlan plan = new StudyPlan();
        plan.setUserId(1);
        plan.setExamName("GRE-" + System.nanoTime());
        plan.setExamDate(LocalDate.now().plusMonths(1));
        plan.setStartDate(LocalDate.now());
        plan.setStatus("ACTIVE");
        dao.save(plan);

        List<StudyPlan> plans = dao.findByUserId(1);
        assertFalse(plans.isEmpty());
        assertTrue(plans.stream().anyMatch(p -> p.getExamName().startsWith("GRE-")));
    }

    @Test
    @DisplayName("update: modifies plan fields")
    void update_shouldModify() {
        StudyPlan plan = new StudyPlan();
        plan.setUserId(1);
        plan.setExamName("Old");
        plan.setExamDate(LocalDate.now().plusMonths(1));
        plan.setStartDate(LocalDate.now());
        plan.setStatus("ACTIVE");
        dao.save(plan);

        plan.setExamName("New Name");
        assertTrue(dao.update(plan));

        Optional<StudyPlan> found = dao.findById(plan.getPlanId());
        assertEquals("New Name", found.get().getExamName());
    }

    @Test
    @DisplayName("deleteById: removes plan")
    void deleteById_shouldRemove() {
        StudyPlan plan = new StudyPlan();
        plan.setUserId(1);
        plan.setExamName("Temp");
        plan.setExamDate(LocalDate.now().plusMonths(1));
        plan.setStartDate(LocalDate.now());
        plan.setStatus("ACTIVE");
        dao.save(plan);
        int id = plan.getPlanId();

        assertTrue(dao.deleteById(id));
        assertTrue(dao.findById(id).isEmpty());
    }
}
