package kz.aceflow.dao;

import kz.aceflow.dao.impl.GoalDaoImpl;
import kz.aceflow.model.Goal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GoalDao Integration Tests")
class GoalDaoTest extends AbstractDaoTest {

    private final GoalDaoImpl dao = new GoalDaoImpl();

    @Test
    @DisplayName("save and findByUserId: CRUD flow")
    void save_shouldPersistAndFind() {
        Goal goal = new Goal();
        goal.setUserId(1);
        goal.setTitle("Study 10 hours");
        goal.setDeadline(LocalDate.now().plusDays(7));
        goal.setXpReward(200);
        goal.setPriority("HIGH");
        goal.setCompleted(false);

        Goal saved = dao.save(goal);
        assertTrue(saved.getGoalId() > 0);

        List<Goal> goals = dao.findByUserId(1);
        assertEquals(1, goals.size());
        assertEquals("Study 10 hours", goals.get(0).getTitle());
    }

    @Test
    @DisplayName("markCompleted: sets completed flag")
    void markCompleted_shouldUpdate() {
        Goal goal = new Goal();
        goal.setUserId(1);
        goal.setTitle("Finish test");
        goal.setXpReward(100);
        goal.setPriority("MEDIUM");
        dao.save(goal);

        assertTrue(dao.markCompleted(goal.getGoalId()));
        Optional<Goal> found = dao.findById(goal.getGoalId());
        assertTrue(found.get().isCompleted());
    }

    @Test
    @DisplayName("deleteById: removes goal")
    void deleteById_shouldRemove() {
        Goal goal = new Goal();
        goal.setUserId(1);
        goal.setTitle("Temp goal");
        goal.setXpReward(50);
        goal.setPriority("LOW");
        dao.save(goal);
        int id = goal.getGoalId();

        assertTrue(dao.deleteById(id));
        assertTrue(dao.findById(id).isEmpty());
    }
}
