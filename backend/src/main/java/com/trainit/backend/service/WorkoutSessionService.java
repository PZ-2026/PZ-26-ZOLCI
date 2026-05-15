package com.trainit.backend.service;

import com.trainit.backend.util.AppLog;

import com.trainit.backend.dto.CompleteWorkoutSessionRequest;
import com.trainit.backend.dto.CreateWorkoutSessionRequest;
import com.trainit.backend.dto.ExerciseResultDto;
import com.trainit.backend.dto.ExerciseResultRequest;
import com.trainit.backend.dto.WorkoutSessionDto;
import com.trainit.backend.entity.Exercise;
import com.trainit.backend.entity.ExerciseResult;
import com.trainit.backend.entity.User;
import com.trainit.backend.entity.Workout;
import com.trainit.backend.entity.WorkoutSession;
import com.trainit.backend.repository.ExerciseRepository;
import com.trainit.backend.repository.ExerciseResultRepository;
import com.trainit.backend.repository.UserRepository;
import com.trainit.backend.repository.WorkoutRepository;
import com.trainit.backend.repository.WorkoutSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serwis zarządzający sesjami treningowymi użytkowników.
 */
@Service
@Transactional
public class WorkoutSessionService {

	private static final Logger log = LoggerFactory.getLogger(WorkoutSessionService.class);

	/** Repozytorium sesji treningowych. */
	private final WorkoutSessionRepository workoutSessionRepository;

	/** Repozytorium planów treningowych powiązanych z sesją. */
	private final WorkoutRepository workoutRepository;

	/** Repozytorium użytkowników (właściciel sesji). */
	private final UserRepository userRepository;

	/** Repozytorium ćwiczeń (wyniki w sesji). */
	private final ExerciseRepository exerciseRepository;

	/** Repozytorium wyników ćwiczeń w sesjach. */
	private final ExerciseResultRepository exerciseResultRepository;

	/**
	 * Tworzy serwis z wymaganymi repozytoriami.
	 *
	 * @param workoutSessionRepository repozytorium sesji treningowych
	 * @param workoutRepository repozytorium planów treningowych
	 * @param userRepository repozytorium użytkowników
	 * @param exerciseRepository repozytorium ćwiczeń
	 * @param exerciseResultRepository repozytorium wyników ćwiczeń
	 */
	public WorkoutSessionService(WorkoutSessionRepository workoutSessionRepository,
								 WorkoutRepository workoutRepository,
								 UserRepository userRepository,
								 ExerciseRepository exerciseRepository,
								 ExerciseResultRepository exerciseResultRepository) {
		this.workoutSessionRepository = workoutSessionRepository;
		this.workoutRepository = workoutRepository;
		this.userRepository = userRepository;
		this.exerciseRepository = exerciseRepository;
		this.exerciseResultRepository = exerciseResultRepository;
	}

	/**
	 * Tworzy nową sesję treningową.
	 *
	 * @param userId identyfikator użytkownika
	 * @param request dane nowej sesji
	 * @return utworzona sesja
	 * @throws RuntimeException gdy użytkownik lub plan nie istnieje
	 */
	public WorkoutSessionDto createSession(Integer userId, CreateWorkoutSessionRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> {
					log.warn("Tworzenie sesji – nie znaleziono użytkownika, userId={}", userId);
					return new RuntimeException("User not found");
				});

		Workout workout = workoutRepository.findById(request.getWorkoutId())
				.orElseThrow(() -> {
					log.warn("Tworzenie sesji – nie znaleziono planu, workoutId={}", request.getWorkoutId());
					return new RuntimeException("Workout not found");
				});

		WorkoutSession session = new WorkoutSession();
		session.setUser(user);
		session.setWorkout(workout);
		session.setPlannedDate(request.getPlannedDate());
		session.setStatus("PLANNED");

