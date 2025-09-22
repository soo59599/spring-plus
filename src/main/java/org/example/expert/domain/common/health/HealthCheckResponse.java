package org.example.expert.domain.common.health;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HealthCheckResponse {
    private String status;
    private LocalDateTime timestamp;

    public static HealthCheckResponse up() {
        return new HealthCheckResponse("UP", LocalDateTime.now());
    }
}
