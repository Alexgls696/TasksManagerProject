package org.example.tasknotesservice.controller.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NewNotePayload {
    @NotBlank(message = "{validation.note.title_is_blank}")
    @Size(min = 3, max = 50, message = "{validation.note.length_is_invalid}")
    private String title;

    @Size(max = 1000, message = "{validation.note.content_length_is_too_big}")
    private String content;

    @NotNull(message = "{validation.note.creator_is_null}")
    private Integer creatorId;

    @NotNull(message = "{validation.note_task_is_null}")
    private Integer taskId;

}
