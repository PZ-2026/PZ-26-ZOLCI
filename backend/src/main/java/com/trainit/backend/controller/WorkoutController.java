package com.trainit.backend.controller;

import com.trainit.backend.util.AppLog;

import com.trainit.backend.dto.CreateWorkoutRequest;
import com.trainit.backend.dto.WorkoutDto;
import com.trainit.backend.service.WorkoutService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Kontroler REST do zarządzania planami treningowymi użytkownika.
 */
@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

	private static final Logger log = LoggerFactory.getLogger(WorkoutController.class);

	private final WorkoutService workoutService;

	/**
	 * Tworzy kontroler z wymaganym serwisem planów treningowych.
	 *
	 * @param workoutService serwis warstwy biznesowej planów
	 */
	public WorkoutController(WorkoutService workoutService) {
		this.workoutService = workoutService;
	}

	/**
	 * Tworzy nowy plan treningowy dla bieżącego użytkownika.
	 *
	 * @param request dane nowego planu
	 * @return utworzony plan treningowy
	 */
	@PostMapping
	public ResponseEntity<WorkoutDto> createWorkout(@RequestBody @Valid CreateWorkoutRequest request) {
		Integer userId = getCurrentUserId();
		AppLog.success(log, "POST /api/workouts, userId={}, name={}", userId, request.getName());
		WorkoutDto result = workoutService.createWorkout(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	/**
	 * Zwraca wszystkie plany treningowe bieżącego użytkownika.
	 *
	 * @return lista planów treningowych
	 */
	@GetMapping
	public ResponseEntity<List<WorkoutDto>> getUserWorkouts() {
		Integer userId = getCurrentUserId();
		AppLog.success(log, "GET /api/workouts, userId={}", userId);
		List<WorkoutDto> workouts = workoutService.getUserWorkouts(userId);
		return ResponseEntity.ok(workouts);
	}

	/**
	 * Zwraca pojedynczy plan treningowy po identyfikatorze.
	 *
	 * @param workoutId identyfikator planu
	 * @return dane planu treningowego
	 */
	@GetMapping("/{workoutId}")
	public ResponseEntity<WorkoutDto> getWorkout(@PathVariable Integer workoutId) {
		Integer userId = getCurrentUserId();
		AppLog.success(log, "GET /api/workouts/{}, userId={}", workoutId, userId);
		WorkoutDto result = workoutService.getWorkout(workoutId, userId);
		return ResponseEntity.ok(result);
	}

	/**
	 * Aktualizuje istniejący plan treningowy.
	 *
	 * @param workoutId identyfikator planu
	 * @param request nowe dane planu
	 * @return zaktualizowany plan treningowy
	 */
	@PutMapping("/{workoutId}")
	public ResponseEntity<WorkoutDto> updateWorkout(
			@PathVariable Integer workoutId,
			@RequestBody @Valid CreateWorkoutRequest request) {
		Integer userId = getCurrentUserId();
		AppLog.success(log, "PUT /api/workouts/{}, userId={}", workoutId, userId);
		WorkoutDto result = workoutService.updateWorkout(workoutId, userId, request);
		return ResponseEntity.ok(result);
	}

	/**
	 * Usuwa plan treningowy po identyfikatorze.
	 *
	 * @param workoutId identyfikator planu
	 * @return odpowiedź bez treści przy sukcesie
	 */
	@DeleteMapping("/{workoutId}")
	public ResponseEntity<Void> deleteWorkout(@PathVariable Integer workoutId) {
		Integer userId = getCurrentUserId();
		AppLog.success(log, "DELETE /api/workouts/{}, userId={}", workoutId, userId);
		workoutService.deleteWorkout(workoutId, userId);
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
