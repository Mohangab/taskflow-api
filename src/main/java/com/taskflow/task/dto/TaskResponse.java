package com.taskflow.task.dto;

import com.taskflow.task.Task;
import com.taskflow.task.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Long ownerId;
    private String ownerEmail;
    private Instant createdAt;
    private Instant updatedAt;

    public static TaskResponse from(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .ownerId(task.getOwner().getId())
                .ownerEmail(task.getOwner().getEmail())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
