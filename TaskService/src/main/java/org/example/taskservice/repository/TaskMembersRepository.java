package org.example.taskservice.repository;

import org.example.taskservice.entity.TaskMembers;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskMembersRepository extends CrudRepository<TaskMembers, Integer> {
    void deleteByTaskId(int taskId);
}
