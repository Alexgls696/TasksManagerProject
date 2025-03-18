package org.example.tasknotesservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tasknotesservice.controller.payload.NewNotePayload;
import org.example.tasknotesservice.controller.payload.UpdateNotePayload;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "notes")
public class Note {
    @Id
    private UUID id;

    private Integer creatorId;
    private Integer taskId;
    private String title;
    private String content;
    private LocalDateTime creationDate;

    public Note(NewNotePayload payload) {
        id = UUID.randomUUID();
        this.creationDate = LocalDateTime.now();
        this.content = payload.content();
        this.title = payload.title();
        this.creatorId = payload.creatorId();
        this.taskId = payload.taskId();
    }

    public void update(UpdateNotePayload payload) {
        this.title = payload.title();
        this.content = payload.content();
    }
}
