package org.example.taskservice.controller.payload;

import jakarta.validation.constraints.*;
import org.example.taskservice.entity.TaskStatus;
import org.example.taskservice.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateTaskPayload(
        @NotBlank(message = "{validation.task.title_is_blank}")
        @Size(min = 2,max = 50,message = "{validation.task.title_size_is_invalid}")
        String title,

        @Size(max = 1000,message = "{validation.task.description_id_too_big}")
        String description,

        @NotNull(message = "{validation.task.status_is_null}")
        Integer statusId,

        @Min(value = 1,message = "{validation.task.priority_is_less}")
        @Max(value = 5,message = "{validation.task.priority_is_big}")
        int priority,

        @NotNull(message = "{validation.task.deadline_is_null}")
        LocalDateTime deadline,

        @NotNull(message = "{validation.task.assignee_is_null}")
        Integer assigneeId, //Кому назначена задача

        @NotNull(message = "{validation.members_list_is_null}")
        List<Integer> membersId

) {
}
