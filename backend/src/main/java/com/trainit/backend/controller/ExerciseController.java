package com.trainit.backend.controller;

import com.trainit.backend.dto.CreateExerciseRequest;
import com.trainit.backend.dto.ExerciseDto;
import com.trainit.backend.service.ExerciseService;
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

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

	private final ExerciseService exerciseService;

	public ExerciseController(ExerciseService exerciseService) {
		this.exerciseService = exerciseService;
	}

	@PostMapping
	public ResponseEntity<ExerciseDto> createExercise(@RequestBody @Valid CreateExerciseRequest request) {
		Integer userId = getCurrentUserId();
		ExerciseDto result = exerciseService.createExercise(request, userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	@GetMapping
	public ResponseEntity<List<ExerciseDto>> getExercises(
			@RequestParam(required = false) String muscleGroup) {
		List<ExerciseDto> exercises;
		if (muscleGroup != null && !muscleGroup.isEmpty()) {
			exercises = exerciseService.getExercisesByMuscleGroup(muscleGroup);
		} else {
			exercises = exerciseService.getAllExercises();
		}
		return ResponseEntity.ok(exercises);
	}

	@GetMapping("/{exerciseId}")
	public ResponseEntity<ExerciseDto> getExercise(@PathVariable Integer exerciseId) {
		ExerciseDto result = exerciseService.getExercise(exerciseId);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/custom/my-exercises")
	public ResponseEntity<List<ExerciseDto>> getUserCustomExercises() {
		Integer userId = getCurrentUserId();
		List<ExerciseDto> exercises = exerciseService.getUserCustomExercises(userId);
		return ResponseEntity.ok(exercises);
	}

	@PutMapping("/{exerciseId}")
	public ResponseEntity<ExerciseDto> updateExercise(
			@PathVariable Integer exerciseId,
			@RequestBody @Valid CreateExerciseRequest request) {
		ExerciseDto result = exerciseService.updateExercise(exerciseId, request);
		return ResponseEntity.ok(result);
	}

	@DeleteMapping("/{exerciseId}")
	public ResponseEntity<Void> deleteExercise(@PathVariable Integer exerciseId) {
		exerciseService.deleteExercise(exerciseId);
		return ResponseEntity.noContent().build();
	}

	private Integer getCurrentUserId() {
		return 1;
	}
}
