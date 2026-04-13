package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.AnswerEntity;
import io.april2nd.commerce.storage.db.core.AnswerRepository;
import io.april2nd.commerce.storage.db.core.QuestionEntity;
import io.april2nd.commerce.storage.db.core.QuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QnAService {
    private QuestionRepository questionRepository;
    private AnswerRepository answerRepository;

    public Page<QnA> findQnA(Long productId, OffsetLimit offsetLimit) {
        Slice<QuestionEntity> questions = questionRepository.findByProductIdAndStatus(
                productId,
                EntityStatus.ACTIVE,
                offsetLimit.toPageable()
        );

        List<Long> questionId = questions.getContent().stream()
                .map(QuestionEntity::getId)
                .collect(Collectors.toList());

        Map<Long, AnswerEntity> answers = answerRepository.findByQuestionIdIn(questionId).stream()
                .filter(AnswerEntity::isActive)
                .collect(Collectors.toMap(
                        AnswerEntity::getQuestionId,
                        answer -> answer
                ));

        return new Page(
                questions.getContent().stream()
                        .map(question -> new QnA(
                                new Question(
                                        question.getId(),
                                        question.getUserId(),
                                        question.getTitle(),
                                        question.getContent()
                                ),
                                Optional.ofNullable(answers.get(question.getId()))
                                        .map(answer -> new Answer(
                                                answer.getId(),
                                                answer.getAdminId(),
                                                answer.getContent()
                                        ))
                                        .orElse(Answer.EMPTY)

                        ))
                        .collect(Collectors.toList()),
                questions.hasNext()
        );
    }

    public Long addQuestion(User user, Long productid, QuestionContent content) {
        QuestionEntity saved = questionRepository.save(
                new QuestionEntity(
                        user.id(),
                        productid,
                        content.title(),
                        content.content()
                )
        );
        return saved.getId();
    }

    @Transactional
    public Long updateQuestion(User user, Long questionId, QuestionContent content) {
        QuestionEntity found = questionRepository.findByIdAndUserId(questionId, user.id())
                .filter(QuestionEntity::isActive)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        found.updateContent(content.title(), content.content());
        return found.getId();
    }

    @Transactional
    public Long removeQuestion(User user, Long questionsId) {
        QuestionEntity found = questionRepository.findByIdAndUserId(questionsId, user.id())
                .filter(QuestionEntity::isActive)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        found.deleted();
        return found.getId();
    }
}
