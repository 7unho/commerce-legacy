package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.QnA;

import java.util.List;
import java.util.stream.Collectors;

public record QnAResponse(
        Long questionId,
        String questionTitle,
        String question,
        Long answerId,
        String answer
) {
    public static QnAResponse of(QnA qna) {
        return new QnAResponse(
                qna.question().id(),
                qna.question().title(),
                qna.question().content(),
                qna.answer().id(),
                qna.answer().content()
        );
    }

    public static List<QnAResponse> of(List<QnA> qnaList) {
        return qnaList.stream()
                .map(QnAResponse::of)
                .collect(Collectors.toList());
    }
}
