package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.controller.v1.request.AddQuestionRequest;
import io.april2nd.commerce.core.api.controller.v1.request.UpdateQuestionRequest;
import io.april2nd.commerce.core.domain.QnA;
import io.april2nd.commerce.core.domain.QnAService;
import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import io.april2nd.commerce.core.support.response.ApiResponse;
import io.april2nd.commerce.core.support.response.PageResponse;
import io.april2nd.commerce.core.api.controller.v1.response.QnAResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class QnAController {
    private QnAService qnaService;

    @GetMapping("/v1/qna")
    ApiResponse<PageResponse<QnAResponse>> getQnA(
            @RequestParam Long productId,
            @RequestParam Integer offset,
            @RequestParam Integer limit) {
        Page<QnA> page = qnaService.findQnA(productId, new OffsetLimit(offset, limit));
        return ApiResponse.success(new PageResponse(QnAResponse.of(page.content()), page.hasNext()));
    }

    @PostMapping("/v1/questions")
    ApiResponse<Void> createQuestion(
            User user,
            @RequestBody AddQuestionRequest request) {
        qnaService.addQuestion(user, request.productId(), request.toContent());
        return ApiResponse.success();
    }

    @PutMapping("/v1/questions/{questionId}")
    ApiResponse<Void> updateQuestion(
            User user,
            @PathVariable Long questionId,
            @RequestBody UpdateQuestionRequest request) {
        qnaService.updateQuestion(user, questionId, request.toContent());
        return ApiResponse.success();
    }

    @DeleteMapping("/v1/questions/{questionsId}")
    ApiResponse<Void> deleteQuestion(
            User user,
            @PathVariable Long questionsId) {
        qnaService.removeQuestion(user, questionsId);
        return ApiResponse.success();
    }
}
