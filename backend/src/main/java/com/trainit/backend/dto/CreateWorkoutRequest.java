package com.trainit.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateWorkoutRequest {

	@NotBlank(message = "Workout name cannot be blank")
	private String name;

	private String description;

	private String difficultyLevel;

	private Integer estimatedDuration;

	private List<WorkoutExerciseRequest> exercises;

	public CreateWorkoutRequest() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDifficultyLevel() {
		return difficultyLevel;
	}

	public void setDifficultyLevel(String difficultyLevel) {
		this.difficultyLevel = difficultyLevel;
	}

	public Integer getEstimatedDuration() {
		return estimatedDuration;
	}

	public void setEstimatedDuration(Integer estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
	}

	public List<WorkoutExerciseRequest> getExercises() {
		return exercises;
	}

	public void setExercises(List<WorkoutExerciseRequest> exercises) {
		this.exercises = exercises;
	}
}
