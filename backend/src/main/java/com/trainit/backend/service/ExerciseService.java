package com.trainit.backend.service;

import com.trainit.backend.util.AppLog;

import com.trainit.backend.dto.CreateExerciseRequest;
import com.trainit.backend.dto.ExerciseDto;
import com.trainit.backend.entity.Exercise;
import com.trainit.backend.entity.User;
import com.trainit.backend.repository.ExerciseRepository;
import com.trainit.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serwis zarządzający ćwiczeniami systemowymi i własnymi użytkowników.
 */
@Service
@Transactional
public class ExerciseService {

	private static final Logger log = LoggerFactory.getLogger(ExerciseService.class);

	/** Repozytorium ćwiczeń w bazie danych. */
	private final ExerciseRepository exerciseRepository;

	/** Repozytorium użytkowników (weryfikacja właściciela ćwiczenia własnego). */
	private final UserRepository userRepository;

	/**
	 * Tworzy serwis z wymaganymi repozytoriami.
	 *
	 * @param exerciseRepository repozytorium ćwiczeń
	 * @param userRepository repozytorium użytkowników
	 */
	public ExerciseService(ExerciseRepository exerciseRepository, UserRepository userRepository) {
		this.exerciseRepository = exerciseRepository;
		this.userRepository = userRepository;
	}

	/**
	 * Tworzy nowe ćwiczenie.
	 *
	 * @param request dane ćwiczenia
	 * @param userId identyfikator użytkownika tworzącego (dla ćwiczeń własnych)
	 * @return utworzone ćwiczenie
	 * @throws RuntimeException gdy użytkownik nie istnieje (dla ćwiczenia własnego)
	 */
	public ExerciseDto createExercise(CreateExerciseRequest request, Integer userId) {
		User user = null;
		if (Boolean.TRUE.equals(request.getIsCustom())) {
			user = userRepository.findById(userId)
					.orElseThrow(() -> {
						log.warn("Tworzenie ćwiczenia – nie znaleziono użytkownika, userId={}", userId);
						return new RuntimeException("User not found");
					});
		}

		Exercise exercise = new Exercise();
		exercise.setName(request.getName());
		exercise.setMuscleGroup(request.getMuscleGroup());
		exercise.setDescription(request.getDescription());
		exercise.setIsCustom(request.getIsCustom() != null ? request.getIsCustom() : false);
		exercise.setCreatedBy(user);

		Exercise saved = exerciseRepository.save(exercise);
		AppLog.success(log, "Utworzono ćwiczenie, exerciseId={}, userId={}, isCustom={}",
				saved.getId(), userId, saved.getIsCustom());
		return mapToDto(saved);
	}

	/**
	 * Zwraca ćwiczenie po identyfikatorze.
	 *
	 * @param exerciseId identyfikator ćwiczenia
	 * @return dane ćwiczenia
	 * @throws RuntimeException gdy ćwiczenie nie istnieje
	 */
	public ExerciseDto getExercise(Integer exerciseId) {
		Exercise exercise = exerciseRepository.findById(exerciseId)
				.orElseThrow(() -> {
					log.warn("Nie znaleziono ćwiczenia, exerciseId={}", exerciseId);
					return new RuntimeException("Exercise not found");
				});
		AppLog.success(log, "Pobrano ćwiczenie, exerciseId={}", exerciseId);
		return mapToDto(exercise);
	}

	/**
	 * Zwraca wszystkie ćwiczenia w systemie.
	 *
	 * @return lista ćwiczeń
	 */
	public List<ExerciseDto> getAllExercises() {
		List<Exercise> exercises = exerciseRepository.findAll();
		AppLog.success(log, "Pobrano wszystkie ćwiczenia, liczba={}", exercises.size());
		return exercises.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	/**
	 * Zwraca ćwiczenia filtrowane po grupie mięśniowej.
	 *
	 * @param muscleGroup grupa mięśniowa
	 * @return lista ćwiczeń
	 */
	public List<ExerciseDto> getExercisesByMuscleGroup(String muscleGroup) {
		List<Exercise> exercises = exerciseRepository.findByMuscleGroup(muscleGroup);
		AppLog.success(log, "Pobrano ćwiczenia dla grupy mięśniowej={}, liczba={}", muscleGroup, exercises.size());
		return exercises.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	/**
	 * Zwraca własne ćwiczenia użytkownika.
	 *
	 * @param userId identyfikator użytkownika
	 * @return lista ćwiczeń własnych
	 */
	public List<ExerciseDto> getUserCustomExercises(Integer userId) {
		List<Exercise> exercises = exerciseRepository.findByCreatedByIdAndIsCustomTrue(userId);
		AppLog.success(log, "Pobrano własne ćwiczenia użytkownika, userId={}, liczba={}", userId, exercises.size());
		return exercises.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	/**
	 * Aktualizuje istniejące ćwiczenie.
	 *
	 * @param exerciseId identyfikator ćwiczenia
	 * @param request nowe dane ćwiczenia
	 * @return zaktualizowane ćwiczenie
	 * @throws RuntimeException gdy ćwiczenie nie istnieje
	 */
	public ExerciseDto updateExercise(Integer exerciseId, CreateExerciseRequest request) {
		Exercise exercise = exerciseRepository.findById(exerciseId)
				.orElseThrow(() -> {
					log.warn("Aktualizacja – nie znaleziono ćwiczenia, exerciseId={}", exerciseId);
					return new RuntimeException("Exercise not found");
				});

		exercise.setName(request.getName());
		exercise.setMuscleGroup(request.getMuscleGroup());
		exercise.setDescription(request.getDescription());

		Exercise updated = exerciseRepository.save(exercise);
		AppLog.success(log, "Zaktualizowano ćwiczenie, exerciseId={}", exerciseId);
		return mapToDto(updated);
	}

	/**
	 * Usuwa ćwiczenie po identyfikatorze.
	 *
	 * @param exerciseId identyfikator ćwiczenia
	 * @throws RuntimeException gdy ćwiczenie nie istnieje
	 */
	public void deleteExercise(Integer exerciseId) {
		Exercise exercise = exerciseRepository.findById(exerciseId)
				.orElseThrow(() -> {
					log.warn("Usuwanie – nie znaleziono ćwiczenia, exerciseId={}", exerciseId);
					return new RuntimeException("Exercise not found");
				});
		exerciseRepository.delete(exercise);
		AppLog.success(log, "Usunięto ćwiczenie, exerciseId={}", exerciseId);
	}

	private ExerciseDto mapToDto(Exercise exercise) {
		ExerciseDto dto = new ExerciseDto();
		dto.setId(exercise.getId());
		dto.setName(exercise.getName());
		dto.setMuscleGroup(exercise.getMuscleGroup());
		dto.setDescription(exercise.getDescription());
		dto.setIsCustom(exercise.getIsCustom());
		if (exercise.getCreatedBy() != null) {
			dto.setCreatedById(exercise.getCreatedBy().getId());
			dto.setCreatedByEmail(exercise.getCreatedBy().getEmail());
		}
		return dto;
	}
}
