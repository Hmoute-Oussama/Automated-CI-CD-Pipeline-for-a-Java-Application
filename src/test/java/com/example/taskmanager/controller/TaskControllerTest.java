package com.example.taskmanager.controller;

import com.example.taskmanager.exception.GlobalExceptionHandler;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {TaskController.class, GlobalExceptionHandler.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    // ── GET /api/tasks ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/tasks returns 200 with list of tasks")
    void getTasks_returns200() throws Exception {
        Task t = new Task(1L, "Fix bug", "Critical issue", TaskStatus.TODO);
        when(taskService.getAllTasks(null)).thenReturn(List.of(t));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Fix bug"))
                .andExpect(jsonPath("$[0].status").value("TODO"));
    }

    @Test
    @DisplayName("GET /api/tasks?status=DONE filters by status")
    void getTasks_withStatusFilter_returns200() throws Exception {
        Task t = new Task(2L, "Deploy", "Deploy to prod", TaskStatus.DONE);
        when(taskService.getAllTasks(TaskStatus.DONE)).thenReturn(List.of(t));

        mockMvc.perform(get("/api/tasks").param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("DONE"));
    }

    // ── GET /api/tasks/{id} ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/tasks/{id} returns 200 when task exists")
    void getTaskById_exists_returns200() throws Exception {
        Task t = new Task(1L, "Write docs", null, TaskStatus.IN_PROGRESS);
        when(taskService.getTaskById(1L)).thenReturn(t);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Write docs"));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} returns 404 when task is missing")
    void getTaskById_missing_returns404() throws Exception {
        when(taskService.getTaskById(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: 99"));
    }

    // ── POST /api/tasks ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/tasks returns 201 with created task")
    void createTask_validPayload_returns201() throws Exception {
        Task incoming = new Task(null, "New feature", "Implement login", TaskStatus.TODO);
        Task saved = new Task(5L, "New feature", "Implement login", TaskStatus.TODO);
        when(taskService.createTask(any())).thenReturn(saved);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incoming)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("New feature"));
    }

    @Test
    @DisplayName("POST /api/tasks returns 400 when title is blank")
    void createTask_blankTitle_returns400() throws Exception {
        Task invalid = new Task(null, "", "desc", TaskStatus.TODO);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/tasks/{id} ───────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/tasks/{id} returns 200 with updated task")
    void updateTask_validPayload_returns200() throws Exception {
        Task payload = new Task(null, "Updated", "New desc", TaskStatus.IN_PROGRESS);
        Task updated = new Task(1L, "Updated", "New desc", TaskStatus.IN_PROGRESS);
        when(taskService.updateTask(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    // ── PATCH /api/tasks/{id}/status ─────────────────────────────────────────

    @Test
    @DisplayName("PATCH /api/tasks/{id}/status returns 200 with new status")
    void updateStatus_returns200() throws Exception {
        Task updated = new Task(1L, "Some task", null, TaskStatus.DONE);
        when(taskService.updateStatus(1L, TaskStatus.DONE)).thenReturn(updated);

        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "DONE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    // ── DELETE /api/tasks/{id} ────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/tasks/{id} returns 204 on success")
    void deleteTask_exists_returns204() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} returns 404 when task is missing")
    void deleteTask_missing_returns404() throws Exception {
        doThrow(new TaskNotFoundException(99L)).when(taskService).deleteTask(99L);

        mockMvc.perform(delete("/api/tasks/99"))
                .andExpect(status().isNotFound());
    }
}
