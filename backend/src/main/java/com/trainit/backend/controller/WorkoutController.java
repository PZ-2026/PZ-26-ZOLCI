package com.trainit.backend.controller;

import com.trainit.backend.dto.CreateWorkoutRequest;
import com.trainit.backend.dto.WorkoutDto;
import com.trainit.backend.service.WorkoutService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

	private final WorkoutService workoutService;

	public WorkoutController(WorkoutService workoutService) {
		this.workoutService = workoutService;
	}

	@PostMapping
	public ResponseEntity<WorkoutDto> createWorkout(@RequestBody @Valid CreateWorkoutRequest request) {
		Integer userId = getCurrentUserId();
		WorkoutDto result = workoutService.createWorkout(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	@GetMapping
	public ResponseEntity<List<WorkoutDto>> getUserWorkouts() {
		Integer userId = getCurrentUserId();
		List<WorkoutDto> workouts = workoutService.getUserWorkouts(userId);
		return ResponseEntity.ok(workouts);
	}

	@GetMapping("/{workoutId}")
	public ResponseEntity<WorkoutDto> getWorkout(@PathVariable Integer workoutId) {
		Integer userId = getCurrentUserId();
		WorkoutDto result = workoutService.getWorkout(workoutId, userId);
		return ResponseEntity.ok(result);
	}

	@PutMapping("/{workoutId}")
	public ResponseEntity<WorkoutDto> updateWorkout(
			@PathVariable Integer workoutId,
			@RequestBody @Valid CreateWorkoutRequest request) {
		Integer userId = getCurrentUserId();
		WorkoutDto result = workoutService.updateWorkout(workoutId, userId, request);
		return ResponseEntity.ok(result);
	}

	@DeleteMapping("/{workoutId}")
	public ResponseEntity<Void> deleteWorkout(@PathVariable Integer workoutId) {
		Integer userId = getCurrentUserId();
		workoutService.deleteWorkout(workoutId, userId);
		return ResponseEntity.noContent().build();
	}

	private Integer getCurrentUserId() {
		return 1;
	}
}
