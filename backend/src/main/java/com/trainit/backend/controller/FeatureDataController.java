package com.trainit.backend.controller;

import com.trainit.backend.util.AppLog;
import com.trainit.backend.dto.CreateExerciseRequest;
import com.trainit.backend.dto.CreateWorkoutRequest;
import com.trainit.backend.dto.ExerciseResultRequest;
import com.trainit.backend.dto.FinishSessionRequest;
import com.trainit.backend.dto.FeatureItemResponse;
import com.trainit.backend.dto.ProfileOverviewResponse;
import com.trainit.backend.dto.SessionExerciseResultResponse;
import com.trainit.backend.dto.StartSessionRequest;
import com.trainit.backend.dto.UpdateSettingRequest;
import com.trainit.backend.dto.WorkoutExerciseLineResponse;
import com.trainit.backend.dto.WorkoutExerciseRequest;
import com.trainit.backend.dto.WorkoutPlanDetailResponse;
import com.trainit.backend.security.JwtPrincipal;
import com.trainit.backend.service.FeatureDataService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
 * Kontroler REST wystawiający dane dla modułowych ekranów aplikacji mobilnej.
 *
 * <p>Endpointy są zgodne z aktualnym kontraktem frontendu i zwracają listę
 * obiektów z polami {@code title}/{@code subtitle}.
 */
@RestController
@RequestMapping("/api/feature")
public class FeatureDataController {

	private static final Logger log = LoggerFactory.getLogger(FeatureDataController.class);

	/** Serwis dostarczający dane listowe dla modułów UI. */
	private final FeatureDataService featureDataService;

	/**
	 * Tworzy kontroler z wymaganym serwisem danych modułowych.
	 *
	 * @param featureDataService serwis warstwy biznesowej
	 */
	public FeatureDataController(FeatureDataService featureDataService) {
		this.featureDataService = featureDataService;
	}

