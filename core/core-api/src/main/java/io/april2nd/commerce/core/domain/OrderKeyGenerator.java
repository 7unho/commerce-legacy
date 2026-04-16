package io.april2nd.commerce.core.domain;

import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@Component
public class OrderKeyGenerator {
    public String generate() {
        UUID uuid = UUID.randomUUID();

        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(buffer.array());
    }
}
