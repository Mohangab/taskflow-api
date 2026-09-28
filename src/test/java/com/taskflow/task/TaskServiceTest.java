package com.taskflow.task;

import com.taskflow.common.exception.ApiException;
import com.taskflow.task.dto.TaskRequest;
import com.taskflow.task.dto.TaskResponse;
import com.taskflow.user.Role;
import com.taskflow.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private User alice;
    private User admin;

    @BeforeEach
    void setUp() {
        alice = User.builder().id(1L).email("alice@example.com").password("hash").role(Role.USER).build();
        admin = User.builder().id(2L).email("admin@example.com").password("hash").role(Role.ADMIN).build();
    }

    @Test
    void createTask_setsOwnerAndDefaultStatus() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Write tests");
        request.setDescription("Unit test TaskService");

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(10L);
            t.setCreatedAt(Instant.now());
            t.setUpdatedAt(Instant.now());
            return t;
        });

        TaskResponse response = taskService.createTask(request, alice);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("Write tests");
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(saved.getOwner()).isEqualTo(alice);
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getOwnerEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void listTasks_userSeesOnlyOwnTasks() {
        Task own = Task.builder()
                .id(1L)
                .title("Mine")
                .status(TaskStatus.TODO)
                .owner(alice)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(taskRepository.findByOwnerOrderByCreatedAtDesc(alice)).thenReturn(List.of(own));

        List<TaskResponse> result = taskService.listTasks(alice);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Mine");
        verify(taskRepository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void listTasks_adminSeesAllTasks() {
        when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        taskService.listTasks(admin);

        verify(taskRepository).findAllByOrderByCreatedAtDesc();
        verify(taskRepository, never()).findByOwnerOrderByCreatedAtDesc(any());
    }

    @Test
    void getTask_throwsWhenNotOwnedByUser() {
        when(taskRepository.findByIdAndOwner(99L, alice)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask(99L, alice))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Task not found");
    }
}
