package com.taskflow.task;

import com.taskflow.common.exception.ApiException;
import com.taskflow.task.dto.TaskRequest;
import com.taskflow.task.dto.TaskResponse;
import com.taskflow.user.Role;
import com.taskflow.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(User currentUser) {
        return listTasks(currentUser, null, null);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(User currentUser, TaskStatus status, String titleQuery) {
        String q = StringUtils.hasText(titleQuery) ? titleQuery.trim() : null;
        List<Task> tasks = currentUser.getRole() == Role.ADMIN
                ? taskRepository.findAllFiltered(status, q)
                : taskRepository.findByOwnerFiltered(currentUser, status, q);
        return tasks.stream().map(TaskResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id, User currentUser) {
        return TaskResponse.from(findAccessibleTask(id, currentUser));
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request, User currentUser) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .owner(currentUser)
                .build();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, User currentUser) {
        Task task = findAccessibleTask(id, currentUser);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id, User currentUser) {
        Task task = findAccessibleTask(id, currentUser);
        taskRepository.delete(task);
    }

    private Task findAccessibleTask(Long id, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return taskRepository.findById(id)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
        }
        return taskRepository.findByIdAndOwner(id, currentUser)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
    }
}
