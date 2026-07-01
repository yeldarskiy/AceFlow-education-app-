package kz.aceflow.service;

import kz.aceflow.dao.StudyPlanDao;
import kz.aceflow.exception.ResourceNotFoundException;
import kz.aceflow.model.StudyPlan;
import kz.aceflow.service.impl.StudyPlanServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudyPlanService Tests")
class StudyPlanServiceTest {

    @Mock private StudyPlanDao studyPlanDao;
    @InjectMocks private StudyPlanServiceImpl studyPlanService;

    @Test
    @DisplayName("getActivePlan: returns active plan")
    void getActivePlan_shouldReturnPlan() {
        StudyPlan plan = new StudyPlan();
        plan.setExamName("IELTS");
        when(studyPlanDao.findActiveByUserId(1)).thenReturn(Optional.of(plan));

        Optional<StudyPlan> result = studyPlanService.getActivePlan(1);

        assertTrue(result.isPresent());
        assertEquals("IELTS", result.get().getExamName());
    }

    @Test
    @DisplayName("createPlan: saves plan with defaults")
    void createPlan_shouldSave() {
        when(studyPlanDao.save(any(StudyPlan.class))).thenAnswer(inv -> {
            StudyPlan p = inv.getArgument(0);
            p.setPlanId(1);
            return p;
        });

        StudyPlan result = studyPlanService.createPlan(1, "IELTS", "LANGUAGE", LocalDate.now().plusMonths(2));

        assertEquals("IELTS", result.getExamName());
        assertEquals("ACTIVE", result.getStatus());
        verify(studyPlanDao).save(any());
    }

    @Test
    @DisplayName("createPlan: throws when exam name blank")
    void createPlan_shouldThrow_whenNameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> studyPlanService.createPlan(1, "  ", null, LocalDate.now()));
    }

    @Test
    @DisplayName("createPlan: throws when exam date null")
    void createPlan_shouldThrow_whenDateNull() {
        assertThrows(IllegalArgumentException.class,
                () -> studyPlanService.createPlan(1, "IELTS", null, null));
    }

    @Test
    @DisplayName("createPlanFromForm: parses date string")
    void createPlanFromForm_shouldParseDate() {
        when(studyPlanDao.save(any(StudyPlan.class))).thenAnswer(inv -> inv.getArgument(0));

        StudyPlan result = studyPlanService.createPlanFromForm(
                1, "IELTS", "LANGUAGE", LocalDate.now().plusMonths(1).toString());

        assertEquals("IELTS", result.getExamName());
    }

    @Test
    @DisplayName("createPlanFromForm: throws when date blank")
    void createPlanFromForm_shouldThrow_whenDateBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studyPlanService.createPlanFromForm(1, "IELTS", null, ""));

        assertEquals("validation.exam.date.required", ex.getMessage());
    }

    @Test
    @DisplayName("deletePlan: deletes when owner")
    void deletePlan_shouldDelete_whenOwner() {
        StudyPlan plan = new StudyPlan();
        plan.setUserId(1);
        plan.setPlanId(5);
        when(studyPlanDao.findById(5)).thenReturn(Optional.of(plan));
        when(studyPlanDao.deleteById(5)).thenReturn(true);

        assertDoesNotThrow(() -> studyPlanService.deletePlan(5, 1));
        verify(studyPlanDao).deleteById(5);
    }

    @Test
    @DisplayName("deletePlan: throws when not owner")
    void deletePlan_shouldThrow_whenNotOwner() {
        StudyPlan plan = new StudyPlan();
        plan.setUserId(1);
        when(studyPlanDao.findById(5)).thenReturn(Optional.of(plan));

        assertThrows(SecurityException.class, () -> studyPlanService.deletePlan(5, 99));
    }

    @Test
    @DisplayName("deletePlan: throws when not found")
    void deletePlan_shouldThrow_whenNotFound() {
        when(studyPlanDao.findById(999)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> studyPlanService.deletePlan(999, 1));
    }

    @Test
    @DisplayName("getPlansByUserId: returns list from DAO")
    void getPlansByUserId_shouldReturnList() {
        when(studyPlanDao.findByUserId(1)).thenReturn(List.of(new StudyPlan()));
        assertEquals(1, studyPlanService.getPlansByUserId(1).size());
    }
}
