package org.example.projectsservice.controller.payload;

import org.example.projectsservice.entity.ProjectStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record UpdateProjectPayload(
        String name,
        String description,
        LocalDateTime deadline,
        ProjectStatus status,
        Set<Integer> membersId
) {
}
