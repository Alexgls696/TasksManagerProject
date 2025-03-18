package org.example.tasknotesservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tasknotesservice.controller.payload.NewNotePayload;
import org.example.tasknotesservice.controller.payload.UpdateNotePayload;
import org.example.tasknotesservice.entity.Note;
import org.example.tasknotesservice.service.NoteService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("task-manager-api/task-notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;

    @GetMapping
    Flux<Note> getAllNotes() {
        return noteService.findAllNotes();
    }

    @GetMapping("/by-task-id/{id}")
    public Flux<Note> getNoteById(@PathVariable("id") int id) {
        Sort sort = Sort.by(Sort.Direction.DESC, "creationDate");
        return noteService.findAllByTaskId(id, Pageable.unpaged(sort));
    }

    @PostMapping
    public Mono<ResponseEntity<Note>> createNote(@Valid @RequestBody Mono<NewNotePayload> monoPayload, UriComponentsBuilder uriBuilder
    ) {
        return monoPayload
                .flatMap(noteService::save)
                .map(added -> ResponseEntity
                        .created(uriBuilder
                                .replacePath("task-manager-api/task-nodes/{id}")
                                .build(Map.of("id", added.getId())))
                        .body(added));
    }

    @PatchMapping("/{id}")
    public Mono<ResponseEntity<Void>> updateNote(@PathVariable("id") String noteId, @Valid @RequestBody Mono<UpdateNotePayload>payloadMono){
        return payloadMono.flatMap(payload->noteService.update(UUID.fromString(noteId),payload))
                .map(updated->ResponseEntity
                        .noContent()
                        .build());
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteNote(@PathVariable("id") String noteId) {
        return noteService.deleteById(UUID.fromString(noteId));
    }
}
