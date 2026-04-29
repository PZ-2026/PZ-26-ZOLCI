package com.trainit.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class WorkoutSessionDto {

	private Integer id;
	private Integer workoutId;
	private String workoutName;
	private LocalDateTime plannedDate;
	private LocalDateTime completedDate;
	private String status;
	private Integer duration;
	private List<ExerciseResultDto> exerciseResults;

	public WorkoutSessionDto() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getWorkoutId() {
		return workoutId;
	}

	public void setWorkoutId(Integer workoutId) {
		this.workoutId = workoutId;
	}

	public String getWorkoutName() {
		return workoutName;
	}

	public void setWorkoutName(String workoutName) {
		this.workoutName = workoutName;
	}

	public LocalDateTime getPlannedDate() {
		return plannedDate;
	}

	public void setPlannedDate(LocalDateTime plannedDate) {
		this.plannedDate = plannedDate;
	}

	public LocalDateTime getCompletedDate() {
		return completedDate;
	}

	public void setCompletedDate(LocalDateTime completedDate) {
		this.completedDate = completedDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public List<ExerciseResultDto> getExerciseResults() {
		return exerciseResults;
	}

	public void setExerciseResults(List<ExerciseResultDto> exerciseResults) {
		this.exerciseResults = exerciseResults;
	}
}
