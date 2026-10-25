package com.example.taskmanager.exception;

/**
 * Thrown when a requested task does not exist.
 */
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("Task not found with id: " + id);
    }
}
