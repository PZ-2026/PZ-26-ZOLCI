package com.trainit.backend.controller;

import com.trainit.backend.dto.CreateExerciseRequest;
import com.trainit.backend.dto.CreateWorkoutRequest;
import com.trainit.backend.dto.FinishSessionRequest;
import com.trainit.backend.dto.FeatureItemResponse;
import com.trainit.backend.dto.StartSessionRequest;
import com.trainit.backend.security.JwtPrincipal;
import com.trainit.backend.service.FeatureDataService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
	 * @return lista pozycji modułu treningów
	 */
	@GetMapping("/workouts")
	public List<FeatureItemResponse> workouts(@RequestParam(required = false) Integer userId, Authentication authentication) {
		return featureDataService.getWorkouts(resolveEffectiveUserId(authentication, userId));
	}

	/**
	 * Zwraca dane listy ćwiczeń.
	 *
	 * @return lista pozycji modułu ćwiczeń
	 */
	@GetMapping("/exercises")
	public List<FeatureItemResponse> exercises(@RequestParam(required = false) Integer userId, Authentication authentication) {
		return featureDataService.getExercises(resolveEffectiveUserId(authentication, userId));
	}

	/**
	 * Zwraca dane listy sesji treningowych.
	 *
	 * @return lista pozycji modułu sesji
	 */
	@GetMapping("/sessions")
	public List<FeatureItemResponse> sessions(@RequestParam(required = false) Integer userId, Authentication authentication) {
		return featureDataService.getSessions(resolveEffectiveUserId(authentication, userId));
	}

	/**
	 * Zwraca dane podsumowania statystyk.
	 *
	 * @return lista pozycji statystycznych
	 */
	@GetMapping("/statistics/summary")
	public List<FeatureItemResponse> statisticsSummary(@RequestParam(required = false) Integer userId, Authentication authentication) {
		return featureDataService.getStatisticsSummary(resolveEffectiveUserId(authentication, userId));
	}

	/**
	 * Zwraca dane listy raportów.
	 *
	 * @return lista pozycji modułu raportów
	 */
	@GetMapping("/reports")
	public List<FeatureItemResponse> reports(@RequestParam(required = false) Integer userId, Authentication authentication) {
		return featureDataService.getReports(resolveEffectiveUserId(authentication, userId));
	}

	/**
	 * Zwraca dane modułu ustawień.
	 *
	 * @return lista pozycji modułu ustawień
	 */
	@GetMapping("/settings")
	public List<FeatureItemResponse> settings(@RequestParam(required = false) Integer userId, Authentication authentication) {
		return featureDataService.getSettings(resolveEffectiveUserId(authentication, userId));
	}

	/**
	 * Zwraca dane modułu powiadomień.
	 *
	 * @return lista pozycji modułu powiadomień
	 */
	@GetMapping("/notifications")
	public List<FeatureItemResponse> notifications(@RequestParam(required = false) Integer userId, Authentication authentication) {
		return featureDataService.getNotifications(resolveEffectiveUserId(authentication, userId));
	}

	/**
	 * Tworzy nowy plan treningowy użytkownika.
	 *
	 * @param request dane nowego planu
	 * @return utworzona pozycja planu
	 */
	@PostMapping("/workouts")
	public ResponseEntity<FeatureItemResponse> createWorkout(
			@Valid @RequestBody CreateWorkoutRequest request,
			Authentication authentication
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(featureDataService.createWorkout(resolveRequiredUserId(authentication), request));
	}

	/**
	 * Usuwa istniejący plan treningowy.
	 *
	 * @param workoutId identyfikator planu
	 * @return odpowiedź bez treści
	 */
	@DeleteMapping("/workouts/{workoutId}")
	public ResponseEntity<Void> deleteWorkout(@PathVariable Integer workoutId, Authentication authentication) {
		featureDataService.deleteWorkout(resolveRequiredUserId(authentication), workoutId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Tworzy własne ćwiczenie użytkownika.
	 *
	 * @param request dane ćwiczenia
	 * @return utworzona pozycja ćwiczenia
	 */
	@PostMapping("/exercises")
	public ResponseEntity<FeatureItemResponse> createExercise(
			@Valid @RequestBody CreateExerciseRequest request,
			Authentication authentication
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(featureDataService.createExercise(resolveRequiredUserId(authentication), request));
	}

	/**
	 * Rozpoczyna nową sesję treningową dla wskazanego planu.
	 *
	 * @param request dane startu sesji
	 * @return nowa pozycja sesji
	 */
	@PostMapping("/sessions/start")
	public ResponseEntity<FeatureItemResponse> startSession(
			@Valid @RequestBody StartSessionRequest request,
			Authentication authentication
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(featureDataService.startSession(resolveRequiredUserId(authentication), request.getWorkoutId()));
	}

	/**
	 * Kończy sesję treningową i zapisuje czas trwania.
	 *
	 * @param sessionId identyfikator sesji
	 * @param request dane zakończenia
	 * @return zaktualizowana pozycja sesji
	 */
	@PostMapping("/sessions/{sessionId}/finish")
	public ResponseEntity<FeatureItemResponse> finishSession(
			@PathVariable Integer sessionId,
			@Valid @RequestBody FinishSessionRequest request,
			Authentication authentication
	) {
		return ResponseEntity.ok(
				featureDataService.finishSession(resolveRequiredUserId(authentication), sessionId, request.getDuration())
		);
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
	 */
	private Integer resolveRequiredUserId(Authentication authentication) {
		return resolvePrincipal(authentication).userId();
	}

	private JwtPrincipal resolvePrincipal(Authentication authentication) {
		Object principal = authentication == null ? null : authentication.getPrincipal();
		if (principal instanceof JwtPrincipal jwtPrincipal) {
			return jwtPrincipal;
		}
		throw new IllegalArgumentException("Brak poprawnego kontekstu uwierzytelnienia");
	}
}
