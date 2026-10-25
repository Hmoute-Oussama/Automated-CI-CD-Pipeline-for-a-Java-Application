# Task Manager API

A RESTful task management API built with **Spring Boot 3**, packaged as a **Docker image**, and shipped through a full **CI/CD pipeline** on GitHub Actions.

---

## CI/CD Pipeline

```
Push / PR to main
       │
       ▼
┌─────────────────────┐
│  1. Build & Test    │  Maven compiles + runs 22 JUnit tests
│     (Java 21)       │
└────────┬────────────┘
         │ tests pass
         ▼
┌─────────────────────┐
│  2. Docker Publish  │  Builds multi-stage image → pushes to GHCR
│     (main only)     │  tags: latest · main · sha-<commit>
└─────────────────────┘
```

[![CI](https://github.com/Hmoute-Oussama/Automated-CI-CD-Pipeline-for-a-Java-Application/actions/workflows/ci.yml/badge.svg)](https://github.com/Hmoute-Oussama/Automated-CI-CD-Pipeline-for-a-Java-Application/actions/workflows/ci.yml)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Build tool | Maven 3.9 |
| Testing | JUnit 5 · Mockito · MockMvc |
| Container | Docker (multi-stage, Alpine JRE) |
| Registry | GitHub Container Registry (GHCR) |
| CI | GitHub Actions |

---

## API Endpoints

Base URL: `http://localhost:8080`

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/tasks` | List all tasks (optional `?status=TODO\|IN_PROGRESS\|DONE`) |
| `GET` | `/api/tasks/{id}` | Get a task by ID |
| `POST` | `/api/tasks` | Create a new task |
| `PUT` | `/api/tasks/{id}` | Full update of a task |
| `PATCH` | `/api/tasks/{id}/status` | Update status only |
| `DELETE` | `/api/tasks/{id}` | Delete a task |
| `GET` | `/actuator/health` | Health check |
| `GET` | `/actuator/info` | App info |

### Task JSON shape

```json
{
  "id": 1,
  "title": "Fix login bug",
  "description": "Users can't log in with SSO",
  "status": "IN_PROGRESS",
  "createdAt": "2026-10-25T09:45:19",
  "updatedAt": "2026-10-25T09:45:32"
}
```

Status values: `TODO` · `IN_PROGRESS` · `DONE`

---

## Running Locally

### Option 1 — Maven (requires Java 21)

```bash
# Run tests
mvn verify

# Start the server
mvn spring-boot:run
```

### Option 2 — Docker Compose (recommended)

```bash
# Pull from GHCR and start
docker compose up

# Or build the image locally first
docker compose up --build

# Stop
docker compose down
```

### Option 3 — Docker directly

```bash
# Build
docker build -t task-manager:latest .

# Run
docker run -p 8080:8080 task-manager:latest
```

---

## Quick API Test

```bash
# Create a task
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Write README","status":"IN_PROGRESS"}' | jq

# List all tasks
curl -s http://localhost:8080/api/tasks | jq

# Update status
curl -s -X PATCH http://localhost:8080/api/tasks/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"DONE"}' | jq

# Health check
curl -s http://localhost:8080/actuator/health | jq
```

---

## Project Structure

```
├── .github/
│   └── workflows/
│       └── ci.yml              # GitHub Actions pipeline
├── src/
│   ├── main/java/com/example/taskmanager/
│   │   ├── TaskManagerApplication.java
│   │   ├── controller/TaskController.java
│   │   ├── service/TaskService.java
│   │   ├── repository/TaskRepository.java
│   │   ├── model/Task.java
│   │   ├── model/TaskStatus.java
│   │   └── exception/
│   │       ├── TaskNotFoundException.java
│   │       └── GlobalExceptionHandler.java
│   └── test/java/com/example/taskmanager/
│       ├── service/TaskServiceTest.java    # 12 unit tests
│       └── controller/TaskControllerTest.java # 10 slice tests
├── Dockerfile                  # Multi-stage build
├── docker-compose.yml          # One-command local setup
└── pom.xml
```

---

## Docker Image

Published to GitHub Container Registry on every push to `main`:

```bash
docker pull ghcr.io/hmoute-oussama/task-manager:latest
```
