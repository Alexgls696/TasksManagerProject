package org.example.projectsservice.controller.payload;

import java.time.LocalDateTime;
import java.util.List;

public record NewProjectPayload(
        String name,
        String description,
        Integer creatorId,
        LocalDateTime deadline,
        List<Integer> membersId
) {
}
