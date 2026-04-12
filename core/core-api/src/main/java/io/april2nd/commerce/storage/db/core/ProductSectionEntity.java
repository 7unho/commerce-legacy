package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.ProductSectionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_section")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductSectionEntity extends BaseEntity {
    private Long productId;

    @Enumerated(EnumType.STRING)
    private ProductSectionType type;

    private String content;
}