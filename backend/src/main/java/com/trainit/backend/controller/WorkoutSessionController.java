package com.trainit.backend.controller;

import com.trainit.backend.util.AppLog;
import com.trainit.backend.dto.CompleteWorkoutSessionRequest;
import com.trainit.backend.dto.CreateWorkoutSessionRequest;
import com.trainit.backend.dto.WorkoutSessionDto;
import com.trainit.backend.service.WorkoutSessionService;
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Kontroler REST do zarządzania sesjami treningowymi użytkownika.
 */
@RestController
@RequestMapping("/api/workout-sessions")
public class WorkoutSessionController {

	private static final Logger log = LoggerFactory.getLogger(WorkoutSessionController.class);

	/** Serwis sesji treningowych użytkownika. */
	private final WorkoutSessionService workoutSessionService;

	/**
	 * Tworzy kontroler z wymaganym serwisem sesji treningowych.
	 *
	 * @param workoutSessionService serwis warstwy biznesowej sesji
	 */
	public WorkoutSessionController(WorkoutSessionService workoutSessionService) {
		this.workoutSessionService = workoutSessionService;
	}

	/**
	 * Tworzy nową sesję treningową.
	 *
	 * @param request dane nowej sesji
	 * @return utworzona sesja treningowa
	 */
	@PostMapping
	public ResponseEntity<WorkoutSessionDto> createSession(
			@RequestBody @Valid CreateWorkoutSessionRequest request) {
		Integer userId = getCurrentUserId();
		AppLog.success(log, "POST /api/workout-sessions, userId={}, workoutId={}", userId, request.getWorkoutId());
		WorkoutSessionDto result = workoutSessionService.createSession(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	/**
	 * Zwraca sesje treningowe użytkownika z opcjonalnym filtrowaniem.
	 *
	 * @param status opcjonalny filtr statusu sesji
	 * @param startDate opcjonalna data początkowa zakresu (ISO-8601)
	 * @param endDate opcjonalna data końcowa zakresu (ISO-8601)
	 * @return lista sesji treningowych
	 */
	@GetMapping
	public ResponseEntity<List<WorkoutSessionDto>> getUserSessions(
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate) {
		Integer userId = getCurrentUserId();
		AppLog.success(log, "GET /api/workout-sessions, userId={}, status={}, startDate={}, endDate={}",
				userId, status, startDate, endDate);

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

	/**
	 * Zwraca pojedynczą sesję treningową po identyfikatorze.
	 *
	 * @param sessionId identyfikator sesji
	 * @return dane sesji treningowej
	 */
	@GetMapping("/{sessionId}")
	public ResponseEntity<WorkoutSessionDto> getSession(@PathVariable Integer sessionId) {
		Integer userId = getCurrentUserId();
		AppLog.success(log, "GET /api/workout-sessions/{}, userId={}", sessionId, userId);
		WorkoutSessionDto result = workoutSessionService.getSession(sessionId, userId);
		return ResponseEntity.ok(result);
	}

	/**
	 * Kończy sesję treningową i zapisuje wyniki ćwiczeń.
	 *
	 * @param sessionId identyfikator sesji
	 * @param request dane zakończenia sesji
	 * @return zaktualizowana sesja treningowa
	 */
	@PutMapping("/{sessionId}/complete")
	public ResponseEntity<WorkoutSessionDto> completeSession(
			@PathVariable Integer sessionId,
			@RequestBody @Valid CompleteWorkoutSessionRequest request) {
		Integer userId = getCurrentUserId();
		AppLog.success(log, "PUT /api/workout-sessions/{}/complete, userId={}", sessionId, userId);
		WorkoutSessionDto result = workoutSessionService.completeSession(sessionId, userId, request);
		return ResponseEntity.ok(result);
	}

	/**
	 * Anuluje sesję treningową.
	 *
	 * @param sessionId identyfikator sesji
	 * @return odpowiedź bez treści przy sukcesie
	 */
	@PutMapping("/{sessionId}/cancel")
	public ResponseEntity<Void> cancelSession(@PathVariable Integer sessionId) {
		Integer userId = getCurrentUserId();
		AppLog.success(log, "PUT /api/workout-sessions/{}/cancel, userId={}", sessionId, userId);
		workoutSessionService.cancelSession(sessionId, userId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Usuwa sesję treningową wraz z powiązanymi wynikami.
	 *
	 * @param sessionId identyfikator sesji
	 * @return odpowiedź bez treści przy sukcesie
	 */
	@DeleteMapping("/{sessionId}")
	public ResponseEntity<Void> deleteSession(@PathVariable Integer sessionId) {
		Integer userId = getCurrentUserId();
		AppLog.success(log, "DELETE /api/workout-sessions/{}, userId={}", sessionId, userId);
		workoutSessionService.deleteSession(sessionId, userId);
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
