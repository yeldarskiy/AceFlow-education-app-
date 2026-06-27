package kz.aceflow.service;

import kz.aceflow.dao.GoalDao;
import kz.aceflow.exception.ResourceNotFoundException;
import kz.aceflow.model.Goal;
import kz.aceflow.service.impl.GoalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GoalServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GoalService Tests")
class GoalServiceTest {

    @Mock
    private GoalDao goalDao;

    @Mock
    private UserService userService;

    @InjectMocks
    private GoalServiceImpl goalService;

    private Goal sampleGoal;

    @BeforeEach
    void setUp() {
        sampleGoal = new Goal();
        sampleGoal.setGoalId(1);
        sampleGoal.setUserId(2);
        sampleGoal.setTitle("Master 200 flashcards");
        sampleGoal.setXpReward(300);
        sampleGoal.setCompleted(false);
        sampleGoal.setPriority("MEDIUM");
    }

    // ── createGoal ─────────────────────────────────────────────────────

    @Test
    @DisplayName("createGoal: should save and return goal with correct XP for HIGH priority")
    void createGoal_shouldAssignHighXp_whenHighPriority() {
        when(goalDao.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));

        Goal result = goalService.createGoal(2, "Important goal", LocalDate.now().plusDays(7), "HIGH");

        assertEquals(300, result.getXpReward());
        assertEquals("HIGH", result.getPriority());
        verify(goalDao).save(any());
    }

    @Test
    @DisplayName("createGoal: should assign MEDIUM priority when null")
    void createGoal_shouldDefaultToMediumPriority_whenNull() {
        when(goalDao.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));

        Goal result = goalService.createGoal(2, "Some goal", null, null);

        assertEquals("MEDIUM", result.getPriority());
    }

    @Test
    @DisplayName("createGoal: should trim title whitespace")
    void createGoal_shouldTrimTitle() {
        when(goalDao.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));

        Goal result = goalService.createGoal(2, "  Goal with spaces  ", null, "LOW");

        assertEquals("Goal with spaces", result.getTitle());
    }

    // ── getGoalsByUserId ───────────────────────────────────────────────

    @Test
    @DisplayName("getGoalsByUserId: should return goals from DAO")
    void getGoalsByUserId_shouldReturnGoals() {
        List<Goal> goals = List.of(sampleGoal, new Goal());
        when(goalDao.findByUserId(2)).thenReturn(goals);

        List<Goal> result = goalService.getGoalsByUserId(2);

        assertEquals(2, result.size());
        verify(goalDao).findByUserId(2);
    }

    @Test
    @DisplayName("getGoalsByUserId: should return empty list when no goals")
    void getGoalsByUserId_shouldReturnEmpty_whenNoGoals() {
        when(goalDao.findByUserId(99)).thenReturn(List.of());

        List<Goal> result = goalService.getGoalsByUserId(99);

        assertTrue(result.isEmpty());
    }

    // ── completeGoal ───────────────────────────────────────────────────

    @Test
    @DisplayName("completeGoal: should mark completed and award XP")
    void completeGoal_shouldMarkAndAwardXp() {
        when(goalDao.findById(1)).thenReturn(Optional.of(sampleGoal));
        when(goalDao.markCompleted(1)).thenReturn(true);
        when(userService.awardXp(2, 300)).thenReturn(300);

        assertDoesNotThrow(() -> goalService.completeGoal(1, 2));

        verify(goalDao).markCompleted(1);
        verify(userService).awardXp(2, 300);
    }

    @Test
    @DisplayName("completeGoal: should not award XP when goal already completed")
    void completeGoal_shouldSkip_whenAlreadyCompleted() {
        sampleGoal.setCompleted(true);
        when(goalDao.findById(1)).thenReturn(Optional.of(sampleGoal));

        goalService.completeGoal(1, 2);

        verify(goalDao, never()).markCompleted(anyInt());
        verify(userService, never()).awardXp(anyInt(), anyInt());
    }

    @Test
    @DisplayName("completeGoal: should throw ResourceNotFoundException when goal not found")
    void completeGoal_shouldThrow_whenGoalNotFound() {
        when(goalDao.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> goalService.completeGoal(999, 2));
    }

    @Test
    @DisplayName("completeGoal: should throw SecurityException when user does not own goal")
    void completeGoal_shouldThrow_whenUserDoesNotOwnGoal() {
        when(goalDao.findById(1)).thenReturn(Optional.of(sampleGoal));

        assertThrows(SecurityException.class,
            () -> goalService.completeGoal(1, 999)); // user 999 ≠ owner 2
    }

    // ── deleteGoal ─────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteGoal: should delete when user is owner")
    void deleteGoal_shouldDelete_whenOwner() {
        when(goalDao.findById(1)).thenReturn(Optional.of(sampleGoal));
        when(goalDao.deleteById(1)).thenReturn(true);

        assertDoesNotThrow(() -> goalService.deleteGoal(1, 2));
        verify(goalDao).deleteById(1);
    }

    @Test
    @DisplayName("deleteGoal: should throw SecurityException for non-owner")
    void deleteGoal_shouldThrow_whenNotOwner() {
        when(goalDao.findById(1)).thenReturn(Optional.of(sampleGoal));

        assertThrows(SecurityException.class,
            () -> goalService.deleteGoal(1, 999));
        verify(goalDao, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("deleteGoal: should throw ResourceNotFoundException when goal not found")
    void deleteGoal_shouldThrow_whenNotFound() {
        when(goalDao.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> goalService.deleteGoal(999, 2));
    }
}
