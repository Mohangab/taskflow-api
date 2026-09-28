package com.taskflow.task;

import com.taskflow.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOwnerOrderByCreatedAtDesc(User owner);
    List<Task> findAllByOrderByCreatedAtDesc();
    Optional<Task> findByIdAndOwner(Long id, User owner);

    @Query("""
            SELECT t FROM Task t
            WHERE t.owner = :owner
              AND (:status IS NULL OR t.status = :status)
              AND (:q IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY t.createdAt DESC
            """)
    List<Task> findByOwnerFiltered(
            @Param("owner") User owner,
            @Param("status") TaskStatus status,
            @Param("q") String q);

    @Query("""
            SELECT t FROM Task t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:q IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY t.createdAt DESC
            """)
    List<Task> findAllFiltered(
            @Param("status") TaskStatus status,
            @Param("q") String q);
}
