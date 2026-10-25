package com.example.taskmanager.service;

import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task(1L, "Write tests", "Cover the service layer", TaskStatus.TODO);
    }

    // ── getAllTasks ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllTasks() returns all tasks when no status filter is given")
    void getAllTasks_noFilter_returnsAll() {
        when(repository.findAll()).thenReturn(List.of(sampleTask));

        List<Task> result = service.getAllTasks(null);

        assertThat(result).hasSize(1).contains(sampleTask);
        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("getAllTasks() delegates to findByStatus when a filter is provided")
    void getAllTasks_withFilter_returnsFiltered() {
        when(repository.findByStatus(TaskStatus.TODO)).thenReturn(List.of(sampleTask));

        List<Task> result = service.getAllTasks(TaskStatus.TODO);

        assertThat(result).containsExactly(sampleTask);
        verify(repository).findByStatus(TaskStatus.TODO);
    }

    // ── getTaskById ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTaskById() returns the task when it exists")
    void getTaskById_exists_returnsTask() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleTask));

        Task result = service.getTaskById(1L);

        assertThat(result.getTitle()).isEqualTo("Write tests");
    }

    @Test
    @DisplayName("getTaskById() throws TaskNotFoundException when id is missing")
    void getTaskById_missing_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTaskById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── createTask ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createTask() defaults status to TODO when none is provided")
    void createTask_nullStatus_defaultsTodo() {
        Task incoming = new Task(null, "New task", "desc", null);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Task result = service.createTask(incoming);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    @DisplayName("createTask() preserves an explicitly set status")
    void createTask_withStatus_preservesStatus() {
        Task incoming = new Task(null, "In flight", "doing it", TaskStatus.IN_PROGRESS);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Task result = service.createTask(incoming);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("createTask() sets createdAt and updatedAt")
    void createTask_setsTimestamps() {
        Task incoming = new Task(null, "Timestamped", null, TaskStatus.TODO);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Task result = service.createTask(incoming);

        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    // ── updateTask ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateTask() applies all fields from the payload")
    void updateTask_validId_updatesFields() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Task payload = new Task(null, "Updated title", "Updated desc", TaskStatus.IN_PROGRESS);
        Task result = service.updateTask(1L, payload);

        assertThat(result.getTitle()).isEqualTo("Updated title");
        assertThat(result.getDescription()).isEqualTo("Updated desc");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("updateTask() throws when task does not exist")
    void updateTask_missingId_throwsNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        Task payload = new Task(null, "x", null, TaskStatus.DONE);
        assertThatThrownBy(() -> service.updateTask(42L, payload))
                .isInstanceOf(TaskNotFoundException.class);
    }

    // ── updateStatus ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus() changes only the status field")
    void updateStatus_changesStatus() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Task result = service.updateStatus(1L, TaskStatus.DONE);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(result.getTitle()).isEqualTo("Write tests"); // unchanged
    }

    // ── deleteTask ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteTask() succeeds when task exists")
    void deleteTask_exists_deletesSuccessfully() {
        when(repository.deleteById(1L)).thenReturn(true);

        assertThatNoException().isThrownBy(() -> service.deleteTask(1L));
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTask() throws TaskNotFoundException when task does not exist")
    void deleteTask_missing_throwsNotFound() {
        when(repository.deleteById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteTask(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }
}
