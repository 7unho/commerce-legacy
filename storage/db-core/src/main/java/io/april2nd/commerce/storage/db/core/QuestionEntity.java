package io.april2nd.commerce.storage.db.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class QuestionEntity extends BaseEntity {
    private Long userId;

    // NOTE: QNA 는 아예 상품 전용으로 지정
    private Long productId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
    }
}