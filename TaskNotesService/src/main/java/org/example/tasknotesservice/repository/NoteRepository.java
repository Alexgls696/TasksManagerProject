package org.example.tasknotesservice.repository;

import org.example.tasknotesservice.entity.Note;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface NoteRepository extends ReactiveCrudRepository<Note, UUID> {
    Flux<Note>findAll();
    Flux<Note>findAllByTaskId(Integer taskId,Pageable pageable);
}
