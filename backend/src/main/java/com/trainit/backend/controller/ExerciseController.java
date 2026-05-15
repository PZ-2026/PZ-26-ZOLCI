package com.trainit.backend.controller;

import com.trainit.backend.util.AppLog;
import com.trainit.backend.dto.CreateExerciseRequest;
import com.trainit.backend.dto.ExerciseDto;
import com.trainit.backend.service.ExerciseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Kontroler REST do zarządzania ćwiczeniami (systemowymi i własnymi).
 */
@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

	private static final Logger log = LoggerFactory.getLogger(ExerciseController.class);

	/** Serwis warstwy biznesowej ćwiczeń. */
	private final ExerciseService exerciseService;

	/**
	 * Tworzy kontroler z wymaganym serwisem ćwiczeń.
	 *
	 * @param exerciseService serwis warstwy biznesowej ćwiczeń
	 */
	public ExerciseController(ExerciseService exerciseService) {
		this.exerciseService = exerciseService;
	}

	/**
	 * Tworzy nowe ćwiczenie.
	 *
	 * @param request dane nowego ćwiczenia
	 * @return utworzone ćwiczenie
	 */
	@PostMapping
	public ResponseEntity<ExerciseDto> createExercise(@RequestBody @Valid CreateExerciseRequest request) {
		Integer userId = getCurrentUserId();
		AppLog.success(log, "POST /api/exercises, userId={}, name={}", userId, request.getName());
		ExerciseDto result = exerciseService.createExercise(request, userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	/**
	 * Zwraca listę ćwiczeń, opcjonalnie filtrowaną po grupie mięśniowej.
	 *
	 * @param muscleGroup opcjonalna grupa mięśniowa
	 * @return lista ćwiczeń
	 */
	@GetMapping
	public ResponseEntity<List<ExerciseDto>> getExercises(
			@RequestParam(required = false) String muscleGroup) {
		if (muscleGroup != null && !muscleGroup.isEmpty()) {
			AppLog.success(log, "GET /api/exercises, muscleGroup={}", muscleGroup);
			return ResponseEntity.ok(exerciseService.getExercisesByMuscleGroup(muscleGroup));
		}
		AppLog.success(log, "GET /api/exercises");
		return ResponseEntity.ok(exerciseService.getAllExercises());
	}

	/**
	 * Zwraca pojedyncze ćwiczenie po identyfikatorze.
	 *
	 * @param exerciseId identyfikator ćwiczenia
	 * @return dane ćwiczenia
	 */
	@GetMapping("/{exerciseId}")
	public ResponseEntity<ExerciseDto> getExercise(@PathVariable Integer exerciseId) {
		AppLog.success(log, "GET /api/exercises/{}", exerciseId);
		ExerciseDto result = exerciseService.getExercise(exerciseId);
		return ResponseEntity.ok(result);
	}

	/**
	 * Zwraca własne ćwiczenia bieżącego użytkownika.
	 *
	 * @return lista ćwiczeń własnych użytkownika
	 */
	@GetMapping("/custom/my-exercises")
	public ResponseEntity<List<ExerciseDto>> getUserCustomExercises() {
		Integer userId = getCurrentUserId();
		AppLog.success(log, "GET /api/exercises/custom/my-exercises, userId={}", userId);
		List<ExerciseDto> exercises = exerciseService.getUserCustomExercises(userId);
		return ResponseEntity.ok(exercises);
	}

	/**
	 * Aktualizuje istniejące ćwiczenie.
	 *
	 * @param exerciseId identyfikator ćwiczenia
	 * @param request nowe dane ćwiczenia
	 * @return zaktualizowane ćwiczenie
	 */
	@PutMapping("/{exerciseId}")
	public ResponseEntity<ExerciseDto> updateExercise(
			@PathVariable Integer exerciseId,
			@RequestBody @Valid CreateExerciseRequest request) {
		AppLog.success(log, "PUT /api/exercises/{}", exerciseId);
		ExerciseDto result = exerciseService.updateExercise(exerciseId, request);
		return ResponseEntity.ok(result);
	}

	/**
	 * Usuwa ćwiczenie po identyfikatorze.
	 *
	 * @param exerciseId identyfikator ćwiczenia
	 * @return odpowiedź bez treści przy sukcesie
	 */
	@DeleteMapping("/{exerciseId}")
	public ResponseEntity<Void> deleteExercise(@PathVariable Integer exerciseId) {
		AppLog.success(log, "DELETE /api/exercises/{}", exerciseId);
		exerciseService.deleteExercise(exerciseId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Zwraca identyfikator bieżącego użytkownika (tymczasowa implementacja).
	 *
	 * @return identyfikator użytkownika
	 */
	private Integer getCurrentUserId() {
		return 1;
	}
}
