package org.example.tasknotesservice.service;

import lombok.RequiredArgsConstructor;
import org.example.tasknotesservice.controller.payload.NewNotePayload;
import org.example.tasknotesservice.controller.payload.UpdateNotePayload;
import org.example.tasknotesservice.entity.Note;
import org.example.tasknotesservice.repository.NoteRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;

    @Override
    public Flux<Note> findAllNotes() {
        return noteRepository.findAll();
    }

    @Override
    public Flux<Note> findAllByTaskId(int taskId, Pageable pageable) {
        return noteRepository.findAllByTaskId(taskId, pageable);
    }

    @Override
    public Mono<Note> save(NewNotePayload payload) {
        return noteRepository.save(new Note(payload));
    }

    @Override
    public Mono<Note> update(UUID id, UpdateNotePayload payload) {
        Mono<Note> updatable = noteRepository.findById(id);
        updatable = updatable.flatMap(note -> {
            note.update(payload);
            return noteRepository.save(note);
        });
        return updatable;
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return noteRepository.existsById(id)
                .flatMap(exists -> {
                    if (exists) {
                        return noteRepository.deleteById(id);
                    } else {
                        return Mono.error(new NoSuchElementException("Note with id " + id + " does not exist"));
                    }
                });
    }
}
