package kz.aceflow.dao;

import kz.aceflow.model.Question;
import java.util.List;
import java.util.Optional;

public interface QuestionDao {
    Question save(Question question);
    Optional<Question> findById(int questionId);
    List<Question> findByTestId(int testId);
    boolean deleteById(int questionId);
}