		WorkoutSession saved = workoutSessionRepository.save(session);
		AppLog.success(log, "Utworzono sesję treningową, sessionId={}, userId={}, workoutId={}",
				saved.getId(), userId, request.getWorkoutId());
		return mapToDto(saved);
	}

	/**
	 * Zwraca sesję treningową użytkownika po identyfikatorze.
	 *
	 * @param sessionId identyfikator sesji
	 * @param userId identyfikator użytkownika
	 * @return dane sesji
	 * @throws RuntimeException gdy sesja nie istnieje lub nie należy do użytkownika
	 */
	public WorkoutSessionDto getSession(Integer sessionId, Integer userId) {
		WorkoutSession session = workoutSessionRepository.findByIdAndUserId(sessionId, userId)
				.orElseThrow(() -> {
					log.warn("Nie znaleziono sesji, sessionId={}, userId={}", sessionId, userId);
					return new RuntimeException("Workout session not found");
				});
		AppLog.success(log, "Pobrano sesję treningową, sessionId={}, userId={}", sessionId, userId);
		return mapToDto(session);
	}

	/**
	 * Zwraca wszystkie sesje treningowe użytkownika.
	 *
	 * @param userId identyfikator użytkownika
	 * @return lista sesji
	 */
	public List<WorkoutSessionDto> getUserSessions(Integer userId) {
		List<WorkoutSession> sessions = workoutSessionRepository.findByUserId(userId);
		AppLog.success(log, "Pobrano sesje użytkownika, userId={}, liczba={}", userId, sessions.size());
		return sessions.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	/**
	 * Zwraca sesje użytkownika filtrowane po statusie.
	 *
	 * @param userId identyfikator użytkownika
	 * @param status status sesji
	 * @return lista sesji
	 */
	public List<WorkoutSessionDto> getUserSessionsByStatus(Integer userId, String status) {
		List<WorkoutSession> sessions = workoutSessionRepository.findByUserIdAndStatus(userId, status);
		AppLog.success(log, "Pobrano sesje użytkownika po statusie, userId={}, status={}, liczba={}",
				userId, status, sessions.size());
		return sessions.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	/**
	 * Zwraca sesje użytkownika w podanym zakresie dat planowanych.
	 *
	 * @param userId identyfikator użytkownika
	 * @param startDate data początkowa
	 * @param endDate data końcowa
	 * @return lista sesji
	 */
	public List<WorkoutSessionDto> getSessionsBetweenDates(Integer userId, LocalDateTime startDate, LocalDateTime endDate) {
		List<WorkoutSession> sessions = workoutSessionRepository.findByUserIdAndPlannedDateBetween(userId, startDate, endDate);
		AppLog.success(log, "Pobrano sesje użytkownika w zakresie dat, userId={}, liczba={}", userId, sessions.size());
		return sessions.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	/**
	 * Kończy sesję treningową i zapisuje wyniki ćwiczeń.
	 *
	 * @param sessionId identyfikator sesji
	 * @param userId identyfikator użytkownika
	 * @param request dane zakończenia sesji
	 * @return zaktualizowana sesja
	 * @throws RuntimeException gdy sesja lub ćwiczenie nie istnieje
	 */
	public WorkoutSessionDto completeSession(Integer sessionId, Integer userId, CompleteWorkoutSessionRequest request) {
		WorkoutSession session = workoutSessionRepository.findByIdAndUserId(sessionId, userId)
				.orElseThrow(() -> {
					log.warn("Zakończenie sesji – nie znaleziono, sessionId={}, userId={}", sessionId, userId);
					return new RuntimeException("Workout session not found");
				});

		session.setStatus("COMPLETED");
		session.setCompletedDate(LocalDateTime.now());
		session.setDuration(request.getDuration());

		if (request.getExerciseResults() != null) {
			for (ExerciseResultRequest resultRequest : request.getExerciseResults()) {
				Exercise exercise = exerciseRepository.findById(resultRequest.getExerciseId())
						.orElseThrow(() -> {
							log.warn("Zakończenie sesji – nie znaleziono ćwiczenia, exerciseId={}",
									resultRequest.getExerciseId());
							return new RuntimeException("Exercise not found");
						});

				ExerciseResult result = new ExerciseResult();
				result.setSession(session);
				result.setExercise(exercise);
				result.setSetsDone(resultRequest.getSetsDone());
				result.setRepsDone(resultRequest.getRepsDone());
				result.setWeightUsed(resultRequest.getWeightUsed());
				result.setDuration(resultRequest.getDuration());
				result.setNotes(resultRequest.getNotes());

				exerciseResultRepository.save(result);
			}
		}

		WorkoutSession updated = workoutSessionRepository.save(session);
		AppLog.success(log, "Zakończono sesję treningową, sessionId={}, userId={}, duration={}",
				sessionId, userId, request.getDuration());
		return mapToDto(updated);
	}

	/**
	 * Anuluje sesję treningową.
	 *
	 * @param sessionId identyfikator sesji
	 * @param userId identyfikator użytkownika
	 * @throws RuntimeException gdy sesja nie istnieje lub nie należy do użytkownika
	 */
	public void cancelSession(Integer sessionId, Integer userId) {
		WorkoutSession session = workoutSessionRepository.findByIdAndUserId(sessionId, userId)
				.orElseThrow(() -> {
					log.warn("Anulowanie sesji – nie znaleziono, sessionId={}, userId={}", sessionId, userId);
					return new RuntimeException("Workout session not found");
				});

		session.setStatus("CANCELLED");
		workoutSessionRepository.save(session);
		AppLog.success(log, "Anulowano sesję treningową, sessionId={}, userId={}", sessionId, userId);
	}

	/**
	 * Usuwa sesję treningową wraz z wynikami ćwiczeń.
	 *
	 * @param sessionId identyfikator sesji
	 * @param userId identyfikator użytkownika
	 * @throws RuntimeException gdy sesja nie istnieje lub nie należy do użytkownika
	 */
	public void deleteSession(Integer sessionId, Integer userId) {
		WorkoutSession session = workoutSessionRepository.findByIdAndUserId(sessionId, userId)
				.orElseThrow(() -> {
					log.warn("Usuwanie sesji – nie znaleziono, sessionId={}, userId={}", sessionId, userId);
					return new RuntimeException("Workout session not found");
				});

		exerciseResultRepository.deleteBySessionId(sessionId);
		workoutSessionRepository.delete(session);
		AppLog.success(log, "Usunięto sesję treningową, sessionId={}, userId={}", sessionId, userId);
	}

	private WorkoutSessionDto mapToDto(WorkoutSession session) {
		WorkoutSessionDto dto = new WorkoutSessionDto();
		dto.setId(session.getId());
		dto.setWorkoutId(session.getWorkout().getId());
		dto.setWorkoutName(session.getWorkout().getName());
		dto.setPlannedDate(session.getPlannedDate());
		dto.setCompletedDate(session.getCompletedDate());
		dto.setStatus(session.getStatus());
		dto.setDuration(session.getDuration());

		List<ExerciseResult> results = exerciseResultRepository.findBySessionId(session.getId());
		dto.setExerciseResults(results.stream()
				.map(this::mapExerciseResultToDto)
				.collect(Collectors.toList()));

		return dto;
	}

	private ExerciseResultDto mapExerciseResultToDto(ExerciseResult result) {
		ExerciseResultDto dto = new ExerciseResultDto();
		dto.setId(result.getId());
		dto.setExerciseId(result.getExercise().getId());
		dto.setExerciseName(result.getExercise().getName());
		dto.setSetsDone(result.getSetsDone());
		dto.setRepsDone(result.getRepsDone());
		dto.setWeightUsed(result.getWeightUsed());
		dto.setDuration(result.getDuration());
		dto.setNotes(result.getNotes());
		return dto;
	}
}
