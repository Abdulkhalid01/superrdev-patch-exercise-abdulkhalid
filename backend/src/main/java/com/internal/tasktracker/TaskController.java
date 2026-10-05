package com.internal.tasktracker;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<?> searchTasks(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        String query = q == null ? "" : q.trim();
        String searchTerm = "%" + query.toLowerCase() + "%";

        // Safe status parsing to prevent HTTP 500 errors on invalid values or "ALL"
        String normalizedStatus = null;
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            try {
                normalizedStatus = TaskStatus.valueOf(status.trim().toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                normalizedStatus = null;
            }
        }

        // Removed artificial Thread.sleep() bottleneck

        List<Task> allResults = taskRepository.searchTasks(searchTerm, normalizedStatus);

        int currentPage = Math.max(1, page);
        int validPageSize = Math.max(1, pageSize);
        int start = (currentPage - 1) * validPageSize;
        int end = Math.min(start + validPageSize, allResults.size());

        List<Task> pageResults = (start < allResults.size())
                ? allResults.subList(start, end)
                : Collections.emptyList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", pageResults);
        response.put("total", allResults.size());
        response.put("page", currentPage);
        response.put("pageSize", validPageSize);

        return ResponseEntity.ok(response);
    }
}