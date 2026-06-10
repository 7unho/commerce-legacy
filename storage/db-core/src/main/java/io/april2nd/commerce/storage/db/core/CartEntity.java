package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.CartType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "cart")
public class CartEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    private CartType type;

    private Long userId;
}
