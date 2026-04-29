package com.trainit.backend.controller;

import com.trainit.backend.dto.CompleteWorkoutSessionRequest;
import com.trainit.backend.dto.CreateWorkoutSessionRequest;
import com.trainit.backend.dto.WorkoutSessionDto;
import com.trainit.backend.service.WorkoutSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/workout-sessions")
public class WorkoutSessionController {

	private final WorkoutSessionService workoutSessionService;

	public WorkoutSessionController(WorkoutSessionService workoutSessionService) {
		this.workoutSessionService = workoutSessionService;
	}

	@PostMapping
	public ResponseEntity<WorkoutSessionDto> createSession(
			@RequestBody @Valid CreateWorkoutSessionRequest request) {
		Integer userId = getCurrentUserId();
		WorkoutSessionDto result = workoutSessionService.createSession(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	@GetMapping
	public ResponseEntity<List<WorkoutSessionDto>> getUserSessions(
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate) {
		Integer userId = getCurrentUserId();

		List<WorkoutSessionDto> sessions;
		if (status != null) {
			sessions = workoutSessionService.getUserSessionsByStatus(userId, status);
		} else if (startDate != null && endDate != null) {
			LocalDateTime start = LocalDateTime.parse(startDate);
			LocalDateTime end = LocalDateTime.parse(endDate);
			sessions = workoutSessionService.getSessionsBetweenDates(userId, start, end);
		} else {
			sessions = workoutSessionService.getUserSessions(userId);
		}

		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/{sessionId}")
	public ResponseEntity<WorkoutSessionDto> getSession(@PathVariable Integer sessionId) {
		Integer userId = getCurrentUserId();
		WorkoutSessionDto result = workoutSessionService.getSession(sessionId, userId);
		return ResponseEntity.ok(result);
	}

	@PutMapping("/{sessionId}/complete")
	public ResponseEntity<WorkoutSessionDto> completeSession(
			@PathVariable Integer sessionId,
			@RequestBody @Valid CompleteWorkoutSessionRequest request) {
		Integer userId = getCurrentUserId();
		WorkoutSessionDto result = workoutSessionService.completeSession(sessionId, userId, request);
		return ResponseEntity.ok(result);
	}

	@PutMapping("/{sessionId}/cancel")
	public ResponseEntity<Void> cancelSession(@PathVariable Integer sessionId) {
		Integer userId = getCurrentUserId();
		workoutSessionService.cancelSession(sessionId, userId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{sessionId}")
	public ResponseEntity<Void> deleteSession(@PathVariable Integer sessionId) {
		Integer userId = getCurrentUserId();
		workoutSessionService.deleteSession(sessionId, userId);
		return ResponseEntity.noContent().build();
	}

	private Integer getCurrentUserId() {
		return 1;
	}
}
