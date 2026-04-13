package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.domain.QuestionContent;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;

public record UpdateQuestionRequest(
        String title,
        String content
) {
    public QuestionContent toContent() {
        if (title.isEmpty()) throw new CoreException(ErrorType.INVALID_REQUEST);
        if (title.length() > 100) throw new CoreException(ErrorType.INVALID_REQUEST);
        if (content.isEmpty()) throw new CoreException(ErrorType.INVALID_REQUEST);

        return new QuestionContent(title, content);
    }
}