	/**
	 * Zwraca dane listy treningów.
	 *
	 * @param userId opcjonalny identyfikator użytkownika (dla trenera/admina)
	 * @param authentication kontekst uwierzytelnienia
	 * @return lista pozycji modułu treningów
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@GetMapping("/workouts")
	public List<FeatureItemResponse> workouts(@RequestParam(required = false) Integer userId, Authentication authentication) {
		Integer effectiveUserId = resolveEffectiveUserId(authentication, userId);
		AppLog.success(log, "GET /api/feature/workouts, userId={}", effectiveUserId);
		return featureDataService.getWorkouts(effectiveUserId);
	}

	/**
	 * Zwraca dane listy ćwiczeń.
	 *
	 * @param userId opcjonalny identyfikator użytkownika (dla trenera/admina)
	 * @param authentication kontekst uwierzytelnienia
	 * @return lista pozycji modułu ćwiczeń
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@GetMapping("/exercises")
	public List<FeatureItemResponse> exercises(@RequestParam(required = false) Integer userId, Authentication authentication) {
		Integer effectiveUserId = resolveEffectiveUserId(authentication, userId);
		AppLog.success(log, "GET /api/feature/exercises, userId={}", effectiveUserId);
		return featureDataService.getExercises(effectiveUserId);
	}

	/**
	 * Zwraca dane listy sesji treningowych.
	 *
	 * @param userId opcjonalny identyfikator użytkownika (dla trenera/admina)
	 * @param authentication kontekst uwierzytelnienia
	 * @return lista pozycji modułu sesji
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@GetMapping("/sessions")
	public List<FeatureItemResponse> sessions(@RequestParam(required = false) Integer userId, Authentication authentication) {
		Integer effectiveUserId = resolveEffectiveUserId(authentication, userId);
		AppLog.success(log, "GET /api/feature/sessions, userId={}", effectiveUserId);
		return featureDataService.getSessions(effectiveUserId);
	}

	/**
	 * Zwraca dane podsumowania statystyk.
	 *
	 * @param userId opcjonalny identyfikator użytkownika (dla trenera/admina)
	 * @param authentication kontekst uwierzytelnienia
	 * @return lista pozycji statystycznych
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@GetMapping("/statistics/summary")
	public List<FeatureItemResponse> statisticsSummary(@RequestParam(required = false) Integer userId, Authentication authentication) {
		Integer effectiveUserId = resolveEffectiveUserId(authentication, userId);
		AppLog.success(log, "GET /api/feature/statistics/summary, userId={}", effectiveUserId);
		return featureDataService.getStatisticsSummary(effectiveUserId);
	}

	/**
	 * Zwraca dane listy raportów.
	 *
	 * @param userId opcjonalny identyfikator użytkownika (dla trenera/admina)
	 * @param authentication kontekst uwierzytelnienia
	 * @return lista pozycji modułu raportów
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@GetMapping("/reports")
	public List<FeatureItemResponse> reports(@RequestParam(required = false) Integer userId, Authentication authentication) {
		Integer effectiveUserId = resolveEffectiveUserId(authentication, userId);
		AppLog.success(log, "GET /api/feature/reports, userId={}", effectiveUserId);
		return featureDataService.getReports(effectiveUserId);
	}

	/**
	 * Zwraca dane modułu ustawień.
	 *
	 * @param userId opcjonalny identyfikator użytkownika (dla trenera/admina)
	 * @param authentication kontekst uwierzytelnienia
	 * @return lista pozycji modułu ustawień
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@GetMapping("/settings")
	public List<FeatureItemResponse> settings(@RequestParam(required = false) Integer userId, Authentication authentication) {
		Integer effectiveUserId = resolveEffectiveUserId(authentication, userId);
		AppLog.success(log, "GET /api/feature/settings, userId={}", effectiveUserId);
		return featureDataService.getSettings(effectiveUserId);
	}

	/**
	 * Aktualizuje pojedyncze ustawienie użytkownika.
	 *
	 * @param settingId identyfikator ustawienia
	 * @param request nowa wartość ustawienia
	 * @param authentication kontekst uwierzytelnienia
	 * @return zaktualizowana pozycja ustawienia
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia lub nieprawidłowa wartość
	 */
	@PutMapping("/settings/{settingId}")
	public ResponseEntity<FeatureItemResponse> updateSetting(
			@PathVariable Integer settingId,
			@Valid @RequestBody UpdateSettingRequest request,
			Authentication authentication
	) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "PUT /api/feature/settings/{}, userId={}", settingId, userId);
		return ResponseEntity.ok(
				featureDataService.updateSetting(userId, settingId, request)
		);
	}

	/**
	 * Zwraca dane modułu powiadomień.
	 *
	 * @param userId opcjonalny identyfikator użytkownika (dla trenera/admina)
	 * @param authentication kontekst uwierzytelnienia
	 * @return lista pozycji modułu powiadomień
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@GetMapping("/notifications")
	public List<FeatureItemResponse> notifications(@RequestParam(required = false) Integer userId, Authentication authentication) {
		Integer effectiveUserId = resolveEffectiveUserId(authentication, userId);
		AppLog.success(log, "GET /api/feature/notifications, userId={}", effectiveUserId);
		return featureDataService.getNotifications(effectiveUserId);
	}

	/**
	 * Zwraca pełne dane ekranu profilu użytkownika.
	 *
	 * @param authentication kontekst uwierzytelnienia
	 * @return podsumowanie profilu użytkownika
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@GetMapping("/profile-overview")
	public ResponseEntity<ProfileOverviewResponse> profileOverview(Authentication authentication) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "GET /api/feature/profile-overview, userId={}", userId);
		return ResponseEntity.ok(featureDataService.getProfileOverview(userId));
	}

	/**
	 * Tworzy nowy plan treningowy użytkownika.
	 *
	 * @param request dane nowego planu
	 * @param authentication kontekst uwierzytelnienia
	 * @return utworzona pozycja planu
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@PostMapping("/workouts")
	public ResponseEntity<FeatureItemResponse> createWorkout(
			@Valid @RequestBody CreateWorkoutRequest request,
			Authentication authentication
	) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "POST /api/feature/workouts, userId={}, name={}", userId, request.getName());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(featureDataService.createWorkout(userId, request));
	}

	/**
	 * Usuwa istniejący plan treningowy.
	 *
	 * @param workoutId identyfikator planu
	 * @param authentication kontekst uwierzytelnienia
	 * @return odpowiedź bez treści
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@DeleteMapping("/workouts/{workoutId}")
	public ResponseEntity<Void> deleteWorkout(@PathVariable Integer workoutId, Authentication authentication) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "DELETE /api/feature/workouts/{}, userId={}", workoutId, userId);
		featureDataService.deleteWorkout(userId, workoutId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Zwraca szczegóły planu treningowego (edycja w aplikacji mobilnej).
	 *
	 * @param workoutId identyfikator planu
	 * @param authentication kontekst uwierzytelnienia
	 * @return dane planu
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@GetMapping("/workouts/{workoutId}")
	public ResponseEntity<WorkoutPlanDetailResponse> getWorkoutDetail(
			@PathVariable Integer workoutId,
			Authentication authentication
	) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "GET /api/feature/workouts/{}, userId={}", workoutId, userId);
		return ResponseEntity.ok(
				featureDataService.getWorkoutDetail(userId, workoutId)
		);
	}

	/**
	 * Aktualizuje istniejący plan treningowy.
	 *
	 * @param workoutId identyfikator planu
	 * @param request nowe dane planu
	 * @param authentication kontekst uwierzytelnienia
	 * @return zaktualizowana pozycja listy
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@PutMapping("/workouts/{workoutId}")
	public ResponseEntity<FeatureItemResponse> updateWorkout(
			@PathVariable Integer workoutId,
			@Valid @RequestBody CreateWorkoutRequest request,
			Authentication authentication
	) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "PUT /api/feature/workouts/{}, userId={}", workoutId, userId);
		return ResponseEntity.ok(
				featureDataService.updateWorkout(userId, workoutId, request)
		);
	}

	/**
	 * Lista ćwiczeń w planie (parametry serii / powtórzeń).
	 *
	 * @param workoutId identyfikator planu
	 * @param authentication kontekst uwierzytelnienia
	 * @return lista pozycji ćwiczeń w planie
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@GetMapping("/workouts/{workoutId}/exercises")
	public List<WorkoutExerciseLineResponse> listWorkoutExercises(
			@PathVariable Integer workoutId,
			Authentication authentication
	) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "GET /api/feature/workouts/{}/exercises, userId={}", workoutId, userId);
		return featureDataService.listWorkoutExerciseLines(userId, workoutId);
	}

	/**
	 * Dodaje ćwiczenie do planu.
	 *
	 * @param workoutId identyfikator planu
	 * @param request dane ćwiczenia w planie
	 * @param authentication kontekst uwierzytelnienia
	 * @return utworzona pozycja ćwiczenia w planie
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@PostMapping("/workouts/{workoutId}/exercises")
	public ResponseEntity<WorkoutExerciseLineResponse> addWorkoutExercise(
			@PathVariable Integer workoutId,
			@Valid @RequestBody WorkoutExerciseRequest request,
			Authentication authentication
	) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "POST /api/feature/workouts/{}/exercises, userId={}, exerciseId={}",
				workoutId, userId, request.getExerciseId());
		return ResponseEntity.status(HttpStatus.CREATED).body(
				featureDataService.addWorkoutExerciseLine(userId, workoutId, request)
		);
	}

	/**
	 * Usuwa pozycję ćwiczenia z planu.
	 *
	 * @param workoutId identyfikator planu
	 * @param lineId identyfikator pozycji w planie
	 * @param authentication kontekst uwierzytelnienia
	 * @return odpowiedź bez treści
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@DeleteMapping("/workouts/{workoutId}/exercises/{lineId}")
	public ResponseEntity<Void> deleteWorkoutExercise(
			@PathVariable Integer workoutId,
			@PathVariable Integer lineId,
			Authentication authentication
	) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "DELETE /api/feature/workouts/{}/exercises/{}, userId={}", workoutId, lineId, userId);
		featureDataService.deleteWorkoutExerciseLine(userId, workoutId, lineId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Tworzy własne ćwiczenie użytkownika.
	 *
	 * @param request dane ćwiczenia
	 * @param authentication kontekst uwierzytelnienia
	 * @return utworzona pozycja ćwiczenia
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@PostMapping("/exercises")
	public ResponseEntity<FeatureItemResponse> createExercise(
			@Valid @RequestBody CreateExerciseRequest request,
			Authentication authentication
	) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "POST /api/feature/exercises, userId={}, name={}", userId, request.getName());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(featureDataService.createExercise(userId, request));
	}

	/**
	 * Rozpoczyna nową sesję treningową dla wskazanego planu.
	 *
	 * @param request dane startu sesji
	 * @param authentication kontekst uwierzytelnienia
	 * @return nowa pozycja sesji
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 * @throws IllegalStateException gdy użytkownik ma już aktywną sesję
	 */
	@PostMapping("/sessions/start")
	public ResponseEntity<FeatureItemResponse> startSession(
			@Valid @RequestBody StartSessionRequest request,
			Authentication authentication
	) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "POST /api/feature/sessions/start, userId={}, workoutId={}", userId, request.getWorkoutId());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(featureDataService.startSession(userId, request.getWorkoutId()));
	}

	/**
	 * Kończy sesję treningową i zapisuje czas trwania.
	 *
	 * @param sessionId identyfikator sesji
	 * @param request dane zakończenia
	 * @param authentication kontekst uwierzytelnienia
	 * @return zaktualizowana pozycja sesji
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@PostMapping("/sessions/{sessionId}/finish")
	public ResponseEntity<FeatureItemResponse> finishSession(
			@PathVariable Integer sessionId,
			@Valid @RequestBody FinishSessionRequest request,
			Authentication authentication
	) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "POST /api/feature/sessions/{}/finish, userId={}, duration={}",
				sessionId, userId, request.getDuration());
		return ResponseEntity.ok(
				featureDataService.finishSession(userId, sessionId, request.getDuration())
		);
	}

	/**
	 * Anuluje aktywną (zaplanowaną) sesję treningową użytkownika.
	 *
	 * @param sessionId identyfikator sesji
	 * @param authentication kontekst uwierzytelnienia
	 * @return odpowiedź bez treści
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@DeleteMapping("/sessions/{sessionId}")
	public ResponseEntity<Void> cancelSession(@PathVariable Integer sessionId, Authentication authentication) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "DELETE /api/feature/sessions/{}, userId={}", sessionId, userId);
		featureDataService.cancelSession(userId, sessionId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Zwraca wyniki ćwiczeń zapisane w sesji treningowej.
	 *
	 * @param sessionId identyfikator sesji
	 * @param authentication kontekst uwierzytelnienia
	 * @return lista wyników ćwiczeń w sesji
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@GetMapping("/sessions/{sessionId}/results")
	public List<SessionExerciseResultResponse> sessionResults(
			@PathVariable Integer sessionId,
			Authentication authentication
	) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "GET /api/feature/sessions/{}/results, userId={}", sessionId, userId);
		return featureDataService.getSessionExerciseResults(userId, sessionId);
	}

	/**
	 * Dodaje wynik ćwiczenia do sesji treningowej.
	 *
	 * @param sessionId identyfikator sesji
	 * @param request dane wyniku ćwiczenia
	 * @param authentication kontekst uwierzytelnienia
	 * @return utworzony wynik ćwiczenia
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@PostMapping("/sessions/{sessionId}/results")
	public ResponseEntity<SessionExerciseResultResponse> addSessionResult(
			@PathVariable Integer sessionId,
			@Valid @RequestBody ExerciseResultRequest request,
			Authentication authentication
	) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "POST /api/feature/sessions/{}/results, userId={}, exerciseId={}",
				sessionId, userId, request.getExerciseId());
		return ResponseEntity.status(HttpStatus.CREATED).body(
				featureDataService.addSessionExerciseResult(userId, sessionId, request)
		);
	}

	/**
	 * Aktualizuje istniejący wynik ćwiczenia w sesji treningowej.
	 *
	 * @param sessionId identyfikator sesji
	 * @param resultId identyfikator wyniku
	 * @param request nowe dane wyniku
	 * @param authentication kontekst uwierzytelnienia
	 * @return zaktualizowany wynik ćwiczenia
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@PutMapping("/sessions/{sessionId}/results/{resultId}")
	public ResponseEntity<SessionExerciseResultResponse> updateSessionResult(
			@PathVariable Integer sessionId,
			@PathVariable Integer resultId,
			@Valid @RequestBody ExerciseResultRequest request,
			Authentication authentication
	) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "PUT /api/feature/sessions/{}/results/{}, userId={}", sessionId, resultId, userId);
		return ResponseEntity.ok(
				featureDataService.updateSessionExerciseResult(
						userId,
						sessionId,
						resultId,
						request
				)
		);
	}

	/**
	 * Usuwa wynik ćwiczenia z sesji treningowej.
	 *
	 * @param sessionId identyfikator sesji
	 * @param resultId identyfikator wyniku
	 * @param authentication kontekst uwierzytelnienia
	 * @return odpowiedź bez treści
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@DeleteMapping("/sessions/{sessionId}/results/{resultId}")
	public ResponseEntity<Void> deleteSessionResult(
			@PathVariable Integer sessionId,
			@PathVariable Integer resultId,
			Authentication authentication
	) {
		Integer userId = resolveRequiredUserId(authentication);
		AppLog.success(log, "DELETE /api/feature/sessions/{}/results/{}, userId={}", sessionId, resultId, userId);
		featureDataService.deleteSessionExerciseResult(userId, sessionId, resultId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Wyznacza efektywny identyfikator użytkownika dla endpointów listowych.
	 *
	 * <p>Dla roli USER zawsze zwracany jest własny identyfikator z tokena. Dla ról
	 * uprzywilejowanych (TRAINER/ADMIN) można opcjonalnie przekazać {@code requestedUserId}.
	 *
	 * @param authentication aktualny kontekst uwierzytelnienia
	 * @param requestedUserId opcjonalny identyfikator użytkownika z query param
	 * @return identyfikator użytkownika do filtrowania danych
	 * @throws IllegalArgumentException gdy principal nie jest typu {@link JwtPrincipal}
	 */
	private Integer resolveEffectiveUserId(Authentication authentication, Integer requestedUserId) {
		JwtPrincipal principal = resolvePrincipal(authentication);
		if ("ADMIN".equals(principal.role()) || "TRAINER".equals(principal.role())) {
			return requestedUserId == null ? principal.userId() : requestedUserId;
		}
		return principal.userId();
	}

	/**
	 * Zwraca identyfikator aktualnie uwierzytelnionego użytkownika.
	 *
	 * @param authentication kontekst uwierzytelnienia
	 * @return identyfikator użytkownika z tokena
	 * @throws IllegalArgumentException gdy principal nie jest typu {@link JwtPrincipal}
	 */
	private Integer resolveRequiredUserId(Authentication authentication) {
		return resolvePrincipal(authentication).userId();
	}

	/**
	 * Rozwiązuje principal JWT z kontekstu uwierzytelnienia.
	 *
	 * @param authentication kontekst uwierzytelnienia
	 * @return obiekt {@link JwtPrincipal}
	 * @throws IllegalArgumentException gdy principal nie jest typu {@link JwtPrincipal}
	 */
	private JwtPrincipal resolvePrincipal(Authentication authentication) {
		Object principal = authentication == null ? null : authentication.getPrincipal();
		if (principal instanceof JwtPrincipal jwtPrincipal) {
			return jwtPrincipal;
		}
		log.warn("Brak poprawnego kontekstu uwierzytelnienia w żądaniu feature");
		throw new IllegalArgumentException("Brak poprawnego kontekstu uwierzytelnienia");
	}
}
