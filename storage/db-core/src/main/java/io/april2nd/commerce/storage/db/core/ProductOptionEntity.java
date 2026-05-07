package io.april2nd.commerce.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "product_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductOptionEntity extends BaseEntity {
    private Long productId;
    private String name;
    private String description;
    private BigDecimal costPrice;
    private BigDecimal salesPrice;
    private BigDecimal discountedPrice;
    private Integer priority;
}
