package io.april2nd.commerce.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "image")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ImageEntity extends BaseEntity {
    private Long userId;
    private String imageUrl;
    private String originalFilename;
}
