package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory task store. Simulates a repository layer without a database.
 */
@Repository
public class TaskRepository {

    private final Map<Long, Task> store = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public List<Task> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<Task> findByStatus(TaskStatus status) {
        return store.values().stream()
                .filter(t -> t.getStatus() == status)
                .toList();
    }

    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(idSequence.getAndIncrement());
        }
        store.put(task.getId(), task);
        return task;
    }

    public boolean deleteById(Long id) {
        return store.remove(id) != null;
    }

    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    /** Clears all data – useful in tests. */
    public void clear() {
        store.clear();
        idSequence.set(1);
    }
}
