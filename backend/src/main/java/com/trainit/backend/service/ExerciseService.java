package com.trainit.backend.service;

import com.trainit.backend.dto.CreateExerciseRequest;
import com.trainit.backend.dto.ExerciseDto;
import com.trainit.backend.entity.Exercise;
import com.trainit.backend.entity.User;
import com.trainit.backend.repository.ExerciseRepository;
import com.trainit.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExerciseService {

	private final ExerciseRepository exerciseRepository;
	private final UserRepository userRepository;

	public ExerciseService(ExerciseRepository exerciseRepository, UserRepository userRepository) {
		this.exerciseRepository = exerciseRepository;
		this.userRepository = userRepository;
	}

	public ExerciseDto createExercise(CreateExerciseRequest request, Integer userId) {
		User user = null;
		if (Boolean.TRUE.equals(request.getIsCustom())) {
			user = userRepository.findById(userId)
					.orElseThrow(() -> new RuntimeException("User not found"));
		}

		Exercise exercise = new Exercise();
		exercise.setName(request.getName());
		exercise.setMuscleGroup(request.getMuscleGroup());
		exercise.setDescription(request.getDescription());
		exercise.setIsCustom(request.getIsCustom() != null ? request.getIsCustom() : false);
		exercise.setCreatedBy(user);

		Exercise saved = exerciseRepository.save(exercise);
		return mapToDto(saved);
	}

	public ExerciseDto getExercise(Integer exerciseId) {
		Exercise exercise = exerciseRepository.findById(exerciseId)
				.orElseThrow(() -> new RuntimeException("Exercise not found"));
		return mapToDto(exercise);
	}

	public List<ExerciseDto> getAllExercises() {
		List<Exercise> exercises = exerciseRepository.findAll();
		return exercises.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	public List<ExerciseDto> getExercisesByMuscleGroup(String muscleGroup) {
		List<Exercise> exercises = exerciseRepository.findByMuscleGroup(muscleGroup);
		return exercises.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	public List<ExerciseDto> getUserCustomExercises(Integer userId) {
		List<Exercise> exercises = exerciseRepository.findByCreatedByIdAndIsCustomTrue(userId);
		return exercises.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	public ExerciseDto updateExercise(Integer exerciseId, CreateExerciseRequest request) {
		Exercise exercise = exerciseRepository.findById(exerciseId)
				.orElseThrow(() -> new RuntimeException("Exercise not found"));

		exercise.setName(request.getName());
		exercise.setMuscleGroup(request.getMuscleGroup());
		exercise.setDescription(request.getDescription());

		Exercise updated = exerciseRepository.save(exercise);
		return mapToDto(updated);
	}

	public void deleteExercise(Integer exerciseId) {
		Exercise exercise = exerciseRepository.findById(exerciseId)
				.orElseThrow(() -> new RuntimeException("Exercise not found"));
		exerciseRepository.delete(exercise);
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
