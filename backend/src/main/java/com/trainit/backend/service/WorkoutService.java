package com.trainit.backend.service;

import com.trainit.backend.util.AppLog;

import com.trainit.backend.dto.CreateWorkoutRequest;
import com.trainit.backend.dto.WorkoutDto;
import com.trainit.backend.dto.WorkoutExerciseDto;
import com.trainit.backend.dto.WorkoutExerciseRequest;
import com.trainit.backend.entity.Exercise;
import com.trainit.backend.entity.User;
import com.trainit.backend.entity.Workout;
import com.trainit.backend.entity.WorkoutExercise;
import com.trainit.backend.repository.ExerciseRepository;
import com.trainit.backend.repository.UserRepository;
import com.trainit.backend.repository.WorkoutExerciseRepository;
import com.trainit.backend.repository.WorkoutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serwis zarządzający planami treningowymi użytkowników.
 */
@Service
@Transactional
public class WorkoutService {

	private static final Logger log = LoggerFactory.getLogger(WorkoutService.class);

	private final WorkoutRepository workoutRepository;
	private final WorkoutExerciseRepository workoutExerciseRepository;
	private final UserRepository userRepository;
	private final ExerciseRepository exerciseRepository;

	/**
	 * Tworzy serwis z wymaganymi repozytoriami.
	 *
	 * @param workoutRepository repozytorium planów treningowych
	 * @param workoutExerciseRepository repozytorium ćwiczeń w planach
	 * @param userRepository repozytorium użytkowników
	 * @param exerciseRepository repozytorium ćwiczeń
	 */
	public WorkoutService(WorkoutRepository workoutRepository,
						 WorkoutExerciseRepository workoutExerciseRepository,
						 UserRepository userRepository,
						 ExerciseRepository exerciseRepository) {
		this.workoutRepository = workoutRepository;
		this.workoutExerciseRepository = workoutExerciseRepository;
		this.userRepository = userRepository;
		this.exerciseRepository = exerciseRepository;
	}

	/**
	 * Tworzy nowy plan treningowy dla użytkownika.
	 *
	 * @param userId identyfikator użytkownika
	 * @param request dane planu treningowego
	 * @return utworzony plan
	 * @throws RuntimeException gdy użytkownik lub ćwiczenie nie istnieje
	 */
	public WorkoutDto createWorkout(Integer userId, CreateWorkoutRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> {
					log.warn("Tworzenie planu – nie znaleziono użytkownika, userId={}", userId);
					return new RuntimeException("User not found");
				});

		Workout workout = new Workout();
		workout.setUser(user);
		workout.setName(request.getName());
		workout.setDescription(request.getDescription());
		workout.setDifficultyLevel(request.getDifficultyLevel());
		workout.setEstimatedDuration(request.getEstimatedDuration());

		Workout saved = workoutRepository.save(workout);

		if (request.getExercises() != null && !request.getExercises().isEmpty()) {
			for (WorkoutExerciseRequest exerciseRequest : request.getExercises()) {
				Exercise exercise = exerciseRepository.findById(exerciseRequest.getExerciseId())
						.orElseThrow(() -> {
							log.warn("Tworzenie planu – nie znaleziono ćwiczenia, exerciseId={}",
									exerciseRequest.getExerciseId());
							return new RuntimeException("Exercise not found");
						});

				WorkoutExercise workoutExercise = new WorkoutExercise();
				workoutExercise.setWorkout(saved);
				workoutExercise.setExercise(exercise);
				workoutExercise.setSets(exerciseRequest.getSets());
				workoutExercise.setReps(exerciseRequest.getReps());
				workoutExercise.setWeight(exerciseRequest.getWeight());
				workoutExercise.setDuration(exerciseRequest.getDuration());

				workoutExerciseRepository.save(workoutExercise);
			}
		}

