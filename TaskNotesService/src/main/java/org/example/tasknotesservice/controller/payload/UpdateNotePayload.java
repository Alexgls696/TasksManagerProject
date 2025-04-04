package org.example.tasknotesservice.controller.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNotePayload(
        @NotBlank(message = "{validation.note.title_is_blank}")
        @Size(min = 3, max = 50, message ="{validation.note.length_is_invalid}" )
        String title,

        @Size(max = 1000,message = "{validation.note.content_length_is_too_big}")
        String content,
        Integer creator
) {
}
