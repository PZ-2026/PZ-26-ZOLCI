package com.trainit.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateWorkoutSessionRequest {

	@NotNull(message = "Workout ID cannot be null")
	private Integer workoutId;

	private LocalDateTime plannedDate;

	public CreateWorkoutSessionRequest() {
	}

	public Integer getWorkoutId() {
		return workoutId;
	}

	public void setWorkoutId(Integer workoutId) {
		this.workoutId = workoutId;
	}

	public LocalDateTime getPlannedDate() {
		return plannedDate;
	}

	public void setPlannedDate(LocalDateTime plannedDate) {
		this.plannedDate = plannedDate;
	}
}
