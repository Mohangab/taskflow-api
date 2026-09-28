package com.taskflow.task;

import com.taskflow.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOwnerOrderByCreatedAtDesc(User owner);
    List<Task> findAllByOrderByCreatedAtDesc();
    Optional<Task> findByIdAndOwner(Long id, User owner);
}
