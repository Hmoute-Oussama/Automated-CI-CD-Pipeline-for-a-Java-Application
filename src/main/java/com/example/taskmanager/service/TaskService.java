package com.example.taskmanager.service;

import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business logic for task management.
 */
@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    /** Returns every task, optionally filtered by status. */
    public List<Task> getAllTasks(TaskStatus status) {
        if (status != null) {
            return repository.findByStatus(status);
        }
        return repository.findAll();
    }

    /** Returns the task with the given id or throws {@link TaskNotFoundException}. */
    public Task getTaskById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    /** Persists a new task. */
    public Task createTask(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        return repository.save(task);
    }

    /** Applies a full update to an existing task. */
    public Task updateTask(Long id, Task updated) {
        Task existing = getTaskById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setStatus(updated.getStatus());
        existing.setUpdatedAt(LocalDateTime.now());
        return repository.save(existing);
    }

    /** Changes only the status of an existing task. */
    public Task updateStatus(Long id, TaskStatus newStatus) {
        Task existing = getTaskById(id);
        existing.setStatus(newStatus);
        existing.setUpdatedAt(LocalDateTime.now());
        return repository.save(existing);
    }

    /** Removes the task or throws {@link TaskNotFoundException}. */
    public void deleteTask(Long id) {
        if (!repository.deleteById(id)) {
            throw new TaskNotFoundException(id);
        }
    }
}
