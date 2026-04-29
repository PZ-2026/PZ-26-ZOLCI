package com.trainit.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Żądanie uruchomienia nowej sesji treningowej na podstawie planu.
 */
public class StartSessionRequest {

	/** Id użytkownika uruchamiającego sesję. */
	@NotNull(message = "Id użytkownika jest wymagane")
	private Integer userId;

	/** Id planu treningowego, który ma zostać wykonany. */
	@NotNull(message = "Id planu treningowego jest wymagane")
	private Integer workoutId;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getWorkoutId() {
		return workoutId;
	}

	public void setWorkoutId(Integer workoutId) {
		this.workoutId = workoutId;
	}
}
