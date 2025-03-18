package org.example.tasknotesservice.service;

import org.example.tasknotesservice.controller.payload.NewNotePayload;
import org.example.tasknotesservice.controller.payload.UpdateNotePayload;
import org.example.tasknotesservice.entity.Note;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface NoteService {
    Flux<Note>findAllNotes();
    Flux<Note> findAllByTaskId(int taskId, Pageable pageable);
    Mono<Note>save(NewNotePayload payload);
    Mono<Note>update(UUID id,UpdateNotePayload payload);
    Mono<Void>deleteById(UUID id);
}
