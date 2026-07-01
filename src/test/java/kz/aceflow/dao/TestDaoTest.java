package kz.aceflow.dao;

import kz.aceflow.dao.impl.TestDaoImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TestDao Integration Tests")
class TestDaoTest extends AbstractDaoTest {

    private final TestDaoImpl dao = new TestDaoImpl();

    @Test
    @DisplayName("findAll: returns paginated tests")
    void findAll_shouldPaginate() {
        List<kz.aceflow.model.Test> tests = dao.findAll(0, 10);
        assertFalse(tests.isEmpty());
        assertTrue(dao.countAll() >= 1);
    }

    @Test
    @DisplayName("findById: returns seeded test")
    void findById_shouldReturnTest() {
        Optional<kz.aceflow.model.Test> test = dao.findById(1);
        assertTrue(test.isPresent());
        assertEquals("Sample Test", test.get().getTitle());
    }

    @Test
    @DisplayName("save: persists new test")
    void save_shouldPersist() {
        kz.aceflow.model.Test test = new kz.aceflow.model.Test();
        test.setTitle("New Quiz");
        test.setTestType("MULTIPLE_CHOICE");
        test.setTimeLimit(20);
        test.setDifficulty("EASY");

        kz.aceflow.model.Test saved = dao.save(test);
        assertTrue(saved.getTestId() > 0);
        assertEquals("New Quiz", dao.findById(saved.getTestId()).get().getTitle());
    }
}