		AppLog.success(log, "Utworzono plan treningowy, workoutId={}, userId={}", saved.getId(), userId);
		return mapToDto(saved);
	}

	/**
	 * Zwraca plan treningowy użytkownika po identyfikatorze.
	 *
	 * @param workoutId identyfikator planu
	 * @param userId identyfikator użytkownika
	 * @return dane planu
	 * @throws RuntimeException gdy plan nie istnieje lub nie należy do użytkownika
	 */
	public WorkoutDto getWorkout(Integer workoutId, Integer userId) {
		Workout workout = workoutRepository.findByIdAndUserId(workoutId, userId)
				.orElseThrow(() -> {
					log.warn("Nie znaleziono planu, workoutId={}, userId={}", workoutId, userId);
					return new RuntimeException("Workout not found");
				});
		AppLog.success(log, "Pobrano plan treningowy, workoutId={}, userId={}", workoutId, userId);
		return mapToDto(workout);
	}

	/**
	 * Zwraca wszystkie plany treningowe użytkownika.
	 *
	 * @param userId identyfikator użytkownika
	 * @return lista planów
	 */
	public List<WorkoutDto> getUserWorkouts(Integer userId) {
		List<Workout> workouts = workoutRepository.findByUserId(userId);
		AppLog.success(log, "Pobrano plany użytkownika, userId={}, liczba={}", userId, workouts.size());
		return workouts.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	/**
	 * Aktualizuje plan treningowy użytkownika.
	 *
	 * @param workoutId identyfikator planu
	 * @param userId identyfikator użytkownika
	 * @param request nowe dane planu
	 * @return zaktualizowany plan
	 * @throws RuntimeException gdy plan, użytkownik lub ćwiczenie nie istnieje
	 */
	public WorkoutDto updateWorkout(Integer workoutId, Integer userId, CreateWorkoutRequest request) {
		Workout workout = workoutRepository.findByIdAndUserId(workoutId, userId)
				.orElseThrow(() -> {
					log.warn("Aktualizacja planu – nie znaleziono, workoutId={}, userId={}", workoutId, userId);
					return new RuntimeException("Workout not found");
				});

		workout.setName(request.getName());
		workout.setDescription(request.getDescription());
		workout.setDifficultyLevel(request.getDifficultyLevel());
		workout.setEstimatedDuration(request.getEstimatedDuration());

		workoutExerciseRepository.deleteByWorkoutId(workoutId);

		if (request.getExercises() != null && !request.getExercises().isEmpty()) {
			for (WorkoutExerciseRequest exerciseRequest : request.getExercises()) {
				Exercise exercise = exerciseRepository.findById(exerciseRequest.getExerciseId())
						.orElseThrow(() -> {
							log.warn("Aktualizacja planu – nie znaleziono ćwiczenia, exerciseId={}",
									exerciseRequest.getExerciseId());
							return new RuntimeException("Exercise not found");
						});

				WorkoutExercise workoutExercise = new WorkoutExercise();
				workoutExercise.setWorkout(workout);
				workoutExercise.setExercise(exercise);
				workoutExercise.setSets(exerciseRequest.getSets());
				workoutExercise.setReps(exerciseRequest.getReps());
				workoutExercise.setWeight(exerciseRequest.getWeight());
				workoutExercise.setDuration(exerciseRequest.getDuration());

				workoutExerciseRepository.save(workoutExercise);
			}
		}

		Workout updated = workoutRepository.save(workout);
		AppLog.success(log, "Zaktualizowano plan treningowy, workoutId={}, userId={}", workoutId, userId);
		return mapToDto(updated);
	}

	/**
	 * Usuwa plan treningowy użytkownika.
	 *
	 * @param workoutId identyfikator planu
	 * @param userId identyfikator użytkownika
	 * @throws RuntimeException gdy plan nie istnieje lub nie należy do użytkownika
	 */
	public void deleteWorkout(Integer workoutId, Integer userId) {
		Workout workout = workoutRepository.findByIdAndUserId(workoutId, userId)
				.orElseThrow(() -> {
					log.warn("Usuwanie planu – nie znaleziono, workoutId={}, userId={}", workoutId, userId);
					return new RuntimeException("Workout not found");
				});

		workoutExerciseRepository.deleteByWorkoutId(workoutId);
		workoutRepository.delete(workout);
		AppLog.success(log, "Usunięto plan treningowy, workoutId={}, userId={}", workoutId, userId);
	}

	private WorkoutDto mapToDto(Workout workout) {
		WorkoutDto dto = new WorkoutDto();
		dto.setId(workout.getId());
		dto.setName(workout.getName());
		dto.setDescription(workout.getDescription());
		dto.setDifficultyLevel(workout.getDifficultyLevel());
		dto.setEstimatedDuration(workout.getEstimatedDuration());
		dto.setCreatedAt(workout.getCreatedAt());

		List<WorkoutExercise> exercises = workoutExerciseRepository.findByWorkoutId(workout.getId());
		dto.setExercises(exercises.stream()
				.map(this::mapWorkoutExerciseToDto)
				.collect(Collectors.toList()));

		return dto;
	}

	private WorkoutExerciseDto mapWorkoutExerciseToDto(WorkoutExercise we) {
		WorkoutExerciseDto dto = new WorkoutExerciseDto();
		dto.setId(we.getId());
		dto.setExerciseId(we.getExercise().getId());
		dto.setExerciseName(we.getExercise().getName());
		dto.setMuscleGroup(we.getExercise().getMuscleGroup());
		dto.setSets(we.getSets());
		dto.setReps(we.getReps());
		dto.setWeight(we.getWeight());
		dto.setDuration(we.getDuration());
		return dto;
	}
}
