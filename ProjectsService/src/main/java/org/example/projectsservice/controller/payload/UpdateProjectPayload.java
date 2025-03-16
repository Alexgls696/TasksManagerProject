package org.example.projectsservice.controller.payload;

import java.time.LocalDateTime;
import java.util.Set;

public record UpdateProjectPayload(
        String name,
        String description,
        LocalDateTime deadline,
        Set<Integer> membersId
) {
}
