package io.april2nd.commerce.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {
    List<AnswerEntity> findByQuestionIdIn(List<Long> questionId);
}
