package com.trainit.backend.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkoutService {

	private final WorkoutRepository workoutRepository;
	private final WorkoutExerciseRepository workoutExerciseRepository;
	private final UserRepository userRepository;
	private final ExerciseRepository exerciseRepository;

	public WorkoutService(WorkoutRepository workoutRepository,
						 WorkoutExerciseRepository workoutExerciseRepository,
						 UserRepository userRepository,
						 ExerciseRepository exerciseRepository) {
		this.workoutRepository = workoutRepository;
		this.workoutExerciseRepository = workoutExerciseRepository;
		this.userRepository = userRepository;
		this.exerciseRepository = exerciseRepository;
	}

	public WorkoutDto createWorkout(Integer userId, CreateWorkoutRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));

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
						.orElseThrow(() -> new RuntimeException("Exercise not found"));

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

		return mapToDto(saved);
	}

	public WorkoutDto getWorkout(Integer workoutId, Integer userId) {
		Workout workout = workoutRepository.findByIdAndUserId(workoutId, userId)
				.orElseThrow(() -> new RuntimeException("Workout not found"));
		return mapToDto(workout);
	}

	public List<WorkoutDto> getUserWorkouts(Integer userId) {
		List<Workout> workouts = workoutRepository.findByUserId(userId);
		return workouts.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	public WorkoutDto updateWorkout(Integer workoutId, Integer userId, CreateWorkoutRequest request) {
		Workout workout = workoutRepository.findByIdAndUserId(workoutId, userId)
				.orElseThrow(() -> new RuntimeException("Workout not found"));

		workout.setName(request.getName());
		workout.setDescription(request.getDescription());
		workout.setDifficultyLevel(request.getDifficultyLevel());
		workout.setEstimatedDuration(request.getEstimatedDuration());

		workoutExerciseRepository.deleteByWorkoutId(workoutId);

		if (request.getExercises() != null && !request.getExercises().isEmpty()) {
			for (WorkoutExerciseRequest exerciseRequest : request.getExercises()) {
				Exercise exercise = exerciseRepository.findById(exerciseRequest.getExerciseId())
						.orElseThrow(() -> new RuntimeException("Exercise not found"));

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
		return mapToDto(updated);
	}

	public void deleteWorkout(Integer workoutId, Integer userId) {
		Workout workout = workoutRepository.findByIdAndUserId(workoutId, userId)
				.orElseThrow(() -> new RuntimeException("Workout not found"));

		workoutExerciseRepository.deleteByWorkoutId(workoutId);
		workoutRepository.delete(workout);
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
