package com.trainit.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class WorkoutDto {

	private Integer id;
	private String name;
	private String description;
	private String difficultyLevel;
	private Integer estimatedDuration;
	private LocalDateTime createdAt;
	private List<WorkoutExerciseDto> exercises;

	public WorkoutDto() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<WorkoutExerciseDto> getExercises() {
		return exercises;
	}

	public void setExercises(List<WorkoutExerciseDto> exercises) {
		this.exercises = exercises;
	}
}
